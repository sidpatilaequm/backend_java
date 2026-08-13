package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterPurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_no")
    private String docNo;

    @Column(name = "doc_cat")
    private String docCat;

    private String category;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "doc_type_text")
    private String docTypeText;

    private String vendor;

    @Column(name = "vendor_name")
    private String vendorName;

    private String item;

    @Column(name = "material_number")
    private String materialNumber;

    @Column(name = "short_text")
    private String shortText;

    private Double quantity;

    @Column(name = "order_unit")
    private String orderUnit;

    @Column(name = "co_code")
    private String coCode;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;
}
