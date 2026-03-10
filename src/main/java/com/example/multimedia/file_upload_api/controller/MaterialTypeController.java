package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.MaterialTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/material-types")
public class MaterialTypeController {

    @Autowired
    private MaterialTypeService materialTypeService;

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllMaterialTypes() {
        ServiceResponse response = materialTypeService.getAllMaterialTypes();
        return ResponseEntity.ok(response);
    }
}
