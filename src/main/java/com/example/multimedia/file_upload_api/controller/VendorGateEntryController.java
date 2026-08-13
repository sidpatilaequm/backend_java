package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.GateEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/gate-entry")
public class VendorGateEntryController {

    @Autowired
    private GateEntryService gateEntryService;

    private String getVendorEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            return auth.getName(); // Assuming vendor BPNO is the username for vendor users
        }
        return "markjhon@gmail.com"; // Fallback for local testing
    }

    @GetMapping("/status")
    public ResponseEntity<ServiceResponse> getGateStatus() {
        return ResponseEntity.ok(gateEntryService.getVendorGateStatus(getVendorEmail()));
    }

    @GetMapping("/status/{asnNumber}")
    public ResponseEntity<ServiceResponse> getGateDiscrepancyReport(@PathVariable String asnNumber) {
        return ResponseEntity.ok(gateEntryService.getGateDiscrepancyReport(asnNumber, getVendorEmail()));
    }
}
