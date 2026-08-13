package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subcon_purchase_orders")
@Getter
@Setter
public class SubconPurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "subcon_po_number", unique = true, length = 50)
    private String subconPoNumber;

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

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "incoterms", length = 200)
    private String incoterms;

    @Column(name = "fg_line_item_no")
    private Integer fgLineItemNo;

    @Column(name = "fg_material_number", length = 100)
    private String fgMaterialNumber;

    @Column(name = "fg_description", length = 500)
    private String fgDescription;

    @Column(name = "fg_ordered_qty", precision = 15, scale = 3)
    private BigDecimal fgOrderedQty;

    @Column(name = "fg_uom", length = 20)
    private String fgUom;

    @Column(name = "processing_charge_per_unit", precision = 15, scale = 2)
    private BigDecimal processingChargePerUnit;

    @Column(name = "total_processing_value", precision = 15, scale = 2)
    private BigDecimal totalProcessingValue;

    @Column(name = "required_delivery_date")
    private LocalDate requiredDeliveryDate;

    @Column(name = "component_line_no", length = 20)
    private String componentLineNo;

    @Column(name = "component_material_no", length = 100)
    private String componentMaterialNo;

    @Column(name = "component_description", length = 500)
    private String componentDescription;

    @Column(name = "required_qty_per_unit", length = 100)
    private String requiredQtyPerUnit;

    @Column(name = "total_issued_qty", length = 100)
    private String totalIssuedQty;

    @Column(name = "stock_at_vendor", length = 100)
    private String stockAtVendor;

    @Column(name = "total_stock_capacity", length = 100)
    private String totalStockCapacity;

    @Column(name = "component_uom", length = 20)
    private String componentUom;

    @Column(name = "scrap_percent", length = 20)
    private String scrapPercent;

    @Column(name = "movement_doc_number", length = 100)
    private String movementDocNumber;

    @Column(name = "movement_type", length = 20)
    private String movementType;

    @Column(name = "movement_description", columnDefinition = "TEXT")
    private String movementDescription;

    @Column(name = "movement_material", length = 200)
    private String movementMaterial;

    @Column(name = "movement_qty", length = 100)
    private String movementQty;

    @Column(name = "movement_date")
    private LocalDate movementDate;

    @Column(name = "invoice_uploaded")
    private Boolean invoiceUploaded = false;

    @Column(name = "quality_certificate_uploaded")
    private Boolean qualityCertificateUploaded = false;

    @Column(name = "sync_status", length = 50)
    private String syncStatus;

    @CreationTimestamp
    @Column(name = "synced_at", updatable = false)
    private LocalDateTime syncedAt;
}
