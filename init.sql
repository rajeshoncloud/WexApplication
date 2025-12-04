-- Note: The database 'purchase_db' is already created by MySQL from MYSQL_DATABASE environment variable
-- This script runs automatically when the MySQL container is first initialized
USE purchase_db;

-- Create purchases table
DROP TABLE IF EXISTS purchases;
CREATE TABLE purchases (
    id CHAR(36) PRIMARY KEY,
    date DATE NOT NULL,
    description VARCHAR(50) NOT NULL,
    purchase_amount DECIMAL(10, 2) NOT NULL,
    country VARCHAR(100) NOT NULL,
    currency_code VARCHAR(100) NOT NULL, -- Stores country_currency_desc (e.g., "Canada-Dollar", "United States-Dollar")
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create api_keys table
DROP TABLE IF EXISTS api_keys;
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    expiration_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
-- Note: currency_code now uses country_currency_desc format from Treasury API
INSERT INTO purchases (id, date, description, purchase_amount, country, currency_code) VALUES
(UUID(), '2025-01-15', 'Laptop Computer', 1299.99, 'United States', 'United States-Dollar'),
(UUID(), '2025-01-16', 'Wireless Mouse', 29.99, 'United States', 'United States-Dollar'),
(UUID(), '2025-01-17', 'Keyboard', 79.99, 'United States', 'United States-Dollar'),
(UUID(), '2025-01-18', 'Monitor 27 inch', 349.99, 'United States', 'United States-Dollar'),
(UUID(), '2025-01-19', 'USB Cable', 12.99, 'United States', 'United States-Dollar');

INSERT INTO api_keys (name, api_key, expiration_date) 
VALUES ('Default API Key', 'wk_3c1f0f65a19444879772ff82833f5347', DATE_ADD(CURDATE(), INTERVAL 1 YEAR));