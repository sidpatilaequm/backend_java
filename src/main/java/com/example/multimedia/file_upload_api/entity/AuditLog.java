package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * One row per account-changing action on an employee/admin UserDetail — who did it, whose
 * account it was, and what changed. Actor and target are stored as text snapshots (not just a
 * foreign key) so the log stays readable even after that person is later renamed or deactivated —
 * the same reasoning PlatformCredential.updatedBy already uses, just fuller. See AuditLogService
 * for how rows are written; a password value never appears here in either direction, only
 * passwordReset.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @Column(name = "actor_email", nullable = false)
    private String actorEmail;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(name = "target_name")
    private String targetName;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "field_changes", columnDefinition = "TEXT")
    private String fieldChanges;

    @Column(name = "password_reset", nullable = false)
    private boolean passwordReset;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }

    public SuperAdmin getSuperAdmin() { return superAdmin; }
    public void setSuperAdmin(SuperAdmin superAdmin) { this.superAdmin = superAdmin; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getFieldChanges() { return fieldChanges; }
    public void setFieldChanges(String fieldChanges) { this.fieldChanges = fieldChanges; }

    public boolean isPasswordReset() { return passwordReset; }
    public void setPasswordReset(boolean passwordReset) { this.passwordReset = passwordReset; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
