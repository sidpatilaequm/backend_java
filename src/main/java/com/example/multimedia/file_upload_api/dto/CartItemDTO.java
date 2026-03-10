package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    
    private Long cartItemId;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Long channelId;
    private String channelCode;
    private String channelName;
    private Long companyId;
    private String companyName;
    private LocalDateTime addedAt;
    private Long imageId;
    private String imageName;
    private String imageType;
    private String imageBase64;
}
