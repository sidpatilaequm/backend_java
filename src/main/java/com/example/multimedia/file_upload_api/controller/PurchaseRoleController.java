package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.PurchaseRoleDtos.*;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.AuditLogService;
import com.example.multimedia.file_upload_api.service.PurchaseRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-roles")
public class PurchaseRoleController {

    private final PurchaseRoleService service;
    private final AdminAuthChecker adminAuthChecker;
    private final AuditLogService auditLogService;

    public PurchaseRoleController(PurchaseRoleService service, AdminAuthChecker adminAuthChecker,
                                   AuditLogService auditLogService) {
        this.service = service;
        this.adminAuthChecker = adminAuthChecker;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String assigneeType) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        return ResponseEntity.ok(service.listRoles(assigneeType));
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<?> get(@PathVariable Long roleId) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        RoleOut role = service.getRole(roleId);
        if (role == null) return notFound();
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RoleIn payload) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        try {
            RoleOut role = service.createRole(payload);
            auditLogService.recordGeneric("PURCHASE_ROLE_CREATED", role.getRoleCode(), List.of(
                    new AuditLogService.FieldChange("assigneeType", null, role.getAssigneeType()),
                    new AuditLogService.FieldChange("grantCount", null, String.valueOf(role.getGrantCount()))
            ));
            return ResponseEntity.status(201).body(role);
        } catch (PurchaseRoleService.ConflictException e) {
            return ResponseEntity.status(409).body(Map.of("detail", e.getMessage()));
        }
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> update(@PathVariable Long roleId, @RequestBody RoleIn payload) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        try {
            RoleOut role = service.updateRole(roleId, payload);
            if (role == null) return notFound();
            auditLogService.recordGeneric("PURCHASE_ROLE_UPDATED", role.getRoleCode(), List.of(
                    new AuditLogService.FieldChange("grantCount", null, String.valueOf(role.getGrantCount()))
            ));
            return ResponseEntity.ok(role);
        } catch (PurchaseRoleService.ConflictException e) {
            return ResponseEntity.status(409).body(Map.of("detail", e.getMessage()));
        }
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> delete(@PathVariable Long roleId) {
        if (!adminAuthChecker.isAdmin()) return forbidden();
        RoleOut existing = service.getRole(roleId);
        if (!service.deleteRole(roleId)) return notFound();
        if (existing != null) {
            auditLogService.recordGeneric("PURCHASE_ROLE_DELETED", existing.getRoleCode(), List.of());
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
    }

    private ResponseEntity<?> notFound() {
        return ResponseEntity.status(404).body(Map.of("detail", "role not found"));
    }
}
