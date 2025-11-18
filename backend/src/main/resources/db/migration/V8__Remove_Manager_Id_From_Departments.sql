-- ================================================================
-- Migration V8: Remove manager_id column from departments table
-- Reason: Entity không map field này, gây conflict khi commit transaction
-- Author: NVMANH with Cline
-- Created: 15/11/2025 16:37
-- ================================================================

-- Drop foreign key constraint first
ALTER TABLE departments DROP FOREIGN KEY departments_ibfk_1;

-- Drop index
ALTER TABLE departments DROP INDEX idx_manager;

-- Drop column
ALTER TABLE departments DROP COLUMN manager_id;
