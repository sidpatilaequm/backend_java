package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class FileUploadDTO {
    private String fileName;
    private String fileType;
    private String documentType; // GST, PAN, Cheque, COI
    private String filePath;
    private Long userId;
} 