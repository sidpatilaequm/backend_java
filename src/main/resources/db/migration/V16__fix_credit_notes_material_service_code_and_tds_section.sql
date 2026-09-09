-- Full column-by-column audit against the current excel headers found two gaps on
-- vendor_credit_notes specifically: "Material / Service Code" was left named material_code
-- (should be material_service_code, same as vendor_invoice_item uses for the identical header
-- text), and "TDS Section" was never given a column at all in the original V11 design. Table is
-- empty -- no data loss.

ALTER TABLE vendor_credit_notes
  RENAME COLUMN material_code TO material_service_code,
  ADD COLUMN tds_section VARCHAR(20) NULL;
