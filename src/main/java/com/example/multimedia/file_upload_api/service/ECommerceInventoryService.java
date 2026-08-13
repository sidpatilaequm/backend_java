package com.example.multimedia.file_upload_api.service;

public interface ECommerceInventoryService {
    void updateInventory(Long productId, Long locationId, Long superAdminId, Double availableQty, Double reservedQty);
}
