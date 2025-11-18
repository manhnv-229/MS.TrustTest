-- ================================================================
-- Migration V10: Fix empty role_name values
-- Reason: Migration V3 inserted roles with empty role_name strings
--         causing "Role not found" error in register API
-- Author: NVMANH with Cline
-- Created: 15/11/2025 17:09
-- ================================================================

-- Fix role names for all 5 roles
UPDATE roles SET role_name = 'ROLE_STUDENT' WHERE id = 1 AND (role_name IS NULL OR role_name = '');
UPDATE roles SET role_name = 'ROLE_TEACHER' WHERE id = 2 AND (role_name IS NULL OR role_name = '');
UPDATE roles SET role_name = 'ROLE_CLASS_MANAGER' WHERE id = 3 AND (role_name IS NULL OR role_name = '');
UPDATE roles SET role_name = 'ROLE_DEPT_MANAGER' WHERE id = 4 AND (role_name IS NULL OR role_name = '');
UPDATE roles SET role_name = 'ROLE_ADMIN' WHERE id = 5 AND (role_name IS NULL OR role_name = '');
