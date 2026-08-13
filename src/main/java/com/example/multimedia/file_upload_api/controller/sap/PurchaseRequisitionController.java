package com.example.multimedia.file_upload_api.controller.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseRequisition;
import com.example.multimedia.file_upload_api.service.sap.PurchaseRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("sapPurchaseRequisitionController")
public class PurchaseRequisitionController {

    @Autowired
    private PurchaseRequisitionService service;

    // VENDOR APIs
    @GetMapping("/api/pr/vendor/list")
    public ResponseEntity<List<PurchaseRequisition>> getVendorPRs() {
        return ResponseEntity.ok(service.getVendorPurchaseRequisitions());
    }

    @GetMapping("/api/pr/vendor/{prNumber}")
    public ResponseEntity<PurchaseRequisition> getVendorPR(@PathVariable String prNumber) {
        return ResponseEntity.ok(service.getVendorPurchaseRequisition(prNumber));
    }

    @PostMapping("/api/pr/sync")
    public ResponseEntity<SapSyncResponse> syncPRs() {
        return ResponseEntity.ok(service.syncPurchaseRequisitions());
    }

    // ADMIN APIs
    @GetMapping("/api/admin/pr/list")
    public ResponseEntity<List<PurchaseRequisition>> getAdminPRs() {
        return ResponseEntity.ok(service.getAdminPurchaseRequisitions());
    }

    @GetMapping("/api/admin/pr/{prNumber}")
    public ResponseEntity<PurchaseRequisition> getAdminPR(@PathVariable String prNumber) {
        return ResponseEntity.ok(service.getAdminPurchaseRequisition(prNumber));
    }
}
