package com.example.multimedia.file_upload_api.playground.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class BusinessCardScannerService {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiHost;
    private final String apiUrl;

    public BusinessCardScannerService(
            @Value("${business.card.scanner.api.key}") String apiKey,
            @Value("${business.card.scanner.api.host}") String apiHost,
            @Value("${business.card.scanner.api.url}") String apiUrl) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.apiUrl = apiUrl;
    }

    public Map<String, Object> scanBusinessCard(MultipartFile file) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        
        // Convert file to base64
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        
        // Create the request body
        String requestBody = createRequestBody(base64Image);
        RequestBody body = RequestBody.create(mediaType, requestBody);

        // Build the request
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            // Parse the response
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, Map.class);
        }
    }

    private String createRequestBody(String base64Image) throws IOException {
        Map<String, Object> extractionDetails = Map.of(
            "name", "Business Card - Extraction",
            "language", "English",
            "fields", new Object[]{
                Map.of(
                    "key", "name",
                    "description", "Name of the person in the business card",
                    "example", "Bardahan Alexandru"
                ),
                Map.of(
                    "key", "job_title",
                    "description", "Job title of the person",
                    "example", "CEO"
                ),
                Map.of(
                    "key", "company_name",
                    "description", "Extract company name from card; if absent, deduce from email, website, or social domains.",
                    "example", "Fastapp Development"
                ),
                Map.of(
                    "key", "address",
                    "description", "Address of the company",
                    "example", "Bucharest, Romania"
                ),
                Map.of(
                    "key", "phone_numbers",
                    "type", "array",
                    "items", Map.of(
                        "type", "string",
                        "example", "+40 755 852 411"
                    )
                ),
                Map.of(
                    "key", "email_addresses",
                    "type", "array",
                    "items", Map.of(
                        "type", "string",
                        "example", "office@fastappgroup.com"
                    )
                ),
                Map.of(
                    "key", "website_url",
                    "description", "Website url of the company",
                    "example", "https://fastappgroup.com"
                ),
                Map.of(
                    "key", "social_media_handles",
                    "description", "Social media handles of the company",
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", new Object[]{
                            Map.of(
                                "key", "type",
                                "description", "Type of the social media handle",
                                "example", "facebook"
                            ),
                            Map.of(
                                "key", "username",
                                "description", "Username of the social media handle",
                                "example", "fastappgroup"
                            )
                        }
                    )
                )
            }
        );

        Map<String, Object> requestBody = Map.of(
            "extractionDetails", extractionDetails,
            "file", base64Image
        );

        return objectMapper.writeValueAsString(requestBody);
    }
} 