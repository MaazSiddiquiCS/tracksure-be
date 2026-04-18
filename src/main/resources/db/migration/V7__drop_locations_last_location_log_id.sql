-- V7: Remove last_location_log_id from latest projection table.
-- This column is not required for serving latest-location reads and can create
-- confusing coupling with location_logs.
ALTER TABLE locations
    DROP COLUMN IF EXISTS last_location_log_id;