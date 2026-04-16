-- R__refresh_statistics.sql
-- Repeatable migration: Runs on every change (checksum-based)
--
-- Use cases:
-- - Stored procedures
-- - Views
-- - Functions
-- - Permissions

-- Product statistics view
CREATE OR REPLACE VIEW product_statistics AS
SELECT
    category,
    COUNT(*) as product_count,
    AVG(price) as avg_price,
    SUM(stock_quantity) as total_stock,
    MIN(price) as min_price,
    MAX(price) as max_price
FROM products
GROUP BY category;

-- User statistics view
CREATE OR REPLACE VIEW user_statistics AS
SELECT
    status,
    COUNT(*) as user_count,
    MIN(created_at) as earliest_registration,
    MAX(created_at) as latest_registration
FROM users
GROUP BY status;
