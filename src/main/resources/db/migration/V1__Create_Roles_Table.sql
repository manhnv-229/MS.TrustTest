-- ===================================================
-- V1: Tạo bảng roles
-- Author: K24DTCN210-NVMANH (13/11/2025 14:24)
-- ===================================================

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    level INT NOT NULL COMMENT '1=STUDENT, 2=TEACHER, 3=CLASS_MANAGER, 4=DEPT_MANAGER, 5=ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name, description, level) VALUES
('ROLE_STUDENT', 'Sinh viên', 1),
('ROLE_TEACHER', 'Giáo viên', 2),
('ROLE_CLASS_MANAGER', 'Quản lý lớp', 3),
('ROLE_DEPT_MANAGER', 'Quản lý khoa', 4),
('ROLE_ADMIN', 'Quản trị viên', 5);
