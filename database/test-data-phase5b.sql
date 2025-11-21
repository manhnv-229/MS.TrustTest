-- ============================================================
-- TEST DATA FOR PHASE 5B - WebSocket & Enhanced APIs
-- Author: K24DTCN210-NVMANH
-- Date: 21/11/2025 07:52
-- Purpose: Setup test data để test Phase 5B features
-- ============================================================

-- ============================================================
-- STEP 1: Fix Existing Exam (examId = 1)
-- ============================================================

-- Update exam 1 to be available NOW
UPDATE exams 
SET 
    start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 7 DAY),
    is_published = 1,  -- 1 = published, 0 = draft
    max_attempts = 5
WHERE id = 1;

-- ============================================================
-- STEP 2: Clean Old Test Data (Optional - nếu muốn reset)
-- ============================================================

-- Xóa old submissions của student test (uncommend nếu cần)
-- DELETE FROM student_answers 
-- WHERE submission_id IN (
--     SELECT id FROM exam_submissions WHERE student_id = 2 AND exam_id = 1
-- );
-- 
-- DELETE FROM exam_submissions WHERE student_id = 2 AND exam_id = 1;

-- ============================================================
-- STEP 3: Create New Test Exam (Optional - Exam riêng cho test)
-- ============================================================

-- Insert new test exam
INSERT INTO exams (
    title,
    description,
    subject_class_id,
    created_by,
    start_time,
    end_time,
    duration_minutes,
    total_score,
    passing_score,
    is_published,
    exam_purpose,
    exam_format,
    max_attempts,
    randomize_questions,
    randomize_options,
    show_results_after_submit,
    show_correct_answers,
    allow_review_after_submit,
    show_score_only,
    allow_code_execution,
    programming_language,
    created_at,
    version
) VALUES (
    'Phase 5B Test Exam - WebSocket Features',
    'Test exam for testing Phase 5B WebSocket and Enhanced APIs features. Can be deleted after testing.',
    1,  -- subject_class_id (sử dụng class có sẵn)
    1,  -- created_by (teacher id = 1)
    NOW(),  -- Available immediately
    DATE_ADD(NOW(), INTERVAL 30 DAY),  -- Available for 30 days
    60,  -- 60 minutes duration
    100.00,  -- total_score
    50.00,   -- passing_score (50%)
    1,  -- is_published = 1 (published)
    'MIDTERM',  -- exam_purpose
    'MIXED',   -- exam_format (MULTIPLE_CHOICE_ONLY, ESSAY_ONLY, CODING_ONLY, MIXED)
    5,  -- max_attempts (5 lần)
    false,  -- randomize_questions
    false,  -- randomize_options
    true,   -- show_results_after_submit
    true,   -- show_correct_answers
    true,   -- allow_review_after_submit
    false,  -- show_score_only
    false,  -- allow_code_execution
    null,   -- programming_language
    NOW(),  -- created_at
    0       -- version
);

-- Get new exam ID (will be used for adding questions)
SET @new_exam_id = LAST_INSERT_ID();

-- ============================================================
-- STEP 4: Add Questions to New Test Exam
-- ============================================================

-- Create test questions in question_bank
INSERT INTO question_bank (
    subject_id,
    question_type,
    question_text,
    options,
    correct_answer,
    difficulty,
    grading_criteria,
    created_by,
    created_at,
    version
) VALUES
-- Question 1: Multiple Choice
(
    1,  -- subject_id
    'MULTIPLE_CHOICE',
    'What is the capital of Vietnam?',
    '{"A": "Hanoi", "B": "Ho Chi Minh City", "C": "Da Nang", "D": "Hue"}',
    '"A"',  -- JSON string
    'EASY',
    'Geography question',
    1,  -- created_by
    NOW(),
    0
),
-- Question 2: Multiple Choice
(
    1,
    'MULTIPLE_CHOICE',
    'Which programming language is used for Spring Boot?',
    '{"A": "Python", "B": "Java", "C": "JavaScript", "D": "C++"}',
    '"B"',
    'EASY',
    'Basic programming knowledge',
    1,
    NOW(),
    0
),
-- Question 3: True/False
(
    1,
    'TRUE_FALSE',
    'Is Java an object-oriented programming language?',
    '{"A": "True", "B": "False"}',
    '"A"',
    'EASY',
    'OOP concept',
    1,
    NOW(),
    0
),
-- Question 4: Multiple Choice
(
    1,
    'MULTIPLE_CHOICE',
    'What does REST stand for?',
    '{"A": "Representational State Transfer", "B": "Remote Execution Service Technology", "C": "Rapid Enterprise Software Testing", "D": "Real-time Embedded System Transfer"}',
    '"A"',
    'MEDIUM',
    'REST API knowledge',
    1,
    NOW(),
    0
),
-- Question 5: Essay
(
    1,
    'ESSAY',
    'Explain the difference between JWT and Session-based authentication.',
    '{}',
    'JWT is stateless, stores token on client. Session stores session ID on client, data on server.',
    'MEDIUM',
    'Check understanding of authentication mechanisms. Min 100 words.',
    1,
    NOW(),
    0
);

-- Link questions to exam
INSERT INTO exam_questions (exam_id, question_id, question_order, points)
SELECT 
    @new_exam_id,
    id,
    ROW_NUMBER() OVER (ORDER BY id),
    CASE question_type
        WHEN 'ESSAY' THEN 30.00
        WHEN 'CODING' THEN 40.00
        ELSE 20.00
    END
FROM question_bank
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
ORDER BY id
LIMIT 5;

-- ============================================================
-- STEP 5: Verify Test Data
-- ============================================================

-- Check available exams
SELECT 
    id,
    title,
    is_published,
    start_time,
    end_time,
    duration_minutes,
    max_attempts,
    (SELECT COUNT(*) FROM exam_questions WHERE exam_id = exams.id) as question_count
FROM exams
WHERE is_published = 1
ORDER BY id DESC
LIMIT 5;

-- Check new exam questions
SELECT 
    eq.id,
    eq.exam_id,
    eq.question_order,
    qb.question_text,
    qb.question_type,
    eq.points
FROM exam_questions eq
JOIN question_bank qb ON eq.question_id = qb.id
WHERE eq.exam_id = @new_exam_id
ORDER BY eq.question_order;

-- ============================================================
-- CLEANUP SCRIPT (Run này khi muốn xóa test data)
-- ============================================================

/*
-- Delete test exam and related data
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions 
    WHERE exam_id IN (SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%')
);

DELETE FROM exam_submissions 
WHERE exam_id IN (SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%');

DELETE FROM exam_questions 
WHERE exam_id IN (SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%');

DELETE FROM exams WHERE title LIKE '%Phase 5B Test%';

DELETE FROM question_bank WHERE created_by = 1 AND created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
*/

-- ============================================================
-- QUICK FIX FOR EXAM 1 (Nếu chỉ muốn fix exam 1)
-- ============================================================

/*
-- Simply update exam 1 to be available
UPDATE exams 
SET 
    start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 7 DAY),
    is_published = 1
WHERE id = 1;

-- Check result
SELECT id, title, is_published, start_time, end_time FROM exams WHERE id = 1;
*/

-- ============================================================
-- END OF SCRIPT
-- ============================================================

SELECT 'Test data setup completed!' as status,
       @new_exam_id as new_test_exam_id,
       (SELECT COUNT(*) FROM exams WHERE is_published = 1) as available_exams;
