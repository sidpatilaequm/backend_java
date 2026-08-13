package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.SubcategoryDTO;
import com.example.multimedia.file_upload_api.dto.SubcategoryTreeDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.HierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoryManagementController {
    private final HierarchyService hierarchyService;

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@RequestBody SubcategoryDTO dto) {
        return ResponseEntity.ok(hierarchyService.createSubcategory(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ServiceResponse> bulkCreate(@RequestBody com.example.multimedia.file_upload_api.dto.SubcategoryBulkCreateRequest request) {
        return ResponseEntity.ok(hierarchyService.bulkCreateSubcategories(request));
    }

    @GetMapping("/tree/{categoryId}")
    public ResponseEntity<ServiceResponse> getTree(@PathVariable Long categoryId) {
        return ResponseEntity.ok(hierarchyService.getSubcategoryTree(categoryId));
    }

    @DeleteMapping("/{subcategoryId}")
    public ResponseEntity<ServiceResponse> delete(@PathVariable Long subcategoryId) {
        return ResponseEntity.ok(hierarchyService.deleteSubcategory(subcategoryId));
    }
}
