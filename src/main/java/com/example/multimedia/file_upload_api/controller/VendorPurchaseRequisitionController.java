package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.VendorPurchaseRequisitionItemResponse;
import com.example.multimedia.file_upload_api.service.PurchaseRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.multimedia.file_upload_api.util.SecurityContextUtils;

@RestController
@RequestMapping("/api/vendor/purchase-requisitions")
public class VendorPurchaseRequisitionController {

    @Autowired
    private PurchaseRequisitionService purchaseRequisitionService;

    @Autowired
    private SecurityContextUtils securityContextUtils;

    @GetMapping
    public ResponseEntity<List<VendorPurchaseRequisitionItemResponse>> getVendorAssignedItems(
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam) {
        Long vendorId = null;
        try {
            vendorId = securityContextUtils.getCurrentVendorId();
        } catch (Exception e) {
            // If not authenticated (e.g. testing), fall back to the query param
        }
        if (vendorId == null) {
            vendorId = vendorIdParam;
        }
        if (vendorId == null) {
            throw new RuntimeException("Vendor ID is required but could not be determined.");
        }
        List<VendorPurchaseRequisitionItemResponse> items = purchaseRequisitionService.getVendorAssignedItems(vendorId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/details")
    public ResponseEntity<List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse>> getAllVendorPurchaseRequisitions() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> response = purchaseRequisitionService.getAllVendorPurchaseRequisitions(vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{prNumber}")
    public ResponseEntity<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> getVendorPurchaseRequisitionByPrNumber(@PathVariable String prNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse response = purchaseRequisitionService.getVendorPurchaseRequisitionByPrNumber(prNumber, vendorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{prId}/respond")
    public ResponseEntity<Void> respondToPurchaseRequisition(
            @PathVariable Long prId,
            @RequestParam String action) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        purchaseRequisitionService.respondToPurchaseRequisition(prId, vendorId, action);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{prId}/accept")
    public ResponseEntity<Void> acceptPurchaseRequisition(@PathVariable Long prId) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        purchaseRequisitionService.respondToPurchaseRequisition(prId, vendorId, "ACCEPT");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{prId}/reject")
    public ResponseEntity<Void> rejectPurchaseRequisition(@PathVariable Long prId) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        purchaseRequisitionService.respondToPurchaseRequisition(prId, vendorId, "REJECT");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse>> getAcceptedVendorPurchaseRequisitions() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> response = purchaseRequisitionService.getAcceptedVendorPurchaseRequisitions(vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accepted/{prId}")
    public ResponseEntity<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> getAcceptedVendorPurchaseRequisitionById(@PathVariable Long prId) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse response = purchaseRequisitionService.getAcceptedVendorPurchaseRequisitionById(prId, vendorId);
        return ResponseEntity.ok(response);
    }
}
