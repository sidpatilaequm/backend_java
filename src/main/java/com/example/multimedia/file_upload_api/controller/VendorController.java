package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.CompanyDetailsDTO;
import com.example.multimedia.file_upload_api.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/all")
    public ResponseEntity<ServiceResponse> getAllVendors() {
        ServiceResponse response = vendorService.getAllVendors();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getVendorById(@PathVariable Long userId) {
        return ResponseEntity.ok(vendorService.getVendorById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ServiceResponse> updateVendorDetails(
            @PathVariable Long userId,
            @RequestBody CompanyDetailsDTO vendorDetails) {
        ServiceResponse response = vendorService.updateVendorDetails(userId, vendorDetails);
        return ResponseEntity.ok(response);
    }
} 