package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Country;
import com.example.multimedia.file_upload_api.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getCountryById(@PathVariable Long id) {
        return ResponseEntity.ok(countryService.getCountryById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createCountry(@RequestBody Country country) {
        return ResponseEntity.ok(countryService.saveCountry(country));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateCountry(@PathVariable Long id, @RequestBody Country country) {
        country.setCountryId(id);
        return ResponseEntity.ok(countryService.saveCountry(country));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteCountry(@PathVariable Long id) {
        return ResponseEntity.ok(countryService.deleteCountry(id));
    }
}
