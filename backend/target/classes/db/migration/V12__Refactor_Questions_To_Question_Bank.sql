-- ----------------------------------------------------------------
-- Migration V12: Refactor Questions to Question Bank Architecture
-- Purpose: Transform questions from exam-specific (1:N) to reusable question bank (N:M)
-- Changes:
--   1. Remove exam_id from questions table (questions become independent)
--   2. Add subject_id, difficulty, tags to questions (for categorization)
--   3. Add version field for optimistic locking
--   4. Create exam_questions join table (N:M relationship)
--   5. Migrate existing data to new structure
-- CreatedBy: NVMANH with Cline (18/11/2025 18:10)
-- ----------------------------------------------------------------

-- Step 1: Create exam_questions join table FIRST (before modifying questions)
CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    points DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE KEY uk_exam_question (exam_id, question_id),
    UNIQUE KEY uk_exam_order (exam_id, question_order),
    
    -- Foreign keys
    CONSTRAINT fk_exam_questions_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_exam_questions_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_exam_id (exam_id),
    INDEX idx_question_id (question_id),
    INDEX idx_order (exam_id, question_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Migrate existing data from questions to exam_questions
-- Copy exam_id, question_order, points relationship
INSERT INTO exam_questions (exam_id, question_id, question_order, points)
SELECT 
    exam_id,
    id as question_id,
    question_order,
    points
FROM questions
WHERE exam_id IS NOT NULL AND deleted_at IS NULL;

-- Step 3: Add new columns to questions table (for Question Bank)
ALTER TABLE questions
    ADD COLUMN subject_id BIGINT NULL AFTER id,
    ADD COLUMN difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' AFTER question_type,
    ADD COLUMN tags JSON NULL AFTER difficulty,
    ADD COLUMN version INT DEFAULT 0 NOT NULL AFTER tags,
    ADD COLUMN created_by BIGINT NULL AFTER deleted_at,
    ADD COLUMN updated_by BIGINT NULL AFTER created_by;

-- Step 4: Set subject_id for existing questions based on their exam's subject_class
UPDATE questions q
INNER JOIN exams e ON q.exam_id = e.id
INNER JOIN subject_classes sc ON e.subject_class_id = sc.id
SET q.subject_id = sc.subject_id
WHERE q.exam_id IS NOT NULL AND q.deleted_at IS NULL;

-- Step 5: Set created_by for existing questions based on exam creator
UPDATE questions q
INNER JOIN exams e ON q.exam_id = e.id
SET q.created_by = e.created_by
WHERE q.exam_id IS NOT NULL AND q.deleted_at IS NULL;

-- Step 6: Add indexes for new columns
ALTER TABLE questions
    ADD INDEX idx_subject (subject_id),
    ADD INDEX idx_difficulty (difficulty),
    ADD INDEX idx_creator (created_by);

-- Step 7: Drop old foreign key constraint from questions
ALTER TABLE questions DROP FOREIGN KEY questions_ibfk_1;

-- Step 8: Drop old index
ALTER TABLE questions DROP INDEX idx_exam;
ALTER TABLE questions DROP INDEX idx_order;

-- Step 9: Remove exam_id and question_order columns (no longer needed)
-- These are now in exam_questions table
ALTER TABLE questions 
    DROP COLUMN exam_id,
    DROP COLUMN question_order,
    DROP COLUMN points;

-- Step 10: Add foreign key for subject_id
ALTER TABLE questions
    ADD CONSTRAINT fk_questions_subject 
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL;

-- Step 11: Add foreign key for created_by and updated_by
ALTER TABLE questions
    ADD CONSTRAINT fk_questions_created_by 
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_questions_updated_by 
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Step 12: Add version field to exams table (for optimistic locking)
ALTER TABLE exams
    ADD COLUMN version INT DEFAULT 0 NOT NULL AFTER deleted_at;

-- Step 13: Update existing exams to have version = 0
UPDATE exams SET version = 0 WHERE version IS NULL;

-- ----------------------------------------------------------------
-- Summary of changes:
-- 1. ✅ Created exam_questions join table (N:M relationship)
-- 2. ✅ Migrated existing exam-question relationships
-- 3. ✅ Added subject_id, difficulty, tags to questions
-- 4. ✅ Added version field for optimistic locking
-- 5. ✅ Removed exam_id from questions (questions now independent)
-- 6. ✅ Questions can now be reused across multiple exams
-- 7. ✅ Question Bank architecture enabled
-- ----------------------------------------------------------------
