package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.MaterialCreateRequest;
import com.example.multimedia.file_upload_api.service.MaterialService;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends MaterialService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialServiceImpl.class);
    private final ItemCategoryRepository itemCategoryRepository;

    @Override
    public ServiceResponse saveMaterial(MaterialCreateRequest material, MultipartFile barcodeImage, List<MultipartFile> materialImages) throws IOException {
        // Validate category exists
        if (material.getItemCategoryCode() == null || material.getItemCategoryCode().trim().isEmpty()) {
            throw new RuntimeException("Category code cannot be null or empty");
        }

        logger.info("Looking up category with code: {}", material.getItemCategoryCode());
        var category = itemCategoryRepository.findByCodeIgnoreCase(material.getItemCategoryCode())
            .orElseThrow(() -> new RuntimeException("Category not found with code: " + material.getItemCategoryCode()));
        logger.info("Found category: {} with ID: {}", category.getCode(), category.getItemCategoryId());

        // Call parent implementation
        return super.saveMaterial(material, barcodeImage, materialImages);
    }
} 