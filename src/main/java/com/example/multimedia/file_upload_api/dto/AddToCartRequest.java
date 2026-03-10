package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal price;
    private Integer quantity;
    private Long channelId;
    private String channelCode;
    private Long imageId;
    private String imageName;
    private String imageType;
    private String imageBase64;
}
