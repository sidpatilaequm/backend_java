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
@Table(name = "portal_purchase_orders")
public class PortalPurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", unique = true, nullable = false, length = 50)
    private String poNumber;

    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_id")
    private PurchaseRequisition purchaseRequisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    private VendorQuotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private CompanyDetails vendor;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "payment_terms_id")
    private Long paymentTermsId;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;

    @Column(name = "confirmed_delivery_date")
    private LocalDate confirmedDeliveryDate;

    @Column(name = "shipping_instructions", columnDefinition = "TEXT")
    private String shippingInstructions;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "RELEASED";

    // Set when the vendor acknowledges the PO — see PortalPurchaseOrderServiceImpl.acknowledgePO.
    // Null until then; not reused for any other status change, unlike modifiedDate.
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "company_code", length = 10)
    private String companyCode;

    @Column(name = "purchasing_doc_type", length = 10)
    private String purchasingDocType;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "language_key", length = 10)
    private String languageKey;

    @Column(name = "purchasing_organization", length = 20)
    private String purchasingOrganization;

    @Column(name = "purchasing_group", length = 20)
    private String purchasingGroup;

    @Column(name = "incoterms", length = 20)
    private String incoterms;

    @Column(name = "incoterms_part2", length = 100)
    private String incotermsPart2;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "gst_total", precision = 15, scale = 2)
    private BigDecimal gstTotal;

    @Column(name = "freight_total", precision = 15, scale = 2)
    private BigDecimal freightTotal;

    @Column(name = "grand_total", precision = 15, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortalPurchaseOrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    public String getVendorName() {
        return vendor != null ? vendor.getCompanyName() : null;
    }
}
