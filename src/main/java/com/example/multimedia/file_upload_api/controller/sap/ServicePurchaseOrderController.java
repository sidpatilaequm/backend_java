package com.example.multimedia.file_upload_api.controller.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.ServicePurchaseOrder;
import com.example.multimedia.file_upload_api.service.sap.ServicePurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ServicePurchaseOrderController {

    @Autowired
    private ServicePurchaseOrderService service;

    // VENDOR APIs
    @GetMapping("/api/service/vendor/list")
    public ResponseEntity<List<ServicePurchaseOrder>> getVendorServicePOs() {
        return ResponseEntity.ok(service.getVendorServicePurchaseOrders());
    }

    @GetMapping("/api/service/vendor/{poNumber}")
    public ResponseEntity<ServicePurchaseOrder> getVendorServicePO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getVendorServicePurchaseOrder(poNumber));
    }

    @PostMapping("/api/service/sync")
    public ResponseEntity<SapSyncResponse> syncServicePOs() {
        return ResponseEntity.ok(service.syncServicePurchaseOrders());
    }

    // ADMIN APIs
    @GetMapping("/api/admin/service/list")
    public ResponseEntity<List<ServicePurchaseOrder>> getAdminServicePOs() {
        return ResponseEntity.ok(service.getAdminServicePurchaseOrders());
    }

    @GetMapping("/api/admin/service/{poNumber}")
    public ResponseEntity<ServicePurchaseOrder> getAdminServicePO(@PathVariable String poNumber) {
        return ResponseEntity.ok(service.getAdminServicePurchaseOrder(poNumber));
    }
}
