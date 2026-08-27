package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.util.SupplierDocumentConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Document field extraction — delegates the actual OCR to Image_Describer's
 * /supplier-doc/extract endpoint (Python/FastAPI) rather than calling OpenAI directly from
 * Java. Image_Describer already has this exact pattern built and proven (invoice extraction):
 * try PyMuPDF's real text layer first — most government-issued certificates (COI, PAN, GST)
 * are digitally generated PDFs, not scans, so their identifiers can be read as exact
 * characters instead of guessed from a rendered image — and only fall back to GPT vision for
 * genuinely scanned/image documents. That text-first path is what actually fixes
 * character-level misreads (e.g. "F" read as "P") that pure vision OCR is prone to on dense
 * print; this class's own doubleCheck fallback (below) only catches misreads that are
 * *inconsistent* between two passes, not ones the model gets wrong the same way twice.
 */
@Service
public class OpenAiVisionOcrService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiVisionOcrService.class);

    @Value("${use.mock.responses:true}")
    private boolean useMockResponses;

    @Value("${image.describer.base-url:http://localhost:5000}")
    private String imageDescriberBaseUrl;

    private final RestTemplate restTemplate;

    public OpenAiVisionOcrService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ExtractResult(Map<String, String> values, Set<String> uncertain) {}

    public ExtractResult extractFields(String docType, MultipartFile file) throws IOException {
        return extractFields(docType, file.getBytes(),
                file.getOriginalFilename() != null ? file.getOriginalFilename() : docType);
    }

    /**
     * Same extraction, from raw bytes rather than a live upload — for callers that only have a
     * file back from storage (e.g. re-running OCR on an approved change request's replacement
     * document, downloaded back from FolderIt) rather than an in-flight MultipartFile.
     */
    public ExtractResult extractFields(String docType, byte[] fileBytes, String fileName) {
        SupplierDocumentConfig.DocDef doc = SupplierDocumentConfig.byId(docType);

        // Nothing to read off the document (e.g. a signed NDA) — skip the OCR round trip
        // entirely rather than asking Image_Describer to extract an empty field list.
        if (doc.fields().isEmpty()) {
            return new ExtractResult(Map.of(), Set.of());
        }

        if (useMockResponses) {
            logger.info("Using mock OCR response for docType={}", docType);
            return new ExtractResult(mockValues(doc), Set.of());
        }

        if (doc.doubleCheck().isEmpty()) {
            return new ExtractResult(extractOnce(doc, fileBytes, fileName), Set.of());
        }

        // Two independent passes for doubleCheck fields (CIN, PAN, GSTIN, Udyam, account
        // number, IFSC) — those only get auto-filled if both reads agree exactly, since a
        // wrong-but-plausible character there is costly. Disagreeing fields are dropped and
        // reported as uncertain so the applicant re-checks and types them in.
        CompletableFuture<Map<String, String>> passA = CompletableFuture.supplyAsync(() -> tryExtract(doc, fileBytes, fileName));
        CompletableFuture<Map<String, String>> passB = CompletableFuture.supplyAsync(() -> tryExtract(doc, fileBytes, fileName));
        Map<String, String> a = passA.join();
        Map<String, String> b = passB.join();

        Map<String, String> values = new HashMap<>(a);
        Set<String> uncertain = new HashSet<>();
        for (String key : doc.doubleCheck()) {
            String av = a.getOrDefault(key, "").trim().toUpperCase();
            String bv = b.getOrDefault(key, "").trim().toUpperCase();
            if (!av.equals(bv)) {
                values.remove(key);
                uncertain.add(key);
            }
        }
        return new ExtractResult(values, uncertain);
    }

    private Map<String, String> tryExtract(SupplierDocumentConfig.DocDef doc, byte[] fileBytes, String fileName) {
        try {
            return extractOnce(doc, fileBytes, fileName);
        } catch (Exception e) {
            logger.error("OCR pass failed for docType={}", doc.id(), e);
            return Map.of();
        }
    }

    private Map<String, String> extractOnce(SupplierDocumentConfig.DocDef doc, byte[] fileBytes, String fileName) {
        JSONArray fieldsJson = new JSONArray();
        for (SupplierDocumentConfig.FieldDef f : doc.fields()) {
            String label = f.isDate() ? f.label() + " (answer in YYYY-MM-DD format)" : f.label();
            fieldsJson.put(new JSONObject().put("key", f.key()).put("label", label));
        }

        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("fields", fieldsJson.toString());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        String url = imageDescriberBaseUrl + "/supplier-doc/extract";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Supplier doc OCR request failed: " + response.getStatusCode());
        }

        JSONObject data = new JSONObject(response.getBody());
        JSONObject valuesJson = data.optJSONObject("values");
        Map<String, String> values = new HashMap<>();
        if (valuesJson != null) {
            for (SupplierDocumentConfig.FieldDef f : doc.fields()) {
                if (valuesJson.has(f.key())) {
                    String raw = valuesJson.optString(f.key(), "");
                    values.put(f.key(), f.isDate() ? normalizeDate(raw) : raw);
                }
            }
        }
        return values;
    }

    // The frontend's date input requires exact yyyy-MM-dd — the prompt asks the model for that
    // format directly, but this is a safety net for whenever it doesn't comply (e.g. the
    // document itself is phrased unusually). Falls back to the raw string if nothing matches,
    // so a field the applicant can still see and fix by hand beats one silently dropped.
    private static final java.time.format.DateTimeFormatter[] DATE_FORMATS = {
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    };

    private static String normalizeDate(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return trimmed;
        for (java.time.format.DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return java.time.LocalDate.parse(trimmed, fmt).toString();
            } catch (java.time.format.DateTimeParseException ignored) {
                // try the next pattern
            }
        }
        logger.warn("Could not normalize extracted date '{}' to yyyy-MM-dd", raw);
        return trimmed;
    }

    private Map<String, String> mockValues(SupplierDocumentConfig.DocDef doc) {
        Map<String, String> mock = new HashMap<>();
        for (SupplierDocumentConfig.FieldDef f : doc.fields()) {
            mock.put(f.key(), switch (f.key()) {
                case "cin" -> "U29999MH2020PTC123456";
                case "pan" -> "AAPFU0939F";
                case "gstin" -> "27AAPFU0939F1ZV";
                case "benName" -> "ACME TECHNOLOGIES PRIVATE LIMITED";
                case "acctNo" -> "123456789012";
                case "ifsc" -> "HDFC0001234";
                case "udyam" -> "UDYAM-MH-01-1234567";
                default -> "";
            });
        }
        return mock;
    }
}
