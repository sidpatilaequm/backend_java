package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MsmeSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.MsmeVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification/msme")
public class MsmeController {

    @Autowired
    private MsmeVerificationService msmeVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyMsme(
            @RequestParam(required = false) String udyamNumber,
            @RequestParam(required = false) Long companyId) {
        
        ServiceResponse response = msmeVerificationService.verifyMsme(udyamNumber, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitMsme(@RequestBody MsmeSubmitDTO dto) {
        ServiceResponse response = msmeVerificationService.submitMsme(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getMsme() {
        ServiceResponse response = msmeVerificationService.getMsmeDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
