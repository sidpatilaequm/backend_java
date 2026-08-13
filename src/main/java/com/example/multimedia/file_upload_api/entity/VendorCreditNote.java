package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vendor_credit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorCreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company
    @Column(name = "company_code")
    private String companyCode;

    // Vendor Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "vendor_name")
    private String vendorName;

    // Purchase Order
    @Column(name = "purchase_order_number")
    private String purchaseOrderNumber;

    @Column(name = "po_item")
    private Integer poItem;

    // Plant
    @Column(name = "plant_code")
    private String plantCode;

    @Column(name = "storage_location")
    private String storageLocation;

    // Material Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_description", length = 1000)
    private String materialDescription;

    // Movement
    @Column(name = "movement_type")
    private String movementType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "material_document")
    private String materialDocument;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "posting_date")
    private LocalDate postingDate;

    // Quantity
    @Column(precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    // Amount
    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    // Status
    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status;

    // SAP Sync
    @Column(name = "sap_synced")
    private Boolean sapSynced = false;

    @Column(name = "sap_sync_message", length = 1000)
    private String sapSyncMessage;
}
