package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "portal_purchase_order_items")
public class PortalPurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PortalPurchaseOrder purchaseOrder;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "document_item", length = 10)
    private String documentItem;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "company_code", length = 10)
    private String companyCode;

    @Column(name = "plant", length = 10)
    private String plant;

    @Column(name = "storage_location", length = 10)
    private String storageLocation;

    @Column(name = "req_tracking_number", length = 50)
    private String reqTrackingNumber;

    @Column(name = "material_group", length = 20)
    private String materialGroup;

    @Column(name = "purchasing_info_record", length = 20)
    private String purchasingInfoRecord;

    @Column(name = "material_number", nullable = false, length = 100)
    private String materialNumber;

    @Column(name = "material_description", length = 500)
    private String materialDescription;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "net_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal netValue;

    @Column(name = "tax_percent", precision = 10, scale = 2)
    private BigDecimal taxPercent;

    @Column(name = "price_unit")
    private Integer priceUnit;

    @Column(name = "igst_percent", precision = 5, scale = 2)
    private BigDecimal igstPercent;

    @Column(name = "sgst_percent", precision = 5, scale = 2)
    private BigDecimal sgstPercent;

    @Column(name = "cgst_percent", precision = 5, scale = 2)
    private BigDecimal cgstPercent;

    @Column(name = "igst_amount", precision = 15, scale = 2)
    private BigDecimal igstAmount;

    @Column(name = "sgst_amount", precision = 15, scale = 2)
    private BigDecimal sgstAmount;

    @Column(name = "cgst_amount", precision = 15, scale = 2)
    private BigDecimal cgstAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "shipped_quantity", precision = 15, scale = 2)
    private BigDecimal shippedQuantity = BigDecimal.ZERO;

    public String getShortText() {
        return materialDescription;
    }

    public String getOrderUnit() {
        return uom;
    }
}
