package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ItemCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item-categories")
public class ItemCategoryController {

    @Autowired
    private ItemCategoryService itemCategoryService;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveItemCategory(@RequestBody ItemCategoryDTO dto) {
        ServiceResponse response = itemCategoryService.saveItemCategory(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceResponse> updateItemCategory(@RequestBody ItemCategoryDTO dto) {
        ServiceResponse response = itemCategoryService.updateItemCategory(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllItemCategories() {
        ServiceResponse response = itemCategoryService.getAllItemCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getItemCategoryById(@PathVariable Long id) {
        ServiceResponse response = itemCategoryService.getItemCategoryById(id);
        return ResponseEntity.ok(response);
    }
} 