-- The user has since edited 4 of the 5 SAP excel templates (payments, credit notes, stock,
-- invoice) removing columns that are no longer part of the real vendor input -- vendor_returns
-- is unchanged. This drops the DB columns whose excel header no longer exists anywhere in the
-- current template, same rule as V12. All affected tables are empty except vendor_payments'
-- 3 rows, confirmed NULL in every one of these columns -- no data loss. Structural columns
-- (PK, FK, created_at/updated_at) are never touched.

ALTER TABLE vendor_payments
  DROP COLUMN vendor_gstin,
  DROP COLUMN vendor_pan,
  DROP COLUMN balance_outstanding,
  DROP COLUMN days_overdue,
  DROP COLUMN payment_status,
  DROP COLUMN account_holder_name,
  DROP COLUMN bank_name,
  DROP COLUMN bank_branch,
  DROP COLUMN bank_account_no,
  DROP COLUMN account_type,
  DROP COLUMN ifsc_swift,
  DROP COLUMN tds_certificate_no,
  DROP COLUMN remarks;

ALTER TABLE vendor_credit_notes
  DROP COLUMN vendor_pan,
  DROP COLUMN original_invoice_no,
  DROP COLUMN original_invoice_date,
  DROP COLUMN adjust_against_invoice_no,
  DROP COLUMN settlement_mode,
  DROP COLUMN account_holder_name,
  DROP COLUMN bank_name,
  DROP COLUMN bank_account_no,
  DROP COLUMN ifsc_swift,
  DROP COLUMN submission_date,
  DROP COLUMN submitted_by_user,
  DROP COLUMN portal_status,
  DROP COLUMN sap_credit_memo_doc_no,
  DROP COLUMN attachment_yn,
  DROP COLUMN remarks;

ALTER TABLE current_stock
  DROP COLUMN company_code,
  DROP COLUMN last_supplied_po_no,
  DROP COLUMN last_supplied_quantity,
  DROP COLUMN remarks;

ALTER TABLE vendor_invoice
  DROP COLUMN vendor_pan,
  DROP COLUMN vendor_address,
  DROP COLUMN payment_method,
  DROP COLUMN tds_section,
  DROP COLUMN tds_deducted_pct,
  DROP COLUMN account_holder_name,
  DROP COLUMN bank_name,
  DROP COLUMN bank_branch,
  DROP COLUMN bank_account_no,
  DROP COLUMN account_type,
  DROP COLUMN ifsc_swift,
  DROP COLUMN submission_date,
  DROP COLUMN submitted_by_user,
  DROP COLUMN portal_status,
  DROP COLUMN attachment_yn,
  DROP COLUMN remarks;
