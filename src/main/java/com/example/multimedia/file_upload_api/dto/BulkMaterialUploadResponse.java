package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkMaterialUploadResponse {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private int skippedCount;
    private List<MaterialUploadResult> results;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialUploadResult {
        private int index;
        private String sku;
        private String materialName;
        private String status; // SUCCESS, FAILED, SKIPPED
        private String message;
        private Long materialId; // Only for successful uploads
        private String errorCode; // Only for failures
    }
}
