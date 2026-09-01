package com.example.multimedia.file_upload_api.entity.workflow;

import com.example.multimedia.file_upload_api.entity.UserDetail;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Read-only mapping onto `approval_actions` — one row per approve/reject/delegate decision made
 * on a WorkFlow request stage. This table (and the workflow/request_stages/workflows tables it
 * joins to) is owned and written to exclusively by the separate Python WorkFlow app; backend_java
 * only reads it here to surface "who approved what" in the Audit Log's Approvals tab. Nothing in
 * the `entity.workflow` package is ever saved — see ApprovalActionRepository, which deliberately
 * extends the no-write-methods `Repository` marker interface instead of `JpaRepository` so that
 * stays true at the type level, not just by convention.
 */
@Entity
@Table(name = "approval_actions")
public class ApprovalActionRO {

    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_stage_id")
    private RequestStageRO requestStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private UserDetail approver;

    private String decision;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_to_id")
    private UserDetail delegatedTo;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    public Integer getId() { return id; }
    public RequestStageRO getRequestStage() { return requestStage; }
    public UserDetail getApprover() { return approver; }
    public String getDecision() { return decision; }
    public String getComment() { return comment; }
    public UserDetail getDelegatedTo() { return delegatedTo; }
    public LocalDateTime getActedAt() { return actedAt; }
}
