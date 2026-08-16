package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.VerifyDetail;
import com.example.multimedia.file_upload_api.dto.VerifyResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Microvista KYC verification (PAN, GSTIN, CIN, Udyam/MSME, bank penny-drop).
 * Java port of become-a-supplier/lib/microvista.ts — same base URL, same
 * auth quirk (every verification call needs the raw TokenID/TokenSecret as
 * query params *in addition to* the bearer token — confirmed against the
 * live API by the original prototype, not documented in Microvista's own
 * spec), same 5-minute token cache.
 */
@Service
public class MicrovistaService {

    private static final Logger logger = LoggerFactory.getLogger(MicrovistaService.class);
    private static final String AUTH_URL = "https://kycapi.microvistatech.com/api/Auth/GenerateAuthtoken";
    private static final String API_BASE = "https://kycapi.microvistatech.com";

    @Value("${microvista.token.id:}")
    private String tokenId;

    @Value("${microvista.token.secret:}")
    private String tokenSecret;

    @Value("${microvista.api.version:1}")
    private String apiVersion;

    private final RestTemplate restTemplate;

    private String cachedToken;
    private long cachedTokenExpiresAt;

    public MicrovistaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static String opt(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.isNull(k)) return obj.optString(k, "");
        }
        return "";
    }

    /**
     * Microvista sometimes reports isSuccess=true with a null "data" body — e.g. a
     * syntactically valid but non-existent PAN — rather than isSuccess=false. Returns null
     * instead of throwing so callers can report a clean "no record found" instead of a raw
     * JSONException.
     */
    private static JSONObject optObj(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.isNull(k)) return obj.optJSONObject(k);
        }
        return null;
    }

    private static boolean optBool(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.isNull(k)) return obj.optBoolean(k, false);
        }
        return false;
    }

    private static String join(String... parts) {
        return Stream.of(parts).filter(p -> p != null && !p.isBlank()).collect(Collectors.joining(" — "));
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && cachedTokenExpiresAt > now) return cachedToken;

        String url = UriComponentsBuilder.fromHttpUrl(AUTH_URL)
                .queryParam("TokenID", tokenId)
                .queryParam("TokenSecret", tokenSecret)
                .toUriString();

        JSONObject res = new JSONObject(restTemplate.getForObject(url, String.class));
        if (!optBool(res, "IsSuccess", "isSuccess")) {
            throw new RuntimeException("Microvista auth failed: " + opt(res, "Message", "message"));
        }
        JSONObject data = res.has("Data") ? res.getJSONObject("Data") : res.getJSONObject("data");
        cachedToken = data.getString("Token");
        cachedTokenExpiresAt = now + 5 * 60_000L;
        return cachedToken;
    }

    private JSONObject verifyGet(String path, java.util.Map<String, String> params, boolean retrying) {
        String token = getAccessToken();
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/api/v" + apiVersion + path)
                .queryParam("TokenID", tokenId)
                .queryParam("TokenSecret", tokenSecret);
        params.forEach(builder::queryParam);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(), org.springframework.http.HttpMethod.GET, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (HttpClientErrorException.Unauthorized e) {
            if (!retrying) {
                cachedToken = null;
                return verifyGet(path, params, true);
            }
            throw e;
        }
    }

    private JSONObject verifyGet(String path, java.util.Map<String, String> params) {
        return verifyGet(path, params, false);
    }

    public VerifyResult verifyPan(String pan) {
        try {
            JSONObject res = verifyGet("/BasicPanVerification/VerifyPan", java.util.Map.of("pan", pan));
            if (!optBool(res, "isSuccess", "IsSuccess")) {
                return VerifyResult.failed(join(opt(res, "message", "Message"), "Could not verify PAN."));
            }
            JSONObject data = optObj(res, "data", "Data");
            if (data == null) return VerifyResult.failed("No record found for this PAN.");
            return new VerifyResult(true, join(opt(data, "paN_STATUS"), opt(data, "user_Full_Name")), new ArrayList<>());
        } catch (Exception e) {
            logger.error("PAN verification failed", e);
            return VerifyResult.failed("Could not verify PAN.");
        }
    }

    /** India's financial year runs April-March. */
    private static String currentFinancialYear() {
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        return startYear + "-" + String.format("%02d", (startYear + 1) % 100);
    }

    private String formatGstAddress(JSONObject addr) {
        if (addr == null) return "";
        String line1 = Stream.of(opt(addr, "flno"), opt(addr, "bno"), opt(addr, "bnm"))
                .filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
        String line2 = Stream.of(opt(addr, "st"), opt(addr, "loc"))
                .filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
        return Stream.of(line1, line2, opt(addr, "stcd"), opt(addr, "pncd"))
                .filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
    }

    /**
     * Returns null (rather than throwing) on any failure — a return-filing hiccup
     * should never block GSTIN verification itself, it's an addition, not a hard
     * requirement. The outer `data.status` field on this endpoint says "No records
     * Found" even when filingStatus has real records (confirmed against the live
     * API by the original prototype) — count the actual records instead.
     */
    private String getGstReturnFilingSummary(String gstin) {
        try {
            String fy = currentFinancialYear();
            JSONObject res = verifyGet("/GstReturnFiling/GstReturnFilingStatus",
                    java.util.Map.of("GSTIN", gstin, "financialYear", fy));
            if (!optBool(res, "isSuccess", "IsSuccess")) return null;
            JSONObject data = res.getJSONObject("data");
            JSONArray filingStatusOuter = data.optJSONArray("filingStatus");
            JSONArray records = (filingStatusOuter != null && filingStatusOuter.length() > 0)
                    ? filingStatusOuter.getJSONArray(0) : new JSONArray();
            if (records.isEmpty()) return "No returns filed for " + fy + ".";
            long filed = 0;
            for (int i = 0; i < records.length(); i++) {
                if ("Filed".equals(records.getJSONObject(i).optString("status"))) filed++;
            }
            if (filed == records.length()) {
                return filed + " of " + records.length() + " returns filed for " + fy + ".";
            }
            return filed + " of " + records.length() + " returns filed for " + fy + " — "
                    + (records.length() - filed) + " not yet filed.";
        } catch (Exception e) {
            return null;
        }
    }

    public VerifyResult verifyGstin(String gstin) {
        try {
            JSONObject res = verifyGet("/GstinVerification/GetGstinVerification", java.util.Map.of("gstin", gstin));
            if (!optBool(res, "isSuccess", "IsSuccess")) {
                return VerifyResult.failed(join(opt(res, "message", "Message"), "Could not verify GSTIN."));
            }
            JSONObject data = optObj(res, "data", "Data");
            JSONObject tp = data != null ? optObj(data, "appSCommonSearchTPResponse") : null;
            if (tp == null) return VerifyResult.failed("No record found for this GSTIN.");
            List<VerifyDetail> details = new ArrayList<>(List.of(
                    new VerifyDetail("Legal name", opt(tp, "lgnm")),
                    new VerifyDetail("Trade name", opt(tp, "tradeNam")),
                    new VerifyDetail("Constitution of business", opt(tp, "ctb")),
                    new VerifyDetail("Registered address", formatGstAddress(tp.optJSONObject("pradr") != null ? tp.getJSONObject("pradr").optJSONObject("addr") : null)),
                    new VerifyDetail("Status", opt(tp, "sts")),
                    new VerifyDetail("Registered from", opt(tp, "rgdt")),
                    new VerifyDetail("State jurisdiction", opt(tp, "stj"))
            ));
            String returnFilingSummary = getGstReturnFilingSummary(gstin);
            if (returnFilingSummary != null) details.add(new VerifyDetail("Return filing", returnFilingSummary));
            return new VerifyResult(true, join(opt(tp, "sts"), opt(tp, "lgnm")), details);
        } catch (Exception e) {
            logger.error("GSTIN verification failed", e);
            return VerifyResult.failed("Could not verify GSTIN.");
        }
    }

    public VerifyResult verifyCin(String cin) {
        try {
            JSONObject res = verifyGet("/CIN/GetCompanyDetails", java.util.Map.of("CINorLLPorFCRN", cin));
            if (!optBool(res, "IsSuccess", "isSuccess")) {
                return VerifyResult.failed(join(opt(res, "Message", "message"), "Could not verify CIN."));
            }
            JSONObject data = optObj(res, "Data", "data");
            JSONObject c = data != null ? optObj(data, "companyData") : null;
            if (c == null) return VerifyResult.failed("No record found for this CIN.");
            List<VerifyDetail> details = new ArrayList<>(List.of(
                    new VerifyDetail("Company name", opt(c, "company")),
                    new VerifyDetail("Date of incorporation", opt(c, "dateOfIncorporation"))
            ));
            JSONArray directorData = data.optJSONArray("directorData");
            if (directorData != null) {
                for (int i = 0; i < directorData.length(); i++) {
                    JSONObject d = directorData.getJSONObject(i);
                    String name = Stream.of(opt(d, "FirstName"), opt(d, "MiddleName"), opt(d, "LastName"))
                            .filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
                    // The API's own field is "DINOrPAN" — it can hold either identifier depending
                    // on the director, so the label says PAN/DIN rather than claiming it's always a PAN.
                    details.add(new VerifyDetail("Director " + name + " — PAN/DIN", opt(d, "DINOrPAN")));
                }
            }
            return new VerifyResult(true, opt(c, "company"), details);
        } catch (Exception e) {
            logger.error("CIN verification failed", e);
            return VerifyResult.failed("Could not verify CIN.");
        }
    }

    public VerifyResult verifyUdyam(String urn) {
        try {
            JSONObject res = verifyGet("/MSME/GetUdyamDetails", java.util.Map.of("urn", urn));
            if (!optBool(res, "isSuccess", "IsSuccess")) {
                return VerifyResult.failed(join(opt(res, "message", "Message"), "Could not verify Udyam registration."));
            }
            JSONObject data = optObj(res, "data", "Data");
            JSONObject msme = data != null ? optObj(data, "msmeResponse") : null;
            JSONObject reg = msme != null ? optObj(msme, "reg_details") : null;
            if (reg == null) return VerifyResult.failed("No record found for this Udyam registration number.");
            String enterpriseType = opt(reg, "typeOfEnterprise");
            return new VerifyResult(true,
                    join(opt(reg, "nameOfEnterprise"), enterpriseType.isBlank() ? "" : enterpriseType + " enterprise"),
                    new ArrayList<>(List.of(
                            new VerifyDetail("Registration number", opt(reg, "urn")),
                            new VerifyDetail("Registered on", opt(reg, "dateOfUdyamReg"))
                    )));
        } catch (Exception e) {
            logger.error("Udyam verification failed", e);
            return VerifyResult.failed("Could not verify Udyam registration.");
        }
    }

    public VerifyResult verifyBank(String accountNumber, String ifsc, String name) {
        try {
            JSONObject res = verifyGet("/BankVerification/VerifyBank",
                    java.util.Map.of("account_number", accountNumber, "ifsc", ifsc, "name", name));
            if (!optBool(res, "isSuccess", "IsSuccess")) {
                return VerifyResult.failed(join(opt(res, "message", "Message"), "Could not verify bank account."));
            }
            JSONObject data = optObj(res, "data", "Data");
            if (data == null) return VerifyResult.failed("No record found for this bank account.");
            boolean verified = "ACCOUNT_VALID".equals(opt(data, "result_status"));
            return new VerifyResult(verified, join(opt(data, "result_status"), opt(data, "result_name")), new ArrayList<>());
        } catch (Exception e) {
            logger.error("Bank verification failed", e);
            return VerifyResult.failed("Could not verify bank account.");
        }
    }
}
