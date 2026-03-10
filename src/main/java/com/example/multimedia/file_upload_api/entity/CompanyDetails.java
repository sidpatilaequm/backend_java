package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "company_details")
@Getter
@Setter
public class CompanyDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "gstin_number")
    private String gstinNumber;

    @Column(name = "legal_trade_name")
    private String legalTradeName;

    @Column(name = "registered_address")
    private String registeredAddress;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "pan_tin_cst")
    private String panTinCst;

    @Column(name = "date_of_registration")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfRegistration;

    @Column(name = "type_of_registration")
    private String typeOfRegistration;

    @Column(name = "auth_key")
    private String authKey;

    @Column(name = "gst_file_name")
    private String gstFileName;

    @Column(name = "pan_file_name")
    private String panFileName;

    @Column(name = "cheque_file_name")
    private String chequeFileName;

    @Column(name = "coi_file_name")
    private String coiFileName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDetail user;

    @ManyToOne
    @JoinColumn(name = "super_admin_id")
    private SuperAdmin superAdmin;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private PanDetails panDetails;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private ChequeDetails chequeDetails;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private CertificateOfIncorporation certificateOfIncorporation;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private PurchasingData purchasingData;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FinancialTerms> financialTerms;
}


