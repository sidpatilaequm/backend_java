package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ChannelCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ChannelCreateRequest;
import com.example.multimedia.file_upload_api.dto.ChannelDTO;
import com.example.multimedia.file_upload_api.dto.ChannelUpdateRequest;
import com.example.multimedia.file_upload_api.dto.CountryDTO;
import com.example.multimedia.file_upload_api.dto.CurrencyDTO;
import com.example.multimedia.file_upload_api.dto.MaterialImageDTO;
import com.example.multimedia.file_upload_api.dto.MaterialWithChannelInfoDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Channel;
import com.example.multimedia.file_upload_api.entity.ChannelCategory;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.MaterialChannelMapping;
import com.example.multimedia.file_upload_api.repository.ChannelCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ChannelRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.MaterialChannelMappingRepository;
import com.example.multimedia.file_upload_api.repository.CountryRepository;
import com.example.multimedia.file_upload_api.repository.CurrencyRepository;
import com.example.multimedia.file_upload_api.entity.Country;
import com.example.multimedia.file_upload_api.entity.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.example.multimedia.file_upload_api.entity.Material;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelCategoryRepository channelCategoryRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private MaterialChannelMappingRepository mappingRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Get all categories associated with a specific channel
     */
    public ServiceResponse getCategoriesByChannelId(Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate channel ID
            if (channelId == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel ID is required");
                return response;
            }

            // Get current user
            SuperAdmin currentUser = currentUserService.getCurrentSuperAdmin();
            if (currentUser == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("User not authenticated");
                return response;
            }

            // Verify channel exists and belongs to company
            Optional<Channel> channelOpt = channelRepository.findById(channelId);
            if (!channelOpt.isPresent()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                return response;
            }

            Channel channel = channelOpt.get();
            if (!channel.getCompany().getSuperAdmin().getSuperAdminId().equals(currentUser.getSuperAdminId())) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel does not belong to your company");
                return response;
            }

            // Get categories for this channel
            List<ChannelCategory> categories = channelCategoryRepository.findByChannel_ChannelIdAndIsActive(channelId,
                    true);

            // Convert to DTOs and add product count
            List<ChannelCategoryDTO> categoryDTOs = categories.stream()
                    .map(category -> {
                        ChannelCategoryDTO dto = new ChannelCategoryDTO();
                        dto.setCategoryId(category.getCategoryId());
                        dto.setCategoryName(category.getCategoryName());
                        dto.setCategoryCode(category.getCategoryCode());
                        dto.setDescription(""); // ChannelCategory doesn't have description field
                        dto.setIsActive(category.getIsActive());

                        // Count products in this category for this channel
                        Long productCount = mappingRepository.countByChannel_ChannelIdAndCategory_CategoryIdAndStatus(
                                channelId, category.getCategoryId(), true);
                        dto.setProductCount(productCount);

                        return dto;
                    })
                    .collect(Collectors.toList());

            response.setStatus("SUCCESS");
            response.setStatusMsg("Categories retrieved successfully");
            response.addData("categories", categoryDTOs);
            response.addData("totalCategories", categoryDTOs.size());
            response.addData("channelName", channel.getChannelName());
            response.addData("channelCode", channel.getChannelCode());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve categories: " + e.getMessage());
        }

        return response;
    }

    /**
     * Create a new channel with categories
     */
    @Transactional
    public ServiceResponse createChannel(ChannelCreateRequest request) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Debug logging
            System.out.println("DEBUG: Request received: " + (request != null ? "NOT NULL" : "NULL"));
            if (request != null) {
                System.out.println("DEBUG: Channel Name: " + request.getChannelName());
                System.out.println("DEBUG: Channel Code: " + request.getChannelCode());
                System.out.println("DEBUG: Description: " + request.getDescription());
                System.out.println("DEBUG: Categories: "
                        + (request.getCategories() != null ? request.getCategories().size() : "NULL"));
            }

            // Validate request data
            if (request == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("Request cannot be null");
                return response;
            }

            if (request.getChannelName() == null || request.getChannelName().trim().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel name is required");
                return response;
            }

            if (request.getChannelCode() == null || request.getChannelCode().trim().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel code is required");
                return response;
            }

            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            // Get the first company for the super admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            // Check if channel code already exists for this company
            String channelCode = request.getChannelCode().trim();
            if (channelRepository.existsByChannelCodeAndCompany_CompanyId(channelCode, company.getCompanyId())) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel code already exists for your company: " + channelCode);
                return response;
            }

            // Create channel
            Channel channel = new Channel();
            channel.setChannelName(request.getChannelName().trim());
            channel.setChannelCode(request.getChannelCode().trim());
            channel.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            channel.setIsActive(true);
            channel.setCompany(company);
            // Set user as null or get from company if needed
            channel.setUser(null);

            // Organization module fields
            if (request.getStatus() != null) {
                channel.setStatus(request.getStatus());
            }
            if (request.getCountryId() != null) {
                countryRepository.findById(request.getCountryId()).ifPresent(channel::setCountry);
            }
            if (request.getCurrencyId() != null) {
                currencyRepository.findById(request.getCurrencyId()).ifPresent(channel::setCurrency);
            }

            channel = channelRepository.save(channel);

            // Create categories if provided
            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                for (ChannelCreateRequest.ChannelCategoryRequest categoryRequest : request.getCategories()) {
                    // Validate category data
                    if (categoryRequest.getCategoryCode() == null
                            || categoryRequest.getCategoryCode().trim().isEmpty()) {
                        response.setStatus("ERROR");
                        response.setStatusMsg("Category code is required for all categories");
                        return response;
                    }

                    if (categoryRequest.getCategoryName() == null
                            || categoryRequest.getCategoryName().trim().isEmpty()) {
                        response.setStatus("ERROR");
                        response.setStatusMsg("Category name is required for all categories");
                        return response;
                    }

                    // Check if category code already exists for this channel (case-insensitive)
                    String categoryCode = categoryRequest.getCategoryCode().trim();
                    if (channelCategoryRepository.existsByCategoryCodeIgnoreCaseAndChannel_ChannelId(
                            categoryCode, channel.getChannelId())) {
                        response.setStatus("ERROR");
                        response.setStatusMsg("Category code already exists: " + categoryCode);
                        return response;
                    }

                    ChannelCategory category = new ChannelCategory();
                    category.setCategoryCode(categoryRequest.getCategoryCode().trim());
                    category.setCategoryName(categoryRequest.getCategoryName().trim());
                    category.setIsActive(true);
                    category.setChannel(channel);
                    channelCategoryRepository.save(category);
                }
            }

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channel created successfully");
            response.addData("channelId", channel.getChannelId());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to create channel: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get all channels for current admin
     */
    public ServiceResponse getAllChannels() {
        ServiceResponse response = new ServiceResponse();

        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<Channel> channels = channelRepository.findByCompany_SuperAdmin_SuperAdminId(superAdminId);
            List<ChannelDTO> channelDTOs = channels.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channels retrieved successfully");
            response.addData("channels", channelDTOs);

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve channels: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get channel by ID
     */
    public ServiceResponse getChannelById(Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());

            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                return response;
            }

            ChannelDTO channelDTO = convertToDTO(channelOpt.get());
            response.setStatus("SUCCESS");
            response.setStatusMsg("Channel retrieved successfully");
            response.addData("channel", channelDTO);

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve channel: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update channel
     */
    @Transactional
    public ServiceResponse updateChannel(Long channelId, ChannelUpdateRequest request) {
        ServiceResponse response = new ServiceResponse();

        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());

            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                return response;
            }

            Channel channel = channelOpt.get();

            // Update channel fields
            if (request.getChannelName() != null) {
                channel.setChannelName(request.getChannelName());
            }
            if (request.getChannelCode() != null) {
                String newChannelCode = request.getChannelCode().trim();
                // Check if new code already exists for this company (case-insensitive)
                if (!newChannelCode.equalsIgnoreCase(channel.getChannelCode()) &&
                        channelRepository.existsByChannelCodeIgnoreCaseAndCompany_CompanyId(newChannelCode,
                                company.getCompanyId())) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Channel code already exists for your company: " + newChannelCode);
                    return response;
                }
                channel.setChannelCode(newChannelCode);
            }
            if (request.getDescription() != null) {
                channel.setDescription(request.getDescription());
            }
            if (request.getIsActive() != null) {
                channel.setIsActive(request.getIsActive());
            }
            if (request.getStatus() != null) {
                channel.setStatus(request.getStatus());
            }
            if (request.getCountryId() != null) {
                countryRepository.findById(request.getCountryId()).ifPresent(channel::setCountry);
            }
            if (request.getCurrencyId() != null) {
                currencyRepository.findById(request.getCurrencyId()).ifPresent(channel::setCurrency);
            }

            channel = channelRepository.save(channel);

            // Update categories if provided
            if (request.getCategories() != null) {
                for (ChannelUpdateRequest.ChannelCategoryRequest categoryRequest : request.getCategories()) {
                    if (categoryRequest.getCategoryId() != null) {
                        // Update existing category
                        Optional<ChannelCategory> categoryOpt = channelCategoryRepository
                                .findById(categoryRequest.getCategoryId());
                        if (categoryOpt.isPresent()) {
                            ChannelCategory category = categoryOpt.get();
                            if (categoryRequest.getCategoryCode() != null) {
                                category.setCategoryCode(categoryRequest.getCategoryCode());
                            }
                            if (categoryRequest.getCategoryName() != null) {
                                category.setCategoryName(categoryRequest.getCategoryName());
                            }
                            if (categoryRequest.getIsActive() != null) {
                                category.setIsActive(categoryRequest.getIsActive());
                            }
                            channelCategoryRepository.save(category);
                        }
                    } else {
                        // Create new category
                        ChannelCategory category = new ChannelCategory();
                        category.setCategoryCode(categoryRequest.getCategoryCode());
                        category.setCategoryName(categoryRequest.getCategoryName());
                        category.setIsActive(
                                categoryRequest.getIsActive() != null ? categoryRequest.getIsActive() : true);
                        category.setChannel(channel);
                        channelCategoryRepository.save(category);
                    }
                }
            }

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channel updated successfully");
            response.addData("channelId", channel.getChannelId());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to update channel: " + e.getMessage());
        }

        return response;
    }

    /**
     * Delete channel
     */
    @Transactional
    public ServiceResponse deleteChannel(Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());

            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                return response;
            }

            Channel channel = channelOpt.get();

            // Delete categories first
            List<ChannelCategory> categories = channelCategoryRepository.findByChannel_ChannelId(channelId);
            channelCategoryRepository.deleteAll(categories);

            // Delete channel
            channelRepository.delete(channel);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channel deleted successfully");

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete channel: " + e.getMessage());
        }

        return response;
    }

    /**
     * Toggle channel active status
     */
    @Transactional
    public ServiceResponse toggleChannelStatus(Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());

            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                return response;
            }

            Channel channel = channelOpt.get();
            channel.setIsActive(!channel.getIsActive());
            channel = channelRepository.save(channel);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channel status updated successfully");
            response.addData("isActive", channel.getIsActive());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to update channel status: " + e.getMessage());
        }

        return response;
    }

    /**
     * Convert entity to DTO
     */
    private ChannelDTO convertToDTO(Channel channel) {
        ChannelDTO dto = new ChannelDTO();
        dto.setChannelId(channel.getChannelId());
        dto.setChannelName(channel.getChannelName());
        dto.setChannelCode(channel.getChannelCode());
        dto.setDescription(channel.getDescription());
        dto.setIsActive(channel.getIsActive());
        dto.setCompanyId(channel.getCompany() != null ? channel.getCompany().getCompanyId() : null);
        dto.setUserId(channel.getUser() != null ? channel.getUser().getUserId() : null);
        dto.setCreatedAt(channel.getCreatedAt());
        dto.setUpdatedAt(channel.getUpdatedAt());

        // Organization fields
        dto.setStatus(channel.getStatus());
        if (channel.getCountry() != null) {
            dto.setCountry(convertCountryToDTO(channel.getCountry()));
        }
        if (channel.getCurrency() != null) {
            dto.setCurrency(convertCurrencyToDTO(channel.getCurrency()));
        }

        if (channel.getCategories() != null) {
            List<ChannelCategoryDTO> categoryDTOs = channel.getCategories().stream()
                    .map(category -> {
                        ChannelCategoryDTO categoryDTO = new ChannelCategoryDTO();
                        categoryDTO.setCategoryId(category.getCategoryId());
                        categoryDTO.setCategoryCode(category.getCategoryCode());
                        categoryDTO.setCategoryName(category.getCategoryName());
                        categoryDTO.setIsActive(category.getIsActive());
                        return categoryDTO;
                    })
                    .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);
        }

        return dto;
    }

    private CountryDTO convertCountryToDTO(Country country) {
        return new CountryDTO(
                country.getCountryId(),
                country.getCountryName(),
                country.getIsoCode(),
                country.getPhoneCode(),
                country.getStatus());
    }

    private CurrencyDTO convertCurrencyToDTO(Currency currency) {
        return new CurrencyDTO(
                currency.getCurrencyId(),
                currency.getCurrencyCode(),
                currency.getCurrencyName(),
                currency.getSymbol(),
                currency.getStatus());
    }

    /**
     * Convert category entity to DTO
     */
    private ChannelCategoryDTO convertCategoryToDTO(ChannelCategory category) {
        ChannelCategoryDTO dto = new ChannelCategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryCode(category.getCategoryCode());
        dto.setCategoryName(category.getCategoryName());
        dto.setIsActive(category.getIsActive());
        dto.setDescription(""); // ChannelCategory doesn't have description field
        dto.setProductCount(0L); // Will be set separately if needed
        return dto;
    }

    /**
     * Get all channels by Company ID
     */
    public ServiceResponse getChannelsByCompanyId(Long companyId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate company ID
            if (companyId == null || companyId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid company ID");
                return response;
            }

            // Check if company exists
            Optional<CompanyDetails> companyOpt = companyDetailsRepository.findById(companyId);
            if (companyOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Company not found with ID: " + companyId);
                return response;
            }

            // Get all channels for the company
            List<Channel> channels = channelRepository.findByCompany_CompanyId(companyId);
            List<ChannelDTO> channelDTOs = channels.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.setStatus("SUCCESS");
            response.setStatusMsg("Channels retrieved successfully for company ID: " + companyId);
            response.addData("companyId", companyId);
            response.addData("channels", channelDTOs);
            response.addData("totalChannels", channelDTOs.size());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve channels for company: " + e.getMessage());
        }

        return response;
    }

    /**
     * Delete a specific category from a channel
     */
    @Transactional
    public ServiceResponse deleteCategoryFromChannel(Long channelId, Long categoryId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate parameters
            if (channelId == null || channelId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid channel ID");
                return response;
            }

            if (categoryId == null || categoryId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid category ID");
                return response;
            }

            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            // Check if channel exists and belongs to company
            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());
            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found or doesn't belong to current company");
                return response;
            }

            // Check if category exists and belongs to the channel
            Optional<ChannelCategory> categoryOpt = channelCategoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Category not found");
                return response;
            }

            ChannelCategory category = categoryOpt.get();
            if (!category.getChannel().getChannelId().equals(channelId)) {
                response.setStatus("ERROR");
                response.setStatusMsg("Category does not belong to the specified channel");
                return response;
            }

            // Check if category is referenced by any material-channel mappings
            // If yes, we need to handle the foreign key constraint
            List<MaterialChannelMapping> mappings = mappingRepository.findByCompany_CompanyIdAndChannel_ChannelId(
                    company.getCompanyId(), channelId);

            boolean categoryReferenced = mappings.stream()
                    .anyMatch(mapping -> mapping.getCategory() != null &&
                            mapping.getCategory().getCategoryId().equals(categoryId));

            if (categoryReferenced) {
                response.setStatus("ERROR");
                response.setStatusMsg(
                        "Cannot delete category: It is referenced by material-channel mappings. Please remove the mappings first.");
                return response;
            }

            // Delete the category
            channelCategoryRepository.delete(category);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Category deleted successfully from channel");
            response.addData("channelId", channelId);
            response.addData("categoryId", categoryId);
            response.addData("categoryName", category.getCategoryName());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete category: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get materials by Channel ID
     */
    public ServiceResponse getMaterialsByChannelId(Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate channel ID
            if (channelId == null || channelId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid channel ID");
                return response;
            }

            // Get current super admin and company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();

            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);

            // Check if channel exists and belongs to company
            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                    channelId, company.getCompanyId());
            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found or doesn't belong to current company");
                return response;
            }

            Channel channel = channelOpt.get();

            // Get all material-channel mappings for this channel
            List<MaterialChannelMapping> mappings = mappingRepository.findByCompany_CompanyIdAndChannel_ChannelId(
                    company.getCompanyId(), channelId);

            if (mappings.isEmpty()) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("No materials found for the specified channel");
                response.addData("channelId", channelId);
                response.addData("channelName", channel.getChannelName());
                response.addData("channelCode", channel.getChannelCode());
                response.addData("materials", new ArrayList<>());
                response.addData("totalMaterials", 0);
                return response;
            }

            // Convert mappings to detailed material information
            List<MaterialWithChannelInfoDTO> materialDTOs = mappings.stream()
                    .map(mapping -> {
                        MaterialWithChannelInfoDTO dto = new MaterialWithChannelInfoDTO();
                        Material material = mapping.getMaterial();

                        // Basic material information
                        dto.setMaterialId(material.getMaterialId());
                        dto.setMaterialName(material.getMaterialName());
                        dto.setDescription(material.getDescription());
                        dto.setType(material.getType());
                        dto.setBaseUnitOfMeasure(material.getBaseUnitOfMeasure());
                        dto.setHsnCode(material.getHsnCode());
                        dto.setSku(material.getSku());
                        dto.setPurchasingCode(material.getPurchasingCode());
                        dto.setVariantMandatory(material.getVariantMandatory());
                        dto.setVendorArticleNumber(material.getVendorArticleNumber());
                        dto.setMaterialCode(material.getMaterialCode());
                        dto.setBlocked(material.getBlocked());
                        dto.setCreatedDate(material.getCreatedDate());
                        dto.setModifiedDate(material.getModifiedDate());

                        // Image information
                        dto.setBarcodeImage(material.getBarcodeImage());

                        // Convert material images to DTOs
                        if (material.getMaterialImages() != null && !material.getMaterialImages().isEmpty()) {
                            List<MaterialImageDTO> imageDTOs = material.getMaterialImages().stream()
                                    .map(img -> new MaterialImageDTO(
                                            img.getImageId(),
                                            img.getImageName(),
                                            img.getImageType(),
                                            img.getImageData(),
                                            img.getSequenceOrder(),
                                            material.getMaterialId()))
                                    .collect(Collectors.toList());
                            dto.setMaterialImages(imageDTOs);
                        }

                        // Item Category information if available
                        if (material.getItemCategory() != null) {
                            dto.setItemCategoryId(material.getItemCategory().getItemCategoryId());
                            dto.setItemCategoryName(material.getItemCategory().getDescription());
                            dto.setItemCategoryCode(material.getItemCategory().getCode());
                        }

                        // Item Subcategory information if available
                        if (material.getSubcategory() != null) {
                            dto.setItemSubcategoryId(material.getSubcategory().getItemSubcategoryId());
                            dto.setItemSubcategoryName(material.getSubcategory().getItemSubcategoryName());
                        }

                        // Channel information
                        dto.setChannelId(mapping.getChannel().getChannelId());
                        dto.setChannelName(mapping.getChannel().getChannelName());
                        dto.setChannelCode(mapping.getChannel().getChannelCode());

                        // Channel Category information if assigned
                        if (mapping.getCategory() != null) {
                            dto.setChannelCategoryId(mapping.getCategory().getCategoryId());
                            dto.setChannelCategoryName(mapping.getCategory().getCategoryName());
                            dto.setChannelCategoryCode(mapping.getCategory().getCategoryCode());
                        }

                        // Channel-specific material information
                        dto.setPrice(mapping.getPrice());
                        dto.setStock(mapping.getStock());
                        dto.setStatus(mapping.getStatus());
                        dto.setMappingCreatedAt(mapping.getCreatedAt());
                        dto.setMappingUpdatedAt(mapping.getUpdatedAt());

                        return dto;
                    })
                    .collect(Collectors.toList());

            response.setStatus("SUCCESS");
            response.setStatusMsg("Materials retrieved successfully for channel: " + channel.getChannelName());
            response.addData("channelId", channelId);
            response.addData("channelName", channel.getChannelName());
            response.addData("channelCode", channel.getChannelCode());
            response.addData("materials", materialDTOs);
            response.addData("totalMaterials", materialDTOs.size());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve materials for channel: " + e.getMessage());
        }

        return response;
    }
}
