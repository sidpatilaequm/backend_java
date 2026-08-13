package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_purchase_orders")
@Getter
@Setter
public class ServicePurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "service_po_number", unique = true, length = 50)
    private String servicePoNumber;

    @Column(name = "po_status", length = 50)
    private String poStatus;

    @Column(name = "po_date")
    private LocalDate poDate;

    @Column(name = "service_period_from")
    private LocalDate servicePeriodFrom;

    @Column(name = "service_period_to")
    private LocalDate servicePeriodTo;

    @Column(name = "company_code", length = 20)
    private String companyCode;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "currency", length = 20)
    private String currency;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "ses_number", length = 100)
    private String sesNumber;

    @Column(name = "ses_month", length = 50)
    private String sesMonth;

    @Column(name = "ses_status", length = 50)
    private String sesStatus;

    @Column(name = "vendor_address", columnDefinition = "TEXT")
    private String vendorAddress;

    @Column(name = "gst_number", length = 30)
    private String gstNumber;

    @Column(name = "pan_number", length = 30)
    private String panNumber;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "service_number", length = 100)
    private String serviceNumber;

    @Column(name = "service_description", length = 500)
    private String serviceDescription;

    @Column(name = "quantity", precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "rate", precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "net_value", precision = 15, scale = 2)
    private BigDecimal netValue;

    @Column(name = "cost_centre", length = 100)
    private String costCentre;

    @Column(name = "timesheet_uploaded")
    private Boolean timesheetUploaded = false;

    @Column(name = "invoice_uploaded")
    private Boolean invoiceUploaded = false;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(name = "sync_status", length = 50)
    private String syncStatus;

    @CreationTimestamp
    @Column(name = "synced_at", updatable = false)
    private LocalDateTime syncedAt;
}
