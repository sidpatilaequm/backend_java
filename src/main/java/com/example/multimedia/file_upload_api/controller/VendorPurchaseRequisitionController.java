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
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam,
            @RequestParam(name = "company_code", required = false) String companyCode) {
        Long vendorId = null;
        if (vendorIdParam != null) {
            vendorId = vendorIdParam;
        } else {
            try {
                vendorId = securityContextUtils.getCurrentVendorId();
            } catch (Exception e) {
                // If not authenticated (e.g. testing)
            }
        }
        if (vendorId == null) {
            throw new RuntimeException("Vendor ID is required but could not be determined.");
        }
        List<VendorPurchaseRequisitionItemResponse> items = purchaseRequisitionService.getVendorAssignedItems(vendorId, companyCode);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/details")
    public ResponseEntity<List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse>> getAllVendorPurchaseRequisitions(
            @RequestParam(name = "vendor_id", required = false) Long vendorIdParam,
            @RequestParam(name = "company_code", required = false) String companyCode) {
        Long vendorId = null;
        if (vendorIdParam != null) {
            vendorId = vendorIdParam;
        } else {
            try {
                vendorId = securityContextUtils.getCurrentVendorId();
            } catch (Exception e) {
                // Ignore if not authenticated
            }
        }
        if (vendorId == null) {
            throw new RuntimeException("Vendor ID is required but could not be determined.");
        }
        List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> response = purchaseRequisitionService.getAllVendorPurchaseRequisitions(vendorId, companyCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{prNumber}")
    public ResponseEntity<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> getVendorPurchaseRequisitionByPrNumber(@PathVariable String prNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse response = purchaseRequisitionService.getVendorPurchaseRequisitionByPrNumber(prNumber, vendorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{prId}/create-rfq")
    public ResponseEntity<java.util.Map<String, String>> createRfq(
            @PathVariable Long prId,
            @RequestBody com.example.multimedia.file_upload_api.dto.CreateRfqRequest request) {
        
        purchaseRequisitionService.createRfq(prId, request.getVendor_ids());
        
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("status", "success");
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

    private Long extractVendorId(java.util.Map<String, Object> payload) {
        Long vendorId = null;
        if (payload != null && payload.containsKey("vendor_id")) {
            Object vId = payload.get("vendor_id");
            if (vId instanceof Number) {
                vendorId = ((Number) vId).longValue();
            } else if (vId instanceof String) {
                vendorId = Long.parseLong((String) vId);
            }
        }
        if (vendorId == null) {
            try {
                vendorId = securityContextUtils.getCurrentVendorId();
            } catch (Exception e) {
                // Ignore if not authenticated
            }
        }
        if (vendorId == null) {
            throw new RuntimeException("Vendor ID is required but could not be determined.");
        }
        return vendorId;
    }

    @PostMapping("/{prId}/accept")
    public ResponseEntity<Void> acceptPurchaseRequisition(
            @PathVariable Long prId, 
            @RequestBody(required = false) java.util.Map<String, Object> payload) {
        Long vendorId = extractVendorId(payload);
        purchaseRequisitionService.respondToPurchaseRequisition(prId, vendorId, "ACCEPT");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{prId}/reject")
    public ResponseEntity<Void> rejectPurchaseRequisition(
            @PathVariable Long prId,
            @RequestBody(required = false) java.util.Map<String, Object> payload) {
        Long vendorId = extractVendorId(payload);
        purchaseRequisitionService.respondToPurchaseRequisition(prId, vendorId, "REJECT");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse>> getAcceptedVendorPurchaseRequisitions(
            @RequestParam(name = "company_code", required = false) String companyCode) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        List<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> response = purchaseRequisitionService.getAcceptedVendorPurchaseRequisitions(vendorId, companyCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accepted/{prId}")
    public ResponseEntity<com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse> getAcceptedVendorPurchaseRequisitionById(@PathVariable Long prId) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse response = purchaseRequisitionService.getAcceptedVendorPurchaseRequisitionById(prId, vendorId);
        return ResponseEntity.ok(response);
    }
}
