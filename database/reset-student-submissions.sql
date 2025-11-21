-- ============================================================
-- RESET STUDENT SUBMISSIONS - Quick Fix
-- Author: K24DTCN210-NVMANH
-- Date: 21/11/2025 08:06
-- Purpose: Xóa active submissions để có thể start exam lại
-- ============================================================

-- ============================================================
-- OPTION 1: Delete All Submissions for Student ID = 2
-- ============================================================
-- Use this to completely reset student's exam history

DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions WHERE student_id = 2
);

DELETE FROM exam_submissions WHERE student_id = 2;

-- ============================================================
-- OPTION 2: Delete Only Exam 1 Submissions for Student ID = 2
-- ============================================================
-- Use this to reset only specific exam

/*
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions 
    WHERE student_id = 2 AND exam_id = 1
);

DELETE FROM exam_submissions 
WHERE student_id = 2 AND exam_id = 1;
*/

-- ============================================================
-- OPTION 3: Mark Active Submission as SUBMITTED (Keep data)
-- ============================================================
-- Use this if you want to keep the data but allow new attempt

/*
UPDATE exam_submissions 
SET status = 'SUBMITTED', 
    submitted_at = NOW()
WHERE student_id = 2 
  AND exam_id = 1 
  AND status = 'IN_PROGRESS';
*/

-- ============================================================
-- OPTION 4: Find Active Submissions (Check before delete)
-- ============================================================

SELECT 
    es.id,
    es.student_id,
    es.exam_id,
    e.title as exam_title,
    es.status,
    es.started_at,
    es.attempt_number,
    (SELECT COUNT(*) FROM student_answers WHERE submission_id = es.id) as answers_count
FROM exam_submissions es
JOIN exams e ON es.exam_id = e.id
WHERE es.student_id = 2 
  AND es.status = 'IN_PROGRESS'
ORDER BY es.started_at DESC;

-- ============================================================
-- Verification Query
-- ============================================================

SELECT 
    'Cleanup completed!' as status,
    (SELECT COUNT(*) FROM exam_submissions WHERE student_id = 2) as total_submissions,
    (SELECT COUNT(*) FROM exam_submissions WHERE student_id = 2 AND status = 'IN_PROGRESS') as active_submissions;
