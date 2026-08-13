package com.example.multimedia.file_upload_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {
    private String status;
    private int totalRows;
    private int inserted;
    private int updated;
    private int failed;
}
