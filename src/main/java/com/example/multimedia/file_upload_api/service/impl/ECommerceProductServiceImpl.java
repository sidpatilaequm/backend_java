package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.FullProductCreateRequest;
import com.example.multimedia.file_upload_api.dto.ProductResponseDTO;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.ECommerceProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ECommerceProductServiceImpl implements ECommerceProductService {

    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;

    private final LocationRepository locationRepository;
    private final SuperAdminRepository superAdminRepository;

    @Override
    @Transactional
    public ProductResponseDTO createFullProduct(FullProductCreateRequest request) {
        Material material = request.getProduct();

        // Load relationships

        SuperAdmin admin = superAdminRepository.findById(request.getSuperAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));


        material.setSuperAdmin(admin);
        material.setLocation(location);

        Material savedMaterial = materialRepository.save(material);

        // Create initial inventory
        Inventory inventory = new Inventory();
        inventory.setMaterial(savedMaterial);
        inventory.setLocation(location);
        inventory.setSuperAdmin(admin);
        inventory.setAvailableQty(request.getInitialQty());
        inventory.setReservedQty(0.0);
        inventory.setPrice(request.getPrice());
        inventory.setIsActive(true);
        inventory.setStockQuantity(request.getInitialQty().intValue());
        inventoryRepository.save(inventory);

        return getProductDetails(savedMaterial.getMaterialId(), admin.getSuperAdminId());
    }

    @Override
    public ProductResponseDTO getProductDetails(Long productId, Long superAdminId) {
        Material material = materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(productId, superAdminId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductResponseDTO response = new ProductResponseDTO();
        response.setProduct(material);

        inventoryRepository.findByMaterialAndSuperAdmin_SuperAdminIdAndIsActiveTrue(material, superAdminId)
                .stream().findFirst().ifPresent(response::setInventory);

        // For simplicity, fetching all channel mappings for this material
        // In a real system, you might filter by company
        return response;
    }


    @Override
    public ProductResponseDTO getProductByChannel(Long productId, Long channelId, Long companyId) {
        ProductResponseDTO response = getProductDetails(productId, null); // Simplified

        return response;
    }
}
