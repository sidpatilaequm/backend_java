package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.CategoryMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category-channel-mappings")
@RequiredArgsConstructor
public class CategoryMappingController {

    private final CategoryMappingService mappingService;

    @PostMapping
    public ResponseEntity<ServiceResponse> mapCategories(
            @RequestParam Long internalCategoryId,
            @RequestParam Long channelCategoryId,
            @RequestParam Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(mappingService.mapCategories(internalCategoryId, channelCategoryId, channelId, companyId));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ServiceResponse> getMappings(
            @PathVariable Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(mappingService.getMappingsByChannel(channelId, companyId));
    }
}
