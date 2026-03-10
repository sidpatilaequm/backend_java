package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingProductDTO {
    
    private Long materialId;
    private String materialName;
    private String materialCode;
    private String materialDescription;
    private BigDecimal price;
    private List<MaterialImageDTO> materialImages;
    private BigDecimal channelSpecificPrice;
    
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
