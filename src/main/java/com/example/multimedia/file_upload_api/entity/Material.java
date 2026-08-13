package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "material", uniqueConstraints = {
        @UniqueConstraint(name = "UK_material_vendor", columnNames = {"material_code", "vendor_id"})
})
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    @Column(name = "type")
    private String type;

    @Column(name = "base_unit_of_measure", nullable = false)
    private String baseUnitOfMeasure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_subcategory_id", nullable = true)
    private ItemSubcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_l1_id", nullable = true)
    private ItemSubcategory subcategoryL1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_l2_id", nullable = true)
    private ItemSubcategory subcategoryL2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_l3_id", nullable = true)
    private ItemSubcategory subcategoryL3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    @Column(name = "hsn_code", nullable = false)
    private String hsnCode;

    @Column(name = "sku")
    private String sku;

    @Column(name = "purchasing_code")
    private String purchasingCode;

    @Column(name = "variant_mandatory", nullable = false)
    private Boolean variantMandatory = false;

    @Column(name = "barcode_image", columnDefinition = "LONGBLOB")
    private byte[] barcodeImage;

    @Column(name = "vendor_article_number")
    private String vendorArticleNumber;

    @Column(name = "material_code")
    private String materialCode;

    private String status;

    @Column(name = "price")
    private Double price;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_terms")
    private String deliveryTerms;

    @Column(name = "contract_period")
    private String contractPeriod;

    @Column(name = "contract_details")
    private String contractDetails;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "vendor_id")
    private Long vendorId;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<MaterialImage> materialImages;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MaterialVariant> variants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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

    // --- SAP Master Data Fields Start ---

    @Column(name = "info_rec")
    private String infoRec;

    @Column(name = "sap_material_description")
    private String sapMaterialDescription;

    @Column(name = "sap_type")
    private String sapType;

    @Column(name = "mat_type_desc")
    private String matTypeDesc;

    @Column(name = "sap_group")
    private String sapGroup;

    @Column(name = "sap_unit")
    private String sapUnit;

    @Column(name = "sap_vendor")
    private String sapVendor;

    @Column(name = "sap_vendor_name")
    private String sapVendorName;

    @Column(name = "pur_org")
    private String purOrg;

    @Column(name = "plant")
    private String plant;

    @Column(name = "sap_name1")
    private String sapName1;

    @Column(name = "sap_price")
    private Double sapPrice;

    @Column(name = "sap_currency")
    private String sapCurrency;

    @Column(name = "company_code")
    private String companyCode;

    // --- SAP Master Data Fields End ---

}