package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.GstSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.GstVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification/gst")
public class GstController {

    @Autowired
    private GstVerificationService gstVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyGst(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "gstin", required = false) String gstin,
            @RequestParam(value = "companyId", required = false) Long companyId) {
        
        ServiceResponse response = gstVerificationService.verifyGst(file, gstin, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitGst(@RequestBody GstSubmitDTO dto) {
        ServiceResponse response = gstVerificationService.submitGst(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getGst() {
        // companyId is intentionally NOT accepted as a param.
        // The company is resolved from the authenticated user's JWT token to prevent data leakage.
        ServiceResponse response = gstVerificationService.getGstDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
