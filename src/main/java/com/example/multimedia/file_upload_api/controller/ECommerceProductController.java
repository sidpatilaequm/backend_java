package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.FullProductCreateRequest;
import com.example.multimedia.file_upload_api.dto.ProductResponseDTO;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.service.ECommerceProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ECommerceProductController {

    private final ECommerceProductService eCommerceProductService;

    @PostMapping("/full-create")
    public ResponseEntity<ProductResponseDTO> createFull(@RequestBody FullProductCreateRequest request) {
        return ResponseEntity.ok(eCommerceProductService.createFullProduct(request));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<ProductResponseDTO> getDetails(@PathVariable Long id, @RequestParam Long adminId) {
        return ResponseEntity.ok(eCommerceProductService.getProductDetails(id, adminId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Material>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(eCommerceProductService.getProductsByCategory(categoryId));
    }

    @GetMapping("/{id}/channel/{channelId}")
    public ResponseEntity<ProductResponseDTO> getByChannel(
            @PathVariable Long id,
            @PathVariable Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(eCommerceProductService.getProductByChannel(id, channelId, companyId));
    }
}
