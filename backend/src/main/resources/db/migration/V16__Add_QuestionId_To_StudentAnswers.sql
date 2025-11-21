-- =====================================================
-- Migration V16: Add question_id column to student_answers table
-- Purpose: Fix bug where StudentAnswer entity doesn't have direct reference to Question
-- Author: K24DTCN210-NVMANH (20/11/2025 14:58)
-- =====================================================

-- Step 1: Add question_id column
-- Note: NOT NULL constraint is added, but we need to handle existing data first
ALTER TABLE student_answers 
ADD COLUMN question_id BIGINT NULL AFTER submission_id;

-- Step 2: Populate question_id from exam_questions via submission
-- This SQL assumes we can trace back from submission -> exam -> exam_questions
UPDATE student_answers sa
INNER JOIN exam_submissions es ON sa.submission_id = es.id
INNER JOIN exam_questions eq ON eq.exam_id = es.exam_id
SET sa.question_id = eq.question_id
WHERE sa.question_id IS NULL;

-- Step 3: Now make the column NOT NULL since all existing rows have been populated
ALTER TABLE student_answers 
MODIFY COLUMN question_id BIGINT NOT NULL;

-- Step 4: Add foreign key constraint
ALTER TABLE student_answers
ADD CONSTRAINT fk_student_answer_question
FOREIGN KEY (question_id) REFERENCES questions(id)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- Step 5: Add index for better query performance
CREATE INDEX idx_student_answer_question ON student_answers(question_id);

-- Step 6: Add composite index for common queries (submission + question)
CREATE INDEX idx_student_answer_submission_question ON student_answers(submission_id, question_id);

-- =====================================================
-- VERIFICATION QUERIES (Run manually to verify)
-- =====================================================
-- SELECT COUNT(*) FROM student_answers WHERE question_id IS NULL;  -- Should be 0
-- SELECT COUNT(*) FROM student_answers WHERE question_id IS NOT NULL;  -- Should match total rows
-- SHOW INDEX FROM student_answers;  -- Should show new indexes
-- =====================================================
