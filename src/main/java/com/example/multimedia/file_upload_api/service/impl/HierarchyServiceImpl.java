package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.CategoryDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryTreeDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryBulkCreateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.entity.ItemSubcategory;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ItemSubcategoryRepository;
import com.example.multimedia.file_upload_api.service.HierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyServiceImpl implements HierarchyService {

    private final ItemCategoryRepository categoryRepository;
    private final ItemSubcategoryRepository subcategoryRepository;
    private final CompanyDetailsRepository companyRepository;
    private final com.example.multimedia.file_upload_api.service.CurrentUserService currentUserService;
    private final com.example.multimedia.file_upload_api.repository.MaterialRepository materialRepository;

    private CompanyDetails getCurrentCompany() {
        Long superAdminId = currentUserService.getCurrentSuperAdminId();
        List<CompanyDetails> companies = companyRepository.findBySuperAdminSuperAdminId(superAdminId);
        if (companies.isEmpty()) {
            throw new RuntimeException("No company found for the current admin");
        }
        return companies.get(0);
    }

    @Override
    @Transactional
    public ServiceResponse createCategory(CategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            CompanyDetails company = getCurrentCompany();
            
            if (categoryRepository.existsByCategoryNameAndCompany_CompanyId(dto.getCategoryName(), company.getCompanyId())) {
                response.setStatus("409");
                response.setStatusMsg("Category already exists for this company");
                response.setErrorCode("1");
                return response;
            }

            ItemCategory category = new ItemCategory();
            category.setCategoryName(dto.getCategoryName());
            category.setCode(dto.getCode());
            category.setIsActive(true);
            category.setCompany(company);

            ItemCategory saved = categoryRepository.save(category);
            
            response.setStatus("200");
            response.setStatusMsg("Category created successfully");
            response.setErrorCode("0");
            response.addData("category", mapToCategoryDTO(saved));
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error creating category: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    @Override
    public ServiceResponse getCategoriesForCurrentCompany() {
        ServiceResponse response = new ServiceResponse();
        try {
            CompanyDetails company = getCurrentCompany();
            List<CategoryDTO> categories = categoryRepository.findByCompany_CompanyId(company.getCompanyId())
                    .stream().map(this::mapToCategoryDTO).collect(Collectors.toList());
            
            response.setStatus("200");
            response.setStatusMsg("Categories retrieved successfully");
            response.setErrorCode("0");
            response.addData("categories", categories);
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error retrieving categories: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    @Override
    @Transactional
    public ServiceResponse createSubcategory(SubcategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            CompanyDetails company = getCurrentCompany();
            ItemCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            ItemSubcategory subcategory = new ItemSubcategory();
            subcategory.setItemSubcategoryName(dto.getName());
            subcategory.setItemCategory(category);
            subcategory.setIsActive(true);
            subcategory.setCompany(company);

            if (dto.getParentSubcategoryId() != null) {
                ItemSubcategory parent = subcategoryRepository.findById(dto.getParentSubcategoryId())
                        .orElseThrow(() -> new RuntimeException("Parent subcategory not found"));
                
                if (parent.getLevelNo() >= 3) {
                    response.setStatus("400");
                    response.setStatusMsg("Maximum hierarchy depth of 3 levels reached");
                    response.setErrorCode("1");
                    return response;
                }
                
                subcategory.setParentSubcategory(parent);
                subcategory.setLevelNo(parent.getLevelNo() + 1);
            } else {
                subcategory.setLevelNo(1);
            }

            ItemSubcategory saved = subcategoryRepository.save(subcategory);
            
            response.setStatus("200");
            response.setStatusMsg("Subcategory created successfully");
            response.setErrorCode("0");
            response.addData("subcategory", mapToSubcategoryDTO(saved));
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error creating subcategory: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    @Override
    @Transactional
    public ServiceResponse bulkCreateSubcategories(SubcategoryBulkCreateRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            CompanyDetails company = getCurrentCompany();
            ItemCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            saveSubcategoryRecursive(request, category, null, 1, company);
            
            response.setStatus("200");
            response.setStatusMsg("Hierarchy created successfully");
            response.setErrorCode("0");
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error in bulk creation: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    private void saveSubcategoryRecursive(SubcategoryBulkCreateRequest request, ItemCategory category, ItemSubcategory parent, int level, CompanyDetails company) {
        if (level > 3) return;

        ItemSubcategory subcategory = new ItemSubcategory();
        subcategory.setItemSubcategoryName(request.getName());
        subcategory.setItemCategory(category);
        subcategory.setParentSubcategory(parent);
        subcategory.setLevelNo(level);
        subcategory.setIsActive(true);
        subcategory.setCompany(company);

        ItemSubcategory saved = subcategoryRepository.save(subcategory);

        if (request.getChildren() != null) {
            for (SubcategoryBulkCreateRequest childRequest : request.getChildren()) {
                saveSubcategoryRecursive(childRequest, category, saved, level + 1, company);
            }
        }
    }

    @Override
    public ServiceResponse getSubcategoryTree(Long categoryId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<ItemSubcategory> roots = subcategoryRepository.findByItemCategory_ItemCategoryIdAndParentSubcategoryIsNull(categoryId);
            List<SubcategoryTreeDTO> tree = roots.stream().map(this::buildTree).collect(Collectors.toList());
            
            response.setStatus("200");
            response.setStatusMsg("Subcategory tree retrieved successfully");
            response.setErrorCode("0");
            response.addData("tree", tree);
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error retrieving tree: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    private SubcategoryTreeDTO buildTree(ItemSubcategory entity) {
        SubcategoryTreeDTO dto = new SubcategoryTreeDTO();
        dto.setId(entity.getItemSubcategoryId());
        dto.setName(entity.getItemSubcategoryName());
        dto.setLevelNo(entity.getLevelNo());
        
        List<ItemSubcategory> children = subcategoryRepository.findByParentSubcategory_ItemSubcategoryId(entity.getItemSubcategoryId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream().map(this::buildTree).collect(Collectors.toList()));
        }
        
        return dto;
    }

    @Override
    public List<ItemSubcategory> getParentHierarchy(Long deepestSubcategoryId) {
        List<ItemSubcategory> hierarchy = new ArrayList<>();
        ItemSubcategory current = subcategoryRepository.findById(deepestSubcategoryId).orElse(null);
        while (current != null) {
            hierarchy.add(0, current); // Add to front to keep L1, L2, L3 order
            current = current.getParentSubcategory();
        }
        return hierarchy;
    }

    @Override
    @Transactional
    public ServiceResponse deleteCategory(Long categoryId) {
        ServiceResponse response = new ServiceResponse();
        try {

            if (subcategoryRepository.countByItemCategory_ItemCategoryId(categoryId) > 0) {
                response.setStatus("400");
                response.setStatusMsg("Cannot delete category as it contains subcategories");
                response.setErrorCode("1");
                return response;
            }

            categoryRepository.deleteById(categoryId);
            response.setStatus("200");
            response.setStatusMsg("Category deleted successfully");
            response.setErrorCode("0");
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error deleting category: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    @Override
    @Transactional
    public ServiceResponse deleteSubcategory(Long subcategoryId) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (subcategoryRepository.findByParentSubcategory_ItemSubcategoryId(subcategoryId).size() > 0) {
                response.setStatus("400");
                response.setStatusMsg("Cannot delete subcategory as it has children");
                response.setErrorCode("1");
                return response;
            }

            subcategoryRepository.deleteById(subcategoryId);
            response.setStatus("200");
            response.setStatusMsg("Subcategory deleted successfully");
            response.setErrorCode("0");
        } catch (Exception e) {
            response.setStatus("500");
            response.setStatusMsg("Error deleting subcategory: " + e.getMessage());
            response.setErrorCode("1");
        }
        return response;
    }

    private CategoryDTO mapToCategoryDTO(ItemCategory entity) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(entity.getItemCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setCode(entity.getCode());
        dto.setIsActive(entity.getIsActive());
        if (entity.getCompany() != null) dto.setCompanyId(entity.getCompany().getCompanyId());
        return dto;
    }

    private SubcategoryDTO mapToSubcategoryDTO(ItemSubcategory entity) {
        SubcategoryDTO dto = new SubcategoryDTO();
        dto.setId(entity.getItemSubcategoryId());
        dto.setName(entity.getItemSubcategoryName());
        dto.setCategoryId(entity.getItemCategory().getItemCategoryId());
        dto.setLevelNo(entity.getLevelNo());
        dto.setIsActive(entity.getIsActive());
        if (entity.getParentSubcategory() != null) dto.setParentSubcategoryId(entity.getParentSubcategory().getItemSubcategoryId());
        if (entity.getCompany() != null) dto.setCompanyId(entity.getCompany().getCompanyId());
        return dto;
    }
}
