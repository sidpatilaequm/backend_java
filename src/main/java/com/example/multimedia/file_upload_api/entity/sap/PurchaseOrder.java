package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "po_number", unique = true, length = 50)
    private String poNumber;

    @Column(name = "po_type", length = 100)
    private String poType;

    @Column(name = "po_status", length = 50)
    private String poStatus;

    @Column(name = "po_date")
    private LocalDate poDate;

    @Column(name = "company_code", length = 20)
    private String companyCode;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(name = "incoterms", length = 100)
    private String incoterms;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "goods_receipt_plant", length = 200)
    private String goodsReceiptPlant;

    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;

    @Column(name = "shipping_instructions", columnDefinition = "TEXT")
    private String shippingInstructions;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "material_number", length = 100)
    private String materialNumber;

    @Column(name = "material_description", length = 500)
    private String materialDescription;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "net_price", precision = 15, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "net_value", precision = 15, scale = 2)
    private BigDecimal netValue;

    @Column(name = "tax_percent", precision = 10, scale = 2)
    private BigDecimal taxPercent;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "confirm_delivery_date")
    private LocalDate confirmDeliveryDate;

    @Column(name = "gstin", length = 30)
    private String gstin;

    @Column(name = "acknowledgement_status", length = 50)
    private String acknowledgementStatus;

    @Column(name = "invoice_uploaded")
    private Boolean invoiceUploaded = false;

    @Column(name = "sync_status", length = 50)
    private String syncStatus;

    @CreationTimestamp
    @Column(name = "synced_at", updatable = false)
    private LocalDateTime syncedAt;
}
