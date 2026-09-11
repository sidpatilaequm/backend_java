-- vendor_address (entity VendorAddress, keyed off vendor_master) was an earlier, abandoned
-- address design, superseded by company_details.registered_address (see V9). Confirmed zero
-- application reads or writes anywhere in the codebase before this migration; predates Flyway,
-- so no prior migration created it either.
DROP TABLE IF EXISTS vendor_address;
