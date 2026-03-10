package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ChannelCreateRequest;
import com.example.multimedia.file_upload_api.dto.ChannelUpdateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {
    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelService channelService;

    /**
     * Create a new channel
     */
    @PostMapping("/create")
    public ResponseEntity<?> createChannel(@RequestBody ChannelCreateRequest request) {
        try {
            logger.info("Creating channel with name: {}, code: {}, categories count: {}", 
                request != null ? request.getChannelName() : "null",
                request != null ? request.getChannelCode() : "null",
                request != null && request.getCategories() != null ? request.getCategories().size() : 0);
            
            ServiceResponse response = channelService.createChannel(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating channel: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to create channel: " + e.getMessage());
        }
    }

    /**
     * Get all channels for current user
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllChannels() {
        try {
            logger.info("Retrieving all channels for current user");
            ServiceResponse response = channelService.getAllChannels();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving channels: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve channels: " + e.getMessage());
        }
    }

    /**
     * Get channel by ID
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<?> getChannelById(@PathVariable Long channelId) {
        try {
            logger.info("Retrieving channel with ID: {}", channelId);
            ServiceResponse response = channelService.getChannelById(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving channel with ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve channel: " + e.getMessage());
        }
    }

    /**
     * Update channel
     */
    @PutMapping("/{channelId}")
    public ResponseEntity<?> updateChannel(@PathVariable Long channelId, @RequestBody ChannelUpdateRequest request) {
        try {
            logger.info("Updating channel with ID: {}", channelId);
            ServiceResponse response = channelService.updateChannel(channelId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating channel with ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update channel: " + e.getMessage());
        }
    }

    /**
     * Delete channel
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<?> deleteChannel(@PathVariable Long channelId) {
        try {
            logger.info("Deleting channel with ID: {}", channelId);
            ServiceResponse response = channelService.deleteChannel(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting channel with ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete channel: " + e.getMessage());
        }
    }

    /**
     * Toggle channel active status
     */
    @PatchMapping("/{channelId}/toggle-status")
    public ResponseEntity<?> toggleChannelStatus(@PathVariable Long channelId) {
        try {
            logger.info("Toggling status for channel with ID: {}", channelId);
            ServiceResponse response = channelService.toggleChannelStatus(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error toggling status for channel with ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to toggle channel status: " + e.getMessage());
        }
    }

    /**
     * Get all channels by Company ID
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getChannelsByCompanyId(@PathVariable Long companyId) {
        try {
            logger.info("Retrieving all channels for company ID: {}", companyId);
            ServiceResponse response = channelService.getChannelsByCompanyId(companyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving channels for company ID {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve channels: " + e.getMessage());
        }
    }

    /**
     * Get all categories by Channel ID
     */
    @GetMapping("/{channelId}/categories")
    public ResponseEntity<?> getCategoriesByChannelId(@PathVariable Long channelId) {
        try {
            logger.info("Retrieving all categories for channel ID: {}", channelId);
            ServiceResponse response = channelService.getCategoriesByChannelId(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving categories for channel ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve categories: " + e.getMessage());
        }
    }

    /**
     * Delete a specific category from a channel
     */
    @DeleteMapping("/{channelId}/categories/{categoryId}")
    public ResponseEntity<?> deleteCategoryFromChannel(@PathVariable Long channelId, @PathVariable Long categoryId) {
        try {
            logger.info("Deleting category ID: {} from channel ID: {}", categoryId, channelId);
            ServiceResponse response = channelService.deleteCategoryFromChannel(channelId, categoryId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting category ID: {} from channel ID: {}: {}", 
                categoryId, channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete category: " + e.getMessage());
        }
    }

    /**
     * Get all materials associated with a specific channel
     */
    @GetMapping("/{channelId}/materials")
    public ResponseEntity<?> getMaterialsByChannelId(@PathVariable Long channelId) {
        try {
            logger.info("Retrieving all materials for channel ID: {}", channelId);
            ServiceResponse response = channelService.getMaterialsByChannelId(channelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving materials for channel ID {}: {}", channelId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve materials for channel: " + e.getMessage());
        }
    }

}
