package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_payments", indexes = {
    @Index(name = "idx_vendor_payment_vendor_id", columnList = "vendor_id"),
    @Index(name = "idx_vendor_payment_doc_number", columnList = "document_number"),
    @Index(name = "idx_vendor_payment_inv_ref", columnList = "invoice_reference"),
    @Index(name = "idx_vendor_payment_status", columnList = "payment_status"),
    @Index(name = "idx_vendor_payment_date", columnList = "payment_date"),
    @Index(name = "idx_vendor_payment_synced_at", columnList = "synced_at")
})
@Getter
@Setter
public class VendorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "company_code", length = 20)
    private String companyCode;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "fiscal_year", length = 10)
    private String fiscalYear;

    @Column(name = "invoice_reference", length = 100)
    private String invoiceReference;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "gross_amount", precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "tds_deducted", precision = 15, scale = 2)
    private BigDecimal tdsDeducted;

    @Column(name = "net_paid", precision = 15, scale = 2)
    private BigDecimal netPaid;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "utr_cheque_number", length = 100)
    private String utrChequeNumber;

    @Column(name = "house_bank", length = 100)
    private String houseBank;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus;

    @Column(name = "overdue_days")
    private Integer overdueDays;

    @Column(name = "sap_document_type", length = 30)
    private String sapDocumentType;

    @Column(name = "sap_reference", length = 100)
    private String sapReference;

    // SAP FI Reference Details
    @Column(name = "fi_document_number", length = 50)
    private String fiDocumentNumber;

    @Column(name = "fi_fiscal_year", length = 10)
    private String fiFiscalYear;

    // Payment Run Details
    @Column(name = "payment_run_date")
    private LocalDate paymentRunDate;
    
    @Column(name = "payment_run_id", length = 50)
    private String paymentRunId;

    // Bank Details
    @Column(name = "vendor_bank_account", length = 50)
    private String vendorBankAccount;

    @Column(name = "vendor_bank_name", length = 200)
    private String vendorBankName;

    // IFSC / Branch
    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    // Reconciliation Account
    @Column(name = "reconciliation_account", length = 50)
    private String reconciliationAccount;

    // Vendor Code
    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    // Penny Drop Verification
    @Column(name = "penny_drop_status", length = 30)
    private String pennyDropStatus;
    
    @Column(name = "penny_drop_ref", length = 100)
    private String pennyDropRef;

    // Payment Timeline Events
    @Column(name = "clearing_date")
    private LocalDate clearingDate;
    
    @Column(name = "clearing_document", length = 50)
    private String clearingDocument;

    // TDS Section
    @Column(name = "tds_section", length = 20)
    private String tdsSection;

    @Column(name = "tds_certificate_number", length = 100)
    private String tdsCertificateNumber;

    // SAP Raw Response tracking
    @Column(name = "sap_raw_response", columnDefinition = "TEXT")
    private String sapRawResponse;

    @Column(name = "payment_term", length = 50)
    private String paymentTerm;

    @Column(name = "posting_date")
    private LocalDate postingDate;

    @Column(name = "baseline_date")
    private LocalDate baselineDate;

    @Column(name = "sync_batch_id", length = 100)
    private String syncBatchId;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
