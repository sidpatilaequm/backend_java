package com.example.multimedia.file_upload_api.playground.controller;

import com.example.multimedia.file_upload_api.playground.service.GSTInsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playground/gst")
public class GSTInsightsController {

    @Autowired
    private GSTInsightsService gstInsightsService;

    @GetMapping("/details/gst/{gstNumber}")
    public ResponseEntity<?> getGSTDetailsByGSTNumber(@PathVariable String gstNumber) {
        return ResponseEntity.ok(gstInsightsService.getGSTDetailsByGSTNumber(gstNumber));
    }

    @GetMapping("/details/pan/{panNumber}")
    public ResponseEntity<?> getGSTDetailsByPAN(@PathVariable String panNumber) {
        return ResponseEntity.ok(gstInsightsService.getGSTDetailsByPAN(panNumber));
    }

    @GetMapping("/details/company/{companyName}")
    public ResponseEntity<?> getGSTDetailsByCompanyName(@PathVariable String companyName) {
        return ResponseEntity.ok(gstInsightsService.getGSTDetailsByCompanyName(companyName));
    }

    @GetMapping("/validate/{gstNumber}")
    public ResponseEntity<?> validateGSTNumber(@PathVariable String gstNumber) {
        return ResponseEntity.ok(gstInsightsService.validateGSTNumber(gstNumber));
    }

    @GetMapping("/status/{gstNumber}")
    public ResponseEntity<?> getGSTStatus(@PathVariable String gstNumber) {
        return ResponseEntity.ok(gstInsightsService.getGSTStatus(gstNumber));
    }

    @GetMapping("/filing/status/{gstNumber}")
    public ResponseEntity<?> getGSTReturnFilingStatus(@PathVariable String gstNumber) {
        return ResponseEntity.ok(gstInsightsService.getGSTReturnFilingStatus(gstNumber));
    }

    @GetMapping("/filing/status/{gstNumber}/{year}")
    public ResponseEntity<?> getGSTReturnFilingStatusForYear(
            @PathVariable String gstNumber,
            @PathVariable String year) {
        return ResponseEntity.ok(gstInsightsService.getGSTReturnFilingStatusForYear(gstNumber, year));
    }

    @GetMapping("/address/{gstNumber}")
    public ResponseEntity<?> getAddressByGSTNumber(@PathVariable String gstNumber) {
        return ResponseEntity.ok(gstInsightsService.getAddressByGSTNumber(gstNumber));
    }
} 