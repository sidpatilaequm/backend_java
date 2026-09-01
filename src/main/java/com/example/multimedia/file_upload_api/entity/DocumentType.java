package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A purchasing document type as configured in SAP (or manually registered here to cover the gap
 * until the next reference refresh — see {@code source}). Which company codes it can actually be
 * granted in lives in {@link DocumentTypeCompanyCode}, not here.
 */
@Data
@Entity
@Table(name = "document_type")
public class DocumentType {

    @Id
    @Column(name = "code", length = 4)
    private String code;

    @Column(nullable = false, length = 80)
    private String description;

    @Column(name = "doc_category", nullable = false, length = 1)
    private String docCategory;

    @Column(nullable = false, length = 20)
    private String classification;

    @Column(nullable = false, length = 10)
    private String source = "SAP";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
