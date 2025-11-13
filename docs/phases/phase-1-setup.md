# Phase 1: Setup & Database Schema

## Tổng Quan

**Mục tiêu**: Thiết lập cấu trúc dự án, cấu hình môi trường phát triển và thiết kế database schema đầy đủ cho toàn bộ hệ thống.

**Thời gian dự kiến**: 1 tuần  
**Độ ưu tiên**: 🔴 Critical  
**Dependencies**: Phase 0 (Planning) hoàn thành và được approval

---

## Objectives

### Primary Goals
1. ✅ Tạo Maven multi-module project structure hoàn chỉnh
2. ✅ Cấu hình Spring Boot backend với tất cả dependencies cần thiết
3. ✅ Cấu hình JavaFX client application
4. ✅ Thiết kế database schema đầy đủ cho tất cả modules
5. ✅ Tạo Flyway migration scripts
6. ✅ Setup MySQL database và test connection

### Secondary Goals
- Tạo README.md với hướng dẫn setup chi tiết
- Configure logging (Logback/SLF4J)
- Setup Git repository với .gitignore phù hợp
- Tạo sample data scripts cho testing

---

## Database Schema Design

### ERD Overview

```
┌─────────────┐         ┌─────────────┐
│   users     │────────<│   roles     │
└─────────────┘         └─────────────┘
      │
      │ 1:N
      ├─────────────┐
      │             │
      ▼             ▼
┌─────────────┐   ┌─────────────┐
│ departments │   │   classes   │
└─────────────┘   └─────────────┘
      │                 │
      │                 │ N:M
      │                 ▼
      │           ┌───────────────┐
      │           │subject_classes│
      │           └───────────────┘
      │                 │
      │                 │ 1:N
      ▼                 ▼
┌─────────────┐   ┌─────────────┐
│    exams    │   │  questions  │
└─────────────┘   └─────────────┘
      │                 │
      │                 │
      │ 1:N             │ N:M
      ▼                 ▼
┌──────────────────────────────┐
│     exam_submissions         │
└──────────────────────────────┘
      │
      │ 1:N
      ├──────────────┬───────────────┐
      │              │               │
      ▼              ▼               ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│monitoring│  │screenshot│  │  alerts  │
│  _logs   │  │    s     │  │          │
└──────────┘  └──────────┘  └──────────┘
```

### Table Definitions

#### 1. users
Lưu trữ thông tin người dùng (sinh viên, giáo viên, admin)

```sql
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
```

#### 2. roles
Định nghĩa các vai trò trong hệ thống

```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    level INT NOT NULL COMMENT '1=STUDENT, 2=TEACHER, 3=CLASS_MANAGER, 4=DEPT_MANAGER, 5=ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 3. user_roles
Bảng trung gian user - role (N:M)

```sql
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
```

#### 4. departments
Khoa

```sql
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
    INDEX idx_manager (manager_id),
    FOREIGN KEY (manager_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 5. classes
Lớp chung (lớp hành chính)

```sql
CREATE TABLE classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL COMMENT 'e.g., 2023-2024',
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
    
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (class_manager_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 6. subjects
Môn học

```sql
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
```

#### 7. subject_classes
Lớp môn học (lớp học phần)

```sql
CREATE TABLE subject_classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    subject_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL COMMENT 'e.g., 2023-2024-1',
    teacher_id BIGINT NOT NULL,
    schedule VARCHAR(500) COMMENT 'JSON or text format',
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
```

#### 8. subject_class_students
Bảng trung gian: sinh viên - lớp môn học (N:M)

```sql
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
```

#### 9. exams
Bài thi

```sql
CREATE TABLE exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject_class_id BIGINT NOT NULL,
    
    -- Phân loại bài thi theo mục đích
    exam_purpose ENUM(
        'QUICK_TEST',           -- Kiểm tra nhanh (15 phút)
        'PROGRESS_TEST',        -- Kiểm tra tiến độ
        'MIDTERM',              -- Thi giữa kỳ
        'FINAL',                -- Thi cuối kỳ
        'MODULE_COMPLETION',    -- Kết thúc học phần
        'MAKEUP',               -- Thi lại
        'ASSIGNMENT',           -- Bài tập về nhà
        'PRACTICE'              -- Luyện tập
    ) NOT NULL,
    
    -- Phân loại theo hình thức làm bài
    exam_format ENUM(
        'MULTIPLE_CHOICE_ONLY',     -- Chỉ trắc nghiệm
        'ESSAY_ONLY',               -- Chỉ tự luận
        'CODING_ONLY',              -- Chỉ lập trình
        'MIXED'                     -- Hỗn hợp (trắc nghiệm + tự luận)
    ) NOT NULL,
    
    -- Thời gian
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    
    -- Điểm số
    passing_score DECIMAL(5,2) DEFAULT 50.00,
    total_score DECIMAL(5,2) DEFAULT 100.00,
    
    -- Cài đặt giám sát
    monitoring_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    screenshot_interval_seconds INT DEFAULT 60,
    allow_tab_switch BOOLEAN DEFAULT FALSE,
    
    -- Cài đặt hiển thị
    randomize_questions BOOLEAN DEFAULT FALSE,
    randomize_options BOOLEAN DEFAULT FALSE,
    allow_review_after_submit BOOLEAN DEFAULT TRUE,
    show_correct_answers BOOLEAN DEFAULT FALSE,
    
    -- Cài đặt cho coding exam
    allow_code_execution BOOLEAN DEFAULT FALSE COMMENT 'Cho phép chạy code trực tiếp',
    programming_language VARCHAR(50) COMMENT 'Java, Python, C++, etc.',
    
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
```

#### 10. questions
Câu hỏi

```sql
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    
    -- Phân loại câu hỏi
    question_type ENUM(
        'MULTIPLE_CHOICE',          -- Trắc nghiệm (1 đáp án đúng)
        'MULTIPLE_SELECT',          -- Chọn nhiều đáp án đúng
        'TRUE_FALSE',               -- Đúng/Sai
        'ESSAY',                    -- Tự luận
        'SHORT_ANSWER',             -- Câu trả lời ngắn
        'CODING',                   -- Lập trình
        'FILL_IN_BLANK',            -- Điền vào chỗ trống
        'MATCHING'                  -- Nối câu
    ) NOT NULL,
    
    question_text TEXT NOT NULL,
    question_order INT NOT NULL,
    points DECIMAL(5,2) DEFAULT 1.00,
    
    -- For multiple choice & multiple select
    options JSON COMMENT 'Array of options: [{id: "A", text: "Option A"}, ...]',
    correct_answer VARCHAR(255) COMMENT 'Single answer (A) or multiple (A,C,D)',
    
    -- For essay & short answer
    max_words INT COMMENT 'Số từ tối đa cho essay/short answer',
    min_words INT COMMENT 'Số từ tối thiểu',
    grading_criteria TEXT COMMENT 'Tiêu chí chấm điểm cho tự luận',
    
    -- For coding questions
    programming_language VARCHAR(50) COMMENT 'Java, Python, C++, JavaScript, etc.',
    starter_code TEXT COMMENT 'Code mẫu ban đầu cho sinh viên',
    test_cases JSON COMMENT 'Array of test cases: [{input, expected_output, points}]',
    time_limit_seconds INT COMMENT 'Giới hạn thời gian chạy code',
    memory_limit_mb INT COMMENT 'Giới hạn memory',
    
    -- For fill in blank
    blank_positions JSON COMMENT 'Vị trí và đáp án đúng cho chỗ trống',
    
    -- For matching
    left_items JSON COMMENT 'Danh sách items bên trái',
    right_items JSON COMMENT 'Danh sách items bên phải',
    correct_matches JSON COMMENT 'Các cặp nối đúng',
    
    -- Hình ảnh/file đính kèm
    attachments JSON COMMENT 'Array of file URLs',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    INDEX idx_exam (exam_id),
    INDEX idx_type (question_type),
    INDEX idx_order (exam_id, question_order),
    
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 11. exam_submissions
Bài làm của sinh viên

```sql
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
    
    -- Monitoring summary
    suspicious_activity_count INT DEFAULT 0,
    tab_switch_count INT DEFAULT 0,
    screenshot_count INT DEFAULT 0,
    
    version BIGINT DEFAULT 0 COMMENT 'For optimistic locking',
    
    INDEX idx_exam (exam_id),
    INDEX idx_student (student_id),
    INDEX idx_status (status),
    INDEX idx_submission (exam_id, student_id),
    
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (graded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 12. submission_answers
Câu trả lời của sinh viên

```sql
CREATE TABLE submission_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    answer_option VARCHAR(255) COMMENT 'For multiple choice',
    is_correct BOOLEAN,
    points_earned DECIMAL(5,2),
    
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_submission (submission_id),
    INDEX idx_question (question_id),
    
    UNIQUE KEY uk_submission_question (submission_id, question_id),
    
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 13. monitoring_logs
Log các hoạt động giám sát

```sql
CREATE TABLE monitoring_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    log_type ENUM('WINDOW_SWITCH', 'PROCESS_DETECTED', 'CLIPBOARD_ACCESS', 'KEYSTROKE', 'OTHER') NOT NULL,
    severity ENUM('INFO', 'WARNING', 'CRITICAL') DEFAULT 'INFO',
    description TEXT NOT NULL,
    metadata JSON COMMENT 'Additional context',
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_submission (submission_id),
    INDEX idx_type (log_type),
    INDEX idx_severity (severity),
    INDEX idx_time (logged_at),
    INDEX idx_submission_time (submission_id, logged_at),
    
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 14. screenshots
Ảnh chụp màn hình

```sql
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
```

#### 15. alerts
Cảnh báo gian lận

```sql
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
```

#### 16. system_configs
Cấu hình hệ thống

```sql
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
```

---

## Flyway Migration Scripts

### V1__initial_schema.sql

```sql
-- Create all tables in order of dependencies
-- (Full script sẽ chứa tất cả CREATE TABLE statements ở trên)
```

### V2__insert_default_data.sql

```sql
-- Insert default roles
INSERT INTO roles (name, description, level) VALUES
('ROLE_STUDENT', 'Sinh viên', 1),
('ROLE_TEACHER', 'Giáo viên', 2),
('ROLE_CLASS_MANAGER', 'Quản lý lớp', 3),
('ROLE_DEPT_MANAGER', 'Quản lý khoa', 4),
('ROLE_ADMIN', 'Quản trị viên', 5);

-- Insert default system configs
INSERT INTO system_configs (config_key, config_value, description, data_type) VALUES
('monitoring.screenshot.default_interval', '60', 'Default screenshot interval in seconds', 'NUMBER'),
('monitoring.screenshot.max_size_mb', '5', 'Max screenshot file size in MB', 'NUMBER'),
('monitoring.alert.auto_flag_threshold', '5', 'Auto flag after N suspicious activities', 'NUMBER'),
('exam.auto_save_interval', '30', 'Auto save interval in seconds', 'NUMBER');

-- Insert default admin user (password: Admin@123)
INSERT INTO users (student_code, email, password_hash, full_name, is_active) VALUES
('ADMIN', 'admin@mstrust.edu.vn', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lk3K3Z7.n8Nu', 'System Administrator', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE student_code = 'ADMIN'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));
```

### V3__add_indexes.sql

```sql
-- Additional performance indexes
CREATE INDEX idx_users_active_dept ON users(department_id, is_active, deleted_at);
CREATE INDEX idx_exams_time_published ON exams(start_time, end_time, is_published);
CREATE INDEX idx_submissions_status_student ON exam_submissions(status, student_id);
CREATE INDEX idx_alerts_severity_status ON alerts(severity, status, created_at);
CREATE INDEX idx_monitoring_submission_severity ON monitoring_logs(submission_id, severity, logged_at);
```

---

## Maven Project Structure

### Root pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mstrust</groupId>
    <artifactId>ms-trust-exam</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>MS.TrustTest</name>
    <description>Online Exam System with Anti-Cheat Monitoring</description>

    <modules>
        <module>backend</module>
        <module>client</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <spring.boot.version>3.2.0</spring.boot.version>
        <javafx.version>21</javafx.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```

---

## Setup Instructions

### 1. Prerequisites

```bash
# Check Java version
java -version  # Should be 17 or higher

# Check Maven version
mvn -version  # Should be 3.9.x or higher

# Check MySQL
mysql --version  # Should be 8.0.x or higher
```

### 2. Database Setup

```bash
# Login to MySQL
mysql -u root -p

# Run database creation script
source database/init-schema.sql
```

### 3. Backend Setup

```bash
# Navigate to backend
cd backend

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

### 4. Client Setup

```bash
# Navigate to client
cd client

# Build project
mvn clean install

# Run application
mvn javafx:run
```

---

## Testing Checklist

### Database
- [ ] Database created successfully
- [ ] All tables created with correct schema
- [ ] Foreign keys working correctly
- [ ] Indexes created
- [ ] Default data inserted
- [ ] Admin user can login

### Backend
- [ ] Spring Boot application starts without errors
- [ ] Database connection successful
- [ ] Flyway migrations run successfully
- [ ] Health check endpoint responds
- [ ] Logging configured correctly

### Client
- [ ] JavaFX application starts without errors
- [ ] Can connect to backend API
- [ ] UI loads correctly

---

## Deliverables

1. ✅ Maven multi-module project structure
2. ✅ Complete database schema (16 tables)
3. ✅ Flyway migration scripts
4. ✅ Backend Spring Boot configuration
5. ✅ Client JavaFX configuration
6. ✅ README.md với setup instructions
7. ✅ .gitignore files
8. ✅ Sample data scripts

---

## Next Phase

Sau khi Phase 1 hoàn thành, chuyển sang **Phase 2: Authentication & Authorization**

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:03  
**Last Updated**: 13/11/2025 14:03
