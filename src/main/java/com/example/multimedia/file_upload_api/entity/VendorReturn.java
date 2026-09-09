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
 * added by V11 that have no excel-header counterpart in the original schema (e.g. return_document_no,
 * delivery_date, batch_no...) aren't mapped here yet -- add them when this entity is actually used.
 */
@Entity
@Table(name = "vendor_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_po_no", nullable = false)
    private String originalPoNo;

    @Column(name = "po_line_item")
    private Integer poLineItem;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "plant")
    private String plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "vendor_no")
    private String vendorNo;

    @Column(name = "vendor_name")
    private String vendorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_description", length = 1000)
    private String materialDescription;

    @Column(name = "grn_material_doc_no")
    private String grnMaterialDocNo;

    @Column(name = "return_quantity", precision = 18, scale = 3)
    private BigDecimal returnQuantity;

    @Column(name = "uom")
    private String uom;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "return_value_excl_gst", precision = 18, scale = 2)
    private BigDecimal returnValueExclGst;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status")
    private VendorReturnStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
