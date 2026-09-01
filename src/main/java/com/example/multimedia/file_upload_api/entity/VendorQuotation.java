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
@Table(name = "vendor_quotations")
public class VendorQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_id")
    private Long quotationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_id", nullable = false)
    private PurchaseRequisition purchaseRequisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private CompanyDetails vendor;

    @Column(name = "quotation_number", unique = true, nullable = false)
    private String quotationNumber;

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "vendor_reference_no")
    private String vendorReferenceNo;

    @Column(name = "currency")
    private String currency;

    @Column(name = "validity_days")
    private Integer validityDays;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "payment_terms_id")
    private Long paymentTermsId;

    @Column(name = "advance_required_percent")
    private BigDecimal advanceRequiredPercent;

    @Column(name = "bank_guarantee_required")
    private Boolean bankGuaranteeRequired;

    @Column(name = "incoterm")
    private String incoterm;

    @Column(name = "named_place")
    private String namedPlace;

    @Column(name = "quoted_delivery_date")
    private LocalDate quotedDeliveryDate;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "shipping_mode")
    private String shippingMode;

    @Column(name = "freight_charge_type")
    private String freightChargeType;

    @Column(name = "freight_amount")
    private BigDecimal freightAmount;

    @Column(name = "cover_note", columnDefinition = "TEXT")
    private String coverNote;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "quotation_pdf")
    private String quotationPdf;

    @Column(name = "status")
    private String status;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column(name = "gst_total_amount")
    private BigDecimal gstTotalAmount;

    @Column(name = "grand_total_amount")
    private BigDecimal grandTotalAmount;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @OneToMany(mappedBy = "vendorQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorQuotationItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "vendorQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorQuotationDocument> documents = new ArrayList<>();
}
