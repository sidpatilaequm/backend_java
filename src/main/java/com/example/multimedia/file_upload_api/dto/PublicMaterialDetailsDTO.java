package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicMaterialDetailsDTO {
    
    // Material Information
    private Long materialId;
    private String materialName;
    private String materialCode;
    private String materialDescription;
    private BigDecimal price;
    private String category;
    private String brand;
    private String material;
    private String features;
    private String specifications;
    private String size;
    private String color;
    private Integer stock;
    private Boolean isActive;
    private String productUrl;
    
    // Channel Information
    private Long channelId;
    private String channelName;
    private String channelCode;
    private String channelDescription;
    private Boolean channelIsActive;
    
    // Images
    private List<MaterialImageDTO> materialImages;
    private byte[] barcodeImage;
    private String firstImageBase64; // Base64 encoded first image data
    
    // Timestamps
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    
    // Additional Channel-specific Information
    private String channelSpecificDescription;
    private BigDecimal channelSpecificPrice;
    private String channelSpecificFeatures;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialImageDTO {
        private Long imageId;
        private String imageName;
        private String imageType;
        private String imageUrl;
        private String imageBase64; // Base64 encoded image data
        private Boolean isPrimary;
        private Integer sequenceOrder;
    }
}
