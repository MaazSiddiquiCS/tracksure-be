-- Allow one user to own multiple devices by dropping legacy unique constraint(s)
-- that enforce uniqueness on devices.owner_user_id.
DO $$
DECLARE c RECORD;
BEGIN
  FOR c IN
    SELECT conname
    FROM pg_constraint
    WHERE conrelid = 'devices'::regclass
      AND contype = 'u'
      AND conkey = ARRAY[
        (SELECT attnum
         FROM pg_attribute
         WHERE attrelid = 'devices'::regclass
           AND attname = 'owner_user_id')
      ]
  LOOP
    EXECUTE format('ALTER TABLE devices DROP CONSTRAINT %I', c.conname);
  END LOOP;
END $$;
