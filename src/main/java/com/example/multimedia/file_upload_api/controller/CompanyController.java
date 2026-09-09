package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.service.CompanyDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/companies")
public class CompanyController {

    @Autowired
    private CompanyDetailsService companyDetailsService;

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllCompanies() {
        return ResponseEntity.ok(companyDetailsService.getAllCompanies());
    }

    // The logged-in vendor's own company_details record (V9 migration) — distinct route from
    // /{id} below (which is admin-only, ownership-checked against the CALLER's own tenant, not
    // scoped to "the caller's own vendor record"). Matched ahead of /{id} since "me" is a literal
    // path segment, not a Long.
    @GetMapping("/me")
    public ResponseEntity<ServiceResponse> getMyCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            ServiceResponse response = new ServiceResponse();
            response.setStatus("ERROR");
            response.setStatusMsg("Unauthorized");
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(companyDetailsService.getMyCompany(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyDetailsService.getCompanyById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createCompany(@RequestBody CompanyDetails company) {
        return ResponseEntity.ok(companyDetailsService.saveCompany(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateCompany(@PathVariable Long id, @RequestBody CompanyDetails company) {
        company.setCompanyId(id);
        return ResponseEntity.ok(companyDetailsService.saveCompany(company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyDetailsService.deleteCompany(id));
    }
}
