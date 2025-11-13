-- ===================================================
-- V3: Insert sample data
-- Author: K24DTCN210-NVMANH (13/11/2025 14:25)
-- ===================================================

-- Insert default system configs
INSERT INTO system_configs (config_key, config_value, description, data_type) VALUES
('monitoring.screenshot.default_interval', '60', 'Default screenshot interval in seconds', 'NUMBER'),
('monitoring.screenshot.max_size_mb', '5', 'Max screenshot file size in MB', 'NUMBER'),
('monitoring.alert.auto_flag_threshold', '5', 'Auto flag after N suspicious activities', 'NUMBER'),
('exam.auto_save_interval', '30', 'Auto save interval in seconds', 'NUMBER'),
('system.maintenance_mode', 'false', 'System maintenance mode', 'BOOLEAN');

-- Insert default admin user (password: Admin@123)
-- Password hash generated with BCrypt cost factor 12
INSERT INTO users (student_code, email, password_hash, full_name, is_active) VALUES
('ADMIN', 'admin@mstrust.edu.vn', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lk3K3Z7.n8Nu', 'System Administrator', TRUE);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE student_code = 'ADMIN'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));

-- Insert sample department
INSERT INTO departments (code, name, description, manager_id) VALUES
('CNTT', 'Khoa Công Nghệ Thông Tin', 'Khoa đào tạo về CNTT', NULL);

-- Insert sample class
INSERT INTO classes (code, name, department_id, academic_year) VALUES
('K24DTCN210', 'Lớp Công Nghệ Thông Tin K24', (SELECT id FROM departments WHERE code = 'CNTT'), '2024-2025');

-- Insert sample subject
INSERT INTO subjects (code, name, description, credits, department_id) VALUES
('CS101', 'Nhập môn Lập trình', 'Môn học cơ bản về lập trình', 4, (SELECT id FROM departments WHERE code = 'CNTT'));
