-- ================================================================
-- Migration V11: Fix Department Unique Constraint
-- Mục đích: Cho phép tạo department với code đã bị soft delete
-- CreatedBy: NVMANH with Cline (15/11/2025 17:28)
-- ================================================================

-- Drop unique constraint cũ (không có điều kiện deleted_at)
ALTER TABLE departments DROP INDEX UK89g8qie2y696a3tarmty43sq9;

-- Tạo unique index mới CHỈ áp dụng cho records chưa xóa
-- MySQL không support partial index như PostgreSQL
-- Workaround: Sử dụng stored generated column
ALTER TABLE departments 
ADD COLUMN department_code_active VARCHAR(20) 
GENERATED ALWAYS AS (IF(deleted_at IS NULL, department_code, NULL)) STORED;

-- Tạo unique constraint trên generated column
ALTER TABLE departments 
ADD UNIQUE INDEX idx_department_code_active (department_code_active);
