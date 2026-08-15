package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * One row per document type per {@link SupplierRegistration} — mirrors the
 * Next.js prototype's UploadedFile shape (hooks/useSupplierForm.ts): a
 * FolderIt file reference (never the file bytes themselves, since FolderIt's
 * download links are presigned and expire in 60 seconds — resolved on
 * demand, not stored), OCR-extracted fields, and verification result.
 */
@Entity
@Table(name = "supplier_registration_document")
@Getter
@Setter
public class SupplierRegistrationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private SupplierRegistration registration;

    /** coi / pan / gst / chq / udyam / iso / as */
    @Column(name = "doc_type", nullable = false)
    private String docType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "folderit_file_uid")
    private String folderItFileUid;

    @Column(name = "ocr_extracted_fields", columnDefinition = "LONGTEXT")
    private String ocrExtractedFieldsJson;

    /** reading / read / verifying / verified / error */
    @Column(name = "verify_status")
    private String verifyStatus;

    @Column(name = "verify_details", columnDefinition = "LONGTEXT")
    private String verifyDetailsJson;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
