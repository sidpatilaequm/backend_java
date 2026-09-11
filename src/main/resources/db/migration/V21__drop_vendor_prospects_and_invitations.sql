-- V21: Drop vendor prospects/invitations tables and columns

DROP TABLE IF EXISTS `vendor_invitations`;
DROP TABLE IF EXISTS `supplier_invitation`;

SET @exist_status := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_details' AND COLUMN_NAME = 'onboarding_status');
SET @sql_status := IF(@exist_status > 0, 'ALTER TABLE user_details DROP COLUMN onboarding_status', 'SELECT 1');
PREPARE stmt_status FROM @sql_status;
EXECUTE stmt_status;
DEALLOCATE PREPARE stmt_status;

SET @exist_token := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_details' AND COLUMN_NAME = 'onboarding_token');
SET @sql_token := IF(@exist_token > 0, 'ALTER TABLE user_details DROP COLUMN onboarding_token', 'SELECT 1');
PREPARE stmt_token FROM @sql_token;
EXECUTE stmt_token;
DEALLOCATE PREPARE stmt_token;

SET @exist_expiry := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_details' AND COLUMN_NAME = 'token_expiry');
SET @sql_expiry := IF(@exist_expiry > 0, 'ALTER TABLE user_details DROP COLUMN token_expiry', 'SELECT 1');
PREPARE stmt_expiry FROM @sql_expiry;
EXECUTE stmt_expiry;
DEALLOCATE PREPARE stmt_expiry;
