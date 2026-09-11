package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.PrLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The Audit Log's "PR Lifecycle" tab — one PR's full journey (workflow approval, RFQ, quotations,
 * PO, ASN, Gate Entry, Material Inward) assembled read-only from every table in that chain. See
 * PrLifecycleService for the actual aggregation; this controller only gates and delegates, same
 * shape as AuditLogController/ApprovalController.
 */
@RestController
@RequestMapping("/api/admin/audit-log/pr-lifecycle")
public class PrLifecycleController {

    private final PrLifecycleService service;
    private final AdminAuthChecker adminAuthChecker;

    public PrLifecycleController(PrLifecycleService service, AdminAuthChecker adminAuthChecker) {
        this.service = service;
        this.adminAuthChecker = adminAuthChecker;
    }

    @GetMapping
    public ResponseEntity<?> getLifecycle(@RequestParam(required = false) String prNumber,
                                           @RequestParam(required = false) String poNumber) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
        }
        if ((prNumber == null || prNumber.isBlank()) && (poNumber == null || poNumber.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("detail", "prNumber or poNumber is required."));
        }
        try {
            // A PO created with no PR (e.g. a SAP Master PO) has no PR number to look up by, so
            // this also accepts poNumber directly — see PrLifecycleService.getLifecycleForPo.
            Map<String, Object> result = (prNumber != null && !prNumber.isBlank())
                    ? service.getLifecycle(prNumber)
                    : service.getLifecycleForPo(poNumber);
            return ResponseEntity.ok(result);
        } catch (PrLifecycleService.NotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "q", required = false) String q) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
        }
        return ResponseEntity.ok(Map.of(
                "prNumbers", service.searchPrNumbers(q, 20),
                // POs with no PR at all -- a PR-linked PO is already reachable by searching its PR.
                "poNumbers", service.searchPoNumbers(q, 20)
        ));
    }

    /** Default (no PR picked yet) view: a flat, paginated feed across the most recently active PRs. */
    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));
        }
        return ResponseEntity.ok(service.getFeed(Math.max(page, 0), size));
    }
}
