package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "current_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company
    @Column(name = "company_code")
    private String companyCode;

    // Plant
    @Column(name = "plant_code")
    private String plantCode;

    // Storage Location
    @Column(name = "storage_location")
    private String storageLocation;

    // Material Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_description")
    private String materialDescription;

    @Column(name = "material_type")
    private String materialType;

    @Column(name = "material_type_description")
    private String materialTypeDescription;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    // Stock Quantity
    @Column(name = "unrestricted_stock", precision = 18, scale = 3)
    private BigDecimal unrestrictedStock;

    // Vendor Relationship (Optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "supplier_material_code")
    private String supplierMaterialCode;

    // Status
    @Enumerated(EnumType.STRING)
    private StockStatus status;

    // SAP Sync
    @Column(name = "sap_synced")
    private Boolean sapSynced = false;

    @Column(name = "sap_sync_message", length = 1000)
    private String sapSyncMessage;
}
