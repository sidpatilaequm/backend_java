package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialImageDTO {
    private Long imageId;
    private String imageName;
    private String imageType;
    private byte[] imageData;
    private Integer sequenceOrder;
    private Long materialId;
} 