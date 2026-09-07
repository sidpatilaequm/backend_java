package com.example.multimedia.file_upload_api.security;

import com.example.multimedia.file_upload_api.entity.OrgConfig;
import com.example.multimedia.file_upload_api.repository.OrgConfigRepository;
import org.springframework.stereotype.Component;

/**
 * Shared "is this procurement stage enabled org-wide" check, read from the single-row
 * org_config table. The requireX() methods throw IllegalArgumentException — already caught by
 * GlobalExceptionHandler as a 400, the same convention PurchaseRequisitionServiceImpl and
 * PortalPurchaseOrderServiceImpl already use for validation failures, so callers that already
 * throw IllegalArgumentException for other checks can add these with no new error-handling path.
 * Fails open (returns true / doesn't throw) if the config row is ever missing, matching the
 * "everything on by default" safety net the table itself ships with.
 */
@Component
public class OrgConfigGate {

    private final OrgConfigRepository configRepository;

    public OrgConfigGate(OrgConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    private OrgConfig config() {
        return configRepository.findById(1L).orElse(new OrgConfig());
    }

    public boolean isVendorOnboardingEnabled() { return config().isVendorOnboardingEnabled(); }
    public boolean isPrToPoEnabled() { return config().isPrToPoEnabled(); }
    public boolean isGoodsReceiptWarehouseEnabled() { return config().isGoodsReceiptWarehouseEnabled(); }
    public boolean isGateEntryShowToVendorEnabled() { return config().isGateEntryShowToVendorEnabled(); }
    public boolean isInvoiceVerificationEnabled() { return config().isInvoiceVerificationEnabled(); }
    public boolean isVendorPaymentsEnabled() { return config().isVendorPaymentsEnabled(); }
    public boolean isVendorReturnsEnabled() { return config().isVendorReturnsEnabled(); }
    public boolean isCreditNotesEnabled() { return config().isCreditNotesEnabled(); }
    public boolean isBudgetingEnabled() { return config().isBudgetingEnabled(); }

    public void requireVendorOnboardingEnabled() {
        if (!isVendorOnboardingEnabled()) throw new IllegalArgumentException("Vendor onboarding is currently disabled by your organisation.");
    }

    public void requirePrToPoEnabled() {
        if (!isPrToPoEnabled()) throw new IllegalArgumentException("Purchase requisitions are currently disabled by your organisation.");
    }
}
