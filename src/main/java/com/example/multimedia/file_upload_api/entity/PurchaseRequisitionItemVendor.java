package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "purchase_requisition_item_vendors")
public class PurchaseRequisitionItemVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_requisition_item_id", nullable = false)
    private PurchaseRequisitionItem purchaseRequisitionItem;

    @Column(nullable = false)
    private Long vendorId;

    @Column(name = "bp_no")
    private String bpNo;

    @Column(nullable = false)
    private String status = "SENT";

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp sentAt;

    // Set when the vendor actually responds (ACCEPTED or REJECTED) — see
    // PurchaseRequisitionServiceImpl.respondToPurchaseRequisition. Null while status is still SENT.
    @Column(name = "responded_at")
    private Timestamp respondedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseRequisitionItem getPurchaseRequisitionItem() {
        return purchaseRequisitionItem;
    }

    public void setPurchaseRequisitionItem(PurchaseRequisitionItem purchaseRequisitionItem) {
        this.purchaseRequisitionItem = purchaseRequisitionItem;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getBpNo() {
        return bpNo;
    }

    public void setBpNo(String bpNo) {
        this.bpNo = bpNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }
}
