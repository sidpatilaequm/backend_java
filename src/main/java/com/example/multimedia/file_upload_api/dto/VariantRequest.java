package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class VariantRequest {
    private List<VariantAttribute> attributes;
    private Double mrp;
    private Double sellingPrice;
    private Double cost;
    private Double stock;

    @Data
    public static class VariantAttribute {
        private Long attributeId;
        private String attributeValue;
    }
} 