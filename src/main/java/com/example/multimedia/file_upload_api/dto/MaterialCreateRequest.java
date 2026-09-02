package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class MaterialCreateRequest {
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

    private List<GeneralAttributeRequest> generalAttributes;
    // Add other fields as needed
    private Long superAdminId;
    private Long locationId;

    @Data
    public static class GeneralAttributeRequest {
        private Long attributeId;
        private String type; // Should be "GENERAL"
    }
} 