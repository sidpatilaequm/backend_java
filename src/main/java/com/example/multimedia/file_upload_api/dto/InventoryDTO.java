package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long inventoryId;
    private Long materialId;
    private String materialName;
    private String sku;
    private Long variantId;
    private String variantCode;
    private Long locationId;
    private String locationName;
    private Integer stockQuantity;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class InventoryUpdateRequest {
    private Integer stockQuantity;
    private BigDecimal price;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class BulkInventoryUpdateRequest {
    private String sku;
    private String variantCode; // Optional, for variant-based inventory
    private String location; // Location name
    private Integer stockQuantity;
    private BigDecimal price;
}
