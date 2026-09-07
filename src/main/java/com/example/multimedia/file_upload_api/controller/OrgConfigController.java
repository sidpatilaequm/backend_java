package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.OrgConfig;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisition;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import com.example.multimedia.file_upload_api.repository.OrgConfigRepository;
import com.example.multimedia.file_upload_api.repository.PurchaseRequisitionRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Org-wide procurement-stage toggles (see entity.OrgConfig). The GET is deliberately public
 * (under /api/public/**, permitAll in SecurityConfig) since it has to resolve for a vendor
 * filling out the pre-login Become-a-Supplier form, not just for logged-in users — an
 * authenticated caller can hit a permitAll route too, so this one endpoint serves both. Only the
 * PATCH (the actual write) is admin-gated.
 */
@RestController
public class OrgConfigController {

    // PRs whose status means the PR-to-PO pipeline is done with them one way or another — a PR in
    // any OTHER status still needs PR-to-PO turned on to keep moving, so those are what blocks the
    // toggle (see requireNoInProcessPurchaseRequisitions below).
    private static final List<PurchaseRequisitionStatus> TERMINAL_PR_STATUSES = List.of(
            PurchaseRequisitionStatus.PO_CREATED, PurchaseRequisitionStatus.REJECTED, PurchaseRequisitionStatus.CLOSED);

    private final OrgConfigRepository configRepository;
    private final AdminAuthChecker adminAuthChecker;
    private final PurchaseRequisitionRepository purchaseRequisitionRepository;

    public OrgConfigController(OrgConfigRepository configRepository, AdminAuthChecker adminAuthChecker,
                                PurchaseRequisitionRepository purchaseRequisitionRepository) {
        this.configRepository = configRepository;
        this.adminAuthChecker = adminAuthChecker;
        this.purchaseRequisitionRepository = purchaseRequisitionRepository;
    }

    @GetMapping("/api/public/org-config")
    public ResponseEntity<OrgConfig> getConfig() {
        OrgConfig config = configRepository.findById(1L).orElse(new OrgConfig());
        return ResponseEntity.ok(config);
    }

    /**
     * Which employee-dashboard cards are currently visible, computed here (not left to the
     * frontend caching an org-config snapshot in AuthContext) so a stale value from before an
     * admin flipped a toggle — e.g. still held from a previous session in the same tab — can
     * never show a card that's actually disabled. Public for the same reason getConfig() is:
     * nothing sensitive in a list of card ids, and it keeps this resolvable without a token
     * timing race right after login.
     */
    @GetMapping("/api/public/employee-dashboard-cards")
    public ResponseEntity<Map<String, Object>> getEmployeeDashboardCards() {
        OrgConfig config = configRepository.findById(1L).orElse(new OrgConfig());
        List<String> cards = new ArrayList<>(List.of(
                "vendorList", "material", "indent", "asn", "gate-entry",
                "material-inward", "stock", "admin-workflows", "dashboards"));
        if (config.isPrToPoEnabled()) cards.addAll(List.of("pr", "rfq", "quotation", "po"));
        if (config.isInvoiceVerificationEnabled()) cards.add("invoice");
        if (config.isVendorPaymentsEnabled()) cards.add("vendor-payment");
        if (config.isVendorReturnsEnabled()) cards.add("vendor-returns");
        return ResponseEntity.ok(Map.of("cards", cards));
    }

    @PatchMapping("/api/admin/org-config")
    public ResponseEntity<OrgConfig> updateConfig(@RequestBody Map<String, Object> payload) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        OrgConfig config = configRepository.findById(1L).orElse(new OrgConfig());

        if (payload.containsKey("vendorOnboardingEnabled")) config.setVendorOnboardingEnabled(Boolean.parseBoolean(payload.get("vendorOnboardingEnabled").toString()));
        if (payload.containsKey("prToPoEnabled")) {
            boolean requested = Boolean.parseBoolean(payload.get("prToPoEnabled").toString());
            if (!requested) requireNoInProcessPurchaseRequisitions();
            config.setPrToPoEnabled(requested);
        }
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

    /**
     * The founder's explicit call: if any PR hasn't yet resulted in a PO (or been rejected/closed),
     * disabling PR-to-PO is refused outright rather than silently leaving that PR stuck — the admin
     * has to see exactly which PR(s) first. Thrown as IllegalArgumentException so
     * GlobalExceptionHandler turns it into the same 400 + message shape every other org-config
     * validation failure already uses.
     */
    private void requireNoInProcessPurchaseRequisitions() {
        List<PurchaseRequisition> inProcess = purchaseRequisitionRepository
                .findTop20ByStatusNotInOrderByCreatedAtDesc(TERMINAL_PR_STATUSES);
        if (inProcess.isEmpty()) return;

        long totalCount = purchaseRequisitionRepository.countByStatusNotIn(TERMINAL_PR_STATUSES);
        String prList = inProcess.stream().map(PurchaseRequisition::getPrNumber).collect(Collectors.joining(", "));
        String suffix = totalCount > inProcess.size() ? " and " + (totalCount - inProcess.size()) + " more" : "";
        throw new IllegalArgumentException(
                "Cannot disable PR to PO — " + totalCount + " purchase requisition(s) are still in process: "
                        + prList + suffix + ".");
    }
}
