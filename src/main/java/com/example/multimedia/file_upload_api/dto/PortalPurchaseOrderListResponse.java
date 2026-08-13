package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PortalPurchaseOrderListResponse {
    private Long poId;
    private String poNumber;
    private LocalDate poDate;
    private String status;
    private BigDecimal grandTotal;
}
