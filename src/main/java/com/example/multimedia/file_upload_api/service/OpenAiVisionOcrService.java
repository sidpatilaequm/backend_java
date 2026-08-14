package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.util.SupplierDocumentConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Document field extraction via OpenAI vision — Java port of
 * become-a-supplier/lib/ocr.ts. Uses the Responses API (gpt-4o-mini) with
 * structured JSON output. PDFs are rasterized ourselves first (reusing the
 * same PDFBox approach GSTOCRService already uses, at the same 300 DPI) —
 * OpenAI's own PDF handling renders pages internally at too low a
 * resolution for dense print, confirmed against a real scanned cheque by
 * the original Next.js prototype.
 */
@Service
public class OpenAiVisionOcrService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiVisionOcrService.class);
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4o-mini";

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${use.mock.responses:true}")
    private boolean useMockResponses;

    private final RestTemplate restTemplate;

    public OpenAiVisionOcrService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ExtractResult(Map<String, String> values, Set<String> uncertain) {}

    public ExtractResult extractFields(String docType, MultipartFile file) throws IOException {
        SupplierDocumentConfig.DocDef doc = SupplierDocumentConfig.byId(docType);

        if (useMockResponses) {
            logger.info("Using mock OCR response for docType={}", docType);
            return new ExtractResult(mockValues(doc), Set.of());
        }

        byte[] imageBytes = toImageBytes(file);

        if (doc.doubleCheck().isEmpty()) {
            return new ExtractResult(extractOnce(doc, imageBytes), Set.of());
        }

        // Two independent OCR passes for doubleCheck fields (e.g. account number, IFSC) —
        // those only get auto-filled if both reads agree exactly, since a wrong-but-plausible
        // digit there is costly. Disagreeing fields are dropped and reported as uncertain.
        CompletableFuture<Map<String, String>> passA = CompletableFuture.supplyAsync(() -> tryExtract(doc, imageBytes));
        CompletableFuture<Map<String, String>> passB = CompletableFuture.supplyAsync(() -> tryExtract(doc, imageBytes));
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

    private Map<String, String> tryExtract(SupplierDocumentConfig.DocDef doc, byte[] imageBytes) {
        try {
            return extractOnce(doc, imageBytes);
        } catch (Exception e) {
            logger.error("OCR pass failed for docType={}", doc.id(), e);
            return Map.of();
        }
    }

    private byte[] toImageBytes(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                BufferedImage image = renderer.renderImageWithDPI(0, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            }
        }
        return file.getBytes();
    }

    private Map<String, String> extractOnce(SupplierDocumentConfig.DocDef doc, byte[] imageBytes) {
        StringBuilder fieldList = new StringBuilder();
        for (SupplierDocumentConfig.FieldDef f : doc.fields()) {
            fieldList.append("- ").append(f.key()).append(": ").append(f.label()).append("\n");
        }
        String instructions = "Extract these fields from the document image, exactly as printed — every "
                + "character matters, especially in long alphanumeric codes like account numbers and IFSC "
                + "codes. The scan may be rotated; read the text regardless of orientation. If the document "
                + "has a MICR line (a row of digits at the very bottom between special symbols, used for "
                + "cheque processing), do not use it as the account number — use the account number printed "
                + "near a label like \"A/c No\" instead. Return ONLY a JSON object with these exact keys — "
                + "use an empty string for any field that isn't legible or present:\n" + fieldList;

        String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

        JSONObject content1 = new JSONObject().put("type", "input_text").put("text", instructions);
        JSONObject content2 = new JSONObject().put("type", "input_image").put("image_url", dataUrl).put("detail", "high");
        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", new JSONArray().put(content1).put(content2));
        JSONObject body = new JSONObject()
                .put("model", MODEL)
                .put("input", new JSONArray().put(message))
                .put("text", new JSONObject().put("format", new JSONObject().put("type", "json_object")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(RESPONSES_URL, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("OpenAI OCR request failed: " + response.getStatusCode());
        }
        JSONObject data = new JSONObject(response.getBody());
        if (data.has("error") && !data.isNull("error")) {
            throw new RuntimeException("OpenAI OCR failed: " + data.getJSONObject("error").optString("message"));
        }

        JSONArray output = data.optJSONArray("output");
        JSONObject textPart = null;
        if (output != null) {
            for (int i = 0; i < output.length(); i++) {
                JSONObject item = output.getJSONObject(i);
                if (!"message".equals(item.optString("type"))) continue;
                JSONArray contentArr = item.optJSONArray("content");
                if (contentArr == null) continue;
                for (int j = 0; j < contentArr.length(); j++) {
                    JSONObject c = contentArr.getJSONObject(j);
                    if ("output_text".equals(c.optString("type"))) {
                        textPart = c;
                        break;
                    }
                }
            }
        }
        if (textPart == null) throw new RuntimeException("OpenAI OCR returned no text output");

        JSONObject parsed = new JSONObject(textPart.getString("text"));
        Map<String, String> values = new HashMap<>();
        for (SupplierDocumentConfig.FieldDef f : doc.fields()) {
            if (parsed.has(f.key()) && parsed.get(f.key()) instanceof String) {
                values.put(f.key(), parsed.getString(f.key()));
            }
        }
        return values;
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
