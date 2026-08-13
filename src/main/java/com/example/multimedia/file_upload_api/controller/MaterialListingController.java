package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MaterialListingDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.MaterialListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/material-listings")
@RequiredArgsConstructor
public class MaterialListingController {

    private final MaterialListingService listingService;

    @PostMapping
    public ResponseEntity<ServiceResponse> createListing(@RequestBody MaterialListingDTO dto) {
        return ResponseEntity.ok(listingService.createListing(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceResponse> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(listingService.updateListingStatus(id, status, companyId));
    }

    @GetMapping("/material/{materialId}")
    public ResponseEntity<ServiceResponse> getByMaterial(
            @PathVariable Long materialId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(listingService.getListingsByMaterial(materialId, companyId));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ServiceResponse> getByChannel(
            @PathVariable Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(listingService.getListingsByChannel(channelId, companyId));
    }
}
