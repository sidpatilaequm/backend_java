package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "material_variant")
public class MaterialVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "variant_code", unique = true, nullable = false)
    private String variantCode;

    @Column(name = "mrp")
    private Double mrp;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "stock")
    private Double stock;

    @Lob
    @Column(name = "barcode_image", columnDefinition = "LONGBLOB")
    private byte[] barcodeImage;

    @Lob
    @Column(name = "variant_image", columnDefinition = "LONGBLOB")
    private byte[] variantImage;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private List<MaterialAttribute> attributes;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}