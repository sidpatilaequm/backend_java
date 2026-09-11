-- V22: Permanently drop unused legacy tables `purchase_orders` and `sap_purchase_requisitions`
-- Active system uses `portal_purchase_orders` and `purchase_requisitions`

DROP TABLE IF EXISTS `purchase_orders`;
DROP TABLE IF EXISTS `sap_purchase_requisitions`;
