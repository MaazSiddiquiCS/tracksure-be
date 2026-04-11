-- V2: Enforce batch-level idempotency for uploads
-- Ensures the same uploader cannot insert the same clientBatchUuid more than once.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_upload_batches_uploader_client_uuid'
    ) THEN
        ALTER TABLE upload_batches
            ADD CONSTRAINT uk_upload_batches_uploader_client_uuid
                UNIQUE (uploader_device_id, client_batch_uuid);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_upload_batches_uploader_client_uuid
    ON upload_batches (uploader_device_id, client_batch_uuid);
