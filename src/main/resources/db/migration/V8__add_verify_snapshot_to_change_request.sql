ALTER TABLE supplier_registration_change_request
  ADD COLUMN new_ocr_extracted_fields_json LONGTEXT NULL,
  ADD COLUMN new_verify_status             VARCHAR(20) NULL,
  ADD COLUMN new_verify_details_json       LONGTEXT NULL;
