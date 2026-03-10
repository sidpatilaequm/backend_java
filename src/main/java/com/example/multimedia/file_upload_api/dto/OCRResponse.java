package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OCRResponse {
    private String status;
    private String message;
    private Map<String, Object> data;
    private double confidence;
} 