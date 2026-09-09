-- One row per ExcelReportType (see enums.ExcelReportType): which Excel column feeds which
-- column of that report's real target table. Config only for now -- no watcher/cron reads this
-- yet, so creating the table changes nothing until an admin actually saves a mapping.
CREATE TABLE excel_import_mappings (
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  report_type       VARCHAR(30) NOT NULL UNIQUE,
  sheet_name        VARCHAR(120) NULL,
  header_row        INT NOT NULL DEFAULT 5,
  mapping_json      LONGTEXT NOT NULL,
  updated_by        BIGINT NULL,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
