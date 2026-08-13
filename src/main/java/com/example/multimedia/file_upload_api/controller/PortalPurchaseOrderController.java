package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderListResponse;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderRequest;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderResponse;
import com.example.multimedia.file_upload_api.service.PortalPurchaseOrderService;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PortalPurchaseOrderController {

    @Autowired
    private PortalPurchaseOrderService poService;

    @Autowired
    private SecurityContextUtils securityContextUtils;

    @Autowired
    private com.example.multimedia.file_upload_api.service.CurrentUserService currentUserService;

    @Autowired
    private com.example.multimedia.file_upload_api.service.AsnService asnService;

    // --- ADMIN APIs ---

    @PostMapping("/api/purchase-orders/from-awarded-quotation/{quotationId}")
    public ResponseEntity<?> createPOFromAwardedQuotation(
            @PathVariable Long quotationId,
            @RequestBody(required = false) PortalPurchaseOrderRequest request) {
        PortalPurchaseOrderRequest finalRequest = request != null ? request : new PortalPurchaseOrderRequest();
        PortalPurchaseOrderResponse response = poService.createPOFromAwardedQuotation(quotationId, finalRequest);
        java.util.Map<String, Object> responseMap = new java.util.LinkedHashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Purchase Order created successfully");
        responseMap.put("poId", response.getPoId());
        responseMap.put("poNumber", response.getPoNumber());
        return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
    }

    @GetMapping("/api/purchase-orders")
    public ResponseEntity<List<PortalPurchaseOrderListResponse>> getAllPOsForAdmin() {
        Long adminId = null;
        if (currentUserService.isCurrentUserSuperAdmin()) {
            adminId = securityContextUtils.getCurrentAdminId();
        }
        List<PortalPurchaseOrderListResponse> response = poService.getAllPOsForAdmin(adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/purchase-orders/{id}")
    public ResponseEntity<PortalPurchaseOrderResponse> getPODetailsForAdmin(@PathVariable Long id) {
        Long adminId = null;
        if (currentUserService.isCurrentUserSuperAdmin()) {
            adminId = securityContextUtils.getCurrentAdminId();
        }
        PortalPurchaseOrderResponse response = poService.getPODetailsForAdmin(id, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/purchase-orders/{id}/cancel")
    public ResponseEntity<Void> cancelPO(@PathVariable Long id) {
        Long adminId = null;
        if (currentUserService.isCurrentUserSuperAdmin()) {
            adminId = securityContextUtils.getCurrentAdminId();
        }
        poService.cancelPO(id, adminId);
        return ResponseEntity.ok().build();
    }

    // --- VENDOR APIs ---

    @GetMapping("/api/vendor/purchase-orders")
    public ResponseEntity<List<PortalPurchaseOrderListResponse>> getPOsForVendor() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        List<PortalPurchaseOrderListResponse> response = poService.getPOsForVendor(vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/vendor/purchase-orders/{id}")
    public ResponseEntity<PortalPurchaseOrderResponse> getPODetailsForVendor(@PathVariable Long id) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        PortalPurchaseOrderResponse response = poService.getPODetailsForVendor(id, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/vendor/asns/history/{poNumber}")
    public ResponseEntity<?> getAsnsForPo(@PathVariable String poNumber) {
        return ResponseEntity.ok(asnService.getAsnsByPoNumber(poNumber));
    }
}
