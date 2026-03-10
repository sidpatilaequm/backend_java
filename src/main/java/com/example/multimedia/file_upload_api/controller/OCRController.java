package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.CardDataDTO;
import com.example.multimedia.file_upload_api.service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin("*")  // For testing purposes
public class OCRController {

    @Autowired
    private OCRService ocrService;

    @PostMapping("/extract")
    public ResponseEntity<?> extractCardData(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a file");
            }
            
            CardDataDTO cardData = ocrService.extractCardData(file);
            return ResponseEntity.ok(cardData);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.badRequest()
                .body("Error processing image: " + e.getMessage());
        }
    }
} 