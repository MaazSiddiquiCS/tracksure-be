-- Convert stolen_devices.device_id from VARCHAR to BIGINT and remove battery_level

ALTER TABLE stolen_devices
    ALTER COLUMN device_id TYPE BIGINT USING device_id::BIGINT;

ALTER TABLE stolen_devices
    DROP COLUMN IF EXISTS battery_level;
