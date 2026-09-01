package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.AuditLog;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuditLogRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Writes audit_log rows for account-changing actions on employee/admin UserDetail records — see
 * the call sites in UserDetailService for what triggers each action. Resolves the actor itself
 * from the security context (same SuperAdmin-then-UserDetail lookup order CurrentUserService/
 * AdminAuthChecker already use) so callers don't have to thread actor identity through every
 * call site themselves.
 *
 * A write here must never block or roll back the real account change it's describing — record()
 * swallows its own failures rather than propagating them, same defensive pattern
 * UserDetailService already applies around its email-notification calls.
 */
@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;

    public AuditLogService(AuditLogRepository auditLogRepository,
                            SuperAdminRepository superAdminRepository,
                            UserDetailRepository userDetailRepository) {
        this.auditLogRepository = auditLogRepository;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
    }

    public record FieldChange(String field, String oldValue, String newValue) {}

    public void record(SuperAdmin tenant, String action, UserDetail target,
                        List<FieldChange> changes, boolean passwordReset) {
        try {
            AuditLog row = new AuditLog();
            row.setSuperAdmin(tenant);
            row.setAction(action);
            row.setPasswordReset(passwordReset);
            applyActor(row);

            if (target != null) {
                row.setTargetUserId(target.getUserId());
                row.setTargetEmail(target.getEmail());
                row.setTargetName(fullName(target.getFirstName(), target.getLastName()));
            }
            applyChanges(row, changes);

            auditLogRepository.save(row);
        } catch (Exception e) {
            logger.warn("Failed to write audit log entry (action={}): {}", action, e.getMessage());
        }
    }

    /**
     * For actions with no natural UserDetail target — platform credentials, role/permission
     * definitions, vendor terms/permissions, budget uploads. These are "coarse" events (the fact
     * that a bulk-replace happened, not a per-field diff of what changed within it) — see the
     * call sites in PlatformCredentialController/RolePermissionController/
     * VendorPermissionServiceImpl/VendorTermsController/BudgetController. Resolves the tenant
     * from the current caller too (not passed in), since in every one of those call sites the
     * row's tenant is simply "whoever made this change" — there's no separately-known target
     * tenant the way UserDetailService's account-change events have one.
     */
    public void recordGeneric(String action, String targetLabel, List<FieldChange> changes) {
        try {
            SuperAdmin tenant = resolveTenant();
            if (tenant == null) {
                logger.warn("Skipped audit log entry (action={}): no tenant resolvable for current caller", action);
                return;
            }
            AuditLog row = new AuditLog();
            row.setSuperAdmin(tenant);
            row.setAction(action);
            row.setPasswordReset(false);
            applyActor(row);
            row.setTargetName(targetLabel);
            applyChanges(row, changes);

            auditLogRepository.save(row);
        } catch (Exception e) {
            logger.warn("Failed to write audit log entry (action={}): {}", action, e.getMessage());
        }
    }

    private void applyChanges(AuditLog row, List<FieldChange> changes) {
        if (changes == null || changes.isEmpty()) return;
        JSONArray arr = new JSONArray();
        for (FieldChange c : changes) {
            JSONObject o = new JSONObject();
            o.put("field", c.field());
            o.put("oldValue", c.oldValue());
            o.put("newValue", c.newValue());
            arr.put(o);
        }
        row.setFieldChanges(arr.toString());
    }

    private void applyActor(AuditLog row) {
        String[] actor = resolveActor();
        row.setActorEmail(actor[0]);
        row.setActorName(actor[1]);
    }

    // Same SuperAdmin-then-UserDetail lookup order as resolveActor(), returning the entity
    // itself rather than a display string — used only by recordGeneric(), which (unlike
    // record()) has no separately-known target tenant to scope the row to.
    private SuperAdmin resolveTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String email = auth.getName();

        return superAdminRepository.findByEmail(email)
                .or(() -> userDetailRepository.findByEmail(email).map(UserDetail::getSuperAdmin))
                .orElse(null);
    }

    private String[] resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new String[]{"unknown", "Unknown"};
        }
        String email = auth.getName();

        return superAdminRepository.findByEmail(email)
                .map(sa -> new String[]{email, fullName(sa.getFirstName(), sa.getLastName())})
                .or(() -> userDetailRepository.findByEmail(email)
                        .map(u -> new String[]{email, fullName(u.getFirstName(), u.getLastName())}))
                .orElse(new String[]{email, email});
    }

    private static String fullName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? null : full;
    }
}
