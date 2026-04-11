-- V4: Latest-location projection table (one row per subject device)
-- This table is maintained by the ingestion service whenever new location_logs are inserted.

CREATE TABLE IF NOT EXISTS locations (
    location_id BIGSERIAL PRIMARY KEY,
    recorded_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    accuracy DOUBLE PRECISION,
    source VARCHAR(32) NOT NULL,
    location geometry(Point,4326) NOT NULL,
    subject_device_id BIGINT NOT NULL,
    uploader_device_id BIGINT NOT NULL,
    CONSTRAINT uk_locations_subject_device_id UNIQUE (subject_device_id),
    CONSTRAINT fk_locations_subject_device FOREIGN KEY (subject_device_id) REFERENCES devices(device_id),
    CONSTRAINT fk_locations_uploader_device FOREIGN KEY (uploader_device_id) REFERENCES devices(device_id)
);

CREATE INDEX IF NOT EXISTS idx_locations_subject_device_id ON locations(subject_device_id);
CREATE INDEX IF NOT EXISTS idx_locations_recorded_at ON locations(recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_locations_location_gist ON locations USING GIST (location);
