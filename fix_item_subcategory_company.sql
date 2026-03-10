-- Fix item_subcategory table to add company_id properly
-- This script handles the case where the migration partially failed

-- Step 1: Check if company_id column exists and drop the failed constraint if it exists
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'item_subcategory' 
    AND CONSTRAINT_NAME = 'FKny0fd77d276pe025iqu845y95'
);

-- Drop the failed constraint if it exists
SET @sql = IF(@constraint_exists > 0, 
    'ALTER TABLE item_subcategory DROP FOREIGN KEY FKny0fd77d276pe025iqu845y95', 
    'SELECT "Constraint does not exist" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Check if company_id column exists
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'item_subcategory' 
    AND COLUMN_NAME = 'company_id'
);

-- Add company_id column if it doesn't exist
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE item_subcategory ADD COLUMN company_id BIGINT NULL', 
    'SELECT "Column already exists" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Populate existing records with a default company_id
-- Get the first available company_id from company_details table
SET @default_company_id = (SELECT MIN(company_id) FROM company_details LIMIT 1);

-- Update all existing records with the default company_id
UPDATE item_subcategory SET company_id = @default_company_id WHERE company_id IS NULL;

-- Step 4: Make the column NOT NULL after populating data
ALTER TABLE item_subcategory MODIFY COLUMN company_id BIGINT NOT NULL;

-- Step 5: Add foreign key constraint
ALTER TABLE item_subcategory ADD CONSTRAINT FK_item_subcategory_company 
    FOREIGN KEY (company_id) REFERENCES company_details(company_id);

-- Step 6: Add indexes for better performance (only if they don't exist)
SET @index_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'item_subcategory' 
    AND INDEX_NAME = 'idx_item_subcategory_company_id'
);

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_item_subcategory_company_id ON item_subcategory(company_id)', 
    'SELECT "Index already exists" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add composite index for company and category filtering
SET @composite_index_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'item_subcategory' 
    AND INDEX_NAME = 'idx_item_subcategory_company_category'
);

SET @sql = IF(@composite_index_exists = 0, 
    'CREATE INDEX idx_item_subcategory_company_category ON item_subcategory(company_id, item_category_id)', 
    'SELECT "Composite index already exists" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Show final result
SELECT 'item_subcategory table updated successfully' as result;

