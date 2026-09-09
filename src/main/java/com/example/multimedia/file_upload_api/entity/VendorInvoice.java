package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unused by any service/controller today (see V11/V12 migrations) -- kept accurate to the real
 * table so whoever wires up the actual SAP report import doesn't inherit a stale mapping. Columns
 * added by V11 that have no excel-header counterpart in the original schema (vendor_name,
 * vendor_address, bank_name...) aren't mapped here yet -- add them when this entity is actually
 * used. consignee/business_place/zip_code/section_code/sap_tax_type/dc_date/payable_amount_ex_gst
 * had no excel counterpart and were dropped in V12.
 */
@Data
@Entity
@Table(name = "vendor_invoice", indexes = {
    @Index(name = "idx_vendor_company", columnList = "vendor_company_id"),
    @Index(name = "idx_vendor_user", columnList = "vendor_user_id"),
    @Index(name = "idx_invoice_number", columnList = "invoice_no")
})
public class VendorInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_no", nullable = false, unique = true, length = 100)
    private String invoiceNo;

    @Column(name = "po_id")
    private Long poId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_company_id", nullable = false)
    private CompanyDetails vendorCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_user_id", nullable = false)
    private UserDetail vendorUser;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "vendor_no", length = 100)
    private String vendorNo;

    @Column(name = "vendor_gstin", length = 50)
    private String vendorGstin;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "invoice_type", length = 50)
    private String invoiceType;

    @Column(name = "grn_delivery_note_no", length = 100)
    private String grnDeliveryNoteNo;

    @Column(name = "tds_section", length = 50)
    private String tdsSection;

    @Column(name = "tds_deducted_pct", precision = 10, scale = 2)
    private BigDecimal tdsDeductedPct;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "portal_status", length = 30)
    private String portalStatus = "SUBMITTED";

    @Column(name = "line_amount_net", precision = 18, scale = 2)
    private BigDecimal lineAmountNet = BigDecimal.ZERO;

    @Column(name = "tax_amount_gst", precision = 18, scale = 2)
    private BigDecimal taxAmountGst = BigDecimal.ZERO;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "line_total_gross", precision = 18, scale = 2)
    private BigDecimal lineTotalGross = BigDecimal.ZERO;

    @Column(name = "net_amount_payable", precision = 18, scale = 2)
    private BigDecimal netAmountPayable = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vendorInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorInvoiceItem> items = new ArrayList<>();
}
