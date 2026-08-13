package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/material-stock")
    public ResponseEntity<List<Map<String, Object>>> getMaterialStock() {
        return ResponseEntity.ok(reportService.getMaterialStock());
    }

    @GetMapping("/credit-notes")
    public ResponseEntity<List<Map<String, Object>>> getCreditNotes() {
        return ResponseEntity.ok(reportService.getCreditNotes());
    }

    @GetMapping("/vendor-returns")
    public ResponseEntity<List<Map<String, Object>>> getVendorReturns() {
        return ResponseEntity.ok(reportService.getVendorReturns());
    }

    @GetMapping("/vendor-payments")
    public ResponseEntity<List<Map<String, Object>>> getVendorPayments() {
        return ResponseEntity.ok(reportService.getVendorPayments());
    }
}
