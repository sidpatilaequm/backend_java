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

    // Deliberately no @NotBlank — a fresh draft has no vendorName yet. Derived
    // automatically from the cancelled cheque's account-holder name at submit
    // time (see SupplierRegistrationService), matching the original Next.js
    // prototype's design — there is no separate "company name" field the
    // applicant types; the legal name comes off a verified document instead.
    @Column(name = "vendor_name")
    private String vendorName;

    private String address;

    /**
     * FolderIt folder holding every document for this application — created on
     * first upload, named "{vendorCode} - {vendorName}" once approval assigns a
     * vendor code (see SupplierRegistrationService.provisionVendorAccount), or a
     * registration-id-based placeholder before that.
     */
    @Column(name = "folderit_folder_uid")
    private String folderitFolderUid;

    // ── Resolved primary contact — always mirrors whichever of contact 1 /
    // contact 2 below is marked primary. Every other part of the system
    // (unique email constraint, resume-code email, WorkFlow submission,
    // post-approval login) reads these four, not the contact1_*/contact2_*
    // columns directly.
    @Column(name = "contact_name")
    private String contactName;

    private String designation;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    private String phone;

    // ── Contact 1 (the original prototype allows up to two contacts) ──────
    @Column(name = "contact1_name")
    private String contact1Name;

    @Column(name = "contact1_role")
    private String contact1Role;

    @Column(name = "contact1_email")
    private String contact1Email;

    @Column(name = "contact1_phone")
    private String contact1Phone;

    // ── Contact 2 (optional) ───────────────────────────────────────────────
    @Column(name = "contact2_name")
    private String contact2Name;

    @Column(name = "contact2_role")
    private String contact2Role;

    @Column(name = "contact2_email")
    private String contact2Email;

    @Column(name = "contact2_phone")
    private String contact2Phone;

    /** 1 or 2 — which of the above is the primary contact. */
    @Column(name = "primary_contact")
    private Integer primaryContact = 1;

    // ── Business profile — mirrors AA-PUR-F-03 "Supplier Evaluation and
    // Registration Form" (Manufacturer/Service/Trader, company type, contact
    // numbers, directors, production facilities) rather than the earlier
    // ad-hoc supply-category/plant/payment-terms fields it replaced.

    /** Comma-separated: Manufacturer / Service / Trader. */
    @Column(name = "business_types", columnDefinition = "TEXT")
    private String businessTypes;

    /** Free-text detail scope for the ticked business type(s). */
    @Column(name = "business_scope", columnDefinition = "TEXT")
    private String businessScope;

    /** Proprietary / Partnership / Private Limited / Public Limited. */
    @Column(name = "company_type")
    private String companyType;

    private String telephone;

    private String fax;

    @Column(name = "weekly_off")
    private String weeklyOff;

    @Column(name = "annual_turnover")
    private String annualTurnover;

    @Column(name = "turnover_year")
    private String turnoverYear;

    @Column(name = "regulatory_acts", columnDefinition = "TEXT")
    private String regulatoryActs;

    @Column(name = "manpower_office")
    private String manpowerOffice;

    @Column(name = "manpower_supervisor")
    private String manpowerSupervisor;

    @Column(name = "manpower_workmen")
    private String manpowerWorkmen;

    @Column(name = "shifts_per_day")
    private String shiftsPerDay;

    @Column(name = "spare_capacity")
    private String spareCapacity;

    @Column(name = "floor_space")
    private String floorSpace;

    /** Comma-separated list of ticked equipment/facility options. */
    @Column(name = "equipment_facilities", columnDefinition = "TEXT")
    private String equipmentFacilities;

    /** JSON array of {name, qualification, experience, commencementDate, capitalEmployed}. */
    @Column(name = "directors_json", columnDefinition = "LONGTEXT")
    private String directorsJson;

    /** JSON array of {description, capacity, makeName, makeYear, numbers, remarks}. */
    @Column(name = "machinery_json", columnDefinition = "LONGTEXT")
    private String machineryJson;

    /**
     * FK into Form Studio's own {@code responses} table (same shared database — see
     * entity/questionnaire/) once the applicant's answers to the admin-defined
     * questionnaire have been recorded at submit time. Null until then, and for
     * registrations submitted before the questionnaire feature existed.
     */
    @Column(name = "form_studio_response_id")
    private Integer formStudioResponseId;

    /**
     * Raw JSON array of {questionId, textValue?, optionIds?} answers to the active dynamic
     * questionnaire, staged here across draft saves the same way directorsJson/machineryJson
     * are — validated and copied into Form Studio's own responses/answers/answer_options tables
     * (QuestionnaireService) at submit time, at which point formStudioResponseId above gets set.
     */
    @Column(name = "dynamic_answers_json", columnDefinition = "LONGTEXT")
    private String dynamicAnswersJson;

    /**
     * Which Form Studio process dynamicAnswersJson above was answered against — stamped from
     * whatever questionnaire was active in the applicant's browser at draft-save time. Lets
     * QuestionnaireService count "how many drafts are in progress against this questionnaire"
     * (for the admin builder's draft count, and to block deleting a questionnaire that still has
     * drafts pending against it) — something Form Studio's own response-count-based lock can't
     * see at all, since a draft only becomes a real `responses` row at submit time.
     */
    @Column(name = "dynamic_questionnaire_process_id")
    private Integer dynamicQuestionnaireProcessId;

    /** Code-of-conduct / GCP / no-child-labour declaration checkbox. */
    @Column(name = "declaration_accepted")
    private Boolean declarationAccepted = false;

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

    /**
     * PRODUCT / SERVICE / SCHEDULING_AGREEMENT / SUBCONTRACTING — set by whichever approver
     * decides it first during review (see VendorClassificationController), never by the vendor.
     * Under a stage requiring several approvers, the first one to set it wins; later approvers
     * see it already decided. Null until the deciding approval happens.
     */
    @Column(name = "vendor_category")
    private String vendorCategory;

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
