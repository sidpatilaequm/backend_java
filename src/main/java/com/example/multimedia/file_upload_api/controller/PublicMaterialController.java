package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.PublicMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicMaterialController {

    @Autowired
    private PublicMaterialService publicMaterialService;

    /**
     * Get material details (Public API - No authentication required)
     * Supports either:
     *  - /materials/{materialId}/{channelId}
     *  - /materials/{materialId}?channelId=123
     *  - /materials/{materialId} (no channelId)
     */
    @GetMapping({"/materials/{materialId}/{channelId}", "/materials/{materialId}"})
    public ResponseEntity<ServiceResponse> getMaterialDetails(
            @PathVariable String materialId,
            @PathVariable(required = false) String channelId,
            @RequestParam(value = "channelId", required = false) String channelIdQuery) {

        String effectiveChannelId = channelId != null ? channelId : channelIdQuery;
        ServiceResponse response = publicMaterialService.getMaterialDetailsWithChannel(materialId, effectiveChannelId);

        if (response.getStatus().equals("SUCCESS")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
