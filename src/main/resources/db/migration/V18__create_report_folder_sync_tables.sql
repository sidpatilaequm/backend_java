-- FolderIt watcher for the excel-to-table report types: one row per report type pointing at a
-- FolderIt folder, checked every N minutes for new/changed excel files, imported using the
-- matching saved excel_import_mappings row. report_folder_sync_file tracks each file's FolderIt
-- updatedAt so an edited file (new rows, changed rows) gets re-processed, not just brand-new files.

CREATE TABLE report_folder_sync (
  id                          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  report_type                 VARCHAR(30) NOT NULL UNIQUE,
  folderit_folder_uid         VARCHAR(100) NOT NULL,
  enabled                     TINYINT(1) NOT NULL DEFAULT 1,
  interval_minutes            INT NOT NULL DEFAULT 5,
  last_run_at                 DATETIME NULL,
  last_run_status             VARCHAR(500) NULL,
  files_processed_last_run    INT NOT NULL DEFAULT 0,
  created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE report_folder_sync_file (
  id                          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  report_type                 VARCHAR(30) NOT NULL,
  file_uid                    VARCHAR(100) NOT NULL,
  file_name                   VARCHAR(255) NULL,
  last_processed_updated_at   BIGINT NULL,
  last_processed_at           DATETIME NULL,
  rows_processed              INT NOT NULL DEFAULT 0,
  last_error                  VARCHAR(1000) NULL,
  UNIQUE KEY uk_report_file (report_type, file_uid)
);

-- Real folders under the "Ankit Aerospace" root in FolderIt (uid TYQvN0HSDZ), confirmed via the
-- live API: Vendor Payments/Vendor Returns/Credit Notes already existed and were empty; Vendor
-- Stock was created new. INVOICES is deliberately left out for now -- vendor_invoice.vendor_company_id
-- and vendor_user_id are NOT NULL FKs designed for a real portal-submitted invoice, not a bulk
-- SAP import, and need more design work before this can insert rows there safely.
INSERT INTO report_folder_sync (report_type, folderit_folder_uid, enabled, interval_minutes) VALUES
  ('VENDOR_PAYMENTS', 'JWVr10lCLs', 1, 5),
  ('VENDOR_RETURNS',  'bPPoe05-MJ', 1, 5),
  ('CREDIT_NOTES',    'IbCiK01Ixm', 1, 5),
  ('VENDOR_STOCK',    'Ykb140ZyWw', 1, 5);
