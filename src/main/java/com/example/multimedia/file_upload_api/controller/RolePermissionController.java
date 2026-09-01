package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.RolePermissionDTO;
import com.example.multimedia.file_upload_api.dto.RolePermissionUpdateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.service.AuditLogService;
import com.example.multimedia.file_upload_api.service.RolePermissionService;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;
    private final ServiceControllerUtils serviceControllerUtils;
    private final AuditLogService auditLogService;

    @GetMapping("/roles")
    public ResponseEntity<ServiceResponse> getAllRoles() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<String> roles = Arrays.stream(UserType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
            response.addData("result", roles);
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Roles fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }

    @GetMapping("/{role}")
    public ResponseEntity<ServiceResponse> getPermissionsByRole(@PathVariable UserType role) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<RolePermissionDTO> permissions = rolePermissionService.getPermissionsByRole(role);
            response.addData("result", permissions);
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Permissions fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveRolePermissions(@RequestBody RolePermissionUpdateRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            rolePermissionService.saveRolePermissions(request);
            auditLogService.recordGeneric("ROLE_PERMISSIONS_UPDATED", request.getRole().name(), List.of(
                    new AuditLogService.FieldChange("permissionsUpdated", null, String.valueOf(request.getPermissions().size()))
            ));
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Role permissions updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }
}
