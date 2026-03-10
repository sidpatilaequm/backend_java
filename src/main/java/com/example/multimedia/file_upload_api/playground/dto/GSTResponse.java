package com.example.multimedia.file_upload_api.playground.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GSTResponse {
    private String status;
    private String message;
    private Object data;
} 