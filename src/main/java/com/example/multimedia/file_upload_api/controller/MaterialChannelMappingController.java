package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MaterialChannelMappingRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.MaterialChannelMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/materials")
public class MaterialChannelMappingController {
    private static final Logger logger = LoggerFactory.getLogger(MaterialChannelMappingController.class);

    @Autowired
    private MaterialChannelMappingService mappingService;

    /**
     * Bulk upsert material channel mappings
     */
    @PostMapping("/{materialId}/mappings")
    public ResponseEntity<?> upsertMappings(@PathVariable Long materialId, 
                                          @RequestBody MaterialChannelMappingRequest request) {
        try {
            logger.info("Upserting mappings for material ID: {}, mappings count: {}", 
                materialId, request != null && request.getMappings() != null ? request.getMappings().size() : 0);
            
            // Set material ID from path variable
            if (request != null) {
                request.setMaterialId(materialId);
            }
            
            ServiceResponse response = mappingService.upsertMappings(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error upserting mappings for material ID {}: {}", materialId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to upsert mappings: " + e.getMessage());
        }
    }

    /**
     * Get all mappings for a material
     */
    @GetMapping("/{materialId}/mappings")
    public ResponseEntity<?> getMappingsByMaterialId(@PathVariable Long materialId) {
        try {
            logger.info("Retrieving mappings for material ID: {}", materialId);
            ServiceResponse response = mappingService.getMappingsByMaterialId(materialId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving mappings for material ID {}: {}", materialId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve mappings: " + e.getMessage());
        }
    }

    /**
     * Delete a specific mapping
     */
    @DeleteMapping("/{materialId}/mappings/{channelId}")
    public ResponseEntity<?> deleteMapping(@PathVariable Long materialId, @PathVariable Long channelId) {
        try {
            logger.info("Deleting mapping for material ID: {} and channel ID: {}", materialId, channelId);
            ServiceResponse response = mappingService.deleteMapping(materialId, channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting mapping for material ID {} and channel ID {}: {}", 
                materialId, channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete mapping: " + e.getMessage());
        }
    }

    /**
     * Delete all material-channel mappings for a specific channel
     */
    @DeleteMapping("/mappings/channel/{channelId}")
    public ResponseEntity<?> deleteAllMappingsByChannelId(@PathVariable Long channelId) {
        try {
            logger.info("Deleting all material-channel mappings for channel ID: {}", channelId);
            ServiceResponse response = mappingService.deleteAllMappingsByChannelId(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting all mappings for channel ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete all mappings for channel: " + e.getMessage());
        }
    }
}
