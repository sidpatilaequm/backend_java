package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_of_incorporation")
@Getter
@Setter
public class CertificateOfIncorporation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateOfIncorporationId;

    @Column(name = "cin_number")
    private String cinNumber;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    private String businessName;
    private String rocCode;
    private String registrationNumber;
    private String category;
    private String subCategory;
    
    @Column(name = "company_class")
    private String companyClass;
    
    private String authorizedCapital;
    private String paidCapital;
    private String incorporatedDate;
    private String email;
    private Boolean listed;
    private String lastAGMDate;
    private String lastBSDate;
    private Boolean active;
    private String status;
    private Boolean inc22AFiled;
    private String soatDate;
    private String regionalDirector;
    private String region;
    private Boolean suspendedAtStockExchange;
    private String insolvencyStatus;
    private String subscribedCapital;
    private String incorporatedCountry;
    private String officeType;
    private String companyType;
    private String type;

    @Column(columnDefinition = "LONGTEXT")
    private String addressesJson; // JSON representation of address list

    @Column(columnDefinition = "LONGTEXT")
    private String directorsJson; // JSON representation of directors list

    @Column(columnDefinition = "LONGTEXT")
    private String chargesJson; // JSON representation of charges list

    @Column(columnDefinition = "LONGTEXT")
    private String efilingsJson; // JSON representation of e-filings list

    private String indexId;
    private Long updatedTimestamp;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}