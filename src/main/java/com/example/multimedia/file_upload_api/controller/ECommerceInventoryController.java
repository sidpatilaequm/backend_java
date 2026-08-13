package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.service.ECommerceInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class ECommerceInventoryController {

    private final ECommerceInventoryService eCommerceInventoryService;

    @PostMapping("/update")
    public ResponseEntity<String> updateInventory(
            @RequestParam Long productId,
            @RequestParam Long locationId,
            @RequestParam Long superAdminId,
            @RequestParam Double availableQty,
            @RequestParam Double reservedQty) {

        eCommerceInventoryService.updateInventory(productId, locationId, superAdminId, availableQty, reservedQty);
        return ResponseEntity.ok("Inventory updated successfully");
    }
}
