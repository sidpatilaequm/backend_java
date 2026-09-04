package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requisitions")
public class PurchaseRequisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String prNumber;

    @Column(name = "company_code")
    private String companyCode;

    // The requisitioning plant + storage location (Enterprise Structure master data —
    // see StorageLocation) this PR's items are delivered to. Replaces the old locationId FK
    // into the separate, mostly-unused Location ("delivery address") table — that column stays
    // in the DB for existing rows' history but is no longer written or read.
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    @Column(name = "sloc_id", length = 4)
    private String slocId;

    @Column(nullable = false)
    private Long requestedBy;

    private LocalDate requiredDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseRequisitionStatus status = PurchaseRequisitionStatus.CREATED;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    /**
     * The workflow engine request ID (from FastAPI WorkFlow DB) for this PR's approval.
     * Set after the PR is submitted to the workflow engine.
     * Nullable — not set until PR is submitted for approval.
     */
    @Column(name = "workflow_request_id")
    private Long workflowRequestId;

    @OneToMany(mappedBy = "purchaseRequisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseRequisitionItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(String prNumber) {
        this.prNumber = prNumber;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(String plantCode) {
        this.plantCode = plantCode;
    }

    public String getSlocId() {
        return slocId;
    }

    public void setSlocId(String slocId) {
        this.slocId = slocId;
    }

    public Long getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Long requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public PurchaseRequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequisitionStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PurchaseRequisitionItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequisitionItem> items) {
        this.items = items;
    }

    public Long getWorkflowRequestId() {
        return workflowRequestId;
    }

    public void setWorkflowRequestId(Long workflowRequestId) {
        this.workflowRequestId = workflowRequestId;
    }
}
