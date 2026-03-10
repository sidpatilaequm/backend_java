package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchasingDataDTO {
    private Long id;
    private String purchasingOrg;
    private String orderCurrency;
    private String incoterms;
    private String termsOfPayment;
    private String vendorSchemaGroup;
    private BigDecimal minimumOrderValue;
    private Integer deliveryDays;
    private Long companyId;
} 