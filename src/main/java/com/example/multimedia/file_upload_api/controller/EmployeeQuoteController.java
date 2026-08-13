package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.service.EmployeeQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeQuoteController {

    private final EmployeeQuoteService employeeQuoteService;

    @GetMapping("/quote-comparison")
    public ResponseEntity<Map<String, Object>> getQuoteComparison() {
        return ResponseEntity.ok(employeeQuoteService.getQuoteComparison());
    }

    @PostMapping("/award-quote")
    public ResponseEntity<Map<String, Object>> awardQuote(@RequestParam Long quoteId) {
        return ResponseEntity.ok(employeeQuoteService.awardQuote(quoteId));
    }
}
