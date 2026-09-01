package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.AnalyticsClientService;
import com.example.multimedia.file_upload_api.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Employee-facing view of reports published to the "employee" role in the Report Designer.
 * Deliberately the only way to discover them — the underlying analytics endpoint
 * (GET /api/processes/published) is itself authenticated (see AnalyticsClientService) precisely
 * so this check, not a guessable link, is what actually gates visibility.
 */
@RestController
@RequestMapping("/api/employee/reports")
public class EmployeeReportsController {

    private final AnalyticsClientService analyticsClientService;
    private final UserDetailRepository userDetailRepository;
    private final AuditLogService auditLogService;

    public EmployeeReportsController(AnalyticsClientService analyticsClientService,
                                      UserDetailRepository userDetailRepository,
                                      AuditLogService auditLogService) {
        this.analyticsClientService = analyticsClientService;
        this.userDetailRepository = userDetailRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        if (!isEmployee()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Employee access required for this account."));
        }
        try {
            List<AnalyticsClientService.PublishedReport> reports = analyticsClientService.listPublished("employee");
            return ResponseEntity.ok(Map.of("reports", reports));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("detail", "Could not reach the reporting service."));
        }
    }

    // Audit-only — the frontend calls this right when a tile is opened, fire-and-forget. Doesn't
    // gate the actual view (the browser goes straight to the analytics URL already returned by
    // list() above); this just records who opened what, re-resolving the report's name
    // server-side against the current published list rather than trusting a client-supplied one.
    @PostMapping("/{key}/view")
    public ResponseEntity<?> recordView(@PathVariable String key) {
        if (!isEmployee()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Employee access required for this account."));
        }
        try {
            String name = analyticsClientService.listPublished("employee").stream()
                    .filter(r -> r.key().equals(key))
                    .findFirst()
                    .map(AnalyticsClientService.PublishedReport::name)
                    .orElse(key);
            auditLogService.recordGeneric("REPORT_VIEWED", name, List.of());
        } catch (Exception e) {
            // Best-effort — never block the employee from viewing the report over a logging hiccup.
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private boolean isEmployee() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        Optional<UserDetail> user = userDetailRepository.findByEmail(auth.getName());
        if (user.isEmpty()) return false;
        UserType type = user.get().getUserType();
        return type == UserType.EMPLOYEE || type == UserType.PURCHASE_DEPT;
    }
}
