package com.example.multimedia.file_upload_api.controller.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.SubconPurchaseOrder;
import com.example.multimedia.file_upload_api.service.sap.SubconPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SubconPurchaseOrderController {

    @Autowired
    private SubconPurchaseOrderService service;

    // VENDOR APIs
    @GetMapping("/api/subcon/vendor/list")
    public ResponseEntity<List<SubconPurchaseOrder>> getVendorSubconPOs() {
        return ResponseEntity.ok(service.getVendorSubconPurchaseOrders());
    }

    @GetMapping("/api/subcon/vendor/{poNumber}")
    public ResponseEntity<SubconPurchaseOrder> getVendorSubconPO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getVendorSubconPurchaseOrder(poNumber));
    }

    @PostMapping("/api/subcon/sync")
    public ResponseEntity<SapSyncResponse> syncSubconPOs() {
        return ResponseEntity.ok(service.syncSubconPurchaseOrders());
    }

    // ADMIN APIs
    @GetMapping("/api/admin/subcon/list")
    public ResponseEntity<List<SubconPurchaseOrder>> getAdminSubconPOs() {
        return ResponseEntity.ok(service.getAdminSubconPurchaseOrders());
    }

    @GetMapping("/api/admin/subcon/{poNumber}")
    public ResponseEntity<SubconPurchaseOrder> getAdminSubconPO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getAdminSubconPurchaseOrder(poNumber));
    }
}
