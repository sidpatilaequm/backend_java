package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.entity.Inventory;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.MaterialChannelMapping;
import com.example.multimedia.file_upload_api.entity.MaterialImage;
import lombok.Data;

import java.util.List;

@Data
public class ProductResponseDTO {
    private Material product;
    private List<MaterialImage> images;
    private Inventory inventory;
    private List<MaterialChannelMapping> pricing;
}
