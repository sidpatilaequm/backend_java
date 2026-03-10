-- Add company_id column to item_subcategory table
-- This allows subcategories to be filtered by company

-- Step 1: Add the company_id column as nullable first
ALTER TABLE item_subcategory ADD COLUMN company_id BIGINT NULL;

-- Step 2: Populate existing records with a default company_id
-- Get the first available company_id from company_details table
SET @default_company_id = (SELECT MIN(company_id) FROM company_details LIMIT 1);

-- Update all existing records with the default company_id
UPDATE item_subcategory SET company_id = @default_company_id WHERE company_id IS NULL;

-- Step 3: Make the column NOT NULL after populating data
ALTER TABLE item_subcategory MODIFY COLUMN company_id BIGINT NOT NULL;

-- Step 4: Add foreign key constraint
ALTER TABLE item_subcategory ADD CONSTRAINT FK_item_subcategory_company 
    FOREIGN KEY (company_id) REFERENCES company_details(company_id);

-- Step 5: Add indexes for better performance
CREATE INDEX idx_item_subcategory_company_id ON item_subcategory(company_id);

-- Add composite index for company and category filtering
CREATE INDEX idx_item_subcategory_company_category ON item_subcategory(company_id, item_category_id);
