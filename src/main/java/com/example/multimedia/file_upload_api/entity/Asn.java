package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "asns")
public class Asn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PortalPurchaseOrder purchaseOrder;

    @Column(name = "vendor_bpno", length = 255)
    private String vendorBpno;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "eway_bill")
    private String ewayBill;

    @Column(name = "ewb_valid_to")
    private LocalDate ewbValidTo;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "transporter_code")
    private String transporterCode;


    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;

    @Column(name = "packaging")
    private String packaging;

    @Column(name = "no_of_packages")
    private Integer noOfPackages;

    @Column(name = "status")
    private String status = "IN_TRANSIT";

    @Column(name = "gate_status")
    private String gateStatus;

    @Column(name = "gate_pass_number")
    private String gatePassNumber;

    // File URLs
    @Column(name = "tax_invoice_url", columnDefinition = "TEXT")
    private String taxInvoiceUrl;

    @Column(name = "eway_bill_url", columnDefinition = "TEXT")
    private String ewayBillUrl;

    @Column(name = "packing_list_url", columnDefinition = "TEXT")
    private String packingListUrl;

    @Column(name = "pdir_url", columnDefinition = "TEXT")
    private String pdirUrl;

    @Column(name = "deviation_url", columnDefinition = "TEXT")
    private String deviationUrl;

    @Column(name = "others_url", columnDefinition = "TEXT")
    private String othersUrl;

    @OneToMany(mappedBy = "asn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsnItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
