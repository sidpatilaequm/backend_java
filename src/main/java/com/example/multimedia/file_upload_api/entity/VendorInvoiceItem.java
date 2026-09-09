package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unused by any service/controller today (see V11/V12 migrations) -- kept accurate to the real
 * table so whoever wires up the actual SAP report import doesn't inherit a stale mapping.
 * pack_code/batch_number/mrp/currency/discount_amount and the cgst/sgst/ugst/igst percent+amount
 * breakdown had no counterpart in the excel (which only has a flat Tax Rate %/Tax Amount) and
 * were dropped in V12 -- re-add them here if a source that actually reports the GST split shows up.
 */
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

    @Column(name = "material_service_code", length = 100)
    private String materialServiceCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "hsn_sac_code", length = 50)
    private String hsnSacCode;

    @Column(name = "po_line_item")
    private String poLineItem;

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "uom")
    private String uom;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_amount_net", precision = 18, scale = 2)
    private BigDecimal lineAmountNet;

    @Column(name = "tax_rate_pct", precision = 6, scale = 2)
    private BigDecimal taxRatePct;

    @Column(name = "tax_amount_gst", precision = 18, scale = 2)
    private BigDecimal taxAmountGst;

    @Column(name = "line_total_gross", precision = 18, scale = 2)
    private BigDecimal lineTotalGross;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
