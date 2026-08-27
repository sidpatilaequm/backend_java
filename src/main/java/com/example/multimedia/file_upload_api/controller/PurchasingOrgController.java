package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Company;
import com.example.multimedia.file_upload_api.entity.PurchasingOrg;
import com.example.multimedia.file_upload_api.repository.CompanyRepository;
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

/** A purchasing organisation under one Company. See Company.java's javadoc for the enterprise-structure design. */
@RestController
@RequestMapping("/api/mm/purchasing-orgs")
public class PurchasingOrgController {

    @Autowired
    private PurchasingOrgRepository purchasingOrgRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String companyCode) {
        List<PurchasingOrg> orgs = (companyCode == null || companyCode.isBlank())
                ? purchasingOrgRepository.findAll()
                : purchasingOrgRepository.findByCompany_CompanyCode(companyCode.trim().toUpperCase());
        List<Map<String, String>> out = orgs.stream().map(PurchasingOrgController::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "purchasingOrgs", out));
    }

    @GetMapping("/{purchOrgCode}")
    public ResponseEntity<?> getByCode(@PathVariable String purchOrgCode) {
        return purchasingOrgRepository.findById(purchOrgCode.toUpperCase())
                .map(o -> ResponseEntity.ok(toMap(o)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No purchasing org with code " + purchOrgCode)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String purchOrgCode = trimToNull(body.get("purchOrgCode"));
        String purchOrgName = trimToNull(body.get("purchOrgName"));
        String companyCode = trimToNull(body.get("companyCode"));

        if (purchOrgCode == null || purchOrgCode.length() > 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "purchOrgCode is required and must be at most 4 characters."));
        }
        if (purchOrgName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "purchOrgName is required."));
        }
        if (companyCode == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "companyCode is required."));
        }

        Company company = companyRepository.findById(companyCode.toUpperCase()).orElse(null);
        if (company == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No company with code " + companyCode));
        }

        purchOrgCode = purchOrgCode.toUpperCase();
        // save() upserts for an assigned (non-generated) @Id — check first, see CompanyController.
        if (purchasingOrgRepository.existsById(purchOrgCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Purchasing org code " + purchOrgCode + " already exists."));
        }

        PurchasingOrg org = new PurchasingOrg();
        org.setPurchOrgCode(purchOrgCode);
        org.setPurchOrgName(purchOrgName);
        org.setCompany(company);

        try {
            PurchasingOrg saved = purchasingOrgRepository.save(org);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Purchasing org code " + purchOrgCode + " already exists."));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, String> toMap(PurchasingOrg o) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("purchOrgCode", o.getPurchOrgCode());
        m.put("purchOrgName", o.getPurchOrgName());
        m.put("companyCode", o.getCompany() != null ? o.getCompany().getCompanyCode() : null);
        return m;
    }
}
