package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class MaterialWithChannelInfoDTO {
    
    // Material basic information
    private Long materialId;
    private String materialName;
    private String description;
    private String type;
    private String baseUnitOfMeasure;
    private String hsnCode;
    private String sku;
    private String purchasingCode;
    private Boolean variantMandatory;
    private String vendorArticleNumber;
    private String materialCode;
    private Boolean blocked;
    
    // Image information
    private byte[] barcodeImage;
    private List<MaterialImageDTO> materialImages;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
    
    // Item Category information
    private Long itemCategoryId;
    private String itemCategoryName;
    private String itemCategoryCode;
    
    // Item Subcategory information
    private Long itemSubcategoryId;
    private String itemSubcategoryName;
    // Note: ItemSubcategory doesn't have a code field
    
    // Channel information
    private Long channelId;
    private String channelName;
    private String channelCode;
    
    // Channel Category information (if assigned)
    private Long channelCategoryId;
    private String channelCategoryName;
    private String channelCategoryCode;
    
    // Channel-specific material information
    private BigDecimal price;
    private Integer stock;
    private Boolean status;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime mappingCreatedAt;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime mappingUpdatedAt;
}
