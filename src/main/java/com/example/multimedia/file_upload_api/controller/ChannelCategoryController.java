package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ChannelCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ChannelCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/channel-categories")
@RequiredArgsConstructor
public class ChannelCategoryController {

    private final ChannelCategoryService categoryService;

    @PostMapping("/{channelId}")
    public ResponseEntity<ServiceResponse> createCategory(
            @PathVariable Long channelId, 
            @RequestParam Long companyId,
            @RequestBody ChannelCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto, channelId, companyId));
    }

    @GetMapping("/tree/{channelId}")
    public ResponseEntity<ServiceResponse> getTree(
            @PathVariable Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(categoryService.getCategoryTree(channelId, companyId));
    }

    @GetMapping("/leaf/{channelId}")
    public ResponseEntity<ServiceResponse> getLeafCategories(
            @PathVariable Long channelId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(categoryService.getLeafCategories(channelId, companyId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ServiceResponse> getByParent(
            @PathVariable Long parentId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(categoryService.getCategoriesByParent(parentId, companyId));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ServiceResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestParam Long companyId,
            @RequestBody ChannelCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, dto, companyId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ServiceResponse> deleteCategory(
            @PathVariable Long categoryId,
            @RequestParam Long companyId) {
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId, companyId));
    }
}
