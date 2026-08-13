package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.service.MaterialExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class SAPMaterialController {

    private final MaterialExcelService materialExcelService;
    private final MaterialRepository materialRepository;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadMaterialMaster(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = materialExcelService.uploadMaterialMaster(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UploadResponse.builder()
                    .status("FAILED: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Material>> getAllMaterials() {
        return ResponseEntity.ok(materialRepository.findAll());
    }
}
