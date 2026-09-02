package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MaterialDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.service.LocationService;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.example.multimedia.file_upload_api.dto.MaterialCreateRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.example.multimedia.file_upload_api.util.CodeGenerator;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MaterialService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MaterialRepository materialRepository;



    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;




    @Autowired
    private CodeGenerator codeGenerator;


    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private InventoryService inventoryService;


    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse saveMaterial(MaterialCreateRequest req, MultipartFile barcodeImage, List<MultipartFile> materialImages) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin from security context first
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            SuperAdmin superAdmin = superAdminRepository.findById(currentAdminId).orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
            
            // Validate and get location
            if (req.getLocationId() == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Location ID is required");
            }
            Location location = locationService.getLocationEntity(req.getLocationId(), superAdmin);
            
            // Check if SKU already exists for this super admin and location
            if (materialRepository.existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(req.getSku(), currentAdminId, location.getLocationId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Material with SKU " + req.getSku() + " already exists for this location");
            }
            


            Material material = new Material();
            String materialCode;
            do {
                materialCode = codeGenerator.generateUniqueCode(10);
            } while (materialRepository.existsByMaterialCode(materialCode));
            material.setMaterialCode(materialCode);
            material.setMaterialName(req.getMaterialName());
            material.setDescription(req.getDescription());
            material.setVendorArticleNumber(req.getVendorArticleNumber());
            material.setBlocked(req.getBlocked() != null ? req.getBlocked() : false);
            material.setType(req.getType());
            material.setBaseUnitOfMeasure(req.getBaseUnitOfMeasure());
            material.setHsnCode(req.getHsnCode());
            material.setSku(req.getSku());
            material.setPurchasingCode(req.getPurchasingCode());
            material.setVariantMandatory(req.getVariantMandatory() != null ? req.getVariantMandatory() : false);
            material.setSuperAdmin(superAdmin);
            material.setLocation(location);

            if (barcodeImage != null && !barcodeImage.isEmpty()) {
                material.setBarcodeImage(barcodeImage.getBytes());
            }

            Material savedMaterial = materialRepository.save(material);

            // Initialize inventory with 0 stock
            inventoryService.initializeInventory(savedMaterial, location, superAdmin);



            Material finalMaterial = materialRepository.findById(savedMaterial.getMaterialId()).orElseThrow(() -> new RuntimeException("Could not re-fetch material"));
            response.addData("material", convertToDTO(finalMaterial));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Material saved successfully");
        } catch (Exception e) {
            logger.error("Error saving material: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to save material: " + e.getMessage());
        }
    }

    @Transactional
    public ServiceResponse updateMaterial(MaterialDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            Material material = materialRepository.findById(dto.getMaterialId()).orElseThrow(() -> new RuntimeException("Material not found with ID: " + dto.getMaterialId()));


            if (!material.getSku().equals(dto.getSku()) && materialRepository.existsBySku(dto.getSku())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Material with SKU " + dto.getSku() + " already exists");
            }

            material.setMaterialName(dto.getMaterialName());
            material.setDescription(dto.getDescription());
            material.setBlocked(dto.getBlocked() != null ? dto.getBlocked() : false);
            material.setType(dto.getType());
            material.setBaseUnitOfMeasure(dto.getBaseUnitOfMeasure());

            material.setHsnCode(dto.getHsnCode());
            material.setSku(dto.getSku());
            material.setPurchasingCode(dto.getPurchasingCode());
            material.setVariantMandatory(dto.getVariantMandatory() != null ? dto.getVariantMandatory() : false);



            material = materialRepository.save(material);

            response.addData("material", convertToDTO(material));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material updated successfully"
            );

        } catch (Exception e) {
            logger.error("Error updating material: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update material: " + e.getMessage()
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse updateMaterialWithImages(Long materialId, MaterialCreateRequest req, Boolean replaceImages, MultipartFile barcodeImage, List<MultipartFile> materialImages) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin from security context
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Find existing material
            Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with ID: " + materialId));

            // Check if material belongs to current admin
            if (!material.getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Access denied: Material does not belong to current admin");
            }

            // Validate and get location
            if (req.getLocationId() == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Location ID is required");
            }
            SuperAdmin superAdmin = superAdminRepository.findById(currentAdminId).orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
            Location location = locationService.getLocationEntity(req.getLocationId(), superAdmin);

            // Check SKU uniqueness (excluding current material) for same super admin and location
            if (!material.getSku().equals(req.getSku()) && materialRepository.existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(req.getSku(), currentAdminId, location.getLocationId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Material with SKU " + req.getSku() + " already exists for this location");
            }


            // Update material fields
            material.setMaterialName(req.getMaterialName());
            material.setDescription(req.getDescription());
            material.setVendorArticleNumber(req.getVendorArticleNumber());
            material.setBlocked(req.getBlocked() != null ? req.getBlocked() : false);
            material.setType(req.getType());
            material.setBaseUnitOfMeasure(req.getBaseUnitOfMeasure());
            material.setHsnCode(req.getHsnCode());
            material.setSku(req.getSku());
            material.setPurchasingCode(req.getPurchasingCode());
            material.setVariantMandatory(req.getVariantMandatory() != null ? req.getVariantMandatory() : false);
            material.setLocation(location);

            // Update barcode image if provided
            if (barcodeImage != null && !barcodeImage.isEmpty()) {
                material.setBarcodeImage(barcodeImage.getBytes());
            }

            Material savedMaterial = materialRepository.save(material);



            

            

            Material finalMaterial = materialRepository.findById(savedMaterial.getMaterialId()).orElseThrow(() -> new RuntimeException("Could not re-fetch material"));
            response.addData("material", convertToDTO(finalMaterial));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Material updated successfully");
        } catch (Exception e) {
            logger.error("Error updating material: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to update material: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getAllMaterials() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Check if current user is SuperAdmin or Vendor
            List<Material> materials;
            if (currentUserService.isCurrentUserSuperAdmin()) {
                Long currentAdminId = currentUserService.getCurrentSuperAdminId();
                materials = materialRepository.findBySuperAdmin_SuperAdminId(currentAdminId);
            } else {
                // It's a Vendor (UserDetail)
                com.example.multimedia.file_upload_api.entity.UserDetail currentUser = currentUserService.getCurrentUser();
                if (currentUser.getCompany() != null) {
                    Long vendorCompanyId = currentUser.getCompany().getCompanyId();
                    materials = materialRepository.findByVendorId(vendorCompanyId);
                } else {
                    materials = new ArrayList<>();
                }
            }

            List<MaterialDTO> materialDTOs = materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("materials", materialDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Materials retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve materials: " + e.getMessage()
            );
        }
    }



    @Transactional(readOnly = true)
    public ServiceResponse getMaterialsByLocationId(Long locationId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter materials by current admin and location
            List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminIdAndLocation_LocationId(currentAdminId, locationId);
            List<MaterialDTO> materialDTOs = materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            response.addData("materials", materialDTOs);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Materials retrieved successfully for location ID: " + locationId
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve materials by location: " + e.getMessage()
            );
        }
    }



    @Transactional(readOnly = true)
    public ServiceResponse getMaterialById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found with ID: " + id));

            // Check if material belongs to current admin
            if (!material.getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Material not found with ID: " + id
                );
            }

            response.addData("material", convertToDTO(material));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material retrieved successfully"
            );
        } catch (Exception e) {
            logger.error("Error retrieving material: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve material: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse filterMaterials(Long locationId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            List<Material> materials = materialRepository.filterMaterials(currentAdminId, locationId);
            List<MaterialDTO> dtos = materials.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            response.addData("materials", dtos);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Materials filtered successfully");
        } catch (Exception e) {
            logger.error("Error filtering materials: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to filter materials: " + e.getMessage());
        }
    }

    private MaterialDTO convertToDTO(Material material) {
        MaterialDTO dto = new MaterialDTO();
        dto.setMaterialId(material.getMaterialId());
        dto.setMaterialCode(material.getMaterialCode());
        dto.setMaterialName(material.getMaterialName());
        dto.setDescription(material.getDescription());
        dto.setVendorArticleNumber(material.getVendorArticleNumber());
        dto.setBlocked(material.getBlocked());
        dto.setType(material.getType());
        dto.setBaseUnitOfMeasure(material.getBaseUnitOfMeasure());
        dto.setHsnCode(material.getHsnCode());
        dto.setSku(material.getSku());
        dto.setPurchasingCode(material.getPurchasingCode());
        dto.setVariantMandatory(material.getVariantMandatory());


        if (material.getSuperAdmin() != null) {
            dto.setSuperAdminId(material.getSuperAdmin().getSuperAdminId());
        }
        if (material.getLocation() != null) {
            dto.setLocationId(material.getLocation().getLocationId());
            dto.setLocationName(material.getLocation().getLocationName());
            dto.setLocationAddress(material.getLocation().getAddress());
        } else {
            // Handle case where location is null (for existing materials)
            dto.setLocationId(null);
            dto.setLocationName("No Location Assigned");
            dto.setLocationAddress("Please assign a location");
        }

        dto.setCreatedDate(material.getCreatedDate());
        dto.setModifiedDate(material.getModifiedDate());

        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse deleteMaterial(Long materialId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Find the material and verify ownership
            Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with ID: " + materialId));

            // Check if material belongs to current admin
            if (!material.getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Access denied: Material does not belong to current admin"
                );
            }

            // Store material info for response before deletion
            String materialName = material.getMaterialName();
            String sku = material.getSku();

            // Manual deletion in proper order to avoid foreign key constraint violations
            
            // 1. Delete inventory records first (they have foreign key to material)
            // Use entityManager to delete all inventory records for this material
            int deletedInventoryCount = entityManager.createQuery("DELETE FROM Inventory i WHERE i.material = :material")
                .setParameter("material", material)
                .executeUpdate();
            if (deletedInventoryCount > 0) {
                logger.info("Deleted {} inventory records for material ID: {}", deletedInventoryCount, materialId);
            }
            
            // 6. Finally delete the material
            materialRepository.delete(material);
            
            // Alternative approach using native SQL if JPA cascade doesn't work
            // Uncomment the following lines if you still get foreign key constraint errors:
            /*
            entityManager.createNativeQuery("DELETE FROM material_attribute WHERE material_id = ?")
                .setParameter(1, materialId)
                .executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM material_variant WHERE material_id = ?")
                .setParameter(1, materialId)
                .executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM material_images WHERE material_id = ?")
                .setParameter(1, materialId)
                .executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM material WHERE material_id = ?")
                .setParameter(1, materialId)
                .executeUpdate();
            */

            // Log the deletion
            logger.info("Material deleted successfully - ID: {}, Name: {}, SKU: {}", materialId, materialName, sku);

            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material '" + materialName + "' (SKU: " + sku + ") and all related data deleted successfully"
            );

        } catch (Exception e) {
            logger.error("Error deleting material with ID {}: {}", materialId, e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to delete material: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getMaterialNamesAndIdsBySuperAdminId(Long superAdminId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminId(superAdminId);
            List<Map<String, Object>> materialInfo = materials.stream()
                .map(material -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("materialId", material.getMaterialId());
                    info.put("materialName", material.getMaterialName());
                    return info;
                })
                .collect(Collectors.toList());
            response.addData("materials", materialInfo);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material names and IDs retrieved successfully"
            );
        } catch (Exception e) {
            logger.error("Error retrieving material names and IDs: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve material names and IDs: " + e.getMessage()
            );
        }
    }


} 