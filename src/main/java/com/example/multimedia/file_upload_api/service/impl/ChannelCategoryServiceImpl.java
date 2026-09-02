package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.ChannelCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Channel;
import com.example.multimedia.file_upload_api.entity.ChannelCategory;
import com.example.multimedia.file_upload_api.repository.CategoryChannelMappingRepository;
import com.example.multimedia.file_upload_api.repository.ChannelCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ChannelRepository;

import com.example.multimedia.file_upload_api.service.ChannelCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelCategoryServiceImpl implements ChannelCategoryService {

    private final ChannelCategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;

    private final CategoryChannelMappingRepository mappingRepository;

    @Override
    @Transactional
    public ServiceResponse createCategory(ChannelCategoryDTO dto, Long channelId, Long companyId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (!channel.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized access to channel").build();
        }

        ChannelCategory category = new ChannelCategory();
        category.setCategoryCode(dto.getCategoryCode());
        category.setCategoryName(dto.getCategoryName());
        category.setChannel(channel);
        category.setCompany(channel.getCompany()); // Set the direct company FK
        category.setExternalCategoryId(dto.getExternalCategoryId());
        category.setIsLeaf(dto.getIsLeaf() != null ? dto.getIsLeaf() : true);
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setIsActive(true);

        if (dto.getParentCategoryId() != null) {
            ChannelCategory parent = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            
            if (!parent.getChannel().getChannelId().equals(channelId)) {
                throw new RuntimeException("Parent category does not belong to the same channel");
            }

            category.setParentCategory(parent);
            category.setLevelNo(parent.getLevelNo() + 1);
            category.setFullPath(parent.getFullPath() + " > " + category.getCategoryName());
            
            if (parent.getIsLeaf()) {
                parent.setIsLeaf(false);
                categoryRepository.save(parent);
            }
        } else {
            category.setLevelNo(1);
            category.setFullPath(category.getCategoryName());
        }

        ChannelCategory saved = categoryRepository.save(category);
        return ServiceResponse.builder()
                .status(true)
                .message("Category created successfully")
                .data(mapToDTO(saved))
                .build();
    }

    @Override
    @Transactional
    public ServiceResponse updateCategory(Long categoryId, ChannelCategoryDTO dto, Long companyId) {
        ChannelCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized access").build();
        }

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryCode(dto.getCategoryCode());
        category.setExternalCategoryId(dto.getExternalCategoryId());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : category.getSortOrder());
        
        // Update full path if name changed
        if (category.getParentCategory() != null) {
            category.setFullPath(category.getParentCategory().getFullPath() + " > " + category.getCategoryName());
        } else {
            category.setFullPath(category.getCategoryName());
        }

        ChannelCategory saved = categoryRepository.save(category);
        return ServiceResponse.builder()
                .status(true)
                .message("Category updated successfully")
                .data(mapToDTO(saved))
                .build();
    }

    @Override
    @Transactional
    public ServiceResponse deleteCategory(Long categoryId, Long companyId) {
        ChannelCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized access").build();
        }

        // Recursive deletion of all child categories and their associated data
        recursiveDelete(category);

        return ServiceResponse.builder()
                .status(true)
                .message("Category and all its subcategories deleted successfully")
                .build();
    }

    private void recursiveDelete(ChannelCategory category) {
        // 1. Find all children
        List<ChannelCategory> children = categoryRepository.findByParentCategory_CategoryId(category.getCategoryId());
        
        // 2. Delete children recursively
        for (ChannelCategory child : children) {
            recursiveDelete(child);
        }

        // 3. Clean up associated data for THIS category

        mappingRepository.deleteByChannelCategory_CategoryId(category.getCategoryId());

        // 4. Finally delete the category itself
        categoryRepository.delete(category);
    }

    @Override
    public ServiceResponse getCategoryTree(Long channelId, Long companyId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (!channel.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized").build();
        }

        List<ChannelCategory> rootCategories = categoryRepository.findByChannel_ChannelIdAndParentCategoryIsNull(channelId);
        List<ChannelCategoryDTO> tree = rootCategories.stream()
                .map(this::mapToTreeDTO)
                .collect(Collectors.toList());

        return ServiceResponse.builder()
                .status(true)
                .data(tree)
                .build();
    }

    @Override
    public ServiceResponse getLeafCategories(Long channelId, Long companyId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (!channel.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized").build();
        }

        List<ChannelCategory> leafCategories = categoryRepository.findByChannel_ChannelIdAndIsLeafTrue(channelId);
        List<ChannelCategoryDTO> dtos = leafCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ServiceResponse.builder()
                .status(true)
                .data(dtos)
                .build();
    }

    @Override
    public ServiceResponse getCategoriesByParent(Long parentId, Long companyId) {
        ChannelCategory parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent category not found"));

        if (!parent.getChannel().getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized").build();
        }

        List<ChannelCategory> children = categoryRepository.findByParentCategory_CategoryId(parentId);
        List<ChannelCategoryDTO> dtos = children.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ServiceResponse.builder()
                .status(true)
                .data(dtos)
                .build();
    }

    private ChannelCategoryDTO mapToDTO(ChannelCategory category) {
        ChannelCategoryDTO dto = new ChannelCategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryCode(category.getCategoryCode());
        dto.setCategoryName(category.getCategoryName());
        dto.setParentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getCategoryId() : null);
        dto.setLevelNo(category.getLevelNo());
        dto.setFullPath(category.getFullPath());
        dto.setExternalCategoryId(category.getExternalCategoryId());
        dto.setIsLeaf(category.getIsLeaf());
        dto.setSortOrder(category.getSortOrder());
        return dto;
    }

    private ChannelCategoryDTO mapToTreeDTO(ChannelCategory category) {
        ChannelCategoryDTO dto = mapToDTO(category);
        List<ChannelCategory> children = categoryRepository.findByParentCategory_CategoryId(category.getCategoryId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::mapToTreeDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildren(new ArrayList<>());
        }
        return dto;
    }
}
