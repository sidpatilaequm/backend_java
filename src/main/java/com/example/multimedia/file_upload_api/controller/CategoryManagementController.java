package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.CategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.HierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryManagementController {
    private final HierarchyService hierarchyService;

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(hierarchyService.createCategory(dto));
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllForCurrentCompany() {
        return ResponseEntity.ok(hierarchyService.getCategoriesForCurrentCompany());
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ServiceResponse> delete(@PathVariable Long categoryId) {
        return ResponseEntity.ok(hierarchyService.deleteCategory(categoryId));
    }
}
