package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * Vendor assigned to a specific RFQ.
 *
 * Status lifecycle: SENT → RESPONDED | REJECTED
 * When a quotation is awarded: status → AWARDED
 *
 * Unique constraint: same vendor cannot be assigned twice to the same RFQ.
 */
@Entity
@Table(
    name = "rfq_vendors",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_rfq_vendor",
            columnNames = {"rfq_id", "vendor_id"}
        )
    }
)
public class RfqVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The RFQ this vendor is assigned to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    /**
     * The vendor from vendor_master.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", referencedColumnName = "vendor_id", nullable = false)
    private VendorMaster vendor;

    /**
     * SAP Business Partner number — denormalized from vendor_master.bp_no
     * for fast query without joining vendor_master each time.
     */
    @Column(name = "bp_no", length = 50)
    private String bpNo;

    /**
     * Status: SENT | RESPONDED | REJECTED | AWARDED
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "SENT";

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private Timestamp sentAt;

    @Column(name = "responded_at")
    private Timestamp respondedAt;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rfq getRfq() {
        return rfq;
    }

    public void setRfq(Rfq rfq) {
        this.rfq = rfq;
    }

    public VendorMaster getVendor() {
        return vendor;
    }

    public void setVendor(VendorMaster vendor) {
        this.vendor = vendor;
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
