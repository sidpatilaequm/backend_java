package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class MaterialVariantDTO {
    private String variantCode;
    private Double mrp;
    private Double sellingPrice;
    private Double cost;
    private Double stock;
    private byte[] barcodeImage;
    private byte[] variantImage;
    private Boolean isActive;
    private List<MaterialAttributeDTO> attributes;
} 