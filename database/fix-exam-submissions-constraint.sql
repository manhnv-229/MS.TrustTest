-- ===============================================
-- FIX EXAM SUBMISSIONS CONSTRAINT  
-- Drop constraint sai và tạo constraint đúng
-- Author: K24DTCN210-NVMANH (03/12/2025 12:45)
-- ===============================================

USE ms_trusttest;

-- Step 1: Check và drop constraint cũ nếu tồn tại
-- MySQL syntax compatible với older versions
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = 'ms_trusttest' 
     AND TABLE_NAME = 'exam_submissions' 
     AND INDEX_NAME = 'uk_exam_student') > 0,
    'ALTER TABLE exam_submissions DROP INDEX uk_exam_student',
    'SELECT "Index uk_exam_student does not exist" AS message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Tạo constraint mới đúng thiết kế
-- Cho phép student làm nhiều attempt, nhưng mỗi attempt phải unique
ALTER TABLE exam_submissions 
ADD CONSTRAINT uk_exam_student_attempt 
UNIQUE KEY (exam_id, student_id, attempt_number);

-- Step 3: Thêm index để improve performance (với check exist)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = 'ms_trusttest' 
     AND TABLE_NAME = 'exam_submissions' 
     AND INDEX_NAME = 'idx_exam_submissions_student_status') = 0,
    'CREATE INDEX idx_exam_submissions_student_status ON exam_submissions (student_id, status)',
    'SELECT "Index idx_exam_submissions_student_status already exists" AS message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = 'ms_trusttest' 
     AND TABLE_NAME = 'exam_submissions' 
     AND INDEX_NAME = 'idx_exam_submissions_exam_status') = 0,
    'CREATE INDEX idx_exam_submissions_exam_status ON exam_submissions (exam_id, status)',
    'SELECT "Index idx_exam_submissions_exam_status already exists" AS message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Verify constraint
SELECT 
    CONSTRAINT_NAME, 
    CONSTRAINT_TYPE,
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'ms_trusttest' 
AND TABLE_NAME = 'exam_submissions' 
AND CONSTRAINT_TYPE = 'UNIQUE';

-- Kiểm tra dữ liệu sau khi fix
SELECT exam_id, student_id, attempt_number, status, started_at 
FROM exam_submissions 
WHERE exam_id = 5 AND student_id = 5
ORDER BY attempt_number;
