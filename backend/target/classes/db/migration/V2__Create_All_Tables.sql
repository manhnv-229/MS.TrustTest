-- ===================================================
-- V2: Tạo tất cả các bảng chính
-- Author: K24DTCN210-NVMANH (13/11/2025 14:24)
-- ===================================================

-- 1. Departments
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    INDEX idx_code (code),
    INDEX idx_manager (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Classes
CREATE TABLE classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    class_manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    INDEX idx_code (code),
    INDEX idx_department (department_id),
    INDEX idx_manager (class_manager_id),
    INDEX idx_year (academic_year),
    FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_code VARCHAR(20) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    avatar_url VARCHAR(500),
    department_id BIGINT,
    class_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    INDEX idx_student_code (student_code),
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_department (department_id),
    INDEX idx_class (class_id),
    INDEX idx_active (is_active, deleted_at),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. User Roles (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Subjects
CREATE TABLE subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    credits INT DEFAULT 0,
    department_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_code (code),
    INDEX idx_department (department_id),
    FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Subject Classes
CREATE TABLE subject_classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    subject_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    teacher_id BIGINT NOT NULL,
    schedule VARCHAR(500),
    max_students INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    INDEX idx_code (code),
    INDEX idx_subject (subject_id),
    INDEX idx_teacher (teacher_id),
    INDEX idx_semester (semester),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Subject Class Students
CREATE TABLE subject_class_students (
    subject_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ENROLLED', 'DROPPED', 'COMPLETED') DEFAULT 'ENROLLED',
    PRIMARY KEY (subject_class_id, student_id),
    INDEX idx_status (status),
    FOREIGN KEY (subject_class_id) REFERENCES subject_classes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Exams
CREATE TABLE exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject_class_id BIGINT NOT NULL,
    exam_purpose ENUM('QUICK_TEST', 'PROGRESS_TEST', 'MIDTERM', 'FINAL', 'MODULE_COMPLETION', 'MAKEUP', 'ASSIGNMENT', 'PRACTICE') NOT NULL,
    exam_format ENUM('MULTIPLE_CHOICE_ONLY', 'ESSAY_ONLY', 'CODING_ONLY', 'MIXED') NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    passing_score DECIMAL(5,2) DEFAULT 50.00,
    total_score DECIMAL(5,2) DEFAULT 100.00,
    monitoring_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    screenshot_interval_seconds INT DEFAULT 60,
    allow_tab_switch BOOLEAN DEFAULT FALSE,
    randomize_questions BOOLEAN DEFAULT FALSE,
    randomize_options BOOLEAN DEFAULT FALSE,
    allow_review_after_submit BOOLEAN DEFAULT TRUE,
    show_correct_answers BOOLEAN DEFAULT FALSE,
    allow_code_execution BOOLEAN DEFAULT FALSE,
    programming_language VARCHAR(50),
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    INDEX idx_subject_class (subject_class_id),
    INDEX idx_exam_purpose (exam_purpose),
    INDEX idx_exam_format (exam_format),
    INDEX idx_time (start_time, end_time),
    INDEX idx_published (is_published),
    INDEX idx_creator (created_by),
    FOREIGN KEY (subject_class_id) REFERENCES subject_classes(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Questions
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    question_type ENUM('MULTIPLE_CHOICE', 'MULTIPLE_SELECT', 'TRUE_FALSE', 'ESSAY', 'SHORT_ANSWER', 'CODING', 'FILL_IN_BLANK', 'MATCHING') NOT NULL,
    question_text TEXT NOT NULL,
    question_order INT NOT NULL,
    points DECIMAL(5,2) DEFAULT 1.00,
    options JSON,
    correct_answer VARCHAR(255),
    max_words INT,
    min_words INT,
    grading_criteria TEXT,
    programming_language VARCHAR(50),
    starter_code TEXT,
    test_cases JSON,
    time_limit_seconds INT,
    memory_limit_mb INT,
    blank_positions JSON,
    left_items JSON,
    right_items JSON,
    correct_matches JSON,
    attachments JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_exam (exam_id),
    INDEX idx_type (question_type),
    INDEX idx_order (exam_id, question_order),
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Exam Submissions
CREATE TABLE exam_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    status ENUM('IN_PROGRESS', 'SUBMITTED', 'GRADED', 'FLAGGED') DEFAULT 'IN_PROGRESS',
    total_score DECIMAL(5,2),
    auto_graded_score DECIMAL(5,2),
    manual_graded_score DECIMAL(5,2),
    final_score DECIMAL(5,2),
    graded_at TIMESTAMP,
    graded_by BIGINT,
    feedback TEXT,
    suspicious_activity_count INT DEFAULT 0,
    tab_switch_count INT DEFAULT 0,
    screenshot_count INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    INDEX idx_exam (exam_id),
    INDEX idx_student (student_id),
    INDEX idx_status (status),
    INDEX idx_submission (exam_id, student_id),
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (graded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Submission Answers
CREATE TABLE submission_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    answer_option VARCHAR(255),
    is_correct BOOLEAN,
    points_earned DECIMAL(5,2),
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_submission (submission_id),
    INDEX idx_question (question_id),
    UNIQUE KEY uk_submission_question (submission_id, question_id),
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Monitoring Logs
CREATE TABLE monitoring_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    log_type ENUM('WINDOW_SWITCH', 'PROCESS_DETECTED', 'CLIPBOARD_ACCESS', 'KEYSTROKE', 'OTHER') NOT NULL,
    severity ENUM('INFO', 'WARNING', 'CRITICAL') DEFAULT 'INFO',
    description TEXT NOT NULL,
    metadata JSON,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_submission (submission_id),
    INDEX idx_type (log_type),
    INDEX idx_severity (severity),
    INDEX idx_time (logged_at),
    INDEX idx_submission_time (submission_id, logged_at),
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Screenshots
CREATE TABLE screenshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    captured_at TIMESTAMP NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_reason TEXT,
    INDEX idx_submission (submission_id),
    INDEX idx_captured (captured_at),
    INDEX idx_flagged (is_flagged),
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Alerts
CREATE TABLE alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    alert_type ENUM('TAB_SWITCH', 'FORBIDDEN_PROCESS', 'SUSPICIOUS_PATTERN', 'MANUAL_FLAG') NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    evidence_screenshot_id BIGINT,
    evidence_log_id BIGINT,
    status ENUM('NEW', 'REVIEWED', 'DISMISSED', 'CONFIRMED') DEFAULT 'NEW',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_submission (submission_id),
    INDEX idx_type (alert_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_reviewer (reviewed_by),
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (evidence_screenshot_id) REFERENCES screenshots(id),
    FOREIGN KEY (evidence_log_id) REFERENCES monitoring_logs(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. System Configs
CREATE TABLE system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    data_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    INDEX idx_key (config_key),
    FOREIGN KEY (updated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign keys that depend on users table
ALTER TABLE departments ADD FOREIGN KEY (manager_id) REFERENCES users(id);
ALTER TABLE classes ADD FOREIGN KEY (class_manager_id) REFERENCES users(id);
