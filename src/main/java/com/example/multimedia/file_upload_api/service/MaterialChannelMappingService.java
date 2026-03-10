package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MaterialChannelMappingDTO;
import com.example.multimedia.file_upload_api.dto.MaterialChannelMappingRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MaterialChannelMappingService {

    @Autowired
    private MaterialChannelMappingRepository mappingRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelCategoryRepository channelCategoryRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Bulk upsert material channel mappings
     */
    @Transactional
    public ServiceResponse upsertMappings(MaterialChannelMappingRequest request) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate request
            if (request == null || request.getMaterialId() == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material ID is required");
                return response;
            }
            
            if (request.getMappings() == null || request.getMappings().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("At least one mapping is required");
                return response;
            }
            
            // Get current company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);
            
            // Validate material exists for current super admin
            Optional<Material> materialOpt = materialRepository.findById(request.getMaterialId());
            if (materialOpt.isEmpty() || !materialOpt.get().getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found for the current company");
                return response;
            }
            Material material = materialOpt.get();
            
            int successCount = 0;
            int errorCount = 0;
            
            // Process each mapping
            for (MaterialChannelMappingRequest.ChannelMappingRequest mappingRequest : request.getMappings()) {
                try {
                    // Validate channel exists for company
                    Optional<Channel> channelOpt = channelRepository.findByChannelIdAndCompany_CompanyId(
                            mappingRequest.getChannelId(), company.getCompanyId());
                    if (channelOpt.isEmpty()) {
                        errorCount++;
                        continue;
                    }
                    Channel channel = channelOpt.get();
                    
                    // Validate category if provided
                    ChannelCategory category = null;
                    if (mappingRequest.getCategoryId() != null) {
                        Optional<ChannelCategory> categoryOpt = channelCategoryRepository.findById(mappingRequest.getCategoryId());
                        if (categoryOpt.isEmpty() || !categoryOpt.get().getChannel().getChannelId().equals(channel.getChannelId())) {
                            errorCount++;
                            continue;
                        }
                        category = categoryOpt.get();
                    }
                    
                    // Find existing mapping or create new one
                    Optional<MaterialChannelMapping> existingMappingOpt = mappingRepository
                            .findByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
                                    company.getCompanyId(), material.getMaterialId(), channel.getChannelId());
                    
                    MaterialChannelMapping mapping;
                    if (existingMappingOpt.isPresent()) {
                        mapping = existingMappingOpt.get();
                    } else {
                        mapping = new MaterialChannelMapping();
                        mapping.setCompany(company);
                        mapping.setMaterial(material);
                        mapping.setChannel(channel);
                    }
                    
                    // Update mapping fields
                    mapping.setCategory(category);
                    mapping.setPrice(mappingRequest.getPrice());
                    mapping.setStock(mappingRequest.getStock());
                    mapping.setStatus(mappingRequest.getStatus() != null ? mappingRequest.getStatus() : true);
                    
                    mappingRepository.save(mapping);
                    successCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Mappings processed successfully. Success: " + successCount + ", Errors: " + errorCount);
            response.addData("successCount", successCount);
            response.addData("errorCount", errorCount);
            
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to upsert mappings: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get all mappings for a material
     */
    public ServiceResponse getMappingsByMaterialId(Long materialId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate material ID
            if (materialId == null || materialId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid material ID");
                return response;
            }
            
            // Get current company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);
            
            // Validate material exists for current super admin
            Optional<Material> materialOpt = materialRepository.findById(materialId);
            if (materialOpt.isEmpty() || !materialOpt.get().getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found for the current company");
                return response;
            }
            
            // Get mappings
            List<MaterialChannelMapping> mappings = mappingRepository
                    .findByMaterialIdAndCompanyIdWithDetails(materialId, company.getCompanyId());
            
            List<MaterialChannelMappingDTO> mappingDTOs = mappings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Mappings retrieved successfully");
            response.addData("materialId", materialId);
            response.addData("mappings", mappingDTOs);
            response.addData("totalMappings", mappingDTOs.size());
            
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve mappings: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Delete a specific mapping
     */
    @Transactional
    public ServiceResponse deleteMapping(Long materialId, Long channelId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate parameters
            if (materialId == null || materialId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid material ID");
                return response;
            }
            
            if (channelId == null || channelId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid channel ID");
                return response;
            }
            
            // Get current company
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = currentSuperAdmin.getSuperAdminId();
            
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(superAdminId);
            if (adminCompanies.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No company found for the current admin");
                return response;
            }
            CompanyDetails company = adminCompanies.get(0);
            
            // Check if mapping exists
            if (!mappingRepository.existsByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
                    company.getCompanyId(), materialId, channelId)) {
                response.setStatus("ERROR");
                response.setStatusMsg("Mapping not found");
                return response;
            }
            
            // Delete mapping
            mappingRepository.deleteByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
                    company.getCompanyId(), materialId, channelId);
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Mapping deleted successfully");
            
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete mapping: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Convert entity to DTO
     */
    private MaterialChannelMappingDTO convertToDTO(MaterialChannelMapping mapping) {
        MaterialChannelMappingDTO dto = new MaterialChannelMappingDTO();
        dto.setId(mapping.getId());
        dto.setCompanyId(mapping.getCompany().getCompanyId());
        dto.setMaterialId(mapping.getMaterial().getMaterialId());
        dto.setChannelId(mapping.getChannel().getChannelId());
        dto.setChannelName(mapping.getChannel().getChannelName());
        dto.setChannelCode(mapping.getChannel().getChannelCode());
        dto.setPrice(mapping.getPrice());
        dto.setStock(mapping.getStock());
        dto.setStatus(mapping.getStatus());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setUpdatedAt(mapping.getUpdatedAt());
        
        if (mapping.getCategory() != null) {
            dto.setCategoryId(mapping.getCategory().getCategoryId());
            dto.setCategoryName(mapping.getCategory().getCategoryName());
            dto.setCategoryCode(mapping.getCategory().getCategoryCode());
        }
        
        return dto;
    }

    /**
     * Delete all material-channel mappings for a specific channel
     */
    @Transactional
    public ServiceResponse deleteAllMappingsByChannelId(Long channelId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate channel ID
            if (channelId == null || channelId <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid channel ID");
                return response;
            }
            
            // Get current company
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
            
            // Find all mappings for this channel
            List<MaterialChannelMapping> mappings = mappingRepository.findByCompany_CompanyIdAndChannel_ChannelId(
                    company.getCompanyId(), channelId);
            
            if (mappings.isEmpty()) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("No mappings found for the specified channel");
                response.addData("deletedCount", 0);
                return response;
            }
            
            // Delete all mappings
            int deletedCount = mappings.size();
            mappingRepository.deleteAll(mappings);
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Successfully deleted " + deletedCount + " material-channel mappings for channel ID: " + channelId);
            response.addData("deletedCount", deletedCount);
            response.addData("channelId", channelId);
            
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete mappings for channel: " + e.getMessage());
        }
        
        return response;
    }
}
