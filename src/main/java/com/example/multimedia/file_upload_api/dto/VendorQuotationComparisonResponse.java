package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VendorQuotationComparisonResponse {
    private Integer rank;
    private Long quotationId;
    private String quotationNumber;
    private Long vendorId;
    private String vendorName;
    private BigDecimal grandTotal;
    private Integer deliveryDays;
    private String paymentTerms;
    private LocalDate validUntil;
    private String status;
}
