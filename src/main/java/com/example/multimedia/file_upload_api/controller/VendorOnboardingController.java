package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.VendorOnboardingRequestDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.VendorOnboardingService;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
public class VendorOnboardingController {

    @Autowired
    private VendorOnboardingService vendorOnboardingService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Step 1: Public endpoint for vendor to submit initial onboarding request details.
     */
    @PostMapping("/api/public/registration/onboarding-request")
    public ResponseEntity<ServiceResponse> createOnboardingRequest(
            @Valid @RequestBody VendorOnboardingRequestDTO dto) {
        ServiceResponse response = vendorOnboardingService.createOnboardingRequest(dto);
        if ("500".equals(response.getErrorCode())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Public endpoint to fetch onboarding details by token.
     * This will be used in Step 2 to pre-fill vendor info.
     */
    @GetMapping("/api/public/registration/onboarding-details")
    public ResponseEntity<ServiceResponse> getOnboardingDetails(@RequestParam("token") String token) {
        ServiceResponse response = vendorOnboardingService.getOnboardingDetailsByToken(token);
        if ("500".equals(response.getErrorCode())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint: Retrieve pending onboarding requests for the logged-in admin.
     */
    @GetMapping("/api/vendor-onboarding/requests")
    public ResponseEntity<ServiceResponse> getPendingRequests() {
        try {
            Long adminId = currentUserService.getCurrentSuperAdminId();
            ServiceResponse response = vendorOnboardingService.getPendingRequests(adminId);
            if ("500".equals(response.getErrorCode())) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setErrorCode("500");
            errorResponse.setStatus("500");
            errorResponse.setStatusMsg("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * Admin endpoint: Generate an onboarding completion link/token for a pending request.
     */
    @PostMapping("/api/vendor-onboarding/generate-link/{userId}")
    public ResponseEntity<ServiceResponse> generateOnboardingLink(@PathVariable("userId") Long userId) {
        try {
            Long adminId = currentUserService.getCurrentSuperAdminId();
            ServiceResponse response = vendorOnboardingService.generateOnboardingLink(userId, adminId);
            if ("500".equals(response.getErrorCode())) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setErrorCode("500");
            errorResponse.setStatus("500");
            errorResponse.setStatusMsg("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}
