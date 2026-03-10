package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.FinancialTermsCustomerDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.FinancialTermsCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-terms-customer")
public class FinancialTermsCustomerController {

    @Autowired
    private FinancialTermsCustomerService financialTermsCustomerService;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveFinancialTermsCustomer(@RequestBody FinancialTermsCustomerDTO dto) {
        ServiceResponse response = financialTermsCustomerService.saveFinancialTermsCustomer(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<ServiceResponse> getFinancialTermsCustomer(
            @RequestParam String gstinNumber,
            @RequestParam String authKey) {
        ServiceResponse response = financialTermsCustomerService.getFinancialTermsCustomerByGstAndAuthKey(gstinNumber, authKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceResponse> updateFinancialTermsCustomer(@RequestBody FinancialTermsCustomerDTO dto) {
        ServiceResponse response = financialTermsCustomerService.updateFinancialTermsCustomer(dto);
        return ResponseEntity.ok(response);
    }
} 