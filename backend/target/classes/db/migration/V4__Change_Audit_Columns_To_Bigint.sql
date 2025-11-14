-- Migration: Change audit columns from VARCHAR(100) to BIGINT
-- Author: K24DTCN210-NVMANH (14/11/2025 15:07)
-- Reason: Fix schema mismatch - entities use Long but database has VARCHAR

-- Update classes table
ALTER TABLE classes 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update departments table
ALTER TABLE departments 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update subjects table
ALTER TABLE subjects 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update subject_classes table
ALTER TABLE subject_classes 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update exams table
ALTER TABLE exams 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update questions table
ALTER TABLE questions 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update users table
ALTER TABLE users 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update exam_submissions table
ALTER TABLE exam_submissions 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update submission_answers table
ALTER TABLE submission_answers 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update screenshots table
ALTER TABLE screenshots 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update monitoring_logs table
ALTER TABLE monitoring_logs 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update alerts table
ALTER TABLE alerts 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;

-- Update system_configs table
ALTER TABLE system_configs 
  MODIFY COLUMN created_by BIGINT,
  MODIFY COLUMN updated_by BIGINT;
