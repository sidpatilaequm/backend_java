package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Company;
import com.example.multimedia.file_upload_api.repository.CompanyRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SAP-style enterprise-structure master data: company code, the top of the
 * company -> plant / company -> purchasing_org -> purchasing_group hierarchy.
 *
 * NOT the same table as CompanyDetails — that's CompanyController's
 * /api/organization/companies, a per-vendor profile row (see Company.java's javadoc for the
 * distinction). Named "Mm" (Materials Management) rather than reusing "CompanyController" to
 * avoid exactly the collision that name invites.
 *
 * GET is open to any authenticated user (reference data needed to build a PR/PO).
 * POST is admin-only and exists to seed this data; per plan, POST will be removed once this
 * master data has been populated for real — no PUT/DELETE are provided at all.
 */
@RestController
@RequestMapping("/api/mm/companies")
public class MmCompanyController {

    private static final Pattern GST_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list() {
        List<Map<String, String>> out = companyRepository.findAll().stream()
                .map(MmCompanyController::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "companies", out));
    }

    @GetMapping("/{companyCode}")
    public ResponseEntity<?> getByCode(@PathVariable String companyCode) {
        return companyRepository.findById(companyCode.toUpperCase())
                .map(c -> ResponseEntity.ok(toMap(c)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No company with code " + companyCode)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String companyCode = trimToNull(body.get("companyCode"));
        String companyName = trimToNull(body.get("companyName"));
        String gstNumber = trimToNull(body.get("gstNumber"));

        if (companyCode == null || companyCode.length() > 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "companyCode is required and must be at most 4 characters."));
        }
        if (companyName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "companyName is required."));
        }
        if (gstNumber != null && !GST_PATTERN.matcher(gstNumber).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "gstNumber is not a valid 15-character GSTIN."));
        }

        companyCode = companyCode.toUpperCase();
        // JPA's save() on an entity with a manually-assigned (non-generated) @Id does a merge
        // (upsert), not an insert — it would silently overwrite an existing row's name/GST
        // rather than rejecting the duplicate, so existence has to be checked explicitly first.
        if (companyRepository.existsById(companyCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Company code " + companyCode + " already exists."));
        }

        Company company = new Company();
        company.setCompanyCode(companyCode);
        company.setCompanyName(companyName);
        company.setGstNumber(gstNumber);

        try {
            Company saved = companyRepository.save(company);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Company code " + companyCode + " already exists."));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, String> toMap(Company c) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("companyCode", c.getCompanyCode());
        m.put("companyName", c.getCompanyName());
        m.put("gstNumber", c.getGstNumber());
        return m;
    }
}
