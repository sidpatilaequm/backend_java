package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Currency;
import com.example.multimedia.file_upload_api.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getCurrencyById(@PathVariable Long id) {
        return ResponseEntity.ok(currencyService.getCurrencyById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createCurrency(@RequestBody Currency currency) {
        return ResponseEntity.ok(currencyService.saveCurrency(currency));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateCurrency(@PathVariable Long id, @RequestBody Currency currency) {
        currency.setCurrencyId(id);
        return ResponseEntity.ok(currencyService.saveCurrency(currency));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteCurrency(@PathVariable Long id) {
        return ResponseEntity.ok(currencyService.deleteCurrency(id));
    }
}
