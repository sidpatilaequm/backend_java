package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A single admin-editable secret/config value for an external integration (FolderIt,
 * Microvista, ...), keyed by a stable dotted key (e.g. "folderit.client_secret") that
 * the owning service looks up at call time instead of a hardcoded constant or a
 * deploy-time-only environment variable — so an admin can rotate a credential from the
 * UI without a code change or a restart.
 */
@Entity
@Table(name = "platform_credential")
public class PlatformCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential_key", nullable = false, unique = true, length = 100)
    private String credentialKey;

    @Column(name = "credential_value", columnDefinition = "TEXT")
    private String credentialValue;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    public Long getId() { return id; }
    public String getCredentialKey() { return credentialKey; }
    public void setCredentialKey(String credentialKey) { this.credentialKey = credentialKey; }
    public String getCredentialValue() { return credentialValue; }
    public void setCredentialValue(String credentialValue) { this.credentialValue = credentialValue; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
