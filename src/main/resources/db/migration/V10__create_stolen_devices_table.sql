-- Create sequence for stolen_devices table
CREATE SEQUENCE IF NOT EXISTS stolen_devices_seq
    START WITH 50
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 50;

-- Create stolen_devices table for tracking stolen/lost devices and recovery analytics
CREATE TABLE IF NOT EXISTS stolen_devices (
    id BIGINT NOT NULL DEFAULT nextval('stolen_devices_seq'::regclass),
    device_id VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    formatted_address VARCHAR(500),
    city VARCHAR(100),
    battery_level INTEGER,
    is_recovered BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP NOT NULL,
    recovery_timestamp TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_stolen_devices_user_id
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- Create indexes for optimal query performance
CREATE INDEX idx_stolen_devices_device_id ON stolen_devices(device_id);
CREATE INDEX idx_stolen_devices_user_id ON stolen_devices(user_id);
CREATE INDEX idx_stolen_devices_city ON stolen_devices(city);
CREATE INDEX idx_stolen_devices_is_recovered ON stolen_devices(is_recovered);
CREATE INDEX idx_stolen_devices_timestamp ON stolen_devices(timestamp);
CREATE INDEX idx_stolen_devices_lat_lon ON stolen_devices(latitude, longitude);

-- Composite index for analytics queries
CREATE INDEX idx_stolen_devices_analytics ON stolen_devices(is_recovered, city, timestamp);

-- Add comments for documentation
COMMENT ON TABLE stolen_devices IS 'Tracks stolen/lost device reports with location data and recovery status for heatmap and analytics';
COMMENT ON COLUMN stolen_devices.device_id IS 'Unique device identifier from BLE tracking';
COMMENT ON COLUMN stolen_devices.user_id IS 'Owner/reporter user ID';
COMMENT ON COLUMN stolen_devices.battery_level IS 'Device battery percentage at time of report';
COMMENT ON COLUMN stolen_devices.is_recovered IS 'Flag indicating if device has been recovered';
COMMENT ON COLUMN stolen_devices.recovery_timestamp IS 'When the device was marked as recovered';
