-- V3: Point-level deterministic deduplication for location_logs
-- Dedup basis: subject_device_id + recorded_at + lat + lon + source

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE location_logs
    ADD COLUMN IF NOT EXISTS dedup_key VARCHAR(64);

-- Backfill dedup_key for existing rows where possible.
-- Uses Postgres text formatting with fixed 6 decimal places for coordinates.
UPDATE location_logs
SET dedup_key = encode(
    digest(
        concat_ws(
            '|',
            subject_device_id::text,
            recorded_at::text,
            to_char(ST_Y(location), 'FM999990.000000'),
            to_char(ST_X(location), 'FM999990.000000'),
            source::text
        ),
        'sha256'
    ),
    'hex'
)
WHERE dedup_key IS NULL
  AND location IS NOT NULL
  AND recorded_at IS NOT NULL
  AND source IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_location_logs_subject_dedup_key'
    ) THEN
        ALTER TABLE location_logs
            ADD CONSTRAINT uk_location_logs_subject_dedup_key
                UNIQUE (subject_device_id, dedup_key);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_location_logs_subject_dedup_key
    ON location_logs (subject_device_id, dedup_key);
