-- Insert initial admin user
-- Username: admin, Email: admin@tracksure.local
-- Password: bcrypt hash of 'admin123' (generated via BCryptPasswordEncoder)
-- This is a temporary credential and must be changed immediately on first login
INSERT INTO users (user_id, username, email, password_hash, role, created_at)
VALUES (
    21,
    'admin',
    'admin@tracksure.local',
    '$2a$12$Hsyl95SWnJguLYFU/4ITXOMbrURNERtaAxGxlhiCHxMsegA7wwKQO',
    'ADMIN',
    NOW()
) ON CONFLICT (user_id) DO NOTHING;
