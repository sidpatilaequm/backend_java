package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTermsDTO {
    private String gstinNumber;
    private String authKey;
    private String deliveryTerms;
    private String deliveryLocation;
    private String blockIndicator;
    private String orderCurrency;
    private String deliveryDays;
    private String reconciliationAccount;
    private String termsOfPayment;
    private Boolean isActive;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
} 