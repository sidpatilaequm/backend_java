package com.example.multimedia.file_upload_api.controller.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseOrder;
import com.example.multimedia.file_upload_api.service.sap.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService service;

    // VENDOR APIs
    @GetMapping("/api/po/vendor/list")
    public ResponseEntity<List<PurchaseOrder>> getVendorPOs() {
        return ResponseEntity.ok(service.getVendorPurchaseOrders());
    }

    @GetMapping("/api/po/vendor/{poNumber}")
    public ResponseEntity<PurchaseOrder> getVendorPO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getVendorPurchaseOrder(poNumber));
    }

    @PostMapping("/api/po/sync")
    public ResponseEntity<SapSyncResponse> syncPOs() {
        return ResponseEntity.ok(service.syncPurchaseOrders());
    }

    // ADMIN APIs
    @GetMapping("/api/admin/po/list")
    public ResponseEntity<List<PurchaseOrder>> getAdminPOs() {
        return ResponseEntity.ok(service.getAdminPurchaseOrders());
    }

    @GetMapping("/api/admin/po/{poNumber}")
    public ResponseEntity<PurchaseOrder> getAdminPO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getAdminPurchaseOrder(poNumber));
    }
}
