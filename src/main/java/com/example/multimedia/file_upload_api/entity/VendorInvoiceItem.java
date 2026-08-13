package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "vendor_invoice_item")
public class VendorInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private VendorInvoice vendorInvoice;

    @Column(name = "sr_no")
    private Integer srNo;

    @Column(name = "pack_code", length = 100)
    private String packCode;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "hsn_code", length = 50)
    private String hsnCode;

    @Column(name = "item_code", length = 100)
    private String itemCode;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "mrp", precision = 18, scale = 2)
    private BigDecimal mrp;

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "rate", precision = 18, scale = 2)
    private BigDecimal rate;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "basic_amount", precision = 18, scale = 2)
    private BigDecimal basicAmount;

    @Column(name = "cgst_percent", precision = 10, scale = 2)
    private BigDecimal cgstPercent;

    @Column(name = "cgst_amount", precision = 18, scale = 2)
    private BigDecimal cgstAmount;

    @Column(name = "sgst_percent", precision = 10, scale = 2)
    private BigDecimal sgstPercent;

    @Column(name = "sgst_amount", precision = 18, scale = 2)
    private BigDecimal sgstAmount;

    @Column(name = "ugst_percent", precision = 10, scale = 2)
    private BigDecimal ugstPercent;

    @Column(name = "ugst_amount", precision = 18, scale = 2)
    private BigDecimal ugstAmount;

    @Column(name = "igst_percent", precision = 10, scale = 2)
    private BigDecimal igstPercent;

    @Column(name = "igst_amount", precision = 18, scale = 2)
    private BigDecimal igstAmount;

    @Column(name = "line_total", precision = 18, scale = 2)
    private BigDecimal lineTotal;
}
