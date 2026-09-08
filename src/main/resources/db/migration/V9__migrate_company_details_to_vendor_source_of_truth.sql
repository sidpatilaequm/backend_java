-- Part of the company_details-as-source-of-truth migration: today company_details is a
-- one-time, 4-field snapshot (company_name/gstin_number/pan_number/registered_address) taken
-- at approval, with no link back to the supplier_registration row it came from and no columns
-- for most of what an approved vendor's profile actually holds. This adds those columns, a
-- real FK from company_details back to its originating supplier_registration, and a real FK
-- from vendor_master to company_details (additive, alongside vendor_master's existing FK to
-- supplier_registration — that one stays, RFQ/Gate Entry still key off vendor_master.vendor_id).
--
-- Purely additive: all new columns are nullable, both new FKs are nullable. No existing reads
-- are affected until application code (a later deploy) starts using them.

ALTER TABLE company_details
  ADD COLUMN supplier_registration_id BIGINT NULL,
  ADD CONSTRAINT fk_company_details_supplier_registration
    FOREIGN KEY (supplier_registration_id) REFERENCES supplier_registration(id),

  -- Contacts (supplier_registration has no company_details equivalent for any of these today)
  ADD COLUMN contact_name    VARCHAR(255) NULL,
  ADD COLUMN designation     VARCHAR(255) NULL,
  ADD COLUMN email           VARCHAR(255) NULL,
  ADD COLUMN phone           VARCHAR(255) NULL,
  ADD COLUMN contact1_name   VARCHAR(255) NULL,
  ADD COLUMN contact1_role   VARCHAR(255) NULL,
  ADD COLUMN contact1_email  VARCHAR(255) NULL,
  ADD COLUMN contact1_phone  VARCHAR(255) NULL,
  ADD COLUMN contact2_name   VARCHAR(255) NULL,
  ADD COLUMN contact2_role   VARCHAR(255) NULL,
  ADD COLUMN contact2_email  VARCHAR(255) NULL,
  ADD COLUMN contact2_phone  VARCHAR(255) NULL,
  ADD COLUMN primary_contact INT NULL,

  -- Business profile / category (vendor_type_* mirror the 4 flags added to
  -- supplier_registration in V7 — same defaults, same meaning, now editable live here too)
  ADD COLUMN business_types  TEXT NULL,
  ADD COLUMN business_scope  TEXT NULL,
  ADD COLUMN company_type    VARCHAR(255) NULL,
  ADD COLUMN vendor_category VARCHAR(30) NULL,
  ADD COLUMN vendor_type_product              TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_service              TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_subcontracting       TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_scheduling_agreement TINYINT(1) NOT NULL DEFAULT 0,

  -- Bank details (note: distinct from the separately-populated ChequeDetails child entity —
  -- these are the vendor's own self-reported/OCR'd bank fields, not the cheque-verification flow)
  ADD COLUMN beneficiary_name VARCHAR(255) NULL,
  ADD COLUMN account_number  VARCHAR(255) NULL,
  ADD COLUMN ifsc_code       VARCHAR(255) NULL,
  ADD COLUMN bank_name       VARCHAR(255) NULL,

  -- MSME / CIN (gstin_number and pan_number already exist on this table — reused, not duplicated)
  ADD COLUMN msme_number VARCHAR(255) NULL,
  ADD COLUMN cin_number  VARCHAR(255) NULL,

  -- Certifications
  ADD COLUMN iso_certificate_no      VARCHAR(255) NULL,
  ADD COLUMN iso_certifying_body    VARCHAR(255) NULL,
  ADD COLUMN iso_expiry             VARCHAR(255) NULL,
  ADD COLUMN iso14001_certificate_no   VARCHAR(255) NULL,
  ADD COLUMN iso14001_certifying_body VARCHAR(255) NULL,
  ADD COLUMN iso14001_expiry           VARCHAR(255) NULL,
  ADD COLUMN iso45001_certificate_no   VARCHAR(255) NULL,
  ADD COLUMN iso45001_certifying_body VARCHAR(255) NULL,
  ADD COLUMN iso45001_expiry           VARCHAR(255) NULL,
  ADD COLUMN iso27001_certificate_no   VARCHAR(255) NULL,
  ADD COLUMN iso27001_certifying_body VARCHAR(255) NULL,
  ADD COLUMN iso27001_expiry           VARCHAR(255) NULL,
  ADD COLUMN as9100d_certificate_no   VARCHAR(255) NULL,
  ADD COLUMN as9100d_certifying_body VARCHAR(255) NULL,
  ADD COLUMN as9100d_expiry           VARCHAR(255) NULL,
  ADD COLUMN nadcap_certificate_no VARCHAR(255) NULL,
  ADD COLUMN nadcap_expiry         VARCHAR(255) NULL;

ALTER TABLE vendor_master
  ADD COLUMN company_id BIGINT NULL,
  ADD CONSTRAINT fk_vendor_master_company
    FOREIGN KEY (company_id) REFERENCES company_details(company_id);

-- Seed company_details.supplier_registration_id from the current soft match
-- (vendor_master.bp_no == company_details.company_code), one time.
UPDATE company_details cd
JOIN vendor_master vm ON vm.bp_no = cd.company_code
SET cd.supplier_registration_id = vm.supplier_registration_id
WHERE cd.supplier_registration_id IS NULL
  AND vm.supplier_registration_id IS NOT NULL;

-- Seed vendor_master.company_id the same way.
UPDATE vendor_master vm
JOIN company_details cd ON cd.company_code = vm.bp_no
SET vm.company_id = cd.company_id
WHERE vm.company_id IS NULL;

-- One-time backfill of every new column from the linked supplier_registration row. Guarded on
-- cin_number (a genuinely new column with no prior data anywhere) being NULL so this is safe to
-- re-run without clobbering values a later write-through may already have set.
UPDATE company_details cd
JOIN supplier_registration sr ON sr.id = cd.supplier_registration_id
SET cd.contact_name    = sr.contact_name,
    cd.designation     = sr.designation,
    cd.email            = sr.email,
    cd.phone            = sr.phone,
    cd.contact1_name   = sr.contact1_name,
    cd.contact1_role   = sr.contact1_role,
    cd.contact1_email  = sr.contact1_email,
    cd.contact1_phone  = sr.contact1_phone,
    cd.contact2_name   = sr.contact2_name,
    cd.contact2_role   = sr.contact2_role,
    cd.contact2_email  = sr.contact2_email,
    cd.contact2_phone  = sr.contact2_phone,
    cd.primary_contact = sr.primary_contact,
    cd.business_types  = sr.business_types,
    cd.business_scope  = sr.business_scope,
    cd.company_type    = sr.company_type,
    cd.vendor_category = sr.vendor_category,
    cd.vendor_type_product              = sr.vendor_type_product,
    cd.vendor_type_service              = sr.vendor_type_service,
    cd.vendor_type_subcontracting       = sr.vendor_type_subcontracting,
    cd.vendor_type_scheduling_agreement = sr.vendor_type_scheduling_agreement,
    cd.beneficiary_name = sr.beneficiary_name,
    cd.account_number   = sr.account_number,
    cd.ifsc_code         = sr.ifsc_code,
    cd.bank_name         = sr.bank_name,
    cd.msme_number       = sr.msme_number,
    cd.cin_number        = sr.cin_number,
    cd.iso_certificate_no      = sr.iso_certificate_no,
    cd.iso_certifying_body    = sr.iso_certifying_body,
    cd.iso_expiry               = sr.iso_expiry,
    cd.iso14001_certificate_no   = sr.iso14001_certificate_no,
    cd.iso14001_certifying_body = sr.iso14001_certifying_body,
    cd.iso14001_expiry           = sr.iso14001_expiry,
    cd.iso45001_certificate_no   = sr.iso45001_certificate_no,
    cd.iso45001_certifying_body = sr.iso45001_certifying_body,
    cd.iso45001_expiry           = sr.iso45001_expiry,
    cd.iso27001_certificate_no   = sr.iso27001_certificate_no,
    cd.iso27001_certifying_body = sr.iso27001_certifying_body,
    cd.iso27001_expiry           = sr.iso27001_expiry,
    cd.as9100d_certificate_no   = sr.as9100d_certificate_no,
    cd.as9100d_certifying_body = sr.as9100d_certifying_body,
    cd.as9100d_expiry           = sr.as9100d_expiry,
    cd.nadcap_certificate_no = sr.nadcap_certificate_no,
    cd.nadcap_expiry         = sr.nadcap_expiry
WHERE cd.supplier_registration_id IS NOT NULL
  AND cd.cin_number IS NULL;

-- Verification query (run manually after this migration, not part of it) — must return 0
-- before Phase 2 (read-path migration) proceeds:
-- SELECT COUNT(*) FROM vendor_master WHERE company_id IS NULL AND supplier_registration_id IS NOT NULL;
