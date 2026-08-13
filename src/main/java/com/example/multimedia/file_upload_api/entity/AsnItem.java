package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "asn_items")
public class AsnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private Asn asn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_id", nullable = false)
    private PortalPurchaseOrderItem purchaseOrderItem;

    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(name = "quantity_shipped", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantityShipped;

    @Column(name = "batch_heat_number")
    private String batchHeatNumber;

    @Column(name = "test_cert_url", columnDefinition = "TEXT")
    private String testCertUrl;
}
