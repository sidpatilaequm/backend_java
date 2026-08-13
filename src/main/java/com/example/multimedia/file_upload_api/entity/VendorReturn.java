package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "vendor_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Purchase Order
    @Column(name = "purchase_order_number", nullable = false)
    private String purchaseOrderNumber;

    @Column(name = "po_item")
    private Integer poItem;

    // Company
    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "plant_code")
    private String plantCode;

    // Vendor Relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "supplier_code")
    private String supplierCode;

    @Column(name = "supplier_name")
    private String supplierName;

    // Material Relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_description", length = 1000)
    private String materialDescription;

    // Material Document
    @Column(name = "material_document")
    private String materialDocument;

    @Column(name = "movement_type")
    private String movementType;

    // Quantity
    @Column(precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "order_unit")
    private String orderUnit;

    // Pricing
    @Column(precision = 18, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "price_per")
    private Integer pricePer;

    @Column(precision = 18, scale = 2)
    private BigDecimal netValue;

    private String currency;

    // Dates
    private LocalDate documentDate;

    private LocalDate returnDate;

    private LocalTime returnTime;

    @Column(name = "return_item")
    private Boolean returnItem;

    // Status
    @Enumerated(EnumType.STRING)
    private VendorReturnStatus status;

    // SAP Sync
    @Column(name = "sap_synced")
    private Boolean sapSynced = false;

    @Column(name = "sap_sync_message", length = 1000)
    private String sapSyncMessage;
}
