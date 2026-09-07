-- Single-row org-wide feature switch, same pattern as folderit_sync_config. All 9 stages
-- default ON so this table's existence changes nothing until an admin actually flips something.
CREATE TABLE org_config (
  id                                 BIGINT NOT NULL PRIMARY KEY,
  vendor_onboarding_enabled          TINYINT(1) NOT NULL DEFAULT 1,
  pr_to_po_enabled                   TINYINT(1) NOT NULL DEFAULT 1,
  goods_receipt_warehouse_enabled    TINYINT(1) NOT NULL DEFAULT 1,
  gate_entry_show_to_vendor_enabled  TINYINT(1) NOT NULL DEFAULT 1,
  invoice_verification_enabled       TINYINT(1) NOT NULL DEFAULT 1,
  vendor_payments_enabled            TINYINT(1) NOT NULL DEFAULT 1,
  vendor_returns_enabled             TINYINT(1) NOT NULL DEFAULT 1,
  credit_notes_enabled               TINYINT(1) NOT NULL DEFAULT 1,
  budgeting_enabled                  TINYINT(1) NOT NULL DEFAULT 1
);

INSERT INTO org_config (id) VALUES (1);
