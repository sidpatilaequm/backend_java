package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.OrgConfig;
import com.example.multimedia.file_upload_api.repository.OrgConfigRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Org-wide procurement-stage toggles (see entity.OrgConfig). The GET is deliberately public
 * (under /api/public/**, permitAll in SecurityConfig) since it has to resolve for a vendor
 * filling out the pre-login Become-a-Supplier form, not just for logged-in users — an
 * authenticated caller can hit a permitAll route too, so this one endpoint serves both. Only the
 * PATCH (the actual write) is admin-gated.
 */
@RestController
public class OrgConfigController {

    private final OrgConfigRepository configRepository;
    private final AdminAuthChecker adminAuthChecker;

    public OrgConfigController(OrgConfigRepository configRepository, AdminAuthChecker adminAuthChecker) {
        this.configRepository = configRepository;
        this.adminAuthChecker = adminAuthChecker;
    }

    @GetMapping("/api/public/org-config")
    public ResponseEntity<OrgConfig> getConfig() {
        OrgConfig config = configRepository.findById(1L).orElse(new OrgConfig());
        return ResponseEntity.ok(config);
    }

    @PatchMapping("/api/admin/org-config")
    public ResponseEntity<OrgConfig> updateConfig(@RequestBody Map<String, Object> payload) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        OrgConfig config = configRepository.findById(1L).orElse(new OrgConfig());

        if (payload.containsKey("vendorOnboardingEnabled")) config.setVendorOnboardingEnabled(Boolean.parseBoolean(payload.get("vendorOnboardingEnabled").toString()));
        if (payload.containsKey("prToPoEnabled")) config.setPrToPoEnabled(Boolean.parseBoolean(payload.get("prToPoEnabled").toString()));
        if (payload.containsKey("goodsReceiptWarehouseEnabled")) config.setGoodsReceiptWarehouseEnabled(Boolean.parseBoolean(payload.get("goodsReceiptWarehouseEnabled").toString()));
        if (payload.containsKey("gateEntryShowToVendorEnabled")) config.setGateEntryShowToVendorEnabled(Boolean.parseBoolean(payload.get("gateEntryShowToVendorEnabled").toString()));
        if (payload.containsKey("invoiceVerificationEnabled")) config.setInvoiceVerificationEnabled(Boolean.parseBoolean(payload.get("invoiceVerificationEnabled").toString()));
        if (payload.containsKey("vendorPaymentsEnabled")) config.setVendorPaymentsEnabled(Boolean.parseBoolean(payload.get("vendorPaymentsEnabled").toString()));
        if (payload.containsKey("vendorReturnsEnabled")) config.setVendorReturnsEnabled(Boolean.parseBoolean(payload.get("vendorReturnsEnabled").toString()));
        if (payload.containsKey("creditNotesEnabled")) config.setCreditNotesEnabled(Boolean.parseBoolean(payload.get("creditNotesEnabled").toString()));
        if (payload.containsKey("budgetingEnabled")) config.setBudgetingEnabled(Boolean.parseBoolean(payload.get("budgetingEnabled").toString()));

        configRepository.save(config);
        return ResponseEntity.ok(config);
    }
}
