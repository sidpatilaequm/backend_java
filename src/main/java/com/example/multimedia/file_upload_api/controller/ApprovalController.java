package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.entity.workflow.ApprovalActionRO;
import com.example.multimedia.file_upload_api.repository.ApprovalActionRepository;
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
 * Read-only view of WorkFlow's approval_actions — "who approved/rejected what," surfaced in the
 * Audit Log's Approvals tab. See ApprovalActionRO/ApprovalActionRepository: this never writes to
 * WorkFlow's tables, only joins and reads them.
 */
@RestController
@RequestMapping("/api/admin/audit-log/approvals")
public class ApprovalController {

    private final ApprovalActionRepository approvalActionRepository;
    private final AdminAuthChecker adminAuthChecker;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;

    public ApprovalController(ApprovalActionRepository approvalActionRepository,
                               AdminAuthChecker adminAuthChecker,
                               SuperAdminRepository superAdminRepository,
                               UserDetailRepository userDetailRepository) {
        this.approvalActionRepository = approvalActionRepository;
        this.adminAuthChecker = adminAuthChecker;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
        }

        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).body(Map.of("detail", "Could not determine organization for this account."));
        }

        int cappedSize = Math.min(Math.max(size, 1), 200);
        Page<ApprovalActionRO> result = approvalActionRepository.findApprovalsForTenant(
                tenantId, PageRequest.of(Math.max(page, 0), cappedSize));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("entries", result.getContent().stream().map(this::toMap).toList());
        body.put("page", result.getNumber());
        body.put("totalPages", result.getTotalPages());
        body.put("totalElements", result.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> toMap(ApprovalActionRO aa) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("actedAt", aa.getActedAt());
        m.put("decision", aa.getDecision());
        m.put("comment", aa.getComment());

        UserDetail approver = aa.getApprover();
        m.put("approverName", approver != null ? fullName(approver) : null);
        m.put("approverEmail", approver != null ? approver.getEmail() : null);

        UserDetail delegatedTo = aa.getDelegatedTo();
        m.put("delegatedToName", delegatedTo != null ? fullName(delegatedTo) : null);

        var stage = aa.getRequestStage();
        var request = stage != null ? stage.getWorkflowRequest() : null;
        if (request != null) {
            m.put("requestTitle", request.getTitle());
            m.put("requestType", request.getRequestType());
            m.put("department", request.getDepartment());
            m.put("amount", request.getAmount());
            m.put("workflowName", request.getWorkflow() != null ? request.getWorkflow().getName() : null);
        }
        return m;
    }

    private static String fullName(UserDetail u) {
        String f = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String l = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? u.getEmail() : full;
    }

    // Same resolution AuditLogController uses — tenant of the currently authenticated admin.
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
