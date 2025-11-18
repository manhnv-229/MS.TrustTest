-- ----------------------------------------------------------------
-- V7: Remove legacy code and name columns from subjects table
-- These columns have been replaced with subject_code and subject_name
-- CreatedBy: NVMANH with Cline (15/11/2025 16:17)
-- ----------------------------------------------------------------

-- Drop legacy columns from subjects table
-- MySQL 8.0 does not support DROP COLUMN IF EXISTS in single statement
-- Must drop each column separately
ALTER TABLE subjects DROP COLUMN code;
ALTER TABLE subjects DROP COLUMN name;

-- Verify the changes
-- The table should now only have subject_code and subject_name columns
