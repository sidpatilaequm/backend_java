package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.service.CompanyDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
