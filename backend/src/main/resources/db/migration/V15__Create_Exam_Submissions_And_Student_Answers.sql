-- V15: Create exam_submissions and student_answers tables for Phase 5
-- Author: K24DTCN210-NVMANH
-- Date: 19/11/2025 14:55

-- Table: exam_submissions
-- Lưu thông tin về lần làm bài thi của student
CREATE TABLE exam_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL COMMENT 'ID của bài thi',
    student_id BIGINT NOT NULL COMMENT 'ID của student',
    attempt_number INT NOT NULL DEFAULT 1 COMMENT 'Lần làm bài thứ mấy',
    
    -- Timing
    started_at TIMESTAMP NULL COMMENT 'Thời điểm bắt đầu làm bài',
    submitted_at TIMESTAMP NULL COMMENT 'Thời điểm nộp bài',
    time_spent_seconds INT DEFAULT 0 COMMENT 'Tổng thời gian làm bài (giây)',
    
    -- Status & Scoring
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'Trạng thái: NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED, EXPIRED',
    total_score DECIMAL(5,2) DEFAULT 0 COMMENT 'Tổng điểm đạt được',
    max_score DECIMAL(5,2) COMMENT 'Điểm tối đa của bài thi',
    passed BOOLEAN DEFAULT FALSE COMMENT 'Đạt/Không đạt',
    
    -- Randomization seed (để recreate câu hỏi đã random)
    question_seed BIGINT COMMENT 'Seed để random thứ tự câu hỏi',
    option_seed BIGINT COMMENT 'Seed để random thứ tự options',
    
    -- Auto-save tracking
    last_saved_at TIMESTAMP NULL COMMENT 'Lần save cuối cùng',
    auto_save_count INT DEFAULT 0 COMMENT 'Số lần auto-save',
    
    -- Audit fields
    version INT DEFAULT 0 COMMENT 'Optimistic locking',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint: student + exam + attempt
    UNIQUE KEY uk_student_exam_attempt (student_id, exam_id, attempt_number),
    
    -- Indexes for performance
    INDEX idx_exam_submissions_exam (exam_id),
    INDEX idx_exam_submissions_student (student_id),
    INDEX idx_exam_submissions_status (status),
    INDEX idx_exam_submissions_started (started_at),
    INDEX idx_exam_submissions_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lưu thông tin lần làm bài thi của student';

-- Table: student_answers
-- Lưu câu trả lời của student cho từng câu hỏi
CREATE TABLE student_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT 'ID của submission',
    question_id BIGINT NOT NULL COMMENT 'ID của câu hỏi',
    
    -- Answer data (flexible for different question types)
    answer_text TEXT COMMENT 'Câu trả lời dạng text (cho essay, short answer)',
    answer_json JSON COMMENT 'Câu trả lời dạng JSON (cho multiple choice, matching, etc)',
    
    -- File upload (for FILE_UPLOAD question type)
    uploaded_file_url VARCHAR(500) COMMENT 'URL của file upload',
    uploaded_file_name VARCHAR(255) COMMENT 'Tên file gốc',
    
    -- Grading
    is_correct BOOLEAN DEFAULT NULL COMMENT 'Đúng/Sai (NULL nếu chưa chấm)',
    points_earned DECIMAL(5,2) DEFAULT 0 COMMENT 'Điểm đạt được',
    max_points DECIMAL(5,2) COMMENT 'Điểm tối đa của câu này',
    
    -- Teacher feedback (for manual grading)
    teacher_feedback TEXT COMMENT 'Nhận xét của giáo viên',
    graded_by BIGINT COMMENT 'ID của giáo viên chấm bài',
    graded_at TIMESTAMP NULL COMMENT 'Thời điểm chấm',
    
    -- Auto-save tracking
    saved_count INT DEFAULT 0 COMMENT 'Số lần save câu trả lời này',
    first_saved_at TIMESTAMP NULL COMMENT 'Lần save đầu tiên',
    last_saved_at TIMESTAMP NULL COMMENT 'Lần save cuối cùng',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Unique constraint: mỗi submission chỉ có 1 answer cho mỗi question
    UNIQUE KEY uk_submission_question (submission_id, question_id),
    
    -- Indexes
    INDEX idx_student_answers_submission (submission_id),
    INDEX idx_student_answers_question (question_id),
    INDEX idx_student_answers_correct (is_correct),
    INDEX idx_student_answers_graded (graded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lưu câu trả lời của student';

-- Add max_attempts field to exams table
ALTER TABLE exams 
ADD COLUMN max_attempts INT DEFAULT 1 COMMENT 'Số lần làm bài tối đa (0 = unlimited)' AFTER is_published;

-- Add show_results_after_submit field to exams table
ALTER TABLE exams
ADD COLUMN show_results_after_submit BOOLEAN DEFAULT FALSE COMMENT 'Cho phép xem kết quả ngay sau khi nộp' AFTER show_correct_answers;

-- Add show_score_only field to exams table  
ALTER TABLE exams
ADD COLUMN show_score_only BOOLEAN DEFAULT FALSE COMMENT 'Chỉ hiển thị điểm, không hiển thị đáp án đúng' AFTER show_results_after_submit;

-- Insert test data: Update exam to allow retakes
UPDATE exams 
SET max_attempts = 3,
    show_results_after_submit = TRUE,
    show_score_only = FALSE
WHERE id = 4;

COMMIT;
