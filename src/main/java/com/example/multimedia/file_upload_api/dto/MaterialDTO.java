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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;



    public MaterialDTO(Long materialId, String materialCode, String materialName, String description, 
            String vendorArticleNumber, Boolean blocked, String type, String baseUnitOfMeasure, 
            String hsnCode, String sku, String purchasingCode, Boolean variantMandatory, 
            Long superAdminId, byte[] barcodeImage,
            LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.materialId = materialId;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.description = description;
        this.vendorArticleNumber = vendorArticleNumber;
        this.blocked = blocked;
        this.type = type;
        this.baseUnitOfMeasure = baseUnitOfMeasure;
        this.hsnCode = hsnCode;
        this.sku = sku;
        this.purchasingCode = purchasingCode;
        this.variantMandatory = variantMandatory;
        this.superAdminId = superAdminId;
        this.barcodeImage = barcodeImage;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
} 