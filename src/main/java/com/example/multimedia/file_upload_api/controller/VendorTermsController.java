package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.VendorTermsDTO;
import com.example.multimedia.file_upload_api.dto.VendorTermsResponseDTO;
import com.example.multimedia.file_upload_api.entity.VendorTerms;
import com.example.multimedia.file_upload_api.repository.VendorTermsRepository;
import com.example.multimedia.file_upload_api.service.VendorTermsService;
import com.example.multimedia.file_upload_api.service.UserService;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vendor_terms")
public class VendorTermsController {

    @Autowired
    private VendorTermsService vendorTermsService;

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUserService currentUserService;

    private void validateCurrentSuperAdmin() {
        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            if (currentSuperAdmin == null) {
                throw new RuntimeException("Super Admin not found in security context");
            }
            if (!userService.isUserActive(currentSuperAdmin.getSuperAdminId())) {
                throw new RuntimeException("Super Admin account is not active");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid Super Admin: " + e.getMessage());
        }
    }

    private void validateVendor(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        if (!userService.isVendor(userId)) {
            throw new RuntimeException("User is not a Vendor");
        }
        if (!userService.isUserActive(userId)) {
            throw new RuntimeException("Vendor account is not active");
        }
    }

    @PostMapping
    public ResponseEntity<VendorTermsResponseDTO> createVendorTerms(
            @RequestParam("paymentTermsFile") MultipartFile paymentTermsFile,
            @RequestParam("incotermsFile") MultipartFile incotermsFile,
            @RequestParam("deliveryTermsFile") MultipartFile deliveryTermsFile,
            @RequestParam("userId") Long userId,
            @RequestParam("companyId") Long companyId) {
        
        validateCurrentSuperAdmin();
        validateVendor(userId);
        
        VendorTermsDTO vendorTermsDTO = new VendorTermsDTO();
        vendorTermsDTO.setPaymentTermsFile(paymentTermsFile);
        vendorTermsDTO.setIncotermsFile(incotermsFile);
        vendorTermsDTO.setDeliveryTermsFile(deliveryTermsFile);
        vendorTermsDTO.setUserId(userId);
        vendorTermsDTO.setCompanyId(companyId);
        
        return new ResponseEntity<>(vendorTermsService.createVendorTerms(vendorTermsDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorTermsResponseDTO> getVendorTerms(@PathVariable Long id) {
        validateCurrentSuperAdmin();
        return ResponseEntity.ok(vendorTermsService.getVendorTerms(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VendorTermsResponseDTO>> getVendorTermsByUser(@PathVariable Long userId) {
        validateCurrentSuperAdmin();
        validateVendor(userId);
        return ResponseEntity.ok(vendorTermsService.getVendorTermsByUser(userId));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<VendorTermsResponseDTO>> getVendorTermsByCompany(@PathVariable Long companyId) {
        validateCurrentSuperAdmin();
        return ResponseEntity.ok(vendorTermsService.getVendorTermsByCompany(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorTermsResponseDTO> updateVendorTerms(
            @PathVariable Long id,
            @RequestParam(value = "paymentTermsFile", required = false) MultipartFile paymentTermsFile,
            @RequestParam(value = "incotermsFile", required = false) MultipartFile incotermsFile,
            @RequestParam(value = "deliveryTermsFile", required = false) MultipartFile deliveryTermsFile) {
        
        validateCurrentSuperAdmin();
        
        VendorTermsDTO vendorTermsDTO = new VendorTermsDTO();
        vendorTermsDTO.setPaymentTermsFile(paymentTermsFile);
        vendorTermsDTO.setIncotermsFile(incotermsFile);
        vendorTermsDTO.setDeliveryTermsFile(deliveryTermsFile);
        
        return ResponseEntity.ok(vendorTermsService.updateVendorTerms(id, vendorTermsDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendorTerms(@PathVariable Long id) {
        validateCurrentSuperAdmin();
        vendorTermsService.deleteVendorTerms(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/payment-terms")
    public ResponseEntity<byte[]> getPaymentTermsFile(@PathVariable Long id) {
        validateCurrentSuperAdmin();
        return vendorTermsService.getPaymentTermsFile(id);
    }

    @GetMapping("/{id}/incoterms")
    public ResponseEntity<byte[]> getIncotermsFile(@PathVariable Long id) {
        validateCurrentSuperAdmin();
        return vendorTermsService.getIncotermsFile(id);
    }

    @GetMapping("/{id}/delivery-terms")
    public ResponseEntity<byte[]> getDeliveryTermsFile(@PathVariable Long id) {
        validateCurrentSuperAdmin();
        return vendorTermsService.getDeliveryTermsFile(id);
    }
} 