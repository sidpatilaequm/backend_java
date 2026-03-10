package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkMaterialCreateRequest {
    private List<MaterialCreateRequest> materials;
    
    @Data
    public static class MaterialCreateRequest {
        private String materialName;
        private String description;
        private String vendorArticleNumber;
        private Boolean blocked;
        private String type;
        private String baseUnitOfMeasure;
        private String itemCategoryCode;
        private String subcategoryName;
        private String hsnCode;
        private String sku;
        private String purchasingCode;
        private Boolean variantMandatory;
        private Long locationId;
        private List<MaterialAttributeDTO> attributes;
        private List<GeneralAttributeRequest> generalAttributes;
        
        // Image data as base64 strings for JSON API
        private String barcodeImageBase64;
        private List<String> materialImagesBase64;
        
        @Data
        public static class GeneralAttributeRequest {
            private Long attributeId;
            private String type; // Should be "GENERAL"
        }
    }
}
