-- ----------------------------------------------------------------
-- Migration V6: Remove legacy columns from classes table
-- Xóa các columns cũ: code, name, class_manager_id
-- Giữ lại: class_code, class_name, homeroom_teacher
-- CreatedBy: NVMANH with Cline (15/11/2025 15:51)
-- ----------------------------------------------------------------

-- Drop indexes liên quan đến column 'code' trước
ALTER TABLE classes DROP INDEX IF EXISTS idx_code;
ALTER TABLE classes DROP INDEX IF EXISTS code;

-- Drop foreign key constraint cho class_manager_id
ALTER TABLE classes DROP FOREIGN KEY IF EXISTS classes_ibfk_2;

-- Drop index cho class_manager_id
ALTER TABLE classes DROP INDEX IF EXISTS idx_manager;

-- Xóa các columns legacy
ALTER TABLE classes 
    DROP COLUMN IF EXISTS code,
    DROP COLUMN IF EXISTS name,
    DROP COLUMN IF EXISTS class_manager_id;

-- Note: Giữ lại class_code, class_name, homeroom_teacher (đang dùng trong entity)
