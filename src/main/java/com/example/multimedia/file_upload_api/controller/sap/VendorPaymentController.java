package com.example.multimedia.file_upload_api.controller.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.VendorPayment;
import com.example.multimedia.file_upload_api.service.sap.VendorPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class VendorPaymentController {

    @Autowired
    private VendorPaymentService service;

    // --- VENDOR APIs ---

    @PostMapping("/api/payments/sync")
    public ResponseEntity<SapSyncResponse> syncPayments() {
        return ResponseEntity.ok(service.syncVendorPayments());
    }

    @GetMapping("/api/payments/vendor/list")
    public ResponseEntity<List<VendorPayment>> getVendorPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.getVendorPayments(status, method, from, to));
    }

    @GetMapping("/api/payments/vendor/{docNo}")
    public ResponseEntity<VendorPayment> getVendorPaymentDetails(@PathVariable String docNo) {
        return ResponseEntity.ok(service.getVendorPaymentDetails(docNo));
    }

    // --- ADMIN APIs ---

    @GetMapping("/api/admin/payments/list")
    public ResponseEntity<List<VendorPayment>> getAdminPayments() {
        return ResponseEntity.ok(service.getAdminPayments());
    }

    @GetMapping("/api/admin/payments/{vendorId}")
    public ResponseEntity<List<VendorPayment>> getAdminPaymentsForVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(service.getAdminPaymentsForVendor(vendorId));
    }
}
