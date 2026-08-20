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
