-- V4__make_status_not_null.sql
-- Contract phase: Now that app handles status, make it required

-- Verify all rows have status (will fail if any are NULL)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE status IS NULL) THEN
        RAISE EXCEPTION 'Cannot apply migration: users.status contains NULL values';
    END IF;
END $$;

-- Make column NOT NULL
ALTER TABLE users ALTER COLUMN status SET NOT NULL;

-- Add check constraint for valid values
ALTER TABLE users ADD CONSTRAINT chk_user_status
    CHECK (status IN ('active', 'inactive', 'suspended', 'deleted'));
