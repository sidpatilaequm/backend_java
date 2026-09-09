-- Drop the columns on the 5 report tables (+ vendor_invoice_item) that have no counterpart in
-- their SAP excel template (see V11's header mapping). All 6 tables were confirmed empty except
-- vendor_payments' 3 rows, which are NULL in every one of these columns -- no data loss.
-- Structural columns (PK, FK relationship columns, created_at/updated_at) are never dropped
-- regardless of excel match.

ALTER TABLE vendor_payments
  DROP COLUMN baseline_date,
  DROP COLUMN clearing_date,
  DROP COLUMN clearing_document,
  DROP COLUMN company_code,
  DROP COLUMN fi_fiscal_year,
  DROP COLUMN fiscal_year,
  DROP COLUMN house_bank,
  DROP COLUMN payment_run_date,
  DROP COLUMN payment_run_id,
  DROP COLUMN penny_drop_ref,
  DROP COLUMN penny_drop_status,
  DROP COLUMN posting_date,
  DROP COLUMN reconciliation_account,
  DROP COLUMN sap_document_type,
  DROP COLUMN sap_raw_response,
  DROP COLUMN sync_batch_id,
  DROP COLUMN synced_at;

ALTER TABLE vendor_returns
  DROP COLUMN document_date,
  DROP COLUMN price_per,
  DROP COLUMN return_item,
  DROP COLUMN return_time,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN currency;

ALTER TABLE vendor_credit_notes
  DROP COLUMN fiscal_year,
  DROP COLUMN movement_type,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN storage_location;

ALTER TABLE vendor_invoice
  DROP COLUMN business_place,
  DROP COLUMN consignee,
  DROP COLUMN dc_date,
  DROP COLUMN sap_tax_type,
  DROP COLUMN section_code,
  DROP COLUMN zip_code,
  DROP COLUMN payable_amount_ex_gst;

ALTER TABLE vendor_invoice_item
  DROP COLUMN batch_number,
  DROP COLUMN cgst_amount,
  DROP COLUMN cgst_percent,
  DROP COLUMN currency,
  DROP COLUMN discount_amount,
  DROP COLUMN igst_amount,
  DROP COLUMN igst_percent,
  DROP COLUMN material,
  DROP COLUMN mrp,
  DROP COLUMN pack_code,
  DROP COLUMN sgst_amount,
  DROP COLUMN sgst_percent,
  DROP COLUMN ugst_amount,
  DROP COLUMN ugst_percent;

ALTER TABLE current_stock
  DROP COLUMN material_type,
  DROP COLUMN material_type_description,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN status,
  DROP COLUMN storage_location,
  DROP COLUMN supplier_material_code;
