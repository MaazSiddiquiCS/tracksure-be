-- V0: Baseline schema for environments that no longer rely on Hibernate ddl-auto.
--
-- Creates the core tables/sequences/extensions that V1+ historically assumed
-- already existed. The script is intentionally idempotent.

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS devices_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS device_links_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS profiles_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS refresh_tokens_seq START WITH 1 INCREMENT BY 10;
CREATE SEQUENCE IF NOT EXISTS upload_batches_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS location_logs_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
    device_id BIGINT PRIMARY KEY DEFAULT nextval('devices_seq'),
    peer_id VARCHAR(16) NOT NULL,
    device_name VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    last_seen_at TIMESTAMPTZ,
    owner_user_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
    profile_id BIGINT PRIMARY KEY DEFAULT nextval('profiles_seq'),
    full_name VARCHAR(255),
    phone_number VARCHAR(255),
    bio TEXT,
    profile_pic TEXT,
    roll_number VARCHAR(255),
    department VARCHAR(255),
    user_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY DEFAULT nextval('refresh_tokens_seq'),
    token VARCHAR(512) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS device_links (
    link_id BIGINT PRIMARY KEY DEFAULT nextval('device_links_seq'),
    permission_type VARCHAR(32) NOT NULL,
    follower_id BIGINT NOT NULL,
    target_device_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS upload_batches (
    batch_id BIGINT PRIMARY KEY DEFAULT nextval('upload_batches_seq'),
    client_batch_uuid VARCHAR(255) NOT NULL,
    points_count INTEGER NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL,
    uploader_device_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS location_logs (
    location_id BIGINT PRIMARY KEY DEFAULT nextval('location_logs_seq'),
    recorded_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    accuracy DOUBLE PRECISION,
    source VARCHAR(32) NOT NULL,
    location geometry(Point,4326) NOT NULL,
    subject_device_id BIGINT NOT NULL,
    uploader_device_id BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_devices_owner_user_id
    ON devices (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_devices_peer_id
    ON devices (peer_id);

CREATE INDEX IF NOT EXISTS idx_profiles_department
    ON profiles (department);

CREATE INDEX IF NOT EXISTS idx_device_links_follower_id
    ON device_links (follower_id);
CREATE INDEX IF NOT EXISTS idx_device_links_target_device_id
    ON device_links (target_device_id);

CREATE INDEX IF NOT EXISTS idx_upload_batches_uploader_device_id
    ON upload_batches (uploader_device_id);
CREATE INDEX IF NOT EXISTS idx_upload_batches_client_batch_uuid
    ON upload_batches (client_batch_uuid);

CREATE INDEX IF NOT EXISTS idx_location_logs_recorded_at
    ON location_logs (recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_location_logs_subject_device_id
    ON location_logs (subject_device_id);
CREATE INDEX IF NOT EXISTS idx_location_logs_uploader_device_id
    ON location_logs (uploader_device_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_email'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uk_users_email UNIQUE (email);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_username'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uk_users_username UNIQUE (username);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_devices_peer_id'
    ) THEN
        ALTER TABLE devices
            ADD CONSTRAINT uk_devices_peer_id UNIQUE (peer_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_profiles_roll_number'
    ) THEN
        ALTER TABLE profiles
            ADD CONSTRAINT uk_profiles_roll_number UNIQUE (roll_number);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_profiles_user_id'
    ) THEN
        ALTER TABLE profiles
            ADD CONSTRAINT uk_profiles_user_id UNIQUE (user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_device_links_follower_target'
    ) THEN
        ALTER TABLE device_links
            ADD CONSTRAINT uk_device_links_follower_target
                UNIQUE (follower_id, target_device_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_refresh_tokens_token'
    ) THEN
        ALTER TABLE refresh_tokens
            ADD CONSTRAINT uk_refresh_tokens_token UNIQUE (token);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_devices_owner_user'
    ) THEN
        ALTER TABLE devices
            ADD CONSTRAINT fk_devices_owner_user
                FOREIGN KEY (owner_user_id) REFERENCES users(user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_profiles_user'
    ) THEN
        ALTER TABLE profiles
            ADD CONSTRAINT fk_profiles_user
                FOREIGN KEY (user_id) REFERENCES users(user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_tokens_user'
    ) THEN
        ALTER TABLE refresh_tokens
            ADD CONSTRAINT fk_refresh_tokens_user
                FOREIGN KEY (user_id) REFERENCES users(user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_device_links_follower'
    ) THEN
        ALTER TABLE device_links
            ADD CONSTRAINT fk_device_links_follower
                FOREIGN KEY (follower_id) REFERENCES users(user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_device_links_target_device'
    ) THEN
        ALTER TABLE device_links
            ADD CONSTRAINT fk_device_links_target_device
                FOREIGN KEY (target_device_id) REFERENCES devices(device_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_upload_batches_uploader_device'
    ) THEN
        ALTER TABLE upload_batches
            ADD CONSTRAINT fk_upload_batches_uploader_device
                FOREIGN KEY (uploader_device_id) REFERENCES devices(device_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_location_logs_subject_device'
    ) THEN
        ALTER TABLE location_logs
            ADD CONSTRAINT fk_location_logs_subject_device
                FOREIGN KEY (subject_device_id) REFERENCES devices(device_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_location_logs_uploader_device'
    ) THEN
        ALTER TABLE location_logs
            ADD CONSTRAINT fk_location_logs_uploader_device
                FOREIGN KEY (uploader_device_id) REFERENCES devices(device_id);
    END IF;
END $$;

-- Ensure existing schemas also use the expected sequence defaults.
ALTER TABLE users
    ALTER COLUMN user_id SET DEFAULT nextval('users_seq');
ALTER TABLE devices
    ALTER COLUMN device_id SET DEFAULT nextval('devices_seq');
ALTER TABLE profiles
    ALTER COLUMN profile_id SET DEFAULT nextval('profiles_seq');
ALTER TABLE refresh_tokens
    ALTER COLUMN id SET DEFAULT nextval('refresh_tokens_seq');
ALTER TABLE device_links
    ALTER COLUMN link_id SET DEFAULT nextval('device_links_seq');
ALTER TABLE upload_batches
    ALTER COLUMN batch_id SET DEFAULT nextval('upload_batches_seq');
ALTER TABLE location_logs
    ALTER COLUMN location_id SET DEFAULT nextval('location_logs_seq');
