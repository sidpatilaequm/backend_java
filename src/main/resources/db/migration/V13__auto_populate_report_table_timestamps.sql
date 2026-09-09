-- created_at/updated_at aren't in any of the 5 SAP excel templates (they're bookkeeping, not
-- vendor input) -- but they existed on vendor_payments/vendor_invoice with no DEFAULT, so a raw
-- insert (like the excel-to-table import) left them NULL forever. Give every report table both
-- columns with DB-level auto-population, so nothing that writes to these tables -- now or later
-- (the FolderIt cron, a future JPA path, anything) has to remember to set them itself.

-- Backfill vendor_payments' existing NULL rows before making the column NOT NULL.
UPDATE vendor_payments SET created_at = NOW(6), updated_at = NOW(6) WHERE created_at IS NULL;

ALTER TABLE vendor_payments
  MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  MODIFY COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE vendor_invoice
  MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  MODIFY COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE vendor_returns
  ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE vendor_credit_notes
  ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE vendor_invoice_item
  ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE current_stock
  ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);
