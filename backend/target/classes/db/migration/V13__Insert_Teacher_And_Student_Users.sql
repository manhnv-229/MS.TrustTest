-- ===================================================
-- V13: Insert Teacher and Student sample users
-- Author: K24DTCN210-NVMANH (19/11/2025 02:51)
-- ===================================================

-- Insert Teacher user (username: teacher1, password: Teacher@123)
-- Password hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lk3K3Z7.n8Nu (Teacher@123)
INSERT INTO users (student_code, email, password_hash, full_name, phone_number, department_id, is_active, created_at) VALUES
('T001', 'giaovien@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lk3K3Z7.n8Nu', 'Nguyen Van B - Giao Vien', '0901234567', (SELECT id FROM departments WHERE code = 'CNTT'), TRUE, NOW());

-- Assign TEACHER role to teacher user
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE student_code = 'T001'), (SELECT id FROM roles WHERE name = 'ROLE_TEACHER'));

-- Update existing student with better data
UPDATE users 
SET 
    full_name = 'Tran Thi C - Sinh Vien',
    phone_number = '0912345678',
    class_id = (SELECT id FROM classes WHERE code = 'K24DTCN210'),
    department_id = (SELECT id FROM departments WHERE code = 'CNTT')
WHERE student_code = 'K24001';
