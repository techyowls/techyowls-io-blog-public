-- V6__drop_old_password_column.sql
-- CONTRACT: Remove old column after app fully migrated

-- Only run this after ALL application instances use encrypted_password

-- Make new column NOT NULL
ALTER TABLE users ALTER COLUMN encrypted_password SET NOT NULL;

-- Drop old column
ALTER TABLE users DROP COLUMN password_hash;

-- Drop backwards compatibility view
DROP VIEW IF EXISTS users_v1;
