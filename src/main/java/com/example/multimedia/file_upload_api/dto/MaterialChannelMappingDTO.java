package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialChannelMappingDTO {
    private Long id;
    private Long companyId;
    private Long materialId;
    private Long channelId;
    private Long categoryId;
    private String channelName;
    private String channelCode;
    private String categoryName;
    private String categoryCode;
    private BigDecimal price;
    private Integer stock;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
