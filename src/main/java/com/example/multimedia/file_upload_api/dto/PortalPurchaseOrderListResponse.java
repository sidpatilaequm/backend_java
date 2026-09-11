package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PortalPurchaseOrderListResponse {
    private Long poId;
    private String poNumber;
    private LocalDate poDate;
    private String status;
    private BigDecimal grandTotal;
    // Which "Procure to pay" tile(s) this PO belongs under (products/services/subcontracting/
    // scheduling) -- derived from the originating PR's doc_type_code -> document_type.classification,
    // same mapping as SupplierRegistrationService.CLASSIFICATION_TILES. Empty when the PR predates
    // doc_type_code being required, or this PO has no linked PR (e.g. a SAP-uploaded Master PO).
    private List<String> tiles;
}
