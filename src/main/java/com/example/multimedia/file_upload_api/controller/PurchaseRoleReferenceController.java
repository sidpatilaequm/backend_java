package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.PurchaseRoleDtos.*;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.PurchaseRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Reference data for purchasing roles — document types, access levels. Company codes themselves
 * are not duplicated here; the frontend calls the existing GET /api/organization/companies
 * (CompanyController), since backend_java already has that enterprise-structure data.
 */
@RestController
@RequestMapping("/api/purchase-roles")
public class PurchaseRoleReferenceController {

    private final PurchaseRoleService service;
    private final AdminAuthChecker adminAuthChecker;

    public PurchaseRoleReferenceController(PurchaseRoleService service, AdminAuthChecker adminAuthChecker) {
        this.service = service;
        this.adminAuthChecker = adminAuthChecker;
    }

    @GetMapping("/access-levels")
    public ResponseEntity<?> accessLevels(@RequestParam(required = false) String assigneeType) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        return ResponseEntity.ok(service.listAccessLevels(assigneeType));
    }

    @GetMapping("/document-types")
    public ResponseEntity<?> documentTypes(@RequestParam(required = false) List<String> companyCode) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        return ResponseEntity.ok(service.listDocumentTypes(companyCode));
    }

    @PostMapping("/document-types")
    public ResponseEntity<?> createDocumentType(@RequestBody DocumentTypeIn payload) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        try {
            return ResponseEntity.status(201).body(service.createDocumentType(payload));
        } catch (PurchaseRoleService.ConflictException e) {
            return ResponseEntity.status(409).body(Map.of("detail", e.getMessage()));
        }
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
    }
}
