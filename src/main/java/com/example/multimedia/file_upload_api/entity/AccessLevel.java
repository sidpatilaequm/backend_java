package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * An access level a role's grant can use — held as data, keyed by which assignee type
 * (VENDOR/EMPLOYEE) it applies to, rather than hardcoded in application logic. A vendor role can
 * never hold "create"/"change"/"full"/"approve" simply because no VENDOR-scoped row for those
 * codes exists — validation just checks the requested (code, assigneeType) pair is present here.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(AccessLevel.Pk.class)
@Table(name = "access_level")
public class AccessLevel {

    @Id
    @Column(length = 10)
    private String code;

    @Id
    @Column(name = "assignee_type", length = 10)
    private String assigneeType;

    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false, length = 40)
    private String activities;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private String code;
        private String assigneeType;
    }
}
