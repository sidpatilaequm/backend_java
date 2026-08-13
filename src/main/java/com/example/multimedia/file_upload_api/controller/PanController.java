package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.PanSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.PanVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification/pan")
public class PanController {

    @Autowired
    private PanVerificationService panVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyPan(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "panNumber", required = false) String panNumber,
            @RequestParam(value = "companyId", required = false) Long companyId) {
        
        ServiceResponse response = panVerificationService.verifyPan(file, panNumber, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitPan(@RequestBody PanSubmitDTO dto) {
        ServiceResponse response = panVerificationService.submitPan(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getPan() {
        ServiceResponse response = panVerificationService.getPanDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
