-- ===================================================
-- MS.TrustTest Database Initialization Script
-- Tạo database và user
-- Author: K24DTCN210-NVMANH (13/11/2025 14:24)
-- ===================================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS ms_trust_exam 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Tạo user
CREATE USER IF NOT EXISTS 'mstrust'@'localhost' IDENTIFIED BY 'mstrust123';
CREATE USER IF NOT EXISTS 'mstrust'@'%' IDENTIFIED BY 'mstrust123';

-- Gán quyền
GRANT ALL PRIVILEGES ON ms_trust_exam.* TO 'mstrust'@'localhost';
GRANT ALL PRIVILEGES ON ms_trust_exam.* TO 'mstrust'@'%';

-- Áp dụng thay đổi
FLUSH PRIVILEGES;

-- Sử dụng database
USE ms_trust_exam;

-- Hiển thị thông báo
SELECT 'Database ms_trust_exam created successfully!' AS message;
SELECT 'User mstrust created with full privileges!' AS message;
