package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Single-row org-wide feature switch — same shape as FolderitSyncConfig. Each flag both hides
 * the corresponding UI section (vendor/employee/admin) and, where a real write path exists,
 * blocks it server-side too (see security.OrgConfigGate). All default true so this table's mere
 * existence never changes behavior — only an admin flipping something does.
 */
@Entity
@Table(name = "org_config")
@Getter
@Setter
public class OrgConfig {

    @Id
    private Long id = 1L; // Single row config

    private boolean vendorOnboardingEnabled = true;
    private boolean prToPoEnabled = true;
    private boolean goodsReceiptWarehouseEnabled = true;
    private boolean gateEntryShowToVendorEnabled = true;
    private boolean invoiceVerificationEnabled = true;
    private boolean vendorPaymentsEnabled = true;
    private boolean vendorReturnsEnabled = true;
    private boolean creditNotesEnabled = true;
    private boolean budgetingEnabled = true;
}
