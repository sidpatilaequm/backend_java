package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "vendor_master")
public class VendorMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "bp_no", unique = true, nullable = false)
    private String bpNo;

    // New relation to the admin that owns this vendor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "pan")
    private String pan;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "street_and_house_number")
    private String streetAndHouseNumber;

    @Column(name = "street_name_1")
    private String streetName1;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @CreationTimestamp
    @Column(name = "sys_created_date", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreatedDate;

    @UpdateTimestamp
    @Column(name = "sys_modified_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifiedDate;

}
