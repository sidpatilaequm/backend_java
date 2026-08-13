package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class MaterialDTO {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String description;
    private String vendorArticleNumber;
    private Boolean blocked;
    private String type;
    private String baseUnitOfMeasure;
    
    // Subcategory information
    private Long subcategoryId;
    private String subcategoryName;
    
    private Long subcategoryL1Id;
    private String subcategoryL1Name;
    
    private Long subcategoryL2Id;
    private String subcategoryL2Name;
    
    private Long subcategoryL3Id;
    private String subcategoryL3Name;
    
    // Category information
    private Long itemCategoryId;
    private String itemCategoryCode;
    private String itemCategoryDescription;
    
    private String hsnCode;
    private String sku;
    private String purchasingCode;
    private Boolean variantMandatory;

    // Super admin information
    private Long superAdminId;
    
    // Location information
    private Long locationId;
    private String locationName;
    private String locationAddress;

    // Image information
    private byte[] barcodeImage;
    private List<MaterialImageDTO> materialImages;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;

    private List<MaterialAttributeDTO> generalAttributes;
    private List<MaterialVariantDTO> variants;
    private List<MaterialAttributeDTO> attributes;

    public MaterialDTO(Long materialId, String materialCode, String materialName, String description, 
            String vendorArticleNumber, Boolean blocked, String type, String baseUnitOfMeasure, 
            Long subcategoryId, String subcategoryName,
            Long itemCategoryId, String itemCategoryCode, String itemCategoryDescription, 
            String hsnCode, String sku, String purchasingCode, Boolean variantMandatory, 
            Long superAdminId, byte[] barcodeImage, List<MaterialImageDTO> materialImages,
            LocalDateTime createdDate, LocalDateTime modifiedDate,
            List<MaterialAttributeDTO> generalAttributes, List<MaterialVariantDTO> variants) {
        this.materialId = materialId;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.description = description;
        this.vendorArticleNumber = vendorArticleNumber;
        this.blocked = blocked;
        this.type = type;
        this.baseUnitOfMeasure = baseUnitOfMeasure;
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.itemCategoryId = itemCategoryId;
        this.itemCategoryCode = itemCategoryCode;
        this.itemCategoryDescription = itemCategoryDescription;
        this.hsnCode = hsnCode;
        this.sku = sku;
        this.purchasingCode = purchasingCode;
        this.variantMandatory = variantMandatory;
        this.superAdminId = superAdminId;
        this.barcodeImage = barcodeImage;
        this.materialImages = materialImages;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.generalAttributes = generalAttributes;
        this.variants = variants;
    }
} 