-- V15's drops (and some of V12/V14's) were found reverted on 5 of 6 tables a second time, well
-- after they were verified applied and stable -- with no cron, no ddl-auto, no mysqld restart,
-- and no other process found responsible. MySQL 8.4's instant ADD/DROP COLUMN metadata is the
-- prime remaining suspect after many rapid instant ALTERs stacked on the same tables. This drops
-- the reverted columns again and forces a real physical rebuild of each table (ENGINE=InnoDB
-- ALGORITHM=COPY, not the default INSTANT) so there's no accumulated instant-DDL metadata left
-- for anything to revert to.

ALTER TABLE vendor_returns
  DROP COLUMN currency,
  DROP COLUMN document_date,
  DROP COLUMN material_document,
  DROP COLUMN movement_type,
  DROP COLUMN net_price,
  DROP COLUMN net_value,
  DROP COLUMN order_unit,
  DROP COLUMN plant_code,
  DROP COLUMN po_item,
  DROP COLUMN price_per,
  DROP COLUMN purchase_order_number,
  DROP COLUMN quantity,
  DROP COLUMN return_item,
  DROP COLUMN return_time,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN status,
  DROP COLUMN supplier_code,
  DROP COLUMN supplier_name,
  ALGORITHM=COPY;
ALTER TABLE vendor_returns ENGINE=InnoDB;

ALTER TABLE vendor_credit_notes
  DROP COLUMN amount,
  DROP COLUMN document_number,
  DROP COLUMN fiscal_year,
  DROP COLUMN material_code,
  DROP COLUMN material_description,
  DROP COLUMN material_document,
  DROP COLUMN movement_type,
  DROP COLUMN plant_code,
  DROP COLUMN po_item,
  DROP COLUMN posting_date,
  DROP COLUMN purchase_order_number,
  DROP COLUMN quantity,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN status,
  DROP COLUMN storage_location,
  DROP COLUMN unit_of_measure,
  DROP COLUMN vendor_code,
  ALGORITHM=COPY;
ALTER TABLE vendor_credit_notes ENGINE=InnoDB;

ALTER TABLE current_stock
  DROP COLUMN company_code,
  DROP COLUMN material_type,
  DROP COLUMN material_type_description,
  DROP COLUMN plant_code,
  DROP COLUMN sap_sync_message,
  DROP COLUMN sap_synced,
  DROP COLUMN status,
  DROP COLUMN storage_location,
  DROP COLUMN supplier_material_code,
  DROP COLUMN unit_of_measure,
  DROP COLUMN unrestricted_stock,
  DROP COLUMN vendor_code,
  ALGORITHM=COPY;
ALTER TABLE current_stock ENGINE=InnoDB;

ALTER TABLE vendor_invoice
  DROP COLUMN bill_type,
  DROP COLUMN business_place,
  DROP COLUMN consignee,
  DROP COLUMN dc_date,
  DROP COLUMN delivery_note_number,
  DROP COLUMN gst_number,
  DROP COLUMN gst_total_amount,
  DROP COLUMN invoice_currency,
  DROP COLUMN invoice_due_date,
  DROP COLUMN invoice_number,
  DROP COLUMN invoice_total_amount,
  DROP COLUMN payable_amount_ex_gst,
  DROP COLUMN payable_amount_inc_gst,
  DROP COLUMN remarks,
  DROP COLUMN sap_tax_code,
  DROP COLUMN sap_tax_type,
  DROP COLUMN section_code,
  DROP COLUMN status,
  DROP COLUMN subtotal_amount,
  DROP COLUMN tds_rate,
  DROP COLUMN tds_section,
  DROP COLUMN vendor_number,
  DROP COLUMN zip_code,
  ALGORITHM=COPY;
ALTER TABLE vendor_invoice ENGINE=InnoDB;

ALTER TABLE vendor_invoice_item
  DROP COLUMN basic_amount,
  DROP COLUMN batch_number,
  DROP COLUMN cgst_amount,
  DROP COLUMN cgst_percent,
  DROP COLUMN currency,
  DROP COLUMN discount_amount,
  DROP COLUMN hsn_code,
  DROP COLUMN igst_amount,
  DROP COLUMN igst_percent,
  DROP COLUMN item_code,
  DROP COLUMN line_total,
  DROP COLUMN material,
  DROP COLUMN mrp,
  DROP COLUMN pack_code,
  DROP COLUMN rate,
  DROP COLUMN sgst_amount,
  DROP COLUMN sgst_percent,
  DROP COLUMN ugst_amount,
  DROP COLUMN ugst_percent,
  ALGORITHM=COPY;
ALTER TABLE vendor_invoice_item ENGINE=InnoDB;
