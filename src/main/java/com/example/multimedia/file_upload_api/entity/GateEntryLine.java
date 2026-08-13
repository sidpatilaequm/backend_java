package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "gate_entry_lines")
public class GateEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_entry_id", nullable = false)
    private GateEntry gateEntry;

    @Column(name = "material_code", nullable = false)
    private String materialCode;

    @Column(name = "declared_qty", precision = 19, scale = 4)
    private BigDecimal declaredQty;

    @Column(name = "counted_qty", precision = 19, scale = 4)
    private BigDecimal countedQty;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
