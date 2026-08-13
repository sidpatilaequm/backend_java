package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.VendorQuotationResponse;
import com.example.multimedia.file_upload_api.dto.VendorQuotationComparisonResponse;
import com.example.multimedia.file_upload_api.service.VendorQuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/quotations")
public class AdminQuotationController {

    @Autowired
    private VendorQuotationService quotationService;

    @Autowired
    private com.example.multimedia.file_upload_api.util.SecurityContextUtils securityContextUtils;

    @GetMapping("/awarded")
    public ResponseEntity<List<VendorQuotationResponse>> getAwardedQuotations() {
        Long adminId = securityContextUtils.getCurrentAdminId();
        List<VendorQuotationResponse> responses = quotationService.getAwardedQuotationsForAdmin(adminId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorQuotationResponse> getQuotationById(@PathVariable Long id) {
        VendorQuotationResponse response = quotationService.getQuotationByIdForAdmin(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{quotationNumber}")
    public ResponseEntity<VendorQuotationResponse> getQuotationByNumber(@PathVariable String quotationNumber) {
        VendorQuotationResponse response = quotationService.getQuotationByQuotationNumber(quotationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorQuotationResponse>> getQuotationsByVendorId(@PathVariable Long vendorId) {
        List<VendorQuotationResponse> responses = quotationService.getAllQuotationsByVendorIdForAdmin(vendorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pr/{prId}")
    public ResponseEntity<?> getQuotationsByPrId(@PathVariable Long prId) {
        List<VendorQuotationResponse> responses = quotationService.getAllQuotationsByPrIdForAdmin(prId);
        
        List<Map<String, Object>> mappedResponses = responses.stream().map(q -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", q.getQuotationId());
            map.put("quotationNumber", q.getQuotationHeader() != null ? q.getQuotationHeader().getQuotationNumber() : null);
            map.put("quoteDate", q.getQuotationHeader() != null ? q.getQuotationHeader().getQuotationDate() : null);
            map.put("validUntil", q.getQuotationHeader() != null ? q.getQuotationHeader().getValidUntil() : null);
            map.put("grandTotal", q.getGrandTotal());
            map.put("currency", q.getQuotationHeader() != null ? q.getQuotationHeader().getCurrency() : null);
            map.put("status", q.getStatus());
            
            Map<String, Object> remarksMap = new java.util.HashMap<>();
            remarksMap.put("coverNote", q.getRemarks() != null ? q.getRemarks().getCoverNote() : null);
            map.put("remarks", remarksMap);
            
            List<Map<String, Object>> itemsList = new java.util.ArrayList<>();
            if (q.getLineItems() != null) {
                for (VendorQuotationResponse.LineItemResponse item : q.getLineItems()) {
                    Map<String, Object> itemMap = new java.util.LinkedHashMap<>();
                    itemMap.put("id", item.getQuotationItemId());
                    itemMap.put("itemCode", item.getItemCode());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("quotedQty", item.getQuotedQty());
                    itemMap.put("uom", item.getUom());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("gstPercent", item.getGstPercent());
                    itemMap.put("freightAmount", item.getFreightAmount());
                    itemMap.put("lineTotal", item.getLineTotal());
                    itemsList.add(itemMap);
                }
            }
            map.put("lineItems", itemsList);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(mappedResponses);
    }

    @PostMapping("/{quotationId}/award")
    public ResponseEntity<?> awardQuotation(
            @PathVariable Long quotationId,
            @RequestBody(required = false) Map<String, String> payload) {
        String remarks = payload != null ? payload.get("remarks") : null;
        quotationService.awardQuotation(quotationId, remarks);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quotation awarded successfully",
                "quotationId", quotationId,
                "status", "AWARDED"
        ));
    }

    @GetMapping("/pr/{prId}/comparison")
    public ResponseEntity<List<VendorQuotationComparisonResponse>> getQuotationComparison(@PathVariable Long prId) {
        List<VendorQuotationComparisonResponse> comparison = quotationService.getQuotationComparison(prId);
        return ResponseEntity.ok(comparison);
    }
}
