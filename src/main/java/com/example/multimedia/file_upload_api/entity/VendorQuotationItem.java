package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "vendor_quotation_items")
public class VendorQuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_item_id")
    private Long quotationItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private VendorQuotation vendorQuotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_line_id", nullable = false)
    private PurchaseRequisitionItem purchaseRequisitionItem;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "pr_qty")
    private BigDecimal prQty;

    @Column(name = "quoted_qty")
    private BigDecimal quotedQty;

    @Column(name = "uom")
    private String uom;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "gst_percent")
    private BigDecimal gstPercent;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "payment_terms_id")
    private Long paymentTermsId;

    @Column(name = "incoterm")
    private String incoterm;

    @Column(name = "freight_amount")
    private BigDecimal freightAmount;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    @Column(name = "gst_amount")
    private BigDecimal gstAmount;
}
