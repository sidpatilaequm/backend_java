package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CategoryDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryTreeDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryBulkCreateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.ItemSubcategory;

import java.util.List;

public interface HierarchyService {
    ServiceResponse createCategory(CategoryDTO categoryDTO);
    ServiceResponse getCategoriesForCurrentCompany();
    ServiceResponse createSubcategory(SubcategoryDTO subcategoryDTO);
    ServiceResponse bulkCreateSubcategories(SubcategoryBulkCreateRequest request);
    ServiceResponse getSubcategoryTree(Long categoryId);
    
    // Internal helper for Material mapping
    List<ItemSubcategory> getParentHierarchy(Long deepestSubcategoryId);

    ServiceResponse deleteCategory(Long categoryId);
    ServiceResponse deleteSubcategory(Long subcategoryId);
}
