package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.entity.Inventory;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.repository.InventoryRepository;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
import com.example.multimedia.file_upload_api.service.ECommerceInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ECommerceInventoryServiceImpl implements ECommerceInventoryService {

    private final InventoryRepository inventoryRepository;
    private final MaterialRepository materialRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public void updateInventory(Long productId, Long locationId, Long superAdminId, Double availableQty,
            Double reservedQty) {
        Material material = materialRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        Inventory inventory = inventoryRepository
                .findByMaterialAndLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
                        material, location, superAdminId)
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setMaterial(material);
                    newInv.setLocation(location);
                    newInv.setSuperAdmin(material.getSuperAdmin());
                    newInv.setIsActive(true);
                    return newInv;
                });

        inventory.setAvailableQty(availableQty);
        inventory.setReservedQty(reservedQty);
        inventory.setStockQuantity(availableQty.intValue()); // Sync with existing stockQuantity if needed

        inventoryRepository.save(inventory);
    }
}
