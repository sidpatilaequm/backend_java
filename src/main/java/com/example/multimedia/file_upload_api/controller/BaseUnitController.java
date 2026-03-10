package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.BaseUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/base-units")
public class BaseUnitController {

    @Autowired
    private BaseUnitService baseUnitService;

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllBaseUnits() {
        ServiceResponse response = baseUnitService.getAllBaseUnits();
        return ResponseEntity.ok(response);
    }
} 