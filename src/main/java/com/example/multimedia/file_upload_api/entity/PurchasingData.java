package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "purchasing_data")
@Data
public class PurchasingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchasing_org")
    private String purchasingOrg;

    @Column(name = "order_currency")
    private String orderCurrency;

    @Column(name = "incoterms")
    private String incoterms;

    @Column(name = "terms_of_payment")
    private String termsOfPayment;

    @Column(name = "vendor_schema_group")
    private String vendorSchemaGroup;

    @Column(name = "minimum_order_value")
    private BigDecimal minimumOrderValue;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;
} 