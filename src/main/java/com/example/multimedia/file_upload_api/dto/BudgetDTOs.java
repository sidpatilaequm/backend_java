package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BudgetDTOs {

    @Data
    public static class BudgetUploadRequest {
        private String versionCode;
        private String fiscalYear;
        private BigDecimal totalAmount;
        // e.g. "HR" -> [{"category": "Travel", "amount": 5000}, ...]
        private Map<String, List<BudgetItemDTO>> departmentBudgets;
    }

    @Data
    public static class BudgetItemDTO {
        private String category;
        private BigDecimal amount;
    }

    @Data
    public static class BudgetVersionResponse {
        private String versionCode;
        private String fiscalYear;
        private BigDecimal totalAmount;
        private boolean isActive;
        private boolean isLocked;
    }
}
