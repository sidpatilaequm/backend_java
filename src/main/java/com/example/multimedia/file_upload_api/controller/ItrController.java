package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ItrSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.ItrVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification/itr")
public class ItrController {

    @Autowired
    private ItrVerificationService itrVerificationService;

    @PostMapping
    public ResponseEntity<ServiceResponse> verifyItr(
            @RequestParam(required = false) String pan,
            @RequestParam(required = false) String birthOrIncorporatedDate,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long companyId) {
        
        ServiceResponse response = itrVerificationService.verifyItr(pan, birthOrIncorporatedDate, name, companyId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ServiceResponse> submitItr(@RequestBody ItrSubmitDTO dto) {
        ServiceResponse response = itrVerificationService.submitItr(dto);
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getItr() {
        ServiceResponse response = itrVerificationService.getItrDetails();
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
