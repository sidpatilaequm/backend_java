package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ItemSubcategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.entity.ItemSubcategory;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ItemSubcategoryRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemSubcategoryService {

    @Autowired
    private ItemSubcategoryRepository itemSubcategoryRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Transactional
    public ServiceResponse saveItemSubcategory(ItemSubcategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            // Validate item category exists by code
            ItemCategory itemCategory = itemCategoryRepository.findByCode(dto.getItemCategoryCode())
                .orElseThrow(() -> new RuntimeException("Item category not found with code: " + dto.getItemCategoryCode()));

            // Check if subcategory name already exists for this category and company
            if (itemSubcategoryRepository.findByItemSubcategoryNameAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                    dto.getItemSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId()).isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Subcategory with name " + dto.getItemSubcategoryName() + " already exists for this category in your company"
                );
            }

            ItemSubcategory itemSubcategory = new ItemSubcategory();
            itemSubcategory.setItemSubcategoryName(dto.getItemSubcategoryName());
            itemSubcategory.setItemCategory(itemCategory);
            itemSubcategory.setCompany(company);
            itemSubcategory.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

            itemSubcategory = itemSubcategoryRepository.save(itemSubcategory);

            response.addData("itemSubcategory", convertToDTO(itemSubcategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item subcategory saved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to save item subcategory: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse updateItemSubcategory(ItemSubcategoryDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            ItemSubcategory itemSubcategory = itemSubcategoryRepository.findById(dto.getItemSubcategoryId())
                .orElseThrow(() -> new RuntimeException("Item subcategory not found with ID: " + dto.getItemSubcategoryId()));

            // Check if the subcategory belongs to the current company
            if (itemSubcategory.getCompany() == null || !itemSubcategory.getCompany().getCompanyId().equals(company.getCompanyId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "You can only update subcategories that belong to your company"
                );
            }

            // Validate item category exists by code
            ItemCategory itemCategory = itemCategoryRepository.findByCode(dto.getItemCategoryCode())
                .orElseThrow(() -> new RuntimeException("Item category not found with code: " + dto.getItemCategoryCode()));

            // Check if subcategory name already exists for this category and company (excluding current subcategory)
            if (!itemSubcategory.getItemSubcategoryName().equals(dto.getItemSubcategoryName()) &&
                itemSubcategoryRepository.findByItemSubcategoryNameAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                    dto.getItemSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId()).isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Subcategory with name " + dto.getItemSubcategoryName() + " already exists for this category in your company"
                );
            }

            itemSubcategory.setItemSubcategoryName(dto.getItemSubcategoryName());
            itemSubcategory.setItemCategory(itemCategory);
            itemSubcategory.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

            itemSubcategory = itemSubcategoryRepository.save(itemSubcategory);

            response.addData("itemSubcategory", convertToDTO(itemSubcategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item subcategory updated successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update item subcategory: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAllItemSubcategories() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            List<ItemSubcategory> itemSubcategories = itemSubcategoryRepository.findByCompany_CompanyIdAndIsActive(company.getCompanyId(), true);
            List<ItemSubcategoryDTO> itemSubcategoryDTOs = itemSubcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("itemSubcategories", itemSubcategoryDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item subcategories retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve item subcategories: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getItemSubcategoriesByCategoryCode(String categoryCode) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            ItemCategory itemCategory = itemCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new RuntimeException("Item category not found with code: " + categoryCode));

            List<ItemSubcategory> itemSubcategories = itemSubcategoryRepository
                .findByCompany_CompanyIdAndItemCategory_ItemCategoryIdAndIsActive(company.getCompanyId(), itemCategory.getItemCategoryId(), true);
            List<ItemSubcategoryDTO> itemSubcategoryDTOs = itemSubcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("itemSubcategories", itemSubcategoryDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item subcategories retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve item subcategories: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getItemSubcategoryById(Long id) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            ItemSubcategory itemSubcategory = itemSubcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item subcategory not found with ID: " + id));

            // Check if the subcategory belongs to the current company
            if (itemSubcategory.getCompany() == null || !itemSubcategory.getCompany().getCompanyId().equals(company.getCompanyId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "You can only access subcategories that belong to your company"
                );
            }

            response.addData("itemSubcategory", convertToDTO(itemSubcategory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Item subcategory retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve item subcategory: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAllSubcategoriesWithCategoryDetails() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No company found for the current admin"
                );
            }
            CompanyDetails company = adminCompanies.get(0);

            List<ItemSubcategory> itemSubcategories = itemSubcategoryRepository.findByCompany_CompanyIdAndIsActive(company.getCompanyId(), true);
            List<ItemSubcategoryDTO> itemSubcategoryDTOs = itemSubcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("subcategories", itemSubcategoryDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Subcategories with category details retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve subcategories with category details: " + e.getMessage()
            );
        }
    }

    private ItemSubcategoryDTO convertToDTO(ItemSubcategory itemSubcategory) {
        return new ItemSubcategoryDTO(
            itemSubcategory.getItemSubcategoryId(),
            itemSubcategory.getItemSubcategoryName(),
            itemSubcategory.getItemCategory().getCode(),
            itemSubcategory.getItemCategory().getDescription(),
            itemSubcategory.getIsActive(),
            itemSubcategory.getCreatedDate(),
            itemSubcategory.getModifiedDate()
        );
    }
} 