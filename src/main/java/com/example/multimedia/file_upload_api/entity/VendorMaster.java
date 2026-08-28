package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The lean vendor identity record — just enough to be a stable reference point for RFQ/PO/Gate
 * Entry/payments to foreign-key against. Everything about who the vendor actually is (name,
 * contact, GST/PAN, certifications, bank details...) lives on the linked SupplierRegistration,
 * reached via supplierRegistration below — not duplicated here. A null supplierRegistration means
 * this vendor predates that link (legacy/SAP-imported data).
 */
@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "vendor_master")
public class VendorMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "bp_no", unique = true, nullable = false)
    private String bpNo;

    // New relation to the admin that owns this vendor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_registration_id")
    private SupplierRegistration supplierRegistration;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "plant_code")
    private String plantCode;

    @Column(name = "purch_org_code")
    private String purchOrgCode;

    @CreationTimestamp
    @Column(name = "sys_created_date", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreatedDate;

    @UpdateTimestamp
    @Column(name = "sys_modified_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifiedDate;

}
