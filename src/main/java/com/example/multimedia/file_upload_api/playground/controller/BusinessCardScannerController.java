package com.example.multimedia.file_upload_api.playground.controller;

import com.example.multimedia.file_upload_api.playground.service.BusinessCardScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/business-card")
public class BusinessCardScannerController {
    private static final Logger logger = LoggerFactory.getLogger(BusinessCardScannerController.class);
    
    private final BusinessCardScannerService scannerService;

    public BusinessCardScannerController(BusinessCardScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanBusinessCard(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Received file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            if (file.isEmpty()) {
                logger.warn("Received empty file");
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> result = scannerService.scanBusinessCard(file);
            logger.info("Successfully processed business card: {}", result);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error processing business card", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 