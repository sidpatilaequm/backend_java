package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.AttributeDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.AttributeType;
import com.example.multimedia.file_upload_api.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attributes")
public class AttributeController {
    
    @Autowired
    private AttributeService attributeService;

    @PostMapping
    public ResponseEntity<ServiceResponse> createAttribute(@RequestBody AttributeDTO attributeDTO) {
        ServiceResponse response = attributeService.createAttribute(attributeDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ServiceResponse> createAttributes(@RequestBody List<AttributeDTO> attributeDTOs) {
        ServiceResponse response = attributeService.createAttributes(attributeDTOs);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateAttribute(
            @PathVariable Long id,
            @RequestBody AttributeDTO attributeDTO) {
        ServiceResponse response = attributeService.updateAttribute(id, attributeDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllAttributes() {
        ServiceResponse response = attributeService.getAllAttributes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<ServiceResponse> getAttributesByType(@PathVariable AttributeType type) {
        ServiceResponse response = attributeService.getAttributesByType(type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getAttributeById(@PathVariable Long id) {
        ServiceResponse response = attributeService.getAttributeById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteAttribute(@PathVariable Long id) {
        ServiceResponse response = attributeService.deleteAttribute(id);
        return ResponseEntity.ok(response);
    }
} 