-- V3__add_user_status.sql
-- Add user status column (Expand phase of Expand-Contract pattern)

-- Step 1: Add nullable column with default
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'active';

-- Step 2: Backfill existing data
UPDATE users SET status = 'active' WHERE status IS NULL;

-- Step 3: Add constraint (will be NOT NULL in next migration after app update)
-- For now, leave it nullable to allow rollback
