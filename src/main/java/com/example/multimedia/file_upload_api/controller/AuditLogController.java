package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.AuditLog;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuditLogRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only view of audit_log — same admin gate and tenant-scoping pattern as the rest of user
 * management (UserDetailController), since this surfaces the same category of information
 * (who's on the account list, what roles they have) just as a change history instead of a
 * snapshot.
 */
@RestController
@RequestMapping("/api/admin/audit-log")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AdminAuthChecker adminAuthChecker;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;

    public AuditLogController(AuditLogRepository auditLogRepository,
                               AdminAuthChecker adminAuthChecker,
                               SuperAdminRepository superAdminRepository,
                               UserDetailRepository userDetailRepository) {
        this.auditLogRepository = auditLogRepository;
        this.adminAuthChecker = adminAuthChecker;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long targetUserId,
                                   @RequestParam(required = false) String action,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
        }

        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).body(Map.of("detail", "Could not determine organization for this account."));
        }

        int cappedSize = Math.min(Math.max(size, 1), 200);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), cappedSize);

        Page<AuditLog> result;
        if (targetUserId != null && action != null) {
            result = auditLogRepository.findBySuperAdmin_SuperAdminIdAndTargetUserIdAndActionOrderByCreatedAtDesc(
                    tenantId, targetUserId, action, pageable);
        } else if (targetUserId != null) {
            result = auditLogRepository.findBySuperAdmin_SuperAdminIdAndTargetUserIdOrderByCreatedAtDesc(
                    tenantId, targetUserId, pageable);
        } else if (action != null) {
            result = auditLogRepository.findBySuperAdmin_SuperAdminIdAndActionOrderByCreatedAtDesc(
                    tenantId, action, pageable);
        } else {
            result = auditLogRepository.findBySuperAdmin_SuperAdminIdOrderByCreatedAtDesc(tenantId, pageable);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("entries", result.getContent().stream().map(this::toMap).toList());
        body.put("page", result.getNumber());
        body.put("totalPages", result.getTotalPages());
        body.put("totalElements", result.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> toMap(AuditLog row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.getId());
        m.put("createdAt", row.getCreatedAt());
        m.put("actorEmail", row.getActorEmail());
        m.put("actorName", row.getActorName());
        m.put("targetUserId", row.getTargetUserId());
        m.put("targetEmail", row.getTargetEmail());
        m.put("targetName", row.getTargetName());
        m.put("action", row.getAction());
        m.put("fieldChanges", row.getFieldChanges());
        m.put("passwordReset", row.isPasswordReset());
        return m;
    }

    // Mirrors UserDetailService.getEffectiveSuperAdmin — the caller viewing this screen is always
    // a SuperAdmin or an admin-tier UserDetail belonging to one; either way, this is the tenant
    // every audit_log row this endpoint may return is scoped to.
    private Long resolveTenantId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<SuperAdmin> sa = superAdminRepository.findByEmail(email);
        if (sa.isPresent()) return sa.get().getSuperAdminId();

        Optional<UserDetail> user = userDetailRepository.findByEmail(email);
        if (user.isPresent() && user.get().getSuperAdmin() != null) {
            return user.get().getSuperAdmin().getSuperAdminId();
        }
        return null;
    }
}
