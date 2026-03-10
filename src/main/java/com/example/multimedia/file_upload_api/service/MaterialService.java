package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MaterialDTO;
import com.example.multimedia.file_upload_api.dto.MaterialImageDTO;
import com.example.multimedia.file_upload_api.dto.MaterialImageSequenceUpdateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.entity.ItemSubcategory;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ItemSubcategoryRepository;
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
import com.example.multimedia.file_upload_api.entity.MaterialImage;
import com.example.multimedia.file_upload_api.repository.MaterialImageRepository;
import com.example.multimedia.file_upload_api.dto.MaterialAttributeDTO;
import com.example.multimedia.file_upload_api.dto.MaterialVariantDTO;
import com.example.multimedia.file_upload_api.entity.Attribute;
import com.example.multimedia.file_upload_api.entity.AttributeType;
import com.example.multimedia.file_upload_api.entity.MaterialVariant;
import com.example.multimedia.file_upload_api.repository.AttributeRepository;
import com.example.multimedia.file_upload_api.repository.MaterialVariantRepository;
import com.example.multimedia.file_upload_api.util.CodeGenerator;
import com.example.multimedia.file_upload_api.dto.VariantRequest;
import com.example.multimedia.file_upload_api.dto.VariantUpdateRequest;
import com.example.multimedia.file_upload_api.entity.MaterialAttribute;
import com.example.multimedia.file_upload_api.repository.MaterialAttributeRepository;
import com.example.multimedia.file_upload_api.dto.VariantBulkRequest;
import com.example.multimedia.file_upload_api.dto.VariantActiveStatusRequest;
import com.example.multimedia.file_upload_api.dto.BulkMaterialCreateRequest;
import com.example.multimedia.file_upload_api.dto.BulkMaterialResponse;
import com.example.multimedia.file_upload_api.dto.BulkMaterialUploadRequest;
import com.example.multimedia.file_upload_api.dto.BulkMaterialUploadResponse;
import java.util.Base64;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

@Service
public class MaterialService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ItemSubcategoryRepository itemSubcategoryRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private MaterialImageRepository materialImageRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private MaterialVariantRepository materialVariantRepository;

    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private MaterialAttributeRepository materialAttributeRepository;

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
            
            ItemCategory itemCategory = itemCategoryRepository.findByCodeIgnoreCase(req.getItemCategoryCode()).orElseThrow(() -> new RuntimeException("Category not found with code: " + req.getItemCategoryCode()));
            
            // Get company for the current admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No company found for the current admin");
            }
            CompanyDetails company = adminCompanies.get(0);
            
            ItemSubcategory subcategory = itemSubcategoryRepository.findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                req.getSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found with name: " + req.getSubcategoryName() + " for category: " + req.getItemCategoryCode() + " in your company"));

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
            material.setItemCategory(itemCategory);
            material.setSubcategory(subcategory);
            material.setLocation(location);

            if (barcodeImage != null && !barcodeImage.isEmpty()) {
                material.setBarcodeImage(barcodeImage.getBytes());
            }

            Material savedMaterial = materialRepository.save(material);

            // Initialize inventory with 0 stock
            inventoryService.initializeInventory(savedMaterial, location, superAdmin);

            List<com.example.multimedia.file_upload_api.entity.MaterialAttribute> generalAttributes = new ArrayList<>();
            List<com.example.multimedia.file_upload_api.entity.MaterialAttribute> variantAttributes = new ArrayList<>();

            // Get current super admin for attribute filtering
            SuperAdmin superAdminFilter = currentUserService.getCurrentSuperAdmin();

            if (req.getGeneralAttributes() != null && !req.getGeneralAttributes().isEmpty()) {
                for (MaterialCreateRequest.GeneralAttributeRequest genAttr : req.getGeneralAttributes()) {
                    Attribute attribute = attributeRepository.findById(genAttr.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + genAttr.getAttributeId()));
                    
                    // Check if attribute belongs to current super admin
                    if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdminFilter.getSuperAdminId())) {
                        throw new RuntimeException("Access denied: Attribute does not belong to current admin");
                    }
                    
                    if (!"GENERAL".equalsIgnoreCase(genAttr.getType())) continue;
                    MaterialAttribute materialAttribute = new MaterialAttribute();
                    materialAttribute.setAttribute(attribute);
                    materialAttribute.setMaterial(savedMaterial);
                    materialAttribute.setAttributeValue(null); // No value for general attributes
                    generalAttributes.add(materialAttribute);
                }
            }

            if (req.getAttributes() != null && !req.getAttributes().isEmpty()) {
                for (MaterialAttributeDTO attr : req.getAttributes()) {
                    Attribute attribute = attributeRepository.findByAttributeNameAndSuperAdmin_SuperAdminId(
                        attr.getAttributeName(), superAdminFilter.getSuperAdminId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + attr.getAttributeName()));
                    
                    if (attribute.getType() == AttributeType.VARIANT) {
                        MaterialAttribute materialAttribute = new MaterialAttribute();
                        materialAttribute.setAttribute(attribute);
                        materialAttribute.setMaterial(savedMaterial);
                        materialAttribute.setAttributeValue(attr.getAttributeValue());
                        variantAttributes.add(materialAttribute);
                    }
                }
            }

            if (!generalAttributes.isEmpty()) {
                materialAttributeRepository.saveAll(generalAttributes);
            }

            if (!variantAttributes.isEmpty()) {
                MaterialVariant variant = new MaterialVariant();
                variant.setMaterial(savedMaterial);
                String variantCode;
                do {
                    variantCode = codeGenerator.generateUniqueCode(10);
                } while (materialVariantRepository.existsByVariantCode(variantCode));
                variant.setVariantCode(variantCode);
                variant.setIsActive(true); // Set default active status
                MaterialVariant savedVariant = materialVariantRepository.save(variant);

                for (com.example.multimedia.file_upload_api.entity.MaterialAttribute attr : variantAttributes) {
                    attr.setVariant(savedVariant);
                }
                savedVariant.setAttributes(variantAttributes);
                materialVariantRepository.save(savedVariant);
            }

            if (materialImages != null) {
                int sequence = 0;
                for (MultipartFile img : materialImages) {
                    if (!img.isEmpty()) {
                        MaterialImage image = new MaterialImage();
                        image.setMaterial(savedMaterial);
                        image.setImageData(img.getBytes());
                        image.setImageName(img.getOriginalFilename());
                        image.setImageType(img.getContentType());
                        image.setSequenceOrder(sequence++);
                        materialImageRepository.save(image);
                    }
                }
            }

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
            ItemCategory itemCategory = itemCategoryRepository.findById(dto.getItemCategoryId()).orElseThrow(() -> new RuntimeException("Item category not found with ID: " + dto.getItemCategoryId()));
            ItemSubcategory subcategory = itemSubcategoryRepository.findById(dto.getSubcategoryId()).orElseThrow(() -> new RuntimeException("Item subcategory not found with ID: " + dto.getSubcategoryId()));

            if (!subcategory.getItemCategory().getItemCategoryId().equals(itemCategory.getItemCategoryId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Subcategory does not belong to the specified category");
            }

            if (!material.getSku().equals(dto.getSku()) && materialRepository.existsBySku(dto.getSku())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Material with SKU " + dto.getSku() + " already exists");
            }

            material.setMaterialName(dto.getMaterialName());
            material.setDescription(dto.getDescription());
            material.setBlocked(dto.getBlocked() != null ? dto.getBlocked() : false);
            material.setType(dto.getType());
            material.setBaseUnitOfMeasure(dto.getBaseUnitOfMeasure());
            material.setSubcategory(subcategory);
            material.setItemCategory(itemCategory);
            material.setHsnCode(dto.getHsnCode());
            material.setSku(dto.getSku());
            material.setPurchasingCode(dto.getPurchasingCode());
            material.setVariantMandatory(dto.getVariantMandatory() != null ? dto.getVariantMandatory() : false);

            material.getGeneralAttributes().clear();
            if (dto.getAttributes() != null) {
                // Get current super admin for attribute filtering
                SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
                
                for (MaterialAttributeDTO attrDTO : dto.getAttributes()) {
                    Attribute attribute = attributeRepository.findByAttributeNameAndSuperAdmin_SuperAdminId(
                        attrDTO.getAttributeName(), superAdmin.getSuperAdminId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + attrDTO.getAttributeName()));
                    
                    if (attribute.getType() == AttributeType.GENERAL) {
                        com.example.multimedia.file_upload_api.entity.MaterialAttribute attr = new com.example.multimedia.file_upload_api.entity.MaterialAttribute();
                        attr.setMaterial(material);
                        attr.setAttribute(attribute);
                        attr.setAttributeValue(attrDTO.getAttributeValue());
                        material.getGeneralAttributes().add(attr);
                    }
                }
            }

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

            // Get company for the current admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No company found for the current admin");
            }
            CompanyDetails company = adminCompanies.get(0);

            ItemCategory itemCategory = itemCategoryRepository.findByCodeIgnoreCase(req.getItemCategoryCode())
                .orElseThrow(() -> new RuntimeException("Category not found with code: " + req.getItemCategoryCode()));
            ItemSubcategory subcategory = itemSubcategoryRepository.findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                req.getSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found with name: " + req.getSubcategoryName() + " for category: " + req.getItemCategoryCode() + " in your company"));

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
            material.setItemCategory(itemCategory);
            material.setSubcategory(subcategory);
            material.setLocation(location);

            // Update barcode image if provided
            if (barcodeImage != null && !barcodeImage.isEmpty()) {
                material.setBarcodeImage(barcodeImage.getBytes());
            }

            Material savedMaterial = materialRepository.save(material);

            // Clear existing attributes
            List<MaterialAttribute> existingAttributes = materialAttributeRepository.findByMaterial_MaterialId(materialId);
            if (!existingAttributes.isEmpty()) {
                materialAttributeRepository.deleteAll(existingAttributes);
            }

            // Handle general attributes
            if (req.getGeneralAttributes() != null && !req.getGeneralAttributes().isEmpty()) {
                List<MaterialAttribute> generalAttributes = new ArrayList<>();
                SuperAdmin superAdminFilter = currentUserService.getCurrentSuperAdmin();
                
                for (MaterialCreateRequest.GeneralAttributeRequest genAttr : req.getGeneralAttributes()) {
                    Attribute attribute = attributeRepository.findById(genAttr.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + genAttr.getAttributeId()));
                    
                    // Check if attribute belongs to current super admin
                    if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdminFilter.getSuperAdminId())) {
                        throw new RuntimeException("Access denied: Attribute does not belong to current admin");
                    }
                    
                    if (!"GENERAL".equalsIgnoreCase(genAttr.getType())) continue;
                    MaterialAttribute materialAttribute = new MaterialAttribute();
                    materialAttribute.setAttribute(attribute);
                    materialAttribute.setMaterial(savedMaterial);
                    materialAttribute.setAttributeValue(null); // No value for general attributes
                    generalAttributes.add(materialAttribute);
                }
                
                if (!generalAttributes.isEmpty()) {
                    materialAttributeRepository.saveAll(generalAttributes);
                }
            }

            // Handle variant attributes (if any)
            if (req.getAttributes() != null && !req.getAttributes().isEmpty()) {
                List<MaterialAttribute> variantAttributes = new ArrayList<>();
                SuperAdmin superAdminFilter = currentUserService.getCurrentSuperAdmin();
                
                for (MaterialAttributeDTO attr : req.getAttributes()) {
                    Attribute attribute = attributeRepository.findByAttributeNameAndSuperAdmin_SuperAdminId(
                        attr.getAttributeName(), superAdminFilter.getSuperAdminId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + attr.getAttributeName()));
                    
                    if (attribute.getType() == AttributeType.VARIANT) {
                        MaterialAttribute materialAttribute = new MaterialAttribute();
                        materialAttribute.setAttribute(attribute);
                        materialAttribute.setMaterial(savedMaterial);
                        materialAttribute.setAttributeValue(attr.getAttributeValue());
                        variantAttributes.add(materialAttribute);
                    }
                }

                if (!variantAttributes.isEmpty()) {
                    materialAttributeRepository.saveAll(variantAttributes);
                }
            }

            // Handle material images
            if (materialImages != null && !materialImages.isEmpty()) {
                // Clear existing images and save new ones
                List<MaterialImage> existingImages = materialImageRepository.findByMaterialOrderBySequenceOrderAsc(savedMaterial);
                if (!existingImages.isEmpty()) {
                    materialImageRepository.deleteAll(existingImages);
                }
                
                // Save new images
                int sequence = 0;
                for (MultipartFile img : materialImages) {
                    if (!img.isEmpty()) {
                        MaterialImage image = new MaterialImage();
                        image.setMaterial(savedMaterial);
                        image.setImageData(img.getBytes());
                        image.setImageName(img.getOriginalFilename());
                        image.setImageType(img.getContentType());
                        image.setSequenceOrder(sequence++);
                        materialImageRepository.save(image);
                    }
                }
            } else if (replaceImages != null && replaceImages) {
                // If replaceImages is true but no new images provided, clear existing images
                List<MaterialImage> existingImages = materialImageRepository.findByMaterialOrderBySequenceOrderAsc(savedMaterial);
                if (!existingImages.isEmpty()) {
                    materialImageRepository.deleteAll(existingImages);
                }
            }
            // If no new images provided and replaceImages is false, existing images are preserved automatically

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
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter materials by current admin
            List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminId(currentAdminId);
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
    public ServiceResponse getMaterialsByCategoryId(Long categoryId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter materials by current admin and category
            List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminIdAndItemCategory_ItemCategoryId(currentAdminId, categoryId);
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
    public ServiceResponse getMaterialsBySubcategoryId(Long subcategoryId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter materials by current admin and subcategory
            List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminIdAndSubcategory_ItemSubcategoryId(currentAdminId, subcategoryId);
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

    @Transactional
    public ServiceResponse updateMaterialImageSequences(MaterialImageSequenceUpdateRequest request) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate material exists
            Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new RuntimeException("Material not found with ID: " + request.getMaterialId()));

            // Update sequences
            for (MaterialImageSequenceUpdateRequest.ImageSequence seq : request.getImageSequences()) {
                MaterialImage image = material.getMaterialImages().stream()
                    .filter(img -> img.getImageId().equals(seq.getImageId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Image not found with ID: " + seq.getImageId()));
                
                image.setSequenceOrder(seq.getSequenceOrder());
                materialImageRepository.save(image);
            }

            response.addData("material", convertToDTO(material));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material image sequences updated successfully"
            );

        } catch (Exception e) {
            logger.error("Error updating material image sequences: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update material image sequences: " + e.getMessage()
            );
        }
    }

    @Transactional
    public void createVariant(Long materialId, VariantRequest request) {
        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Material not found"));

        // Check for duplicate variant
        List<MaterialVariant> existingVariants = materialVariantRepository.findByMaterial(material);
        for (MaterialVariant variant : existingVariants) {
            List<MaterialAttribute> attrs = variant.getAttributes();
            if (attrs != null && attrs.size() == request.getAttributes().size()) {
                boolean allMatch = true;
                for (VariantRequest.VariantAttribute reqAttr : request.getAttributes()) {
                        boolean match = attrs.stream().anyMatch(
                            a -> a.getAttribute().getAttributeId().equals(reqAttr.getAttributeId())
                            && a.getAttributeValue() != null
                            && a.getAttributeValue().equalsIgnoreCase(reqAttr.getAttributeValue())
                        );
                    if (!match) {
                        allMatch = false;
                        break;
                    }
                }
                if (allMatch) {
                    throw new RuntimeException("Variant with these attributes already exists");
                }
            }
        }

        // Generate unique variant code
        String baseSku = material.getSku();
        int nextNumber = materialVariantRepository.countByMaterial(material) + 1;
        String variantCode = baseSku + String.format("-%04d", nextNumber);

        // Create MaterialVariant
        MaterialVariant variant = new MaterialVariant();
        variant.setMaterial(material);
        variant.setVariantCode(variantCode);
        variant.setMrp(request.getMrp());
        variant.setSellingPrice(request.getSellingPrice());
        variant.setCost(request.getCost());
        variant.setStock(request.getStock());
        variant.setBarcodeImage(null); // Will be set later
        variant.setIsActive(true); // Set default active status

        materialVariantRepository.save(variant);

        // Save variant attributes
        for (VariantRequest.VariantAttribute attr : request.getAttributes()) {
            // Get current super admin for attribute filtering
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            Attribute attribute = attributeRepository.findById(attr.getAttributeId())
                .orElseThrow(() -> new RuntimeException("Attribute not found"));

            // Check if attribute belongs to current super admin
            if (!attribute.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                throw new RuntimeException("Access denied: Attribute does not belong to current admin");
            }

            MaterialAttribute materialAttribute = new MaterialAttribute();
            materialAttribute.setMaterial(material);
            materialAttribute.setAttribute(attribute);
            materialAttribute.setAttributeValue(attr.getAttributeValue());
            materialAttribute.setVariant(variant);

            materialAttributeRepository.save(materialAttribute);
        }
    }

    public ServiceResponse getAllMaterialVariants() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter variants by current admin
            List<MaterialVariant> variants = materialVariantRepository.findByMaterial_SuperAdmin_SuperAdminId(currentAdminId);
            List<Map<String, Object>> variantList = new ArrayList<>();
            for (MaterialVariant variant : variants) {
                Map<String, Object> variantMap = new HashMap<>();
                Material material = variant.getMaterial();
                variantMap.put("materialName", material.getMaterialName());
                variantMap.put("sku", material.getSku());
                // Extract last 3 digits from variant code (e.g., SHIRT001-0001 -> 001)
                String variantCode = variant.getVariantCode();
                String codeSuffix = variantCode.length() >= 3 ? variantCode.substring(variantCode.length() - 3) : variantCode;
                variantMap.put("variantCode", codeSuffix);
                
                // Add barcodeImage field - convert byte array to Base64 string
                if (variant.getBarcodeImage() != null) {
                    variantMap.put("barcodeImage", Base64.getEncoder().encodeToString(variant.getBarcodeImage()));
                } else {
                    variantMap.put("barcodeImage", null);
                }
                
                // Add variantImage field - convert byte array to Base64 string
                if (variant.getVariantImage() != null) {
                    variantMap.put("variantImage", Base64.getEncoder().encodeToString(variant.getVariantImage()));
                } else {
                    variantMap.put("variantImage", null);
                }
                
                variantMap.put("active", variant.getIsActive() != null ? variant.getIsActive() : true);
                
                variantList.add(variantMap);
            }
            response.addData("variants", variantList);
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variants fetched successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    public ServiceResponse createVariantsBulk(Long materialId, VariantBulkRequest request) {
        ServiceResponse response = new ServiceResponse();
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (VariantRequest variant : request.getVariants()) {
            try {
                createVariant(materialId, variant); // uses your existing logic (with duplicate check)
                created.add(variant.getAttributes().toString());
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                    skipped.add(variant.getAttributes().toString());
                } else {
                    errors.add(variant.getAttributes().toString() + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", created);
        result.put("skipped", skipped);
        result.put("errors", errors);

        response.addData("result", result);
        serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Bulk variant creation processed");
        return response;
    }

    public ServiceResponse getAllVariants() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Filter variants by current admin
            List<MaterialVariant> variants = materialVariantRepository.findByMaterial_SuperAdmin_SuperAdminId(currentAdminId);
            response.addData("variants", buildVariantList(variants));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variants fetched successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    public ServiceResponse getVariantsByMaterial(Long materialId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
            List<MaterialVariant> variants = materialVariantRepository.findByMaterial(material);
            response.addData("variants", buildVariantList(variants));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variants fetched successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    private List<Map<String, Object>> buildVariantList(List<MaterialVariant> variants) {
        List<Map<String, Object>> variantList = new ArrayList<>();
        for (MaterialVariant variant : variants) {
            Map<String, Object> variantMap = new HashMap<>();
            Material material = variant.getMaterial();
            variantMap.put("variantCode", variant.getVariantCode());
            variantMap.put("materialName", material.getMaterialName());
            variantMap.put("materialDescription", material.getDescription());
            variantMap.put("mrp", variant.getMrp());
            variantMap.put("sellingPrice", variant.getSellingPrice());
            variantMap.put("cost", variant.getCost());
            variantMap.put("stock", variant.getStock());
            variantMap.put("barcode", variant.getVariantCode()); // or barcodeImage if you want
            
            // Add barcodeImage field - convert byte array to Base64 string
            if (variant.getBarcodeImage() != null) {
                variantMap.put("barcodeImage", Base64.getEncoder().encodeToString(variant.getBarcodeImage()));
            } else {
                variantMap.put("barcodeImage", null);
            }
            
            // Add variantImage field - convert byte array to Base64 string
            if (variant.getVariantImage() != null) {
                variantMap.put("variantImage", Base64.getEncoder().encodeToString(variant.getVariantImage()));
            } else {
                variantMap.put("variantImage", null);
            }
            
            variantMap.put("active", variant.getIsActive() != null ? variant.getIsActive() : true);

            // Attributes
            List<Map<String, String>> attrs = new ArrayList<>();
            if (variant.getAttributes() != null) {
                for (MaterialAttribute attr : variant.getAttributes()) {
                    Map<String, String> attrMap = new HashMap<>();
                    attrMap.put("attributeName", attr.getAttribute().getAttributeName());
                    attrMap.put("attributeValue", attr.getAttributeValue());
                    attrs.add(attrMap);
                }
            }
            variantMap.put("attributes", attrs);

            variantList.add(variantMap);
        }
        return variantList;
    }

    public ServiceResponse getVariantByCode(String variantCode) {
        ServiceResponse response = new ServiceResponse();
        try {
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
            List<MaterialVariant> singleList = new ArrayList<>();
            singleList.add(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant fetched successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    public ServiceResponse updateVariantByCode(String variantCode, VariantRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            // 1. Find the variant by code
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

            // 2. Update variant fields
            variant.setMrp(request.getMrp());
            variant.setSellingPrice(request.getSellingPrice());
            variant.setCost(request.getCost());
            variant.setStock(request.getStock());
            materialVariantRepository.save(variant);

            // 3. Update attributes
            List<MaterialAttribute> attributes = materialAttributeRepository.findByVariant_Id(variant.getId());
            Map<Long, String> attrIdToValue = request.getAttributes().stream()
                .collect(Collectors.toMap(VariantRequest.VariantAttribute::getAttributeId, VariantRequest.VariantAttribute::getAttributeValue));

            for (MaterialAttribute attr : attributes) {
                if (attrIdToValue.containsKey(attr.getAttribute().getAttributeId())) {
                    attr.setAttributeValue(attrIdToValue.get(attr.getAttribute().getAttributeId()));
                }
            }
            materialAttributeRepository.saveAll(attributes);

            // Build and return response as before
            List<MaterialVariant> singleList = java.util.Collections.singletonList(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant updated successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    @Transactional
    public ServiceResponse updateVariantByCodeWithImages(String variantCode, VariantUpdateRequest request, MultipartFile barcodeImage, MultipartFile variantImage) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            // 1. Find the variant by code
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

            // 2. Update variant fields
            if (request.getMrp() != null) {
                variant.setMrp(request.getMrp());
            }
            if (request.getSellingPrice() != null) {
                variant.setSellingPrice(request.getSellingPrice());
            }
            if (request.getCost() != null) {
                variant.setCost(request.getCost());
            }
            if (request.getStock() != null) {
                variant.setStock(request.getStock());
            }
            if (request.getIsActive() != null) {
                variant.setIsActive(request.getIsActive());
            }

            // 3. Update barcode image if provided
            if (barcodeImage != null && !barcodeImage.isEmpty()) {
                // Validate file size (max 5MB)
                if (barcodeImage.getSize() > 5 * 1024 * 1024) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Barcode image file size too large. Maximum allowed size is 5MB.");
                }
                
                // Validate file type
                String contentType = barcodeImage.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Invalid barcode image file type. Only image files are allowed.");
                }
                
                variant.setBarcodeImage(barcodeImage.getBytes());
            }

            // 4. Update variant image if provided
            if (variantImage != null && !variantImage.isEmpty()) {
                // Validate file size (max 5MB)
                if (variantImage.getSize() > 5 * 1024 * 1024) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Variant image file size too large. Maximum allowed size is 5MB.");
                }
                
                // Validate file type
                String contentType = variantImage.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Invalid variant image file type. Only image files are allowed.");
                }
                
                variant.setVariantImage(variantImage.getBytes());
            }

            materialVariantRepository.save(variant);

            // 5. Update attributes if provided
            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                List<MaterialAttribute> attributes = materialAttributeRepository.findByVariant_Id(variant.getId());
                Map<Long, String> attrIdToValue = request.getAttributes().stream()
                    .collect(Collectors.toMap(VariantUpdateRequest.VariantAttribute::getAttributeId, VariantUpdateRequest.VariantAttribute::getAttributeValue));

                for (MaterialAttribute attr : attributes) {
                    if (attrIdToValue.containsKey(attr.getAttribute().getAttributeId())) {
                        attr.setAttributeValue(attrIdToValue.get(attr.getAttribute().getAttributeId()));
                    }
                }
                materialAttributeRepository.saveAll(attributes);
            }

            // Build and return response
            List<MaterialVariant> singleList = java.util.Collections.singletonList(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant updated successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    @Transactional
    public ServiceResponse updateVariantActiveStatus(String variantCode, VariantActiveStatusRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

            // Update active status
            variant.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            materialVariantRepository.save(variant);

            List<MaterialVariant> singleList = new ArrayList<>();
            singleList.add(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant active status updated successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    @Transactional
    public ServiceResponse updateVariantBarcodeImageByCode(String variantCode, MultipartFile file) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "File size too large. Maximum allowed size is 5MB.");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Invalid file type. Only image files are allowed.");
            }
            
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found with code: " + variantCode));
            
            variant.setBarcodeImage(file.getBytes());
            materialVariantRepository.save(variant);
            
            List<MaterialVariant> singleList = new ArrayList<>();
            singleList.add(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Barcode image uploaded successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    @Transactional
    public ServiceResponse updateVariantImageByCode(String variantCode, MultipartFile file) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "File size too large. Maximum allowed size is 5MB.");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Invalid file type. Only image files are allowed.");
            }
            
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found with code: " + variantCode));
            
            variant.setVariantImage(file.getBytes());
            materialVariantRepository.save(variant);
            
            List<MaterialVariant> singleList = new ArrayList<>();
            singleList.add(variant);
            response.addData("variant", buildVariantList(singleList).get(0));
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant image uploaded successfully");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return response;
    }

    @Transactional(readOnly = true)
    public ServiceResponse getMaterialAttributes(Long materialId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // 1. Get all MaterialAttribute for this material
            List<MaterialAttribute> materialAttributes = materialAttributeRepository.findByMaterial_MaterialId(materialId);

            // 2. Group by attributeId
            Map<Long, List<MaterialAttribute>> grouped = materialAttributes.stream()
                .collect(Collectors.groupingBy(attr -> attr.getAttribute().getAttributeId()));

            List<MaterialAttributeDTO> attributeDTOs = new ArrayList<>();

            for (Map.Entry<Long, List<MaterialAttribute>> entry : grouped.entrySet()) {
                Attribute attribute = entry.getValue().get(0).getAttribute();
                String type = attribute.getType().name(); // "GENERAL" or "VARIANT"
                String attributeName = attribute.getAttributeName();

                // Collect all unique, non-null values
                Set<String> values = entry.getValue().stream()
                    .map(MaterialAttribute::getAttributeValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

                MaterialAttributeDTO dto = new MaterialAttributeDTO();
                dto.setAttributeName(attributeName);
                dto.setType(type);

                if (type.equals("VARIANT")) {
                    dto.setAttributeValue(String.join(",", values));
                } else {
                    // For GENERAL, usually only one value or null
                    dto.setAttributeValue(values.isEmpty() ? null : values.iterator().next());
                }
                attributeDTOs.add(dto);
            }

            // 3. Get all variants for this material with their images
            Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
            List<MaterialVariant> variants = materialVariantRepository.findByMaterial(material);
            
            List<Map<String, Object>> variantImages = new ArrayList<>();
            for (MaterialVariant variant : variants) {
                Map<String, Object> variantImageData = new HashMap<>();
                variantImageData.put("variantCode", variant.getVariantCode());
                variantImageData.put("variantId", variant.getId());
                
                // Add barcode image (Base64 encoded)
                if (variant.getBarcodeImage() != null) {
                    variantImageData.put("barcodeImage", Base64.getEncoder().encodeToString(variant.getBarcodeImage()));
                } else {
                    variantImageData.put("barcodeImage", null);
                }
                
                // Add variant image (Base64 encoded)
                if (variant.getVariantImage() != null) {
                    variantImageData.put("variantImage", Base64.getEncoder().encodeToString(variant.getVariantImage()));
                } else {
                    variantImageData.put("variantImage", null);
                }
                
                variantImageData.put("isActive", variant.getIsActive());
                variantImageData.put("mrp", variant.getMrp());
                variantImageData.put("sellingPrice", variant.getSellingPrice());
                variantImageData.put("cost", variant.getCost());
                variantImageData.put("stock", variant.getStock());
                
                variantImages.add(variantImageData);
            }

            response.addData("materialId", materialId);
            response.addData("attributes", attributeDTOs);
            response.addData("variantImages", variantImages);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material attributes and variant images fetched successfully"
            );
        } catch (Exception e) {
            logger.error("Error fetching material attributes: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to fetch material attributes: " + e.getMessage()
            );
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

        if (material.getSubcategory() != null) {
            dto.setSubcategoryId(material.getSubcategory().getItemSubcategoryId());
            dto.setSubcategoryName(material.getSubcategory().getItemSubcategoryName());
        }
        if (material.getItemCategory() != null) {
            dto.setItemCategoryId(material.getItemCategory().getItemCategoryId());
            dto.setItemCategoryCode(material.getItemCategory().getCode());
            dto.setItemCategoryDescription(material.getItemCategory().getDescription());
        }
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

        // Handle images
        dto.setBarcodeImage(material.getBarcodeImage());
        if (material.getMaterialImages() != null) {
            dto.setMaterialImages(material.getMaterialImages().stream().map(img -> {
                MaterialImageDTO imgDto = new MaterialImageDTO();
                imgDto.setImageId(img.getImageId());
                imgDto.setImageName(img.getImageName());
                imgDto.setSequenceOrder(img.getSequenceOrder());
                imgDto.setImageType(img.getImageType());
                imgDto.setMaterialId(img.getMaterial() != null ? img.getMaterial().getMaterialId() : null);
                // Send image data as raw byte array (no base64 encoding)
                imgDto.setImageData(img.getImageData());
                return imgDto;
            }).collect(Collectors.toList()));
        }   

        // Map general attributes
        if (material.getGeneralAttributes() != null) {
            dto.setGeneralAttributes(material.getGeneralAttributes().stream().map(attr -> {
                MaterialAttributeDTO attrDto = new MaterialAttributeDTO();
                if (attr.getAttribute() != null) {
                    attrDto.setAttributeName(attr.getAttribute().getAttributeName());
                }
                attrDto.setAttributeValue(attr.getAttributeValue());
                return attrDto;
            }).collect(Collectors.toList()));
        }

        // Map variants
        if (material.getVariants() != null) {
            dto.setVariants(material.getVariants().stream().map(variant -> {
                MaterialVariantDTO variantDto = new MaterialVariantDTO();
                variantDto.setVariantCode(variant.getVariantCode());
                variantDto.setMrp(variant.getMrp());
                variantDto.setSellingPrice(variant.getSellingPrice());
                variantDto.setCost(variant.getCost());
                variantDto.setStock(variant.getStock());
                variantDto.setBarcodeImage(variant.getBarcodeImage());
                variantDto.setVariantImage(variant.getVariantImage());
                variantDto.setIsActive(variant.getIsActive());
                if (variant.getAttributes() != null) {
                    variantDto.setAttributes(variant.getAttributes().stream().map(attr -> {
                        MaterialAttributeDTO attrDto = new MaterialAttributeDTO();
                        if (attr.getAttribute() != null) {
                            attrDto.setAttributeName(attr.getAttribute().getAttributeName());
                        }
                        attrDto.setAttributeValue(attr.getAttributeValue());
                        return attrDto;
                    }).collect(Collectors.toList()));
                }
                return variantDto;
            }).collect(Collectors.toList()));
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
            
            // 2. Delete variant-specific attributes
            List<MaterialVariant> variants = materialVariantRepository.findByMaterial(material);
            for (MaterialVariant variant : variants) {
                // Delete variant attributes
                List<MaterialAttribute> variantAttributes = materialAttributeRepository.findByVariant_Id(variant.getId());
                if (!variantAttributes.isEmpty()) {
                    materialAttributeRepository.deleteAll(variantAttributes);
                }
            }
            
            // 3. Delete variants
            if (!variants.isEmpty()) {
                materialVariantRepository.deleteAll(variants);
            }
            
            // 4. Delete general material attributes
            List<MaterialAttribute> generalAttributes = materialAttributeRepository.findByMaterial_MaterialId(materialId);
            if (!generalAttributes.isEmpty()) {
                materialAttributeRepository.deleteAll(generalAttributes);
            }
            
            // 5. Delete material images
            List<MaterialImage> materialImages = materialImageRepository.findByMaterialOrderBySequenceOrderAsc(material);
            if (!materialImages.isEmpty()) {
                materialImageRepository.deleteAll(materialImages);
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

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse deleteVariantByCode(String variantCode) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            // Find the variant by code
            MaterialVariant variant = materialVariantRepository.findByVariantCode(variantCode)
                .orElseThrow(() -> new RuntimeException("Variant not found with code: " + variantCode));

            // Check if variant belongs to current admin (through material)
            if (!variant.getMaterial().getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Access denied: Variant does not belong to current admin"
                );
            }

            // Store variant info for response before deletion
            String variantCodeValue = variant.getVariantCode();
            String materialName = variant.getMaterial().getMaterialName();
            Long variantId = variant.getId();

            // Manual deletion in proper order to avoid foreign key constraint violations
            
            // 1. Delete variant-specific attributes first
            List<MaterialAttribute> variantAttributes = materialAttributeRepository.findByVariant_Id(variantId);
            if (!variantAttributes.isEmpty()) {
                materialAttributeRepository.deleteAll(variantAttributes);
            }
            
            // 2. Delete the variant (this will also delete barcode_image and variant_image)
            materialVariantRepository.delete(variant);

            // Log the deletion
            logger.info("Variant deleted successfully - Code: {}, Material: {}, ID: {}", variantCodeValue, materialName, variantId);

            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Variant '" + variantCodeValue + "' of material '" + materialName + "' deleted successfully"
            );

        } catch (Exception e) {
            logger.error("Error deleting variant with code {}: {}", variantCode, e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to delete variant: " + e.getMessage()
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

    @Transactional(rollbackFor = Exception.class)
    public BulkMaterialResponse saveMaterialsBulk(BulkMaterialCreateRequest request) {
        BulkMaterialResponse response = new BulkMaterialResponse();
        List<BulkMaterialResponse.MaterialResult> results = new ArrayList<>();
        
        int totalMaterials = request.getMaterials().size();
        int successfulMaterials = 0;
        int failedMaterials = 0;
        
        logger.info("Starting bulk material creation for {} materials", totalMaterials);
        
        for (BulkMaterialCreateRequest.MaterialCreateRequest materialRequest : request.getMaterials()) {
            BulkMaterialResponse.MaterialResult result = new BulkMaterialResponse.MaterialResult();
            result.setSku(materialRequest.getSku());
            
            try {
                // Get current admin from security context first
                Long currentAdminId = currentUserService.getCurrentSuperAdminId();
                
                SuperAdmin superAdmin = superAdminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
                
                // Validate and get location
                if (materialRequest.getLocationId() == null) {
                    result.setSuccess(false);
                    result.setMessage("Location ID is required");
                    results.add(result);
                    failedMaterials++;
                    continue;
                }
                Location location = locationService.getLocationEntity(materialRequest.getLocationId(), superAdmin);
                
                // Check if SKU already exists for this admin and location
                if (materialRepository.existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(materialRequest.getSku(), currentAdminId, location.getLocationId())) {
                    result.setSuccess(false);
                    result.setMessage("Material with SKU " + materialRequest.getSku() + " already exists for this location");
                    results.add(result);
                    failedMaterials++;
                    continue;
                }
                
                // Find category and subcategory
                ItemCategory itemCategory = itemCategoryRepository.findByCodeIgnoreCase(materialRequest.getItemCategoryCode())
                    .orElseThrow(() -> new RuntimeException("Category not found with code: " + materialRequest.getItemCategoryCode()));
                
                // Get company for the current admin
                List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
                if (adminCompanies.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("No company found for the current admin");
                    results.add(result);
                    failedMaterials++;
                    continue;
                }
                CompanyDetails company = adminCompanies.get(0);
                
                ItemSubcategory subcategory = itemSubcategoryRepository.findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                    materialRequest.getSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Subcategory not found with name: " + materialRequest.getSubcategoryName() + " for category: " + materialRequest.getItemCategoryCode() + " in your company"));
                
                // Create material
                Material material = new Material();
                String materialCode;
                do {
                    materialCode = codeGenerator.generateUniqueCode(10);
                } while (materialRepository.existsByMaterialCode(materialCode));
                
                material.setMaterialCode(materialCode);
                material.setMaterialName(materialRequest.getMaterialName());
                material.setDescription(materialRequest.getDescription());
                material.setVendorArticleNumber(materialRequest.getVendorArticleNumber());
                material.setBlocked(materialRequest.getBlocked() != null ? materialRequest.getBlocked() : false);
                material.setType(materialRequest.getType());
                material.setBaseUnitOfMeasure(materialRequest.getBaseUnitOfMeasure());
                material.setItemCategory(itemCategory);
                material.setSubcategory(subcategory);
                material.setHsnCode(materialRequest.getHsnCode());
                material.setSku(materialRequest.getSku());
                material.setPurchasingCode(materialRequest.getPurchasingCode());
                material.setVariantMandatory(materialRequest.getVariantMandatory() != null ? materialRequest.getVariantMandatory() : false);
                material.setSuperAdmin(superAdmin);
                material.setLocation(location);
                
                // Save material first
                Material savedMaterial = materialRepository.save(material);
                
                // Initialize inventory with 0 stock
                inventoryService.initializeInventory(savedMaterial, location, superAdmin);
                
                // Handle general attributes
                if (materialRequest.getGeneralAttributes() != null && !materialRequest.getGeneralAttributes().isEmpty()) {
                    for (BulkMaterialCreateRequest.MaterialCreateRequest.GeneralAttributeRequest generalAttr : materialRequest.getGeneralAttributes()) {
                        Attribute attribute = attributeRepository.findById(generalAttr.getAttributeId())
                            .orElseThrow(() -> new RuntimeException("Attribute not found with ID: " + generalAttr.getAttributeId()));
                        
                        if (attribute.getType() == AttributeType.GENERAL) {
                            MaterialAttribute materialAttribute = new MaterialAttribute();
                            materialAttribute.setMaterial(savedMaterial);
                            materialAttribute.setAttribute(attribute);
                            materialAttribute.setAttributeValue(null); // General attributes don't have values in bulk creation
                            materialAttributeRepository.save(materialAttribute);
                        }
                    }
                }
                
                // Handle variant attributes
                if (materialRequest.getAttributes() != null && !materialRequest.getAttributes().isEmpty()) {
                    for (MaterialAttributeDTO attrDTO : materialRequest.getAttributes()) {
                        Attribute attribute = attributeRepository.findByAttributeNameAndSuperAdmin_SuperAdminId(
                            attrDTO.getAttributeName(), superAdmin.getSuperAdminId())
                            .orElseThrow(() -> new RuntimeException("Attribute not found: " + attrDTO.getAttributeName()));
                        
                        if (attribute.getType() == AttributeType.VARIANT) {
                            MaterialAttribute materialAttribute = new MaterialAttribute();
                            materialAttribute.setMaterial(savedMaterial);
                            materialAttribute.setAttribute(attribute);
                            materialAttribute.setAttributeValue(attrDTO.getAttributeValue());
                            materialAttributeRepository.save(materialAttribute);
                        }
                    }
                }
                
                result.setSuccess(true);
                result.setMessage("Material created successfully");
                result.setMaterialId(savedMaterial.getMaterialId());
                result.setMaterialCode(savedMaterial.getMaterialCode());
                results.add(result);
                successfulMaterials++;
                
                logger.info("Successfully created material: {} with SKU: {}", savedMaterial.getMaterialName(), savedMaterial.getSku());
                
            } catch (Exception e) {
                logger.error("Error creating material with SKU {}: {}", materialRequest.getSku(), e.getMessage(), e);
                result.setSuccess(false);
                result.setMessage("Failed to create material: " + e.getMessage());
                results.add(result);
                failedMaterials++;
            }
        }
        
        response.setTotalMaterials(totalMaterials);
        response.setSuccessfulMaterials(successfulMaterials);
        response.setFailedMaterials(failedMaterials);
        response.setResults(results);
        
        logger.info("Bulk material creation completed. Total: {}, Successful: {}, Failed: {}", 
                   totalMaterials, successfulMaterials, failedMaterials);
        
        return response;
    }

    public BulkMaterialResponse saveMaterialsBulkWithImages(BulkMaterialCreateRequest request, 
                                                           List<MultipartFile> barcodeImages, 
                                                           List<MultipartFile> materialImages) {
        BulkMaterialResponse response = new BulkMaterialResponse();
        List<BulkMaterialResponse.MaterialResult> results = new ArrayList<>();
        
        int totalMaterials = request.getMaterials().size();
        int successfulMaterials = 0;
        int failedMaterials = 0;
        
        logger.info("Starting bulk material creation with images for {} materials", totalMaterials);
        
        for (int i = 0; i < request.getMaterials().size(); i++) {
            BulkMaterialCreateRequest.MaterialCreateRequest materialRequest = request.getMaterials().get(i);
            BulkMaterialResponse.MaterialResult result = new BulkMaterialResponse.MaterialResult();
            result.setSku(materialRequest.getSku());
            
            try {
                // Process each material in its own transaction
                result = processMaterialWithTransaction(materialRequest, barcodeImages, materialImages, i, totalMaterials);
                if (result.isSuccess()) {
                    successfulMaterials++;
                } else {
                    failedMaterials++;
                }
                results.add(result);
            } catch (Exception e) {
                logger.error("Error creating material with SKU {}: {}", materialRequest.getSku(), e.getMessage(), e);
                result.setSuccess(false);
                result.setMessage("Failed to create material: " + e.getMessage());
                results.add(result);
                failedMaterials++;
            }
        }
        
        response.setTotalMaterials(totalMaterials);
        response.setSuccessfulMaterials(successfulMaterials);
        response.setFailedMaterials(failedMaterials);
        response.setResults(results);
        
        logger.info("Bulk material creation with images completed. Total: {}, Successful: {}, Failed: {}", 
                   totalMaterials, successfulMaterials, failedMaterials);
        
        return response;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public BulkMaterialResponse.MaterialResult processMaterialWithTransaction(
            BulkMaterialCreateRequest.MaterialCreateRequest materialRequest,
            List<MultipartFile> barcodeImages,
            List<MultipartFile> materialImages,
            int index,
            int totalMaterials) {
        
        BulkMaterialResponse.MaterialResult result = new BulkMaterialResponse.MaterialResult();
        result.setSku(materialRequest.getSku());
        
        try {
            // Get current admin from security context first
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            
            SuperAdmin superAdmin = superAdminRepository.findById(currentAdminId)
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
            
            // Validate and get location
            if (materialRequest.getLocationId() == null) {
                result.setSuccess(false);
                result.setMessage("Location ID is required");
                return result;
            }
            Location location = locationService.getLocationEntity(materialRequest.getLocationId(), superAdmin);
            
            // Check if SKU already exists for this admin and location
            if (materialRepository.existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(materialRequest.getSku(), currentAdminId, location.getLocationId())) {
                result.setSuccess(false);
                result.setMessage("Material with SKU " + materialRequest.getSku() + " already exists for this location");
                return result;
            }
            
            // Find category and subcategory
            ItemCategory itemCategory = itemCategoryRepository.findByCodeIgnoreCase(materialRequest.getItemCategoryCode())
                .orElseThrow(() -> new RuntimeException("Category not found with code: " + materialRequest.getItemCategoryCode()));
            
            // Get company for the current admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            if (adminCompanies.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("No company found for the current admin");
                return result;
            }
            CompanyDetails company = adminCompanies.get(0);
            
            ItemSubcategory subcategory = itemSubcategoryRepository.findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                materialRequest.getSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found with name: " + materialRequest.getSubcategoryName() + " for category: " + materialRequest.getItemCategoryCode() + " in your company"));
            
            // Create material
            Material material = new Material();
            String materialCode;
            do {
                materialCode = codeGenerator.generateUniqueCode(10);
            } while (materialRepository.existsByMaterialCode(materialCode));
            
            material.setMaterialCode(materialCode);
            material.setMaterialName(materialRequest.getMaterialName());
            material.setDescription(materialRequest.getDescription());
            material.setVendorArticleNumber(materialRequest.getVendorArticleNumber());
            material.setBlocked(materialRequest.getBlocked() != null ? materialRequest.getBlocked() : false);
            material.setType(materialRequest.getType());
            material.setBaseUnitOfMeasure(materialRequest.getBaseUnitOfMeasure());
            material.setItemCategory(itemCategory);
            material.setSubcategory(subcategory);
            material.setHsnCode(materialRequest.getHsnCode());
            material.setSku(materialRequest.getSku());
            material.setPurchasingCode(materialRequest.getPurchasingCode());
            material.setVariantMandatory(materialRequest.getVariantMandatory() != null ? materialRequest.getVariantMandatory() : false);
            material.setSuperAdmin(superAdmin);
            material.setLocation(location);
            
            // Handle barcode image if provided
            if (barcodeImages != null && index < barcodeImages.size() && barcodeImages.get(index) != null && !barcodeImages.get(index).isEmpty()) {
                material.setBarcodeImage(barcodeImages.get(index).getBytes());
            }
            
            // Save material first
            Material savedMaterial = materialRepository.save(material);
            
            // Initialize inventory with 0 stock
            inventoryService.initializeInventory(savedMaterial, location, superAdmin);
            
            // Handle material images if provided
            if (materialImages != null && !materialImages.isEmpty()) {
                // Calculate how many images per material
                int imagesPerMaterial = materialImages.size() / totalMaterials;
                int remainingImages = materialImages.size() % totalMaterials;
                
                // Calculate start and end indices for this material
                int imageStartIndex = index * imagesPerMaterial + Math.min(index, remainingImages);
                int imageEndIndex = imageStartIndex + imagesPerMaterial + (index < remainingImages ? 1 : 0);
                
                int sequenceOrder = 1;
                for (int j = imageStartIndex; j < imageEndIndex; j++) {
                    if (j < materialImages.size() && materialImages.get(j) != null && !materialImages.get(j).isEmpty()) {
                        MaterialImage materialImage = new MaterialImage();
                        materialImage.setMaterial(savedMaterial);
                        materialImage.setImageName(materialImages.get(j).getOriginalFilename());
                        materialImage.setImageData(materialImages.get(j).getBytes());
                        materialImage.setImageType(materialImages.get(j).getContentType());
                        materialImage.setSequenceOrder(sequenceOrder++);
                        materialImageRepository.save(materialImage);
                    }
                }
            }
            
            // Handle general attributes
            if (materialRequest.getGeneralAttributes() != null && !materialRequest.getGeneralAttributes().isEmpty()) {
                for (BulkMaterialCreateRequest.MaterialCreateRequest.GeneralAttributeRequest generalAttr : materialRequest.getGeneralAttributes()) {
                    Attribute attribute = attributeRepository.findById(generalAttr.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found with ID: " + generalAttr.getAttributeId()));
                    
                    if (attribute.getType() == AttributeType.GENERAL) {
                        MaterialAttribute materialAttribute = new MaterialAttribute();
                        materialAttribute.setMaterial(savedMaterial);
                        materialAttribute.setAttribute(attribute);
                        materialAttribute.setAttributeValue(null);
                        materialAttributeRepository.save(materialAttribute);
                    }
                }
            }
            
            // Handle variant attributes
            if (materialRequest.getAttributes() != null && !materialRequest.getAttributes().isEmpty()) {
                for (MaterialAttributeDTO attrDTO : materialRequest.getAttributes()) {
                    Attribute attribute = attributeRepository.findByAttributeNameAndSuperAdmin_SuperAdminId(
                        attrDTO.getAttributeName(), superAdmin.getSuperAdminId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found: " + attrDTO.getAttributeName()));
                    
                    if (attribute.getType() == AttributeType.VARIANT) {
                        MaterialAttribute materialAttribute = new MaterialAttribute();
                        materialAttribute.setMaterial(savedMaterial);
                        materialAttribute.setAttribute(attribute);
                        materialAttribute.setAttributeValue(attrDTO.getAttributeValue());
                        materialAttributeRepository.save(materialAttribute);
                    }
                }
            }
            
            result.setSuccess(true);
            result.setMessage("Material created successfully");
            result.setMaterialId(savedMaterial.getMaterialId());
            result.setMaterialCode(savedMaterial.getMaterialCode());
            
            logger.info("Successfully created material with images: {} with SKU: {}", savedMaterial.getMaterialName(), savedMaterial.getSku());
            
        } catch (Exception e) {
            logger.error("Error creating material with SKU {}: {}", materialRequest.getSku(), e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Failed to create material: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional(readOnly = true)
    public ServiceResponse debugCategories() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current admin and company
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            
            // Get all categories
            List<ItemCategory> categories = itemCategoryRepository.findAll();
            List<Map<String, Object>> categoryData = categories.stream().map(cat -> {
                Map<String, Object> data = new HashMap<>();
                data.put("categoryId", cat.getItemCategoryId());
                data.put("code", cat.getCode());
                data.put("description", cat.getDescription());
                return data;
            }).collect(Collectors.toList());
            
            // Get all subcategories
            List<ItemSubcategory> subcategories = itemSubcategoryRepository.findAll();
            List<Map<String, Object>> subcategoryData = subcategories.stream().map(sub -> {
                Map<String, Object> data = new HashMap<>();
                data.put("subcategoryId", sub.getItemSubcategoryId());
                data.put("name", sub.getItemSubcategoryName());
                data.put("categoryId", sub.getItemCategory() != null ? sub.getItemCategory().getItemCategoryId() : null);
                data.put("categoryCode", sub.getItemCategory() != null ? sub.getItemCategory().getCode() : null);
                data.put("companyId", sub.getCompany() != null ? sub.getCompany().getCompanyId() : null);
                data.put("isActive", sub.getIsActive());
                return data;
            }).collect(Collectors.toList());
            
            // Get current admin's companies
            List<Map<String, Object>> companyData = adminCompanies.stream().map(comp -> {
                Map<String, Object> data = new HashMap<>();
                data.put("companyId", comp.getCompanyId());
                data.put("companyName", comp.getCompanyName());
                data.put("superAdminId", comp.getSuperAdmin() != null ? comp.getSuperAdmin().getSuperAdminId() : null);
                return data;
            }).collect(Collectors.toList());
            
            response.addData("currentAdminId", currentAdminId);
            response.addData("currentAdminCompanies", companyData);
            response.addData("categories", categoryData);
            response.addData("subcategories", subcategoryData);
            
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Debug data retrieved successfully"
            );
        } catch (Exception e) {
            logger.error("Error in debug categories: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve debug data: " + e.getMessage()
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse bulkUploadMaterials(BulkMaterialUploadRequest request) {
        ServiceResponse response = new ServiceResponse();
        List<BulkMaterialUploadResponse.MaterialUploadResult> results = new ArrayList<>();
        
        try {
            // Get current admin
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            SuperAdmin superAdmin = superAdminRepository.findById(currentAdminId)
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
            
            // Get company for the current admin
            List<CompanyDetails> adminCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            if (adminCompanies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No company found for the current admin");
            }
            CompanyDetails company = adminCompanies.get(0);
            
            int successCount = 0;
            int failureCount = 0;
            int skippedCount = 0;
            
            for (int i = 0; i < request.getMaterials().size(); i++) {
                BulkMaterialUploadRequest.MaterialUploadItem item = request.getMaterials().get(i);
                BulkMaterialUploadResponse.MaterialUploadResult result = new BulkMaterialUploadResponse.MaterialUploadResult();
                result.setIndex(i);
                result.setSku(item.getSku());
                result.setMaterialName(item.getMaterialName());
                
                try {
                    // Validate location exists and belongs to current admin
                    Location location = locationService.getLocationEntityByName(item.getLocation(), superAdmin);
                    
                    // Check if SKU already exists for this super admin and location
                    if (materialRepository.existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(item.getSku(), currentAdminId, location.getLocationId())) {
                        result.setStatus("SKIPPED");
                        result.setMessage("SKU already exists for this admin and location");
                        skippedCount++;
                        results.add(result);
                        continue;
                    }
                    
                    // Validate category exists
                    ItemCategory itemCategory = itemCategoryRepository.findByCodeIgnoreCase(item.getItemCategoryCode())
                        .orElseThrow(() -> new RuntimeException("Category not found with code: " + item.getItemCategoryCode()));
                    
                    // Validate subcategory exists for this category and company
                    ItemSubcategory subcategory = itemSubcategoryRepository.findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(
                        item.getSubcategoryName(), itemCategory.getItemCategoryId(), company.getCompanyId())
                        .orElseThrow(() -> new RuntimeException("Subcategory not found with name: " + item.getSubcategoryName() + " for category: " + item.getItemCategoryCode() + " in your company"));
                    
                    // Create material
                    Material material = new Material();
                    String materialCode;
                    do {
                        materialCode = codeGenerator.generateUniqueCode(10);
                    } while (materialRepository.existsByMaterialCode(materialCode));
                    
                    material.setMaterialCode(materialCode);
                    material.setMaterialName(item.getMaterialName());
                    material.setDescription(item.getDescription());
                    material.setVendorArticleNumber(item.getVendorArticleNumber());
                    material.setBlocked(item.getBlocked() != null ? item.getBlocked() : false);
                    material.setType(item.getType());
                    material.setBaseUnitOfMeasure(item.getBaseUnitOfMeasure());
                    material.setHsnCode(item.getHsnCode());
                    material.setSku(item.getSku());
                    material.setPurchasingCode(item.getPurchasingCode());
                    material.setVariantMandatory(item.getVariantMandatory() != null ? item.getVariantMandatory() : false);
                    material.setSuperAdmin(superAdmin);
                    material.setItemCategory(itemCategory);
                    material.setSubcategory(subcategory);
                    material.setLocation(location);
                    
                    Material savedMaterial = materialRepository.save(material);
                    
                    // Initialize inventory with 0 stock
                    inventoryService.initializeInventory(savedMaterial, location, superAdmin);
                    
                    result.setStatus("SUCCESS");
                    result.setMessage("Material created successfully");
                    result.setMaterialId(savedMaterial.getMaterialId());
                    successCount++;
                    
                } catch (Exception e) {
                    logger.error("Error processing material item {}: {}", item.getSku(), e.getMessage(), e);
                    result.setStatus("FAILED");
                    // Get root cause for better error message
                    Throwable cause = e.getCause();
                    String errorMessage = (cause != null && cause.getMessage() != null) ? cause.getMessage() : e.getMessage();
                    result.setMessage("Error: " + errorMessage);
                    result.setErrorCode("VALIDATION_ERROR");
                    failureCount++;
                }
                
                results.add(result);
            }
            
            BulkMaterialUploadResponse uploadResponse = new BulkMaterialUploadResponse(
                request.getMaterials().size(),
                successCount,
                failureCount,
                skippedCount,
                results
            );
            
            response.addData("uploadResult", uploadResponse);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                String.format("Bulk upload completed. Success: %d, Failed: %d, Skipped: %d", successCount, failureCount, skippedCount)
            );
            
        } catch (Exception e) {
            logger.error("Error in bulk upload materials: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to process bulk upload: " + e.getMessage()
            );
        }
    }
} 