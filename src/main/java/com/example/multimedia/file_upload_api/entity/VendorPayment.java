package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "VendorPaymentMain")
@Table(name = "vendor_payments_main")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vendor Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorMaster vendor;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "vendor_name")
    private String vendorName;

    // Company
    @Column(name = "company_code")
    private String companyCode;

    // Payment Details
    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_run_date")
    private LocalDate paymentRunDate;

    @Column(name = "payment_run_id")
    private String paymentRunId;

    @Column(name = "payment_document_number")
    private String paymentDocumentNumber;

    @Column(name = "payment_amount", precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(length = 10)
    private String currency;

    // Bank Details
    @Column(name = "utr_number")
    private String utrNumber;

    @Column(name = "payee_name")
    private String payeeName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_id")
    private String bankAccountId;

    @Column(name = "house_bank")
    private String houseBank;

    // Status
    @Enumerated(EnumType.STRING)
    private VendorPaymentStatus status;

    // SAP Sync
    @Column(name = "sap_synced")
    private Boolean sapSynced = false;

    @Column(name = "sap_sync_message", length = 1000)
    private String sapSyncMessage;
}
