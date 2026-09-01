package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A named bundle of purchasing-document grants (see {@link PurchaseRoleGrant}), scoped to a set
 * of company codes (see {@link PurchaseRoleCompanyCode}) and belonging to either a vendor or an
 * employee. {@code assigneeRef} is where a specific vendor code or employee identifier attaches
 * — this table defines what the role can do, not a membership list.
 */
@Data
@Entity
@Table(name = "purchase_role")
public class PurchaseRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 40)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 120)
    private String roleName;

    @Column(name = "assignee_type", nullable = false, length = 10)
    private String assigneeType;

    @Column(name = "assignee_ref", length = 40)
    private String assigneeRef;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
