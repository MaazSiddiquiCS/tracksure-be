-- V9: Normalize latest projection table to use location_id as the sole primary key.
-- Some environments still carry legacy location_pk (NOT NULL, PRIMARY KEY),
-- which causes inserts to fail because application writes location_id only.

-- Ensure identity sequence exists and is wired to location_id.
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

    ALTER TABLE locations
        ALTER COLUMN location_id SET DEFAULT nextval('locations_location_id_seq');

    PERFORM setval(
        'locations_location_id_seq',
        GREATEST((SELECT COALESCE(MAX(location_id), 1) FROM locations), 1),
        true
    );
END $$;

-- If legacy location_pk exists, backfill any missing location_id values.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'locations'
          AND column_name = 'location_pk'
    ) THEN
        UPDATE locations
        SET location_id = COALESCE(location_id, location_pk)
        WHERE location_id IS NULL;
    END IF;
END $$;

-- Drop primary-key constraints that are anchored to location_pk.
DO $$
DECLARE c RECORD;
BEGIN
    FOR c IN
        SELECT pc.conname
        FROM pg_constraint pc
        JOIN pg_attribute pa
          ON pa.attrelid = pc.conrelid
         AND pa.attnum = ANY (pc.conkey)
        WHERE pc.conrelid = 'locations'::regclass
          AND pc.contype = 'p'
          AND pa.attname = 'location_pk'
    LOOP
        EXECUTE format('ALTER TABLE locations DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

-- Ensure location_id is the primary key.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint pc
        JOIN pg_attribute pa
          ON pa.attrelid = pc.conrelid
         AND pa.attnum = ANY (pc.conkey)
        WHERE pc.conrelid = 'locations'::regclass
          AND pc.contype = 'p'
          AND pa.attname = 'location_id'
    ) THEN
        ALTER TABLE locations
            ADD CONSTRAINT locations_pkey PRIMARY KEY (location_id);
    END IF;
END $$;

-- Remove obsolete legacy column once identity has been normalized.
ALTER TABLE locations
    DROP COLUMN IF EXISTS location_pk;
