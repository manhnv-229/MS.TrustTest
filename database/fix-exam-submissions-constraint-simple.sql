-- ===============================================
-- FIX EXAM SUBMISSIONS CONSTRAINT (Simple Version)
-- Drop constraint sai và tạo constraint đúng
-- Author: K24DTCN210-NVMANH (03/12/2025 13:32)
-- Compatible với MySQL older versions
-- ===============================================

USE ms_trusttest;

-- Step 1: Drop constraint cũ (ignore error nếu không tồn tại)
-- Sử dụng simple syntax
DROP INDEX uk_exam_student ON exam_submissions;

-- Step 2: Tạo constraint mới đúng thiết kế
-- Cho phép student làm nhiều attempt, nhưng mỗi attempt phải unique
ALTER TABLE exam_submissions 
ADD CONSTRAINT uk_exam_student_attempt 
UNIQUE (exam_id, student_id, attempt_number);

-- Step 3: Thêm index để improve performance
CREATE INDEX idx_exam_submissions_student_status 
ON exam_submissions (student_id, status);

CREATE INDEX idx_exam_submissions_exam_status 
ON exam_submissions (exam_id, status);

-- Step 4: Verify results
SELECT 'Constraint fix completed' AS status;

-- Kiểm tra dữ liệu sau khi fix
SELECT exam_id, student_id, attempt_number, status, started_at 
FROM exam_submissions 
WHERE exam_id = 5 AND student_id = 5
ORDER BY attempt_number;
