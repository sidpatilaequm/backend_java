package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "material", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sku", "super_admin_id"})
})
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "base_unit_of_measure", nullable = false)
    private String baseUnitOfMeasure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_subcategory_id", nullable = false)
    private ItemSubcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false)
    private ItemCategory itemCategory;

    @Column(name = "hsn_code", nullable = false)
    private String hsnCode;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "purchasing_code")
    private String purchasingCode;

    @Column(name = "variant_mandatory", nullable = false)
    private Boolean variantMandatory = false;

    @Column(name = "barcode_image", columnDefinition = "LONGBLOB")
    private byte[] barcodeImage;

    @Column(name = "vendor_article_number", nullable = false)
    private String vendorArticleNumber;

    @Column(name = "material_code", unique = true)
    private String materialCode;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<MaterialImage> materialImages;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MaterialVariant> variants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialAttribute> generalAttributes;

    
} 