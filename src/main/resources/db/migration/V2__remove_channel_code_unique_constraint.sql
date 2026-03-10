-- Remove unique constraint from channel_code column
-- This allows the same channel code to exist for different companies

-- First, check if the constraint exists and drop it
SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'channel' 
    AND COLUMN_NAME = 'channel_code' 
    AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

-- Drop the existing unique constraint if it exists
SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE channel DROP INDEX ', @constraint_name), 
    'SELECT "No unique constraint found on channel_code" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add a composite unique constraint on channel_code and company_id
-- This ensures channel codes are unique within each company
ALTER TABLE channel ADD CONSTRAINT UK_channel_code_company_id UNIQUE (channel_code, company_id);

