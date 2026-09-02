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

    @Autowired
    private com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository companyDetailsRepository;

    private Long resolveVendorId(String vendorCode) {
        if (vendorCode != null && !vendorCode.trim().isEmpty()) {
            List<com.example.multimedia.file_upload_api.entity.CompanyDetails> list = companyDetailsRepository.findByCompanyCode(vendorCode.trim());
            if (!list.isEmpty()) {
                return list.get(0).getCompanyId();
            }
        }
        try {
            return securityContextUtils.getCurrentVendorId();
        } catch (Exception e) {
            List<com.example.multimedia.file_upload_api.entity.CompanyDetails> list = companyDetailsRepository.findAll();
            if (!list.isEmpty()) {
                return list.get(0).getCompanyId();
            }
            return 1L;
        }
    }

    // --- VENDOR APIs ---

    @GetMapping("/api/vendor/purchase-orders")
    public ResponseEntity<List<PortalPurchaseOrderListResponse>> getPOsForVendor(
            @RequestParam(required = false, name = "vendor_code") String vendorCode,
            @RequestParam(required = false, name = "company_code") String companyCode) {
        Long vendorId = resolveVendorId(vendorCode);
        List<PortalPurchaseOrderListResponse> response = poService.getPOsForVendor(vendorId, companyCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/vendor/purchase-orders/{id}")
    public ResponseEntity<PortalPurchaseOrderResponse> getPODetailsForVendor(
            @PathVariable Long id,
            @RequestParam(required = false, name = "vendor_code") String vendorCode) {
        Long vendorId = resolveVendorId(vendorCode);
        PortalPurchaseOrderResponse response = poService.getPODetailsForVendor(id, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/api/vendor/asns/history/{poNumber}", "/api/vendor/purchase-orders/{poNumber}/asns", "/api/purchase-orders/{poNumber}/asns"})
    public ResponseEntity<?> getAsnsForPo(@PathVariable String poNumber) {
        return ResponseEntity.ok(asnService.getAsnsByPoNumber(poNumber));
    }

    @PostMapping("/api/vendor/purchase-orders/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgePO(
            @PathVariable Long id,
            @RequestParam(required = false, name = "vendor_code") String vendorCode) {
        Long vendorId = resolveVendorId(vendorCode);
        poService.acknowledgePO(id, vendorId);
        return ResponseEntity.ok().build();
    }
}
