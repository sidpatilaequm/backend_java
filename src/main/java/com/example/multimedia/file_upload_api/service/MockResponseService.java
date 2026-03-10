package com.example.multimedia.file_upload_api.service;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class MockResponseService {

    private Map<String, JSONObject> mockResponses = new HashMap<>();
    private boolean useMockResponses = true;

    @PostConstruct
    public void init() {
        loadMockResponses();
    }

    public void loadMockResponses() {
        try {
            ClassPathResource resource = new ClassPathResource("mock-responses.json");
            String jsonContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonContent);

            // Store each response type
            mockResponses.put("gst", jsonObject.getJSONObject("gst"));
            mockResponses.put("pan", jsonObject.getJSONObject("pan"));
            mockResponses.put("cheque", jsonObject.getJSONObject("cheque"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getMockResponse(String type) {
        return mockResponses.getOrDefault(type, new JSONObject());
    }

    public boolean isUseMockResponses() {
        return useMockResponses;
    }

    public void setUseMockResponses(boolean useMockResponses) {
        this.useMockResponses = useMockResponses;
    }
} 