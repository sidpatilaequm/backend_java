package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.InventoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVariantRepository materialVariantRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    /**
     * Get all inventory for current super admin and location
     * Shows ALL materials for the location, including those without inventory records (stock=0, price=null)
     */
    @Transactional(readOnly = true)
    public ServiceResponse getAllInventory(String locationName) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            List<InventoryDTO> inventoryDTOs = new ArrayList<>();
            Map<Long, Inventory> inventoryMap = new java.util.HashMap<>();
            
            if (locationName != null && !locationName.trim().isEmpty()) {
                // Get location
                Location location = locationService.getLocationEntityByName(locationName.trim(), superAdmin);
                
                // Get all materials for this location and super admin
                List<Material> materials = materialRepository.findBySuperAdmin_SuperAdminIdAndLocation_LocationId(
                    superAdmin.getSuperAdminId(), location.getLocationId());
                
                // Get existing inventory records for this location
                List<Inventory> existingInventories = inventoryRepository.findByLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                    location, superAdmin.getSuperAdminId());
                
                // Create a map of material ID to inventory for quick lookup
                for (Inventory inv : existingInventories) {
                    if (inv.getVariant() == null) { // Only material-level inventory for now
                        inventoryMap.put(inv.getMaterial().getMaterialId(), inv);
                    }
                }
                
                // Create DTOs for all materials
                for (Material material : materials) {
                    Inventory inventory = inventoryMap.get(material.getMaterialId());
                    if (inventory != null) {
                        // Material has inventory record - use it
                        inventoryDTOs.add(convertToDTO(inventory));
                    } else {
                        // Material doesn't have inventory record - create DTO with defaults
                        inventoryDTOs.add(createDefaultInventoryDTO(material, location));
                    }
                }
                
            } else {
                // No location filter - get all materials for all locations
                List<Location> locations = locationService.getActiveLocations(superAdmin).stream()
                    .map(dto -> {
                        try {
                            return locationService.getLocationEntity(dto.getLocationId(), superAdmin);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(loc -> loc != null)
                    .collect(Collectors.toList());
                
                // Get all inventories for all locations
                List<Inventory> allInventories = new ArrayList<>();
                for (Location loc : locations) {
                    allInventories.addAll(inventoryRepository.findByLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(loc, superAdmin.getSuperAdminId()));
                }
                
                // Create map of (materialId, locationId) -> Inventory
                Map<String, Inventory> inventoryLocationMap = new java.util.HashMap<>();
                for (Inventory inv : allInventories) {
                    if (inv.getVariant() == null) {
                        String key = inv.getMaterial().getMaterialId() + "_" + inv.getLocation().getLocationId();
                        inventoryLocationMap.put(key, inv);
                    }
                }
                
                // Get all materials for all locations
                List<Material> allMaterials = materialRepository.findBySuperAdmin_SuperAdminId(superAdmin.getSuperAdminId());
                
                // Create DTOs for all materials
                for (Material material : allMaterials) {
                    Location materialLocation = material.getLocation();
                    if (materialLocation != null) {
                        String key = material.getMaterialId() + "_" + materialLocation.getLocationId();
                        Inventory inventory = inventoryLocationMap.get(key);
                        
                        if (inventory != null) {
                            inventoryDTOs.add(convertToDTO(inventory));
                        } else {
                            inventoryDTOs.add(createDefaultInventoryDTO(material, materialLocation));
                        }
                    }
                }
            }
            
            response.addData("inventory", inventoryDTOs);
            response.addData("totalCount", inventoryDTOs.size());
            
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Inventory retrieved successfully"
            );
            
        } catch (Exception e) {
            logger.error("Error retrieving inventory: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Error retrieving inventory: " + e.getMessage()
            );
        }
    }

    /**
     * Update stock for a single material
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse updateStock(Long materialId, Integer stockQuantity, BigDecimal price, String locationName) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Get material
            Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with ID: " + materialId));
            
            // Verify material belongs to current admin
            if (!material.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Access denied: Material does not belong to current admin"
                );
            }
            
            // Get location
            Location location;
            if (locationName != null && !locationName.trim().isEmpty()) {
                location = locationService.getLocationEntityByName(locationName.trim(), superAdmin);
            } else {
                // Use material's default location
                location = material.getLocation();
                if (location == null) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response,
                        AppConstants.ERRORCODE,
                        "Location is required"
                    );
                }
            }
            
            // Find or create inventory
            Inventory inventory = inventoryRepository
                .findByMaterialAndLocationAndVariantIsNullAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                    material, location, superAdmin.getSuperAdminId())
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setMaterial(material);
                    newInventory.setLocation(location);
                    newInventory.setSuperAdmin(superAdmin);
                    newInventory.setStockQuantity(0);
                    newInventory.setIsActive(true);
                    return newInventory;
                });
            
            // Update stock and price
            if (stockQuantity != null) {
                inventory.setStockQuantity(stockQuantity);
            }
            if (price != null) {
                inventory.setPrice(price);
            }
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            response.addData("inventory", convertToDTO(savedInventory));
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Stock updated successfully"
            );
            
        } catch (Exception e) {
            logger.error("Error updating stock: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Error updating stock: " + e.getMessage()
            );
        }
    }

    /**
     * Bulk update stock by SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse bulkUpdateStock(List<Map<String, Object>> updates) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            List<InventoryDTO> updatedInventories = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (Map<String, Object> update : updates) {
                try {
                    String sku = (String) update.get("sku");
                    String variantCode = (String) update.get("variantCode");
                    String locationName = (String) update.get("location");
                    Integer stockQuantity = update.get("stockQuantity") != null ? 
                        Integer.valueOf(update.get("stockQuantity").toString()) : null;
                    BigDecimal price = update.get("price") != null ? 
                        new BigDecimal(update.get("price").toString()) : null;
                    
                    if (sku == null || sku.trim().isEmpty()) {
                        failureCount++;
                        continue;
                    }
                    
                    // Find material by SKU
                    Material material = materialRepository.findBySku(sku)
                        .orElse(null);
                    
                    if (material == null || !material.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                        failureCount++;
                        continue;
                    }
                    
                    // Get location
                    if (locationName == null || locationName.trim().isEmpty()) {
                        failureCount++;
                        continue;
                    }
                    
                    Location location = locationService.getLocationEntityByName(locationName.trim(), superAdmin);
                    
                    Inventory inventory;
                    if (variantCode != null && !variantCode.trim().isEmpty()) {
                        // Variant-based inventory
                        MaterialVariant variant = materialVariantRepository.findByMaterialAndVariantCode(material, variantCode)
                            .orElse(null);
                        
                        if (variant == null) {
                            failureCount++;
                            continue;
                        }
                        
                        inventory = inventoryRepository
                            .findByMaterialAndVariantAndLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                                material, variant, location, superAdmin.getSuperAdminId())
                            .orElseGet(() -> {
                                Inventory newInventory = new Inventory();
                                newInventory.setMaterial(material);
                                newInventory.setVariant(variant);
                                newInventory.setLocation(location);
                                newInventory.setSuperAdmin(superAdmin);
                                newInventory.setStockQuantity(0);
                                newInventory.setIsActive(true);
                                return newInventory;
                            });
                    } else {
                        // Material-level inventory
                        inventory = inventoryRepository
                            .findByMaterialAndLocationAndVariantIsNullAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                                material, location, superAdmin.getSuperAdminId())
                            .orElseGet(() -> {
                                Inventory newInventory = new Inventory();
                                newInventory.setMaterial(material);
                                newInventory.setLocation(location);
                                newInventory.setSuperAdmin(superAdmin);
                                newInventory.setStockQuantity(0);
                                newInventory.setIsActive(true);
                                return newInventory;
                            });
                    }
                    
                    // Update stock and price
                    if (stockQuantity != null) {
                        inventory.setStockQuantity(stockQuantity);
                    }
                    if (price != null) {
                        inventory.setPrice(price);
                    }
                    
                    Inventory savedInventory = inventoryRepository.save(inventory);
                    updatedInventories.add(convertToDTO(savedInventory));
                    successCount++;
                    
                } catch (Exception e) {
                    logger.error("Error processing bulk update item: {}", e.getMessage(), e);
                    failureCount++;
                }
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("updatedInventories", updatedInventories);
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("totalProcessed", updates.size());
            
            response.addData("bulkUpdateResult", result);
            
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                String.format("Bulk update completed. Success: %d, Failed: %d", successCount, failureCount)
            );
            
        } catch (Exception e) {
            logger.error("Error in bulk stock update: {}", e.getMessage(), e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Error in bulk stock update: " + e.getMessage()
            );
        }
    }

    /**
     * Initialize inventory when material is created (stock = 0)
     */
    @Transactional(rollbackFor = Exception.class)
    public void initializeInventory(Material material, Location location, SuperAdmin superAdmin) {
        try {
            // Check if inventory already exists
            boolean exists = inventoryRepository.existsByMaterialAndLocationAndVariantIsNullAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                material, location, superAdmin.getSuperAdminId());
            
            if (!exists) {
                Inventory inventory = new Inventory();
                inventory.setMaterial(material);
                inventory.setLocation(location);
                inventory.setSuperAdmin(superAdmin);
                inventory.setStockQuantity(0);
                inventory.setPrice(null);
                inventory.setIsActive(true);
                inventoryRepository.save(inventory);
            }
        } catch (Exception e) {
            logger.error("Error initializing inventory for material {}: {}", material.getSku(), e.getMessage(), e);
            // Don't throw exception to prevent material creation failure
        }
    }

    /**
     * Create default inventory DTO for materials without inventory records
     */
    private InventoryDTO createDefaultInventoryDTO(Material material, Location location) {
        InventoryDTO dto = new InventoryDTO();
        dto.setInventoryId(null); // No inventory record yet
        dto.setMaterialId(material.getMaterialId());
        dto.setMaterialName(material.getMaterialName());
        dto.setSku(material.getSku());
        dto.setVariantId(null);
        dto.setVariantCode(null);
        dto.setLocationId(location.getLocationId());
        dto.setLocationName(location.getLocationName());
        dto.setStockQuantity(0); // Default stock
        dto.setPrice(null); // Default price
        dto.setIsActive(true);
        dto.setCreatedDate(null);
        dto.setModifiedDate(null);
        return dto;
    }

    /**
     * Convert Inventory entity to DTO
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setInventoryId(inventory.getInventoryId());
        dto.setMaterialId(inventory.getMaterial().getMaterialId());
        dto.setMaterialName(inventory.getMaterial().getMaterialName());
        dto.setSku(inventory.getMaterial().getSku());
        
        if (inventory.getVariant() != null) {
            dto.setVariantId(inventory.getVariant().getId());
            dto.setVariantCode(inventory.getVariant().getVariantCode());
        }
        
        dto.setLocationId(inventory.getLocation().getLocationId());
        dto.setLocationName(inventory.getLocation().getLocationName());
        dto.setStockQuantity(inventory.getStockQuantity());
        dto.setPrice(inventory.getPrice());
        dto.setIsActive(inventory.getIsActive());
        dto.setCreatedDate(inventory.getCreatedDate());
        dto.setModifiedDate(inventory.getModifiedDate());
        
        return dto;
    }
}
