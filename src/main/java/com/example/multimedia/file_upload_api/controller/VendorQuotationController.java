package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.VendorQuotationRequest;
import com.example.multimedia.file_upload_api.dto.VendorQuotationResponse;
import com.example.multimedia.file_upload_api.service.VendorQuotationService;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/quotations")
public class VendorQuotationController {

    @Autowired
    private VendorQuotationService quotationService;

    @Autowired
    private SecurityContextUtils securityContextUtils;

    @PostMapping
    public ResponseEntity<VendorQuotationResponse> createQuotation(@RequestBody VendorQuotationRequest request) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.createQuotation(request, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VendorQuotationResponse>> getAllMyQuotations() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        List<VendorQuotationResponse> responses = quotationService.getQuotationsByVendorId(vendorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorQuotationResponse> getMyQuotationById(@PathVariable Long id) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.getQuotationById(id, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{quotationNumber}")
    public ResponseEntity<VendorQuotationResponse> getMyQuotationByNumber(@PathVariable String quotationNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.getQuotationByQuotationNumberAndVendorId(quotationNumber, vendorId);
        return ResponseEntity.ok(response);
    }
}
