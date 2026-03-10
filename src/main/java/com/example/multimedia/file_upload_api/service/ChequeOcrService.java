package com.example.multimedia.file_upload_api.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ChequeOcrService {

    private static final Logger logger = LoggerFactory.getLogger(ChequeOcrService.class);
    private static final String UPLOAD_URL = "https://api.attestr.com/api/v2/public/media/image/multipart";
    private static final String OCR_URL = "https://api.attestr.com/api/v2/public/xtract";

    @Value("${attestr.auth.token}")
    private String AUTH_TOKEN;

    @Value("${use.mock.responses:false}")
    private boolean useMockResponses;

    private final RestTemplate restTemplate;

    @Autowired
    private MockResponseService mockResponseService;

    @Autowired
    public ChequeOcrService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JSONObject processChequeFile(MultipartFile file) throws IOException {
        if (mockResponseService.isUseMockResponses()) {
            return mockResponseService.getMockResponse("cheque");
        }

        String mediaId = uploadFile(file);
        return extractChequeData(mediaId);
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] fileBytes;

        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            fileBytes = convertPdfToImage(file);
            fileName = fileName.replace(".pdf", ".png");
            contentType = "image/png";
        } else {
            fileBytes = file.getBytes();
        }

        String finalFileName = fileName;
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return finalFileName;
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(UPLOAD_URL, HttpMethod.POST, requestEntity, String.class);

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

    private JSONObject extractChequeData(String mediaId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + AUTH_TOKEN);

            JSONObject requestBody = new JSONObject();
            requestBody.put("src", mediaId);
            requestBody.put("type", "BANK_CHEQUE");

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                OCR_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to extract cheque data: Invalid response");
            }

            JSONObject jsonResponse = new JSONObject(response.getBody());
            if (!jsonResponse.optBoolean("valid", false)) {
                throw new RuntimeException("Failed to extract cheque data: " + jsonResponse.optString("message", "Invalid cheque document"));
            }

            return jsonResponse;
        } catch (Exception e) {
            logger.error("Error in cheque extraction", e);
            throw new RuntimeException("Failed to extract cheque data: " + e.getMessage());
        }
    }
}
