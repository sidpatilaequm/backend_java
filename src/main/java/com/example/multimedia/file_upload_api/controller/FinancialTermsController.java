package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.FinancialTermsDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.FinancialTermsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:8000", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/financial-terms")
public class FinancialTermsController {

    @Autowired
    private FinancialTermsService financialTermsService;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveFinancialTerms(@RequestBody FinancialTermsDTO dto) {
        ServiceResponse response = financialTermsService.saveFinancialTerms(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceResponse> updateFinancialTerms(@RequestBody FinancialTermsDTO dto) {
        ServiceResponse response = financialTermsService.updateFinancialTerms(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<ServiceResponse> getFinancialTerms(
            @RequestParam String gstinNumber,
            @RequestParam String authKey) {
        ServiceResponse response = financialTermsService.getFinancialTermsByGstAndAuthKey(gstinNumber, authKey);
        return ResponseEntity.ok(response);
    }
} 