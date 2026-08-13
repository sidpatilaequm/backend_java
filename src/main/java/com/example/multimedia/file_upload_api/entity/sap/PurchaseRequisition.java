package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "SapPurchaseRequisition")
@Table(name = "sap_purchase_requisitions")
@Getter
@Setter
public class PurchaseRequisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "pr_number", unique = true, length = 50)
    private String prNumber;

    @Column(name = "pr_type", length = 100)
    private String prType;

    @Column(name = "pr_status", length = 50)
    private String prStatus;

    @Column(name = "release_group", length = 50)
    private String releaseGroup;

    @Column(name = "release_indicator", length = 50)
    private String releaseIndicator;

    @Column(name = "sap_status_code", length = 20)
    private String sapStatusCode;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "changed_date")
    private LocalDate changedDate;

    @Column(name = "company_code", length = 20)
    private String companyCode;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "plant", length = 100)
    private String plant;

    @Column(name = "purchasing_group", length = 100)
    private String purchasingGroup;

    @Column(name = "purchasing_org", length = 100)
    private String purchasingOrg;

    @Column(name = "material_number", length = 100)
    private String materialNumber;

    @Column(name = "material_description", length = 500)
    private String materialDescription;

    @Column(name = "material_group", length = 100)
    private String materialGroup;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "valuation_price", precision = 15, scale = 2)
    private BigDecimal valuationPrice;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "fixed_vendor", length = 200)
    private String fixedVendor;

    @Column(name = "account_assignment", length = 200)
    private String accountAssignment;

    @Column(name = "cost_center", length = 100)
    private String costCenter;

    @Column(name = "gl_account", length = 100)
    private String glAccount;

    @Column(name = "hsn_sac_code", length = 50)
    private String hsnSacCode;

    @Column(name = "item_status", length = 50)
    private String itemStatus;

    @Column(name = "header_notes", columnDefinition = "TEXT")
    private String headerNotes;

    @Column(name = "sync_status", length = 50)
    private String syncStatus;

    @CreationTimestamp
    @Column(name = "synced_at", updatable = false)
    private LocalDateTime syncedAt;
}
