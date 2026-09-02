package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.entity.Inventory;
import com.example.multimedia.file_upload_api.entity.Material;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponseDTO {
    private Material product;
    private Inventory inventory;
}
