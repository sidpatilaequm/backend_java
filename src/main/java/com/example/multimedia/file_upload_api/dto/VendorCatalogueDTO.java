package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
class CatalogueCheckResponseDTO {
    private boolean catalogueExists;
    private CatalogueInfoDTO catalogueInfo;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CatalogueInfoDTO {
    private String fileName;
    private LocalDateTime uploadDate;
    private String fileSize;
    private String fileType;
    private Long vendorId;
    private String catalogueId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CatalogueUploadResponseDTO {
    private String fileName;
    private LocalDateTime uploadDate;
    private String fileSize;
    private Long vendorId;
    private String catalogueId;
}
