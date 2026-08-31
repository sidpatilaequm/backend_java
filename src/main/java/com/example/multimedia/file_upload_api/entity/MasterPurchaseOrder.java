package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Column(name = "document_item", length = 10)
    private String documentItem;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "plant", length = 10)
    private String plant;

    @Column(name = "storage_location", length = 10)
    private String storageLocation;

    @Column(name = "req_tracking_number", length = 50)
    private String reqTrackingNumber;

    @Column(name = "material_group", length = 20)
    private String materialGroup;

    @Column(name = "purchasing_info_record", length = 20)
    private String purchasingInfoRecord;

    @Column(name = "net_order_price")
    private Double netOrderPrice;

    @Column(name = "price_unit")
    private Integer priceUnit;

    @Column(name = "net_order_value")
    private Double netOrderValue;

    @Column(name = "igst_percent")
    private Double igstPercent;

    @Column(name = "sgst_percent")
    private Double sgstPercent;

    @Column(name = "cgst_percent")
    private Double cgstPercent;

    @Column(name = "igst_amount")
    private Double igstAmount;

    @Column(name = "sgst_amount")
    private Double sgstAmount;

    @Column(name = "cgst_amount")
    private Double cgstAmount;

    @Column(name = "gross_order_value")
    private Double grossOrderValue;

    @Column(name = "sys_created_date")
    private LocalDateTime sysCreatedDate;
}
