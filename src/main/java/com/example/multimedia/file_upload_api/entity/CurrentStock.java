package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unused by any service/controller today (see V11/V12 migrations) -- kept accurate to the real
 * table so whoever wires up the actual SAP report import doesn't inherit a stale mapping. Columns
 * added by V11 that have no excel-header counterpart in the original schema (vendor_gstin,
 * hsn_sac_code, last_supplied_po_no...) aren't mapped here yet -- add them when this entity is
 * actually used. `status` had no excel counterpart and was dropped in V12.
 */
@Entity
@Table(name = "current_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant")
    private String plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_description")
    private String materialDescription;

    @Column(name = "uom")
    private String uom;

    @Column(name = "total_stock_quantity", precision = 18, scale = 3)
    private BigDecimal totalStockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    @Column(name = "vendor_no")
    private String vendorNo;

    @Column(name = "vendor_name")
    private String vendorName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
