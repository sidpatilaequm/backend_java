package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A vendor's self-service request to change one already-approved document, attachment, or
 * questionnaire answer — goes through the same "Vendor Approval" approver team (a dedicated
 * "Vendor Change Request" WorkFlow workflow, same approver_group), and the actual document/
 * attachment/answer is only replaced once that request is approved (see
 * VendorChangeRequestService.applyApprovedChange). itemType/itemKey identify what's being
 * changed: "document"+docType, "attachment"+attachment id (as string), or "answer"+questionId
 * (as string).
 */
@Entity
@Table(name = "supplier_registration_change_request")
@Getter
@Setter
public class SupplierChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private SupplierRegistration registration;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "item_key", nullable = false)
    private String itemKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "old_value_summary", columnDefinition = "TEXT")
    private String oldValueSummary;

    @Column(name = "new_file_name")
    private String newFileName;

    @Column(name = "new_folderit_file_uid")
    private String newFolderItFileUid;

    @Column(name = "new_answer_json", columnDefinition = "LONGTEXT")
    private String newAnswerJson;

    /** Snapshot of the vendor's own submission-time preview (see
     *  VendorChangeRequestService.previewVerify) — OCR fields read off the proposed replacement
     *  document and the Microvista verification run against them, captured at submit() so an
     *  admin reviewing a still-PENDING request sees the same information the vendor saw, without
     *  needing to re-download/re-OCR the file just to review it. Only ever set for
     *  itemType="document"; null for attachment/answer requests. Independent of — and not reused
     *  by — applyApprovedChange, which re-derives its own authoritative OCR/verify result from the
     *  file fresh at approval time. */
    @Column(name = "new_ocr_extracted_fields_json", columnDefinition = "LONGTEXT")
    private String newOcrExtractedFieldsJson;

    /** "verified" / "error" — mirrors SupplierRegistrationDocument.verifyStatus's vocabulary. */
    @Column(name = "new_verify_status")
    private String newVerifyStatus;

    @Column(name = "new_verify_details_json", columnDefinition = "LONGTEXT")
    private String newVerifyDetailsJson;

    /** PENDING / APPROVED / REJECTED */
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "workflow_request_id")
    private Long workflowRequestId;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "decided_date")
    private LocalDateTime decidedDate;
}
