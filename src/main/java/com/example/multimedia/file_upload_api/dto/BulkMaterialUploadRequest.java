package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Data
public class BulkMaterialUploadRequest {
    
    @NotNull(message = "Materials list cannot be null")
    private List<MaterialUploadItem> materials;
    
    @Data
    public static class MaterialUploadItem {
        @NotBlank(message = "Material name is required")
        private String materialName;
        
        @NotBlank(message = "Description is required")
        private String description;
        
        @NotBlank(message = "Vendor article number is required")
        private String vendorArticleNumber;
        
        private Boolean blocked = false;
        
        @NotBlank(message = "Type is required")
        private String type;
        
        @NotBlank(message = "Base unit of measure is required")
        private String baseUnitOfMeasure;
        
        @NotBlank(message = "Item category code is required")
        private String itemCategoryCode;
        
        @NotBlank(message = "Subcategory name is required")
        private String subcategoryName;
        
        @NotBlank(message = "HSN code is required")
        @Pattern(regexp = "^[0-9]{8}$", message = "HSN code must be 8 digits")
        private String hsnCode;
        
        @NotBlank(message = "SKU is required")
        private String sku;
        
        @NotBlank(message = "Purchasing code is required")
        private String purchasingCode;
        
        private Boolean variantMandatory = false;
        
        @NotBlank(message = "Location name is required")
        private String location;
    }
}
