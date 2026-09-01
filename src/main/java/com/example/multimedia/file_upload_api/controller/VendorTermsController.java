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

    @Autowired
    private com.example.multimedia.file_upload_api.service.AuditLogService auditLogService;

    private SuperAdmin validateCurrentSuperAdmin() {
        try {
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            if (currentSuperAdmin == null) {
                throw new RuntimeException("Super Admin not found in security context");
            }
            // Was: userService.isUserActive(currentSuperAdmin.getSuperAdminId()) — isUserActive
            // looks up UserAuthentication by UserDetail.userId, a different ID space entirely from
            // SuperAdmin.superAdminId, so this either spuriously threw or coincidentally matched
            // an unrelated user with the same numeric id. getCurrentSuperAdmin() already only
            // succeeds for a real, authenticated super admin, which is definitionally what this
            // check exists to confirm — nothing more to check against the wrong table.
            return currentSuperAdmin;
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

        VendorTermsResponseDTO result = vendorTermsService.createVendorTerms(vendorTermsDTO);
        auditLogService.recordGeneric("VENDOR_TERMS_CREATED", "Company #" + companyId,
                changedFileFields(paymentTermsFile, incotermsFile, deliveryTermsFile));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorTermsResponseDTO> getVendorTerms(@PathVariable Long id) {
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();
        return ResponseEntity.ok(vendorTermsService.getVendorTerms(id, currentSuperAdmin.getSuperAdminId()));
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
        
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();

        VendorTermsDTO vendorTermsDTO = new VendorTermsDTO();
        vendorTermsDTO.setPaymentTermsFile(paymentTermsFile);
        vendorTermsDTO.setIncotermsFile(incotermsFile);
        vendorTermsDTO.setDeliveryTermsFile(deliveryTermsFile);

        VendorTermsResponseDTO result = vendorTermsService.updateVendorTerms(id, vendorTermsDTO, currentSuperAdmin.getSuperAdminId());
        auditLogService.recordGeneric("VENDOR_TERMS_UPDATED", "Vendor terms #" + id,
                changedFileFields(paymentTermsFile, incotermsFile, deliveryTermsFile));
        return ResponseEntity.ok(result);
    }

    // Records which of the three file slots were actually re-uploaded on this call — no values
    // (they're binary files), just field names, same treatment platform credentials get.
    private java.util.List<com.example.multimedia.file_upload_api.service.AuditLogService.FieldChange> changedFileFields(
            MultipartFile paymentTermsFile, MultipartFile incotermsFile, MultipartFile deliveryTermsFile) {
        java.util.List<com.example.multimedia.file_upload_api.service.AuditLogService.FieldChange> changes = new java.util.ArrayList<>();
        if (paymentTermsFile != null && !paymentTermsFile.isEmpty()) {
            changes.add(new com.example.multimedia.file_upload_api.service.AuditLogService.FieldChange("paymentTermsFile", null, null));
        }
        if (incotermsFile != null && !incotermsFile.isEmpty()) {
            changes.add(new com.example.multimedia.file_upload_api.service.AuditLogService.FieldChange("incotermsFile", null, null));
        }
        if (deliveryTermsFile != null && !deliveryTermsFile.isEmpty()) {
            changes.add(new com.example.multimedia.file_upload_api.service.AuditLogService.FieldChange("deliveryTermsFile", null, null));
        }
        return changes;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendorTerms(@PathVariable Long id) {
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();
        vendorTermsService.deleteVendorTerms(id, currentSuperAdmin.getSuperAdminId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/payment-terms")
    public ResponseEntity<byte[]> getPaymentTermsFile(@PathVariable Long id) {
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();
        return vendorTermsService.getPaymentTermsFile(id, currentSuperAdmin.getSuperAdminId());
    }

    @GetMapping("/{id}/incoterms")
    public ResponseEntity<byte[]> getIncotermsFile(@PathVariable Long id) {
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();
        return vendorTermsService.getIncotermsFile(id, currentSuperAdmin.getSuperAdminId());
    }

    @GetMapping("/{id}/delivery-terms")
    public ResponseEntity<byte[]> getDeliveryTermsFile(@PathVariable Long id) {
        SuperAdmin currentSuperAdmin = validateCurrentSuperAdmin();
        return vendorTermsService.getDeliveryTermsFile(id, currentSuperAdmin.getSuperAdminId());
    }
} 