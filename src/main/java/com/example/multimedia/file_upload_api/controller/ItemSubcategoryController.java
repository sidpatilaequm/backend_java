package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ItemSubcategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ItemSubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item-subcategories")
public class ItemSubcategoryController {

    @Autowired
    private ItemSubcategoryService itemSubcategoryService;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveItemSubcategory(@RequestBody ItemSubcategoryDTO dto) {
        ServiceResponse response = itemSubcategoryService.saveItemSubcategory(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceResponse> updateItemSubcategory(@RequestBody ItemSubcategoryDTO dto) {
        ServiceResponse response = itemSubcategoryService.updateItemSubcategory(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ServiceResponse> getAllItemSubcategories() {
        ServiceResponse response = itemSubcategoryService.getAllItemSubcategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ServiceResponse> getItemSubcategoriesByCategoryCode(@PathVariable String categoryCode) {
        ServiceResponse response = itemSubcategoryService.getItemSubcategoriesByCategoryCode(categoryCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getItemSubcategoryById(@PathVariable Long id) {
        ServiceResponse response = itemSubcategoryService.getItemSubcategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-category-details")
    public ResponseEntity<ServiceResponse> getAllSubcategoriesWithCategoryDetails() {
        ServiceResponse response = itemSubcategoryService.getAllSubcategoriesWithCategoryDetails();
        return ResponseEntity.ok(response);
    }
}