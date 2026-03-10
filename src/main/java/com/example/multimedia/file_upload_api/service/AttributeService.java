package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.AttributeDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Attribute;
import com.example.multimedia.file_upload_api.entity.AttributeType;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.AttributeRepository;
import com.example.multimedia.file_upload_api.repository.MaterialAttributeRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class AttributeService {
    private static final Logger logger = LoggerFactory.getLogger(AttributeService.class);

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private MaterialAttributeRepository materialAttributeRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Transactional
    public ServiceResponse createAttribute(AttributeDTO attributeDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Validate attribute name
            if (attributeDTO.getAttributeName() == null || attributeDTO.getAttributeName().trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Attribute name is required"
                );
            }

            // Check if attribute name already exists for current super admin
            if (attributeRepository.existsByAttributeNameAndSuperAdmin_SuperAdminId(
                attributeDTO.getAttributeName(), superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Attribute with name '" + attributeDTO.getAttributeName() + "' already exists"
                );
            }

            Attribute attribute = new Attribute();
            attribute.setAttributeName(attributeDTO.getAttributeName());
            attribute.setIsActive(attributeDTO.getIsActive() != null ? attributeDTO.getIsActive() : true);
            attribute.setType(attributeDTO.getType());
            attribute.setSuperAdmin(superAdmin);

            attribute = attributeRepository.save(attribute);
            
            response.addData("attribute", convertToDTO(attribute));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attribute created successfully"
            );
        } catch (Exception e) {
            logger.error("Error creating attribute: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to create attribute: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse updateAttribute(Long id, AttributeDTO attributeDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found with ID: " + id));

            // Check if attribute belongs to current super admin
            if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Access denied: Attribute does not belong to current admin"
                );
            }

            // Check if new name already exists for different attribute of current super admin
            if (!attribute.getAttributeName().equals(attributeDTO.getAttributeName()) &&
                attributeRepository.existsByAttributeNameAndSuperAdmin_SuperAdminId(
                    attributeDTO.getAttributeName(), superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Attribute with name '" + attributeDTO.getAttributeName() + "' already exists"
                );
            }

            attribute.setAttributeName(attributeDTO.getAttributeName());
            if (attributeDTO.getIsActive() != null) {
                attribute.setIsActive(attributeDTO.getIsActive());
            }
            if (attributeDTO.getType() != null) {
                attribute.setType(attributeDTO.getType());
            }

            attribute = attributeRepository.save(attribute);
            
            response.addData("attribute", convertToDTO(attribute));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attribute updated successfully"
            );
        } catch (Exception e) {
            logger.error("Error updating attribute: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update attribute: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAllAttributes() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            List<AttributeDTO> attributes = attributeRepository.findBySuperAdmin_SuperAdminId(
                superAdmin.getSuperAdminId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("attributes", attributes);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attributes retrieved successfully"
            );
        } catch (Exception e) {
            logger.error("Error retrieving attributes: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve attributes: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAttributeById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found with ID: " + id));

            // Check if attribute belongs to current super admin
            if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Access denied: Attribute does not belong to current admin"
                );
            }

            response.addData("attribute", convertToDTO(attribute));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attribute retrieved successfully"
            );
        } catch (Exception e) {
            logger.error("Error retrieving attribute: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve attribute: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse deleteAttribute(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found with ID: " + id));

            // Check if attribute belongs to current super admin
            if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Access denied: Attribute does not belong to current admin"
                );
            }

            // Check if attribute is in use
            if (materialAttributeRepository.existsByAttribute_AttributeId(id)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Cannot delete attribute '" + attribute.getAttributeName() + "' because it is currently in use by materials or variants. Please remove all references to this attribute before deleting."
                );
            }

            // Store attribute info for response before deletion
            String attributeName = attribute.getAttributeName();
            String attributeType = attribute.getType().name();

            attributeRepository.deleteById(id);
            
            // Log the deletion
            logger.info("Attribute deleted successfully - ID: {}, Name: {}, Type: {}", id, attributeName, attributeType);

            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attribute '" + attributeName + "' (" + attributeType + ") deleted successfully"
            );
        } catch (Exception e) {
            logger.error("Error deleting attribute: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to delete attribute: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse createAttributes(List<AttributeDTO> attributeDTOs) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            List<AttributeDTO> created = new ArrayList<>();
            for (AttributeDTO dto : attributeDTOs) {
                if (dto.getAttributeName() == null || dto.getAttributeName().trim().isEmpty()) {
                    continue; // skip invalid
                }
                if (attributeRepository.existsByAttributeNameAndSuperAdmin_SuperAdminId(
                    dto.getAttributeName(), superAdmin.getSuperAdminId())) {
                    continue; // skip duplicates
                }
                Attribute attribute = new Attribute();
                attribute.setAttributeName(dto.getAttributeName());
                attribute.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
                attribute.setType(dto.getType());
                attribute.setSuperAdmin(superAdmin);
                attribute = attributeRepository.save(attribute);
                created.add(convertToDTO(attribute));
            }
            response.addData("attributes", created);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attributes created successfully"
            );
        } catch (Exception e) {
            logger.error("Error creating attributes: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to create attributes: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAttributesByType(AttributeType type) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            List<AttributeDTO> attributes = attributeRepository.findBySuperAdmin_SuperAdminIdAndType(
                superAdmin.getSuperAdminId(), type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("attributes", attributes);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Attributes retrieved successfully by type: " + type
            );
        } catch (Exception e) {
            logger.error("Error retrieving attributes by type: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve attributes by type: " + e.getMessage()
            );
        }
    }

    private AttributeDTO convertToDTO(Attribute attribute) {
        return new AttributeDTO(
            attribute.getAttributeId(),
            attribute.getAttributeName(),
            attribute.getIsActive(),
            attribute.getCreatedDate(),
            attribute.getModifiedDate(),
            attribute.getType()
        );
    }
} 