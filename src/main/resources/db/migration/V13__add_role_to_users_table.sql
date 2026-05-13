-- Add role column to users table with default CUSTOMER role
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

-- Create index on role for potential filtering
CREATE INDEX idx_users_role ON users(role);

-- Add comment
COMMENT ON COLUMN users.role IS 'User role: CUSTOMER or ADMIN';
