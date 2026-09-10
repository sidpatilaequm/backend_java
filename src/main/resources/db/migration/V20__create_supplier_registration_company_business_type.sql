-- Per-company business types: today the 4 vendor_type_* flags on supplier_registration/
-- company_details are ONE uniform set applied to every company the vendor is linked to. This
-- makes them explicit per (registration, company_code) rows so an admin can set e.g. Product-only
-- for company 1000 and Service-only for company 2000. Mirrors supplier_registration_document_type's
-- composite-key shape exactly.
CREATE TABLE supplier_registration_company_business_type (
  registration_id BIGINT NOT NULL,
  company_code VARCHAR(4) NOT NULL,
  vendor_type_product TINYINT(1) NOT NULL DEFAULT 0,
  vendor_type_service TINYINT(1) NOT NULL DEFAULT 0,
  vendor_type_subcontracting TINYINT(1) NOT NULL DEFAULT 0,
  vendor_type_scheduling_agreement TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (registration_id, company_code),
  CONSTRAINT fk_srcbt_registration FOREIGN KEY (registration_id) REFERENCES supplier_registration(id)
);

-- Backfill: one row per (registration, company code) the vendor is already linked to, copying
-- today's uniform flags -- the explicit form of "the first approver chose Product and Service for
-- both companies."
INSERT INTO supplier_registration_company_business_type
  (registration_id, company_code, vendor_type_product, vendor_type_service,
   vendor_type_subcontracting, vendor_type_scheduling_agreement)
SELECT DISTINCT srdt.registration_id, srdt.company_code,
       sr.vendor_type_product, sr.vendor_type_service,
       sr.vendor_type_subcontracting, sr.vendor_type_scheduling_agreement
FROM supplier_registration_document_type srdt
JOIN supplier_registration sr ON sr.id = srdt.registration_id;
