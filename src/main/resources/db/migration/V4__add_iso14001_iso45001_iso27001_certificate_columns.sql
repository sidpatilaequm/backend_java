-- Add ISO 14001, ISO 45001 and ISO 27001 certificate columns to supplier_registration,
-- mirroring the existing iso_* (ISO 9001) / as9100d_* columns exactly: optional certificate
-- number + certifying body + expiry, no verification.
--
-- Flyway isn't actually wired into this project (no flyway dependency, no spring.flyway.*
-- config) — files under db/migration/ are applied manually via SSH, same as V2/V3 and the
-- earlier nadcap_*/nda columns (which were never committed as a migration file at all). This
-- one at least gets committed and documented, unlike that one.

ALTER TABLE supplier_registration ADD COLUMN iso14001_certificate_no VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso14001_certifying_body VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso14001_expiry VARCHAR(255) NULL;

ALTER TABLE supplier_registration ADD COLUMN iso45001_certificate_no VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso45001_certifying_body VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso45001_expiry VARCHAR(255) NULL;

ALTER TABLE supplier_registration ADD COLUMN iso27001_certificate_no VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso27001_certifying_body VARCHAR(255) NULL;
ALTER TABLE supplier_registration ADD COLUMN iso27001_expiry VARCHAR(255) NULL;
