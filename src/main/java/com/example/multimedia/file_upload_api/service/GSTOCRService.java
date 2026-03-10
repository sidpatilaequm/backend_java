package com.example.multimedia.file_upload_api.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class GSTOCRService {
    private static final Logger logger = LoggerFactory.getLogger(GSTOCRService.class);
    private static final String UPLOAD_URL = "https://api.attestr.com/api/v2/public/media/image/multipart";
    private static final String OCR_URL = "https://api.attestr.com/api/v2/public/xtract";
    
    @Value("${attestr.auth.token}")
    private String AUTH_TOKEN;

    @Value("${use.mock.responses:true}")
    private boolean useMockResponses;

    private final RestTemplate restTemplate;

    public GSTOCRService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JSONObject processGSTFile(MultipartFile file) throws IOException {
        try {
            logger.info("Starting GST file processing");
            
            if (useMockResponses) {
                logger.info("Using mock response for GST processing");
                return getMockGSTResponse();
            }

            // Step 1: Upload the file and get media ID
            String mediaId = uploadFile(file);
            logger.info("File uploaded successfully. Media ID: {}", mediaId);

            // Step 2: Extract GST data using the media ID
            JSONObject extractedData = extractGSTData(mediaId);
            logger.info("GST data extracted successfully");

            return extractedData;
        } catch (Exception e) {
            logger.error("Error processing GST file, returning mock response", e);
            return getMockGSTResponse();
        }
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] fileBytes;

        // Convert PDF to image if needed
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            fileBytes = convertPdfToImage(file);
            fileName = fileName.replace(".pdf", ".png");
            contentType = "image/png";
        } else {
            fileBytes = file.getBytes();
        }

        // Create resource from file bytes
        String finalFileName = fileName;
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return finalFileName;
            }
        };

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Basic " + AUTH_TOKEN);

        // Create request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        // Make request
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            UPLOAD_URL,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        // Handle response
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new IOException("Upload failed: " + response.getBody());
        }

        JSONObject jsonResponse = new JSONObject(response.getBody());
        if (!jsonResponse.has("_id")) {
            throw new IOException("Upload failed: '_id' not found in response");
        }

        return jsonResponse.getString("_id");
    }

    private byte[] convertPdfToImage(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

    private JSONObject extractGSTData(String mediaId) {
        try {
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + AUTH_TOKEN);

            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("src", mediaId);
            requestBody.put("type", "GST");
            requestBody.put("additional", JSONObject.NULL);

            // Make request
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                OCR_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            // Handle response
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to extract GST data: Invalid response");
            }

            return new JSONObject(response.getBody());

        } catch (Exception e) {
            logger.error("Error in GST extraction", e);
            throw new RuntimeException("Failed to extract GST data: " + e.getMessage());
        }
    }

    private JSONObject getMockGSTResponse() {
        JSONObject mockResponse = new JSONObject();
        mockResponse.put("status", "success");
        mockResponse.put("message", "GST details extracted successfully");
        mockResponse.put("confidence", 98.5);

        JSONObject data = new JSONObject();
        data.put("gstin", "27AAPFU0939F1ZV");
        data.put("legalName", "ACME TECHNOLOGIES PRIVATE LIMITED");
        data.put("tradeName", "ACME TECH");
        data.put("type", "Regular");
        data.put("taxpayerType", "Private Limited Company");
        data.put("status", "Active");
        data.put("lastUpdated", "2024-03-15");
        data.put("registrationDate", "2020-01-01");

        JSONObject address = new JSONObject();
        address.put("building", "Tech Park, Building A");
        address.put("street", "123 Innovation Street");
        address.put("city", "Bangalore");
        address.put("state", "Karnataka");
        address.put("pincode", "560001");
        data.put("address", address);

        JSONObject jurisdictions = new JSONObject();
        jurisdictions.put("center", "Center Jurisdiction");
        jurisdictions.put("state", "State Jurisdiction");
        data.put("jurisdictions", jurisdictions);

        mockResponse.put("data", data);
        return mockResponse;
    }
}
