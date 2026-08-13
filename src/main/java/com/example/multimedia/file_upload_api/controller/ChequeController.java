package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ChequeSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ChequeVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification/cheque")
public class ChequeController {

    @Autowired
    private ChequeVerificationService chequeVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyCheque(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "accountNumber", required = false) String accountNumber,
            @RequestParam(value = "ifscCode", required = false) String ifscCode,
            @RequestParam(value = "companyId", required = false) Long companyId) {
        
        ServiceResponse response = chequeVerificationService.verifyCheque(file, accountNumber, ifscCode, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitCheque(@RequestBody ChequeSubmitDTO dto) {
        ServiceResponse response = chequeVerificationService.submitCheque(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getCheque() {
        ServiceResponse response = chequeVerificationService.getChequeDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
