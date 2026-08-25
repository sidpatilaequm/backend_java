package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_entry_id", nullable = false)
    private GateEntry gateEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private Asn asn;

    @Column(name = "decision")
    private String decision; // ACCEPT, REJECT

    @Column(name = "grn_number")
    private String grnNumber;

    @Column(name = "rtv_number")
    private String rtvNumber;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // We can store the final JSON of boxes/lines handled here for history
    @Column(name = "inward_details", columnDefinition = "TEXT")
    private String inwardDetails; 

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
