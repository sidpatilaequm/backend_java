-- Channel and ChannelCategory Tables Creation Script (Updated)
-- This script creates the necessary tables for the Channel management system
-- Updated to make user_id nullable and focus on company isolation

-- Create channel table
CREATE TABLE IF NOT EXISTS channel (
    channel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_name VARCHAR(255) NOT NULL,
    channel_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    user_id BIGINT NULL,  -- Made nullable
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (company_id) REFERENCES company_details(company_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_detail(user_id) ON DELETE SET NULL,  -- Changed to SET NULL
    
    -- Indexes for better performance
    INDEX idx_channel_company (company_id),
    INDEX idx_channel_code (channel_code),
    INDEX idx_channel_active (is_active)
);

-- Create channel_category table
CREATE TABLE IF NOT EXISTS channel_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    channel_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_category_channel (channel_id),
    INDEX idx_category_code (category_code),
    INDEX idx_category_active (is_active),
    
    -- Unique constraint to prevent duplicate category codes within the same channel
    UNIQUE KEY uk_channel_category_code (channel_id, category_code)
);

-- Insert sample data for testing (optional)
-- INSERT INTO channel (channel_name, channel_code, description, company_id) 
-- VALUES ('Amazon', 'AMZ', 'Amazon India marketplace', 1);

-- INSERT INTO channel_category (category_code, category_name, channel_id) 
-- VALUES 
--     ('ELEC', 'Electronics', 1),
--     ('FASH', 'Fashion', 1),
--     ('SHOE', 'Shoes', 1);
