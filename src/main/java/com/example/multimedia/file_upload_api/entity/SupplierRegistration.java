package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * One row per "Become a Supplier" application. Lives in the shared MySQL
 * database (unlike Admin/pages/models.py's VendorRegistration, which turned
 * out to be isolated in Admin's own SQLite with zero rows) so backend_java
 * can read/write it directly. Field shape borrows from that Django model,
 * plus beneficiary_name and the ISO/AS9100D columns it was missing.
 */
@Entity
@Table(name = "supplier_registration")
@Getter
@Setter
public class SupplierRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "vendor_name")
    private String vendorName;

    private String address;

    @Column(name = "contact_name")
    private String contactName;

    private String designation;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    private String phone;

    /** DRAFT / REGISTRATION_SUBMITTED / UNDER_VERIFICATION / ACTIVE / REJECTED */
    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    /** Unique short code emailed on "Save Draft", used to resume later without an account. */
    @Column(name = "resume_code", unique = true)
    private String resumeCode;

    /** Set once submitted into WorkFlow's "Vendor Approval" workflow. */
    @Column(name = "workflow_request_id")
    private Long workflowRequestId;

    /** Generated on approval. */
    @Column(name = "vendor_code", unique = true)
    private String vendorCode;

    /** Set once the real login (UserDetail/UserAuthentication/CompanyDetails) is provisioned on approval. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "company_id")
    private Long companyId;

    // ── KYC ──────────────────────────────────────────────────────────────
    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "pan_number")
    private String panNumber;

    /** Udyam / MSME registration number. */
    @Column(name = "msme_number")
    private String msmeNumber;

    @Column(name = "cin_number")
    private String cinNumber;

    // ── Bank ─────────────────────────────────────────────────────────────
    /** Account holder name as printed on the cheque — missing from Django's version. */
    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "bank_name")
    private String bankName;

    // ── Optional certifications — no equivalent columns in Django's version ──
    @Column(name = "iso_certificate_no")
    private String isoCertificateNo;

    @Column(name = "iso_certifying_body")
    private String isoCertifyingBody;

    @Column(name = "iso_expiry")
    private String isoExpiry;

    @Column(name = "as9100d_certificate_no")
    private String as9100dCertificateNo;

    @Column(name = "as9100d_certifying_body")
    private String as9100dCertifyingBody;

    @Column(name = "as9100d_expiry")
    private String as9100dExpiry;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
