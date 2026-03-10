package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ItemCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemCategoryService {

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Transactional
    public ServiceResponse saveItemCategory(ItemCategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Check if code already exists
            if (itemCategoryRepository.existsByCode(dto.getCode())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Item category with code " + dto.getCode() + " already exists"
                );
            }

            ItemCategory itemCategory = new ItemCategory();
            itemCategory.setCode(dto.getCode());
            itemCategory.setDescription(dto.getDescription());
            itemCategory.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

            itemCategory = itemCategoryRepository.save(itemCategory);

            response.addData("itemCategory", convertToDTO(itemCategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item category saved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to save item category: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse updateItemCategory(ItemCategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            ItemCategory itemCategory = itemCategoryRepository.findById(dto.getItemCategoryId())
                .orElseThrow(() -> new RuntimeException("Item category not found with ID: " + dto.getItemCategoryId()));

            // Check if code is being changed and if new code already exists
            if (!itemCategory.getCode().equals(dto.getCode()) && 
                itemCategoryRepository.existsByCode(dto.getCode())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Item category with code " + dto.getCode() + " already exists"
                );
            }

            itemCategory.setCode(dto.getCode());
            itemCategory.setDescription(dto.getDescription());
            itemCategory.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

            itemCategory = itemCategoryRepository.save(itemCategory);

            response.addData("itemCategory", convertToDTO(itemCategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item category updated successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update item category: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAllItemCategories() {
        ServiceResponse response = new ServiceResponse();

        try {
            List<ItemCategory> itemCategories = itemCategoryRepository.findByIsActive(true);
            List<ItemCategoryDTO> itemCategoryDTOs = itemCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("itemCategories", itemCategoryDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item categories retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve item categories: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getItemCategoryById(Long id) {
        ServiceResponse response = new ServiceResponse();

        try {
            ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item category not found with ID: " + id));

            response.addData("itemCategory", convertToDTO(itemCategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item category retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve item category: " + e.getMessage()
            );
        }
    }

    private ItemCategoryDTO convertToDTO(ItemCategory itemCategory) {
        return new ItemCategoryDTO(
            itemCategory.getItemCategoryId(),
            itemCategory.getCode(),
            itemCategory.getDescription(),
            itemCategory.getIsActive(),
            itemCategory.getCreatedDate(),
            itemCategory.getModifiedDate()
        );
    }
} 