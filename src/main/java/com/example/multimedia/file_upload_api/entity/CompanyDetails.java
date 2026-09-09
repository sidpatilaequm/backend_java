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
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "company_details")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CompanyDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(name = "company_name")
    private String companyName;

    @NotBlank(message = "Company code is required")
    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

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
    private MsmeDetails msmeDetails;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private ItrDetails itrDetails;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private PurchasingData purchasingData;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FinancialTerms> financialTerms;

    // ── Live vendor-profile fields (V9 migration) ──────────────────────────────────────
    // company_details is now the source of truth for an approved vendor's profile.
    // supplierRegistration is the link back to the original onboarding application these
    // fields were seeded from (and, via VendorChangeRequestService, kept in sync with) —
    // the application itself stays the system of record for documents/OCR/audit history.

    @ManyToOne
    @JoinColumn(name = "supplier_registration_id")
    private SupplierRegistration supplierRegistration;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "contact1_name")
    private String contact1Name;

    @Column(name = "contact1_role")
    private String contact1Role;

    @Column(name = "contact1_email")
    private String contact1Email;

    @Column(name = "contact1_phone")
    private String contact1Phone;

    @Column(name = "contact2_name")
    private String contact2Name;

    @Column(name = "contact2_role")
    private String contact2Role;

    @Column(name = "contact2_email")
    private String contact2Email;

    @Column(name = "contact2_phone")
    private String contact2Phone;

    @Column(name = "primary_contact")
    private Integer primaryContact;

    @Column(name = "business_types")
    private String businessTypes;

    @Column(name = "business_scope")
    private String businessScope;

    @Column(name = "company_type")
    private String companyType;

    @Column(name = "vendor_category")
    private String vendorCategory;

    @Column(name = "vendor_type_product")
    private boolean vendorTypeProduct;

    @Column(name = "vendor_type_service")
    private boolean vendorTypeService;

    @Column(name = "vendor_type_subcontracting")
    private boolean vendorTypeSubcontracting;

    @Column(name = "vendor_type_scheduling_agreement")
    private boolean vendorTypeSchedulingAgreement;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "msme_number")
    private String msmeNumber;

    @Column(name = "cin_number")
    private String cinNumber;

    @Column(name = "iso_certificate_no")
    private String isoCertificateNo;

    @Column(name = "iso_certifying_body")
    private String isoCertifyingBody;

    @Column(name = "iso_expiry")
    private String isoExpiry;

    @Column(name = "iso14001_certificate_no")
    private String iso14001CertificateNo;

    @Column(name = "iso14001_certifying_body")
    private String iso14001CertifyingBody;

    @Column(name = "iso14001_expiry")
    private String iso14001Expiry;

    @Column(name = "iso45001_certificate_no")
    private String iso45001CertificateNo;

    @Column(name = "iso45001_certifying_body")
    private String iso45001CertifyingBody;

    @Column(name = "iso45001_expiry")
    private String iso45001Expiry;

    @Column(name = "iso27001_certificate_no")
    private String iso27001CertificateNo;

    @Column(name = "iso27001_certifying_body")
    private String iso27001CertifyingBody;

    @Column(name = "iso27001_expiry")
    private String iso27001Expiry;

    @Column(name = "as9100d_certificate_no")
    private String as9100dCertificateNo;

    @Column(name = "as9100d_certifying_body")
    private String as9100dCertifyingBody;

    @Column(name = "as9100d_expiry")
    private String as9100dExpiry;

    @Column(name = "nadcap_certificate_no")
    private String nadcapCertificateNo;

    @Column(name = "nadcap_expiry")
    private String nadcapExpiry;
}
