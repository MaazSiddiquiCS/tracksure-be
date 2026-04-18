-- V6: Normalize latest-location projection identity and add reverse relation to location_logs.
-- Goals:
-- 1) Keep locations.location_id as the projection-row identity (do not overwrite with log ids)
-- 2) Ensure columns expected by application exist
-- 3) Add optional one-to-many link: locations -> location_logs via location_logs.projection_location_id

-- Ensure projection metadata columns exist for latest projection updates.
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;

ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS last_location_log_id BIGINT;

-- Ensure location_id can still auto-generate for new projection rows.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_class
        WHERE relkind = 'S'
          AND relname = 'locations_location_id_seq'
    ) THEN
        CREATE SEQUENCE locations_location_id_seq;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'locations'
          AND column_name = 'location_id'
          AND (column_default IS NULL OR column_default NOT LIKE 'nextval(%')
    ) THEN
        ALTER TABLE locations
            ALTER COLUMN location_id SET DEFAULT nextval('locations_location_id_seq');
    END IF;

    PERFORM setval(
        'locations_location_id_seq',
        GREATEST((SELECT COALESCE(MAX(location_id), 1) FROM locations), 1),
        true
    );
END $$;

-- If a legacy schema has location_pk (projection row id), normalize location_id
-- to represent projection identity as intended for API responses.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'locations'
          AND column_name = 'location_pk'
    ) THEN
        UPDATE locations
        SET location_id = location_pk
        WHERE location_pk IS NOT NULL;
    END IF;
END $$;

-- Ensure last_location_log_id is not modeled as a FK (keep it as metadata pointer only).
DO $$
DECLARE c RECORD;
BEGIN
    FOR c IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'locations'::regclass
          AND contype = 'f'
          AND conkey = ARRAY[
            (SELECT attnum
             FROM pg_attribute
             WHERE attrelid = 'locations'::regclass
               AND attname = 'last_location_log_id')
          ]
    LOOP
        EXECUTE format('ALTER TABLE locations DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'locations'
          AND column_name = 'owner_user_id'
    ) THEN
        -- Backfill owner_user_id from subject device owner when missing.
        UPDATE locations l
        SET owner_user_id = d.owner_user_id
        FROM devices d
        WHERE l.subject_device_id = d.device_id
          AND l.owner_user_id IS NULL;

        ALTER TABLE locations
            ALTER COLUMN owner_user_id SET NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_locations_owner_user'
        ) THEN
            ALTER TABLE locations
                ADD CONSTRAINT fk_locations_owner_user
                    FOREIGN KEY (owner_user_id) REFERENCES users(user_id);
        END IF;
    END IF;
END $$;

-- Add reverse relation from location_logs to locations (optional, non-breaking).
ALTER TABLE location_logs
    ADD COLUMN IF NOT EXISTS projection_location_id BIGINT;

-- Backfill relation by subject_device_id (one projection row per subject device).
UPDATE location_logs ll
SET projection_location_id = l.location_id
FROM locations l
WHERE l.subject_device_id = ll.subject_device_id
  AND ll.projection_location_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_location_logs_projection_location_id
    ON location_logs (projection_location_id);
