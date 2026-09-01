package com.example.multimedia.file_upload_api.entity.workflow;

import com.example.multimedia.file_upload_api.entity.UserDetail;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Read-only mapping onto `workflow_requests` — see ApprovalActionRO for why this exists. */
@Entity
@Table(name = "workflow_requests")
public class WorkflowRequestRO {

    @Id
    private Integer id;

    private String title;

    private String department;

    @Column(name = "request_type")
    private String requestType;

    private Double amount;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private WorkflowRO workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id")
    private UserDetail submitter;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getRequestType() { return requestType; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
    public WorkflowRO getWorkflow() { return workflow; }
    public UserDetail getSubmitter() { return submitter; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
