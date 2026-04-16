-- V5__rename_column_safely.sql
-- Safe column rename using Expand-Contract pattern
--
-- Goal: Rename users.password_hash -> users.encrypted_password
-- Problem: Direct rename breaks running instances
-- Solution: Expand (add new), Migrate (copy), Contract (remove old)

-- EXPAND: Add new column (old name still works)
ALTER TABLE users ADD COLUMN encrypted_password VARCHAR(255);

-- MIGRATE: Copy data
UPDATE users SET encrypted_password = password_hash;

-- Note: In production, deploy app that writes to BOTH columns
-- Then run V6 to drop old column after all instances updated

-- Create view for backwards compatibility (optional)
CREATE OR REPLACE VIEW users_v1 AS
SELECT
    id,
    email,
    password_hash,  -- Old name
    encrypted_password,  -- New name (same value)
    first_name,
    last_name,
    status,
    created_at,
    updated_at
FROM users;
