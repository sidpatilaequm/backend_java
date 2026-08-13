package com.example.multimedia.file_upload_api.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class EmployeeQuoteService {

    public Map<String, Object> getQuoteComparison() {
        // mock logic for returning comparison scores based on vendor responses
        return Collections.singletonMap("comparison", "mocked score data");
    }

    public Map<String, Object> awardQuote(Long quoteId) {
        // mock logic for updating PurchaseRequisitionItemVendor status to AWARDED
        return Collections.singletonMap("status", "AWARDED");
    }
}
