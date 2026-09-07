-- Replaces the old single vendor_category string (PRODUCT,SERVICE,... comma-joined, set once
-- via the now-dead /classification endpoint) with 4 independent flags a vendor can hold at
-- once, editable anytime from the admin vendor detail page. vendor_category is left in place
-- (additive) since two admin display screens still read it.
ALTER TABLE supplier_registration
  ADD COLUMN vendor_type_product              TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_service              TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_subcontracting       TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN vendor_type_scheduling_agreement TINYINT(1) NOT NULL DEFAULT 0;

-- Backfill from whichever vendors were already classified via the old flat field.
-- FIND_IN_SET is exact-token safe against the comma-joined string (no LIKE partial-match risk).
UPDATE supplier_registration
SET vendor_type_product              = IF(FIND_IN_SET('PRODUCT', vendor_category) > 0, 1, vendor_type_product),
    vendor_type_service              = IF(FIND_IN_SET('SERVICE', vendor_category) > 0, 1, vendor_type_service),
    vendor_type_subcontracting       = IF(FIND_IN_SET('SUBCONTRACTING', vendor_category) > 0, 1, vendor_type_subcontracting),
    vendor_type_scheduling_agreement = IF(FIND_IN_SET('SCHEDULING_AGREEMENT', vendor_category) > 0, 1, vendor_type_scheduling_agreement)
WHERE vendor_category IS NOT NULL;
