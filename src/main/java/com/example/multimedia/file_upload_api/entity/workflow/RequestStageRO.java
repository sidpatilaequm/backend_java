package com.example.multimedia.file_upload_api.entity.workflow;

import jakarta.persistence.*;

/** Read-only mapping onto `request_stages` — see ApprovalActionRO for why this exists. */
@Entity
@Table(name = "request_stages")
public class RequestStageRO {

    @Id
    private Integer id;

    @Column(name = "stage_order")
    private Integer stageOrder;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private WorkflowRequestRO workflowRequest;

    public Integer getId() { return id; }
    public Integer getStageOrder() { return stageOrder; }
    public String getStatus() { return status; }
    public WorkflowRequestRO getWorkflowRequest() { return workflowRequest; }
}
