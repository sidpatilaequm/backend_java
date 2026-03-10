package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.CompanyCoverPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/company/cover-photos")
public class CompanyCoverPhotoController {
    
    private static final Logger logger = LoggerFactory.getLogger(CompanyCoverPhotoController.class);

    @Autowired
    private CompanyCoverPhotoService coverPhotoService;

    /**
     * Upload cover photo
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCoverPhoto(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading cover photo: {}", file.getOriginalFilename());
            ServiceResponse response = coverPhotoService.uploadCoverPhoto(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error uploading cover photo: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Failed to upload cover photo: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get all cover photos for company
     */
    @GetMapping("/all")
    public ResponseEntity<?> getCompanyCoverPhotos() {
        try {
            logger.info("Getting all cover photos for company");
            ServiceResponse response = coverPhotoService.getCompanyCoverPhotos();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting cover photos: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Failed to retrieve cover photos: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get primary cover photo for company
     */
    @GetMapping("/primary")
    public ResponseEntity<?> getPrimaryCoverPhoto() {
        try {
            logger.info("Getting primary cover photo for company");
            ServiceResponse response = coverPhotoService.getPrimaryCoverPhoto();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting primary cover photo: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Failed to retrieve primary cover photo: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
