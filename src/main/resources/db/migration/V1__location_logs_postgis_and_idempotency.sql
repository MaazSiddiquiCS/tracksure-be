-- V1: PostGIS support, idempotency, and indexing for location_logs
--
-- This migration assumes the location_logs table already exists (created by
-- Hibernate ddl-auto). It adds:
--   1. client_point_id column for idempotent batch uploads
--   2. Unique constraint for deduplication
--   3. GIST spatial index on the location geometry column
--   4. Composite read-optimised index on (subject_device_id, recorded_at DESC)

-- 1. Add client_point_id column (nullable to support pre-existing rows)
ALTER TABLE location_logs
    ADD COLUMN IF NOT EXISTS client_point_id VARCHAR(255);

-- 2. Unique constraint: prevents duplicate points from offline retry uploads
ALTER TABLE location_logs
    ADD CONSTRAINT uk_location_logs_idempotency
        UNIQUE (subject_device_id, uploader_device_id, client_point_id);

-- 3. GIST spatial index on the PostGIS geometry column
--    (cannot be expressed as a standard B-tree index; must be GIST for spatial queries)
CREATE INDEX IF NOT EXISTS idx_location_logs_location_gist
    ON location_logs USING GIST (location);

-- 4. Composite B-tree index for efficient per-subject time-ordered reads
CREATE INDEX IF NOT EXISTS idx_location_logs_subject_recorded_at
    ON location_logs (subject_device_id, recorded_at DESC);
