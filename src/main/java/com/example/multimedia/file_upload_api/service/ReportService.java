package com.example.multimedia.file_upload_api.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    public List<Map<String, Object>> getMaterialStock() {
        return Collections.singletonList(Collections.singletonMap("status", "mocked material stock"));
    }

    public List<Map<String, Object>> getCreditNotes() {
        return Collections.singletonList(Collections.singletonMap("status", "mocked credit notes"));
    }

    public List<Map<String, Object>> getVendorReturns() {
        return Collections.singletonList(Collections.singletonMap("status", "mocked vendor returns"));
    }

    public List<Map<String, Object>> getVendorPayments() {
        return Collections.singletonList(Collections.singletonMap("status", "mocked vendor payments"));
    }
}
