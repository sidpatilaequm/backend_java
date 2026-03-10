package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkMaterialResponse {
    private int totalMaterials;
    private int successfulMaterials;
    private int failedMaterials;
    private List<MaterialResult> results;
    
    @Data
    public static class MaterialResult {
        private String sku;
        private boolean success;
        private String message;
        private Long materialId;
        private String materialCode;
    }
}
