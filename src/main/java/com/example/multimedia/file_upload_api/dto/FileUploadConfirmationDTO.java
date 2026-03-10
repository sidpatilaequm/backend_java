package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class FileUploadConfirmationDTO {
    private Long userId;
    private String gstinNumber;
    private String panNumber;
    private String accountNumber;
    private boolean confirmed;
    private String cinNo; // Optional, for Certificate of Incorporation
} 