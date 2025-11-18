-- ================================================================
-- Migration V9: Fix NULL version fields in all tables
-- Reason: Hibernate @Version optimistic locking requires non-NULL version
-- Author: NVMANH with Cline
-- Created: 15/11/2025 17:01
-- ================================================================

-- Fix departments table
UPDATE departments SET version = 0 WHERE version IS NULL;

-- Fix classes table  
UPDATE classes SET version = 0 WHERE version IS NULL;

-- Fix users table
UPDATE users SET version = 0 WHERE version IS NULL;

-- Fix roles table
UPDATE roles SET version = 0 WHERE version IS NULL;

-- Fix subjects table (if has version column)
-- UPDATE subjects SET version = 0 WHERE version IS NULL;

-- Fix subject_classes table (if has version column)
-- UPDATE subject_classes SET version = 0 WHERE version IS NULL;
