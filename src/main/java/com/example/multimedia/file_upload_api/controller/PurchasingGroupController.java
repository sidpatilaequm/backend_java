package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.PurchasingGroup;
import com.example.multimedia.file_upload_api.entity.PurchasingOrg;
import com.example.multimedia.file_upload_api.repository.PurchasingGroupRepository;
import com.example.multimedia.file_upload_api.repository.PurchasingOrgRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The buyer/category team under one PurchasingOrg. See Company.java's javadoc for the enterprise-structure design. */
@RestController
@RequestMapping("/api/mm/purchasing-groups")
public class PurchasingGroupController {

    @Autowired
    private PurchasingGroupRepository purchasingGroupRepository;

    @Autowired
    private PurchasingOrgRepository purchasingOrgRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String purchOrgCode) {
        List<PurchasingGroup> groups = (purchOrgCode == null || purchOrgCode.isBlank())
                ? purchasingGroupRepository.findAll()
                : purchasingGroupRepository.findByPurchasingOrg_PurchOrgCode(purchOrgCode.trim().toUpperCase());
        List<Map<String, String>> out = groups.stream().map(PurchasingGroupController::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "purchasingGroups", out));
    }

    @GetMapping("/{purchGroupCode}")
    public ResponseEntity<?> getByCode(@PathVariable String purchGroupCode) {
        return purchasingGroupRepository.findById(purchGroupCode.toUpperCase())
                .map(g -> ResponseEntity.ok(toMap(g)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No purchasing group with code " + purchGroupCode)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String purchGroupCode = trimToNull(body.get("purchGroupCode"));
        String purchGroupName = trimToNull(body.get("purchGroupName"));
        String purchOrgCode = trimToNull(body.get("purchOrgCode"));

        if (purchGroupCode == null || purchGroupCode.length() > 3) {
            return ResponseEntity.badRequest().body(Map.of("message", "purchGroupCode is required and must be at most 3 characters."));
        }
        if (purchGroupName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "purchGroupName is required."));
        }
        if (purchOrgCode == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "purchOrgCode is required."));
        }

        PurchasingOrg org = purchasingOrgRepository.findById(purchOrgCode.toUpperCase()).orElse(null);
        if (org == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No purchasing org with code " + purchOrgCode));
        }

        purchGroupCode = purchGroupCode.toUpperCase();
        // save() upserts for an assigned (non-generated) @Id — check first, see CompanyController.
        if (purchasingGroupRepository.existsById(purchGroupCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Purchasing group code " + purchGroupCode + " already exists."));
        }

        PurchasingGroup group = new PurchasingGroup();
        group.setPurchGroupCode(purchGroupCode);
        group.setPurchGroupName(purchGroupName);
        group.setPurchasingOrg(org);

        try {
            PurchasingGroup saved = purchasingGroupRepository.save(group);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Purchasing group code " + purchGroupCode + " already exists."));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, String> toMap(PurchasingGroup g) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("purchGroupCode", g.getPurchGroupCode());
        m.put("purchGroupName", g.getPurchGroupName());
        m.put("purchOrgCode", g.getPurchasingOrg() != null ? g.getPurchasingOrg().getPurchOrgCode() : null);
        return m;
    }
}
