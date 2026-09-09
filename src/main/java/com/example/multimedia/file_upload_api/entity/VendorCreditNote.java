package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unused by any service/controller today (see V11/V12 migrations) -- kept accurate to the real
 * table so whoever wires up the actual SAP report import doesn't inherit a stale mapping. Columns
 * added by V11 that have no excel-header counterpart in the original schema (e.g. vendor_gstin,
 * original_invoice_no, bank_name...) aren't mapped here yet -- add them when this entity is
 * actually used.
 */
@Entity
@Table(name = "vendor_credit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorCreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_code")
    private String companyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "vendor_no")
    private String vendorNo;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "original_po_no")
    private String originalPoNo;

    @Column(name = "po_line_item")
    private Integer poLineItem;

    @Column(name = "plant")
    private String plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "material_service_code")
    private String materialServiceCode;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "credit_note_no")
    private String creditNoteNo;

    @Column(name = "credit_note_date")
    private LocalDate creditNoteDate;

    @Column(name = "credit_quantity", precision = 18, scale = 3)
    private BigDecimal creditQuantity;

    @Column(name = "uom")
    private String uom;

    @Column(name = "total_credit_amount", precision = 18, scale = 2)
    private BigDecimal totalCreditAmount;

    @Column(length = 10)
    private String currency;

    @Column(name = "tds_section", length = 20)
    private String tdsSection;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
