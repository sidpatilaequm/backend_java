package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * One row per sign-in attempt — password, Microsoft, or Google — successful or not. See
 * LoginAttemptService for where these get written. superAdmin stays null for an attempt against
 * an email that matches no account anywhere (nothing to scope it to); the read endpoint only ever
 * returns rows with a matching tenant, so those attempts are captured for direct DB inspection
 * but never surfaced to any tenant's admin.
 */
@Entity
@Table(name = "login_attempt")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(name = "failure_reason", length = 60)
    private String failureReason;

    @ManyToOne
    @JoinColumn(name = "super_admin_id")
    private SuperAdmin superAdmin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public SuperAdmin getSuperAdmin() { return superAdmin; }
    public void setSuperAdmin(SuperAdmin superAdmin) { this.superAdmin = superAdmin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
