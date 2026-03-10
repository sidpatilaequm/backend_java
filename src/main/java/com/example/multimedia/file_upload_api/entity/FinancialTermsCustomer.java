package com.example.multimedia.file_upload_api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "financial_terms_customer")
public class FinancialTermsCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long financialTermsCustomerId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    @JsonBackReference
    private CompanyDetails company;

    @Column(name = "delivery_terms")
    private String deliveryTerms;         // INCO1

    @Column(name = "delivery_location")
    private String deliveryLocation;      // INCO2

    @Column(name = "block_indicator")
    private String blockIndicator;

    @Column(name = "order_currency")
    private String orderCurrency;

    @Column(name = "delivery_days")
    private String deliveryDays;

    @Column(name = "reconciliation_account")
    private String reconciliationAccount;

    @Column(name = "terms_of_payment")
    private String termsOfPayment;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
} 