package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.CompanySubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.CompanyVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification/company")
public class CompanyVerificationController {

    @Autowired
    private CompanyVerificationService companyVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyCompany(
            @RequestParam(required = false) String reg,
            @RequestParam(required = false) Long companyId) {
        
        ServiceResponse response = companyVerificationService.verifyCompanyRegistration(reg, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitCompany(@RequestBody CompanySubmitDTO dto) {
        ServiceResponse response = companyVerificationService.submitCompany(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getCompany() {
        ServiceResponse response = companyVerificationService.getCompanyRegistrationDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
