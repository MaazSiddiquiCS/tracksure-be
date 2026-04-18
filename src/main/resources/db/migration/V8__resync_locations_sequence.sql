-- V8: Re-sync locations identity sequence after historical updates that changed
-- location_id values to log-derived ids.
-- Without this, inserts can hit duplicate primary-key violations when the sequence
-- falls behind MAX(location_id).
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
