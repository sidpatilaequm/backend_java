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
    public ResponseEntity<VendorQuotationResponse> createQuotation(
            @RequestBody VendorQuotationRequest request,
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam) {
        Long vendorId = vendorIdParam != null ? vendorIdParam : securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.createQuotation(request, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VendorQuotationResponse>> getAllMyQuotations(
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam,
            @RequestParam(name = "company_code", required = false) String companyCode) {
        Long vendorId = vendorIdParam != null ? vendorIdParam : securityContextUtils.getCurrentVendorId();
        List<VendorQuotationResponse> responses = quotationService.getQuotationsByVendorId(vendorId, companyCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorQuotationResponse> getMyQuotationById(
            @PathVariable Long id,
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam) {
        Long vendorId = vendorIdParam != null ? vendorIdParam : securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.getQuotationById(id, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{quotationNumber}")
    public ResponseEntity<VendorQuotationResponse> getMyQuotationByNumber(
            @PathVariable String quotationNumber,
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam) {
        Long vendorId = vendorIdParam != null ? vendorIdParam : securityContextUtils.getCurrentVendorId();
        VendorQuotationResponse response = quotationService.getQuotationByQuotationNumberAndVendorId(quotationNumber, vendorId);
        return ResponseEntity.ok(response);
    }
}
