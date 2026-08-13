package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.MasterPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.MasterPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/master-purchase-orders")
public class MasterPurchaseOrderController {

    @Autowired
    private MasterPurchaseOrderService service;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            service.saveExcelData(file);
            return ResponseEntity.ok(Map.of("message", "Excel uploaded and data saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }

    @GetMapping("/user-data")
    public ResponseEntity<?> getOrdersForVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        String email = authentication.getName();
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getUserId();
            List<MasterPurchaseOrder> orders = service.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        }

        return ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }
}
