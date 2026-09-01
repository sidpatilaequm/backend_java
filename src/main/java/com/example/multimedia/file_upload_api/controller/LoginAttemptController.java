package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.LoginAttempt;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.LoginAttemptRepository;
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

/** Read-only view of login_attempt — same admin gate and tenant scoping as AuditLogController. */
@RestController
@RequestMapping("/api/admin/audit-log/logins")
public class LoginAttemptController {

    private final LoginAttemptRepository loginAttemptRepository;
    private final AdminAuthChecker adminAuthChecker;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;

    public LoginAttemptController(LoginAttemptRepository loginAttemptRepository,
                                   AdminAuthChecker adminAuthChecker,
                                   SuperAdminRepository superAdminRepository,
                                   UserDetailRepository userDetailRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.adminAuthChecker = adminAuthChecker;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean success,
                                   @RequestParam(required = false) String method,
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

        Page<LoginAttempt> result;
        if (success != null && method != null) {
            result = loginAttemptRepository.findBySuperAdmin_SuperAdminIdAndSuccessAndMethodOrderByCreatedAtDesc(
                    tenantId, success, method, pageable);
        } else if (success != null) {
            result = loginAttemptRepository.findBySuperAdmin_SuperAdminIdAndSuccessOrderByCreatedAtDesc(
                    tenantId, success, pageable);
        } else if (method != null) {
            result = loginAttemptRepository.findBySuperAdmin_SuperAdminIdAndMethodOrderByCreatedAtDesc(
                    tenantId, method, pageable);
        } else {
            result = loginAttemptRepository.findBySuperAdmin_SuperAdminIdOrderByCreatedAtDesc(tenantId, pageable);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("entries", result.getContent().stream().map(this::toMap).toList());
        body.put("page", result.getNumber());
        body.put("totalPages", result.getTotalPages());
        body.put("totalElements", result.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> toMap(LoginAttempt a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("createdAt", a.getCreatedAt());
        m.put("email", a.getEmail());
        m.put("method", a.getMethod());
        m.put("success", a.isSuccess());
        m.put("failureReason", a.getFailureReason());
        return m;
    }

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
