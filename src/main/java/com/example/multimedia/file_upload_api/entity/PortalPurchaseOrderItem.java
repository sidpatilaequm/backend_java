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
    private PortalPurchaseOrder purchaseOrder;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

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
