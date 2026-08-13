package com.example.multimedia.file_upload_api.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
public class AttestrIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AttestrIntegrationService.class);

    @Value("${attestr.api.base-url:https://api.attestr.com/api/v2}")
    private String attestrBaseUrl;

    @Value("${attestr.auth.token:}")
    private String attestrApiToken;

    private final RestTemplate restTemplate;

    public AttestrIntegrationService() {
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // The token provided is a base64 string, so we'll set it as a Basic Authorization header
        headers.set("Authorization", "Basic " + attestrApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders getMultipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + attestrApiToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    // 1. GST OCR & Verification
    public JSONObject verifyGst(String gstin) {
        String url = attestrBaseUrl + "/public/check/gstin";
        Map<String, String> request = new HashMap<>();
        request.put("reg", gstin);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr GST API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    public JSONObject extractGstFromImage(MultipartFile file) {
        // Implementation for Attestr's GST Image Reader API
        // Usually requires uploading the file and polling or getting immediate extraction
        return uploadFileToAttestr(file, "/public/images/gst");
    }

    // 2. PAN OCR & Verification
    public JSONObject verifyPan(String pan) {
        String url = attestrBaseUrl + "/public/check/pan";
        Map<String, String> request = new HashMap<>();
        request.put("pan", pan);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr PAN API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    public JSONObject extractPanFromImage(MultipartFile file) {
        return uploadFileToAttestr(file, "/public/images/pan");
    }

    // 3. MSME Verification
    public JSONObject verifyMsmeUdyam(String udyamNumber) {
        String url = attestrBaseUrl + "/public/check/udyam";
        Map<String, String> request = new HashMap<>();
        request.put("reg", udyamNumber);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr MSME API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    // 4. Cancelled Cheque / Bank Verification
    public JSONObject verifyBankAccount(String accountNumber, String ifsc) {
        String url = attestrBaseUrl + "/public/check/bank";
        Map<String, String> request = new HashMap<>();
        request.put("acc", accountNumber);
        request.put("ifsc", ifsc);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr Bank Verification API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    public JSONObject extractChequeDetails(MultipartFile file) {
        return uploadFileToAttestr(file, "/public/images/cheque");
    }

    // 5. ITR Verification
    public JSONObject verifyItr(String pan) {
        String url = attestrBaseUrl + "/public/check/itr";
        Map<String, String> request = new HashMap<>();
        request.put("pan", pan);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr ITR API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    // 6. Certificate of Incorporation (Company Master Data)
    public JSONObject verifyCompanyCin(String cin) {
        String url = attestrBaseUrl + "/public/check/cin";
        Map<String, String> request = new HashMap<>();
        request.put("reg", cin);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, getHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Attestr CIN API", e);
            return createErrorResponse(e.getMessage());
        }
    }

    // Generic Multi-part file uploader for OCR APIs
    private JSONObject uploadFileToAttestr(MultipartFile file, String endpointPath) {
        String url = attestrBaseUrl + endpointPath;
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, getMultipartHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error uploading file to Attestr: {}", endpointPath, e);
            return createErrorResponse(e.getMessage());
        }
    }

    private JSONObject createErrorResponse(String errorMessage) {
        JSONObject error = new JSONObject();
        error.put("status", "error");
        error.put("message", errorMessage);
        return error;
    }
}
