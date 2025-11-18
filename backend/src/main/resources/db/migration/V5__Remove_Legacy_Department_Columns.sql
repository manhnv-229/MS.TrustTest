-- ----------------------------------------------------------------
-- Migration: Xóa các cột legacy code và name từ bảng departments
-- Chỉ giữ lại department_code và department_name
-- CreatedBy: NVMANH with Cline
-- CreatedAt: 15/11/2025 13:36
-- ----------------------------------------------------------------

-- Xóa index trên cột code trước khi xóa cột
ALTER TABLE departments DROP INDEX idx_code;
ALTER TABLE departments DROP INDEX code;

-- Xóa các cột legacy
ALTER TABLE departments DROP COLUMN code;
ALTER TABLE departments DROP COLUMN name;
