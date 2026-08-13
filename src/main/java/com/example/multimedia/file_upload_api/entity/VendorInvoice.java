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

@Data
@Entity
@Table(name = "vendor_invoice", indexes = {
    @Index(name = "idx_vendor_company", columnList = "vendor_company_id"),
    @Index(name = "idx_vendor_user", columnList = "vendor_user_id"),
    @Index(name = "idx_invoice_number", columnList = "invoice_number")
})
public class VendorInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

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

    @Column(name = "invoice_due_date")
    private LocalDate invoiceDueDate;

    @Column(name = "vendor_number", length = 100)
    private String vendorNumber;

    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    @Column(name = "invoice_currency", length = 20)
    private String invoiceCurrency;

    @Column(name = "bill_type", length = 50)
    private String billType;

    @Column(name = "consignee", length = 255)
    private String consignee;

    @Column(name = "business_place", length = 255)
    private String businessPlace;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "delivery_note_number", length = 100)
    private String deliveryNoteNumber;

    @Column(name = "section_code", length = 50)
    private String sectionCode;

    @Column(name = "tds_section", length = 50)
    private String tdsSection;

    @Column(name = "tds_rate", precision = 10, scale = 2)
    private BigDecimal tdsRate;

    @Column(name = "sap_tax_type", length = 50)
    private String sapTaxType;

    @Column(name = "sap_tax_code", length = 50)
    private String sapTaxCode;

    @Column(name = "dc_date")
    private LocalDate dcDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "status", length = 30)
    private String status = "SUBMITTED";

    @Column(name = "subtotal_amount", precision = 18, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "gst_total_amount", precision = 18, scale = 2)
    private BigDecimal gstTotalAmount = BigDecimal.ZERO;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "invoice_total_amount", precision = 18, scale = 2)
    private BigDecimal invoiceTotalAmount = BigDecimal.ZERO;

    @Column(name = "payable_amount_inc_gst", precision = 18, scale = 2)
    private BigDecimal payableAmountIncGst = BigDecimal.ZERO;

    @Column(name = "payable_amount_ex_gst", precision = 18, scale = 2)
    private BigDecimal payableAmountExGst = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vendorInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorInvoiceItem> items = new ArrayList<>();
}
