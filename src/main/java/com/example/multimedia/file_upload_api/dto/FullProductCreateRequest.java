package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.entity.Material;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FullProductCreateRequest {
    private Material product;
    private Double initialQty;
    private BigDecimal price;
    private Long categoryId;
    private Long locationId;
    private Long superAdminId;
}
