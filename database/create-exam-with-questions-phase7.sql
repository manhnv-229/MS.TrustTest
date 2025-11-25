-- ============================================================================
-- Script: Tạo Exam với Questions đầy đủ cho Phase 7 Testing
-- Purpose: Tạo 1 exam hoàn chỉnh với mixed question types để test grading system
-- Author: K24DTCN210-NVMANH
-- Date: 21/11/2025 16:45
-- Note: Tương thích với V12 Migration - Question Bank Architecture (N:M)
-- ============================================================================

USE `MS.TrustTest`;

-- ============================================================================
-- BƯỚC 1: Xóa dữ liệu cũ (nếu có)
-- ============================================================================
DELETE FROM exam_questions WHERE exam_id = 100;
DELETE FROM exams WHERE id = 100;
DELETE FROM questions WHERE id BETWEEN 1001 AND 1009;

-- ============================================================================
-- BƯỚC 2: Tạo Exam mới
-- ============================================================================
INSERT INTO exams (
    id,
    title,
    description,
    subject_class_id,
    exam_purpose,
    exam_format,
    start_time,
    end_time,
    duration_minutes,
    passing_score,
    total_score,
    monitoring_level,
    screenshot_interval_seconds,
    allow_tab_switch,
    randomize_questions,
    randomize_options,
    allow_review_after_submit,
    show_correct_answers,
    allow_code_execution,
    programming_language,
    is_published,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    101,
    'Java OOP - Đề thi giữa kỳ (Complete)',
    'Đề thi giữa kỳ môn Lập trình Java OOP với đầy đủ các loại câu hỏi: Multiple Choice, True/False, Essay, Short Answer, và Coding. Đề thi này được thiết kế để test Phase 7 Grading System.',
    1, -- subject_class_id = 1 (Lớp học Lập trình Java OOP)
    'MIDTERM',
    'MIXED',
    '2025-11-20 08:00:00',
    '2025-12-31 23:59:59',
    90, -- 90 phút
    50.0, -- Điểm đạt: 50/100
    100.0, -- Tổng điểm: 100
    'MEDIUM',
    60, -- Screenshot mỗi 60 giây
    FALSE, -- Không cho phép chuyển tab
    FALSE, -- Không xáo trộn câu hỏi
    FALSE, -- Không xáo trộn đáp án
    TRUE, -- Cho phép xem lại sau khi nộp
    TRUE, -- Hiển thị đáp án đúng
    TRUE, -- Cho phép chạy code
    'Java',
    TRUE, -- Đã publish
    6, -- Created by teacher_id = 6 (giaovien@gmail.com)
    NOW(),
    NOW(),
    0 -- version = 0
);

-- ============================================================================
-- BƯỚC 3: Tạo Questions trong Question Bank (independent)
-- ============================================================================

-- Question 1001: Multiple Choice (sẽ được gán 10 điểm trong exam_questions)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    options,
    correct_answer,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1011,
    1, -- subject_id = 1 (Lập trình Java)
    'MULTIPLE_CHOICE',
    'MEDIUM',
    JSON_ARRAY('OOP', 'Lý thuyết', 'Java'),
    'Tính chất nào KHÔNG phải là tính chất cơ bản của Lập trình hướng đối tượng (OOP)?',
    JSON_OBJECT(
        'A', 'Encapsulation (Đóng gói)',
        'B', 'Inheritance (Kế thừa)',
        'C', 'Polymorphism (Đa hình)',
        'D', 'Compilation (Biên dịch)'
    ),
    'D',
    6, -- teacher_id = 6
    NOW(),
    NOW(),
    0
);

-- Question 1002: Multiple Choice
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    options,
    correct_answer,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1012,
    1,
    'MULTIPLE_CHOICE',
    'EASY',
    JSON_ARRAY('Java', 'Kế thừa', 'Syntax'),
    'Trong Java, keyword nào được sử dụng để kế thừa từ một class khác?',
    JSON_OBJECT(
        'A', 'implements',
        'B', 'inherits',
        'C', 'extends',
        'D', 'derives'
    ),
    'C',
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1003: True/False
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    correct_answer,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1013,
    1,
    'TRUE_FALSE',
    'EASY',
    JSON_ARRAY('Java', 'Kế thừa', 'Multiple Inheritance'),
    'Trong Java, một class có thể kế thừa từ nhiều class khác cùng lúc (multiple inheritance).',
    'FALSE',
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1004: True/False
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    correct_answer,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1014,
    1,
    'TRUE_FALSE',
    'MEDIUM',
    JSON_ARRAY('Java', 'Abstract Class', 'OOP'),
    'Abstract class trong Java có thể chứa cả abstract methods và concrete methods.',
    'TRUE',
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1005: Essay (CẦN CHẤM TAY)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    grading_criteria,
    min_words,
    max_words,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1015,
    1,
    'ESSAY',
    'HARD',
    JSON_ARRAY('OOP', 'Lý thuyết', 'Essay'),
    'Giải thích chi tiết 4 tính chất cơ bản của Lập trình hướng đối tượng (OOP): Encapsulation, Inheritance, Polymorphism, và Abstraction. Cho ví dụ cụ thể cho mỗi tính chất bằng Java code.',
    'Đánh giá dựa trên: (1) Hiểu đúng 4 tính chất OOP - 8 điểm, (2) Giải thích rõ ràng từng tính chất - 8 điểm, (3) Ví dụ code chính xác và dễ hiểu - 4 điểm',
    200,
    1000,
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1006: Essay (CẦN CHẤM TAY)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    grading_criteria,
    min_words,
    max_words,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1016,
    1,
    'ESSAY',
    'HARD',
    JSON_ARRAY('Interface', 'Abstract Class', 'So sánh'),
    'So sánh Interface và Abstract Class trong Java. Khi nào nên sử dụng Interface và khi nào nên sử dụng Abstract Class? Cho ví dụ minh họa.',
    'Đánh giá: (1) So sánh đúng điểm giống/khác - 6 điểm, (2) Phân tích khi nào dùng Interface/Abstract - 6 điểm, (3) Ví dụ thực tế - 3 điểm',
    150,
    800,
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1007: Short Answer (CẦN CHẤM TAY)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    grading_criteria,
    min_words,
    max_words,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1017,
    1,
    'SHORT_ANSWER',
    'MEDIUM',
    JSON_ARRAY('Constructor', 'Java', 'OOP'),
    'Constructor trong Java là gì? Liệt kê các loại constructor và cho ví dụ.',
    'Đánh giá: (1) Định nghĩa đúng Constructor - 4 điểm, (2) Liệt kê đủ các loại - 3 điểm, (3) Ví dụ chính xác - 3 điểm',
    50,
    300,
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1008: Short Answer (CẦN CHẤM TAY)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    grading_criteria,
    min_words,
    max_words,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1018,
    1,
    'SHORT_ANSWER',
    'MEDIUM',
    JSON_ARRAY('Overloading', 'Overriding', 'Polymorphism'),
    'Method overloading và method overriding khác nhau như thế nào? Cho ví dụ cho mỗi trường hợp.',
    'Đánh giá: (1) So sánh đúng overloading/overriding - 5 điểm, (2) Ví dụ overloading - 2.5 điểm, (3) Ví dụ overriding - 2.5 điểm',
    50,
    300,
    6,
    NOW(),
    NOW(),
    0
);

-- Question 1009: Coding (CẦN CHẤM TAY)
INSERT INTO questions (
    id,
    subject_id,
    question_type,
    difficulty,
    tags,
    question_text,
    grading_criteria,
    programming_language,
    starter_code,
    time_limit_seconds,
    memory_limit_mb,
    created_by,
    created_at,
    updated_at,
    version
) VALUES (
    1019,
    1,
    'CODING',
    'MEDIUM',
    JSON_ARRAY('Coding', 'Class', 'OOP'),
    'Viết class Rectangle trong Java với các yêu cầu sau:\n- Thuộc tính: width (double), height (double)\n- Constructor: khởi tạo width và height\n- Method calculateArea(): tính diện tích\n- Method calculatePerimeter(): tính chu vi\n- Getters và Setters cho width và height\n\nYêu cầu: Code phải biên dịch được, logic đúng, có comment giải thích.',
    'Đánh giá: (1) Syntax đúng, biên dịch được - 5 điểm, (2) Logic tính toán chính xác - 5 điểm, (3) Code style, comment, getters/setters - 5 điểm',
    'Java',
    'public class Rectangle {\n    // Write your code here\n    \n}',
    300,
    128,
    6,
    NOW(),
    NOW(),
    0
);

-- ============================================================================
-- BƯỚC 4: Tạo quan hệ Exam-Question qua bảng exam_questions
-- ============================================================================

-- Link Question 1001 to Exam 100 (order 1, 10 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1011, 1, 10.0);

-- Link Question 1002 to Exam 100 (order 2, 10 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1012, 2, 10.0);

-- Link Question 1003 to Exam 100 (order 3, 5 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1013, 3, 5.0);

-- Link Question 1004 to Exam 100 (order 4, 5 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1014, 4, 5.0);

-- Link Question 1005 to Exam 100 (order 5, 20 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1015, 5, 20.0);

-- Link Question 1006 to Exam 100 (order 6, 15 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1016, 6, 15.0);

-- Link Question 1007 to Exam 100 (order 7, 10 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1017, 7, 10.0);

-- Link Question 1008 to Exam 100 (order 8, 10 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1018, 8, 10.0);

-- Link Question 1009 to Exam 100 (order 9, 15 điểm)
INSERT INTO exam_questions (exam_id, question_id, question_order, points) 
VALUES (101, 1019, 9, 15.0);

-- ============================================================================
-- TỔNG KẾT
-- ============================================================================
-- Exam ID: 100
-- Tổng số câu hỏi: 9 questions (trong Question Bank)
-- Tổng điểm: 100 points (qua exam_questions)
-- 
-- Phân loại:
-- - Auto-graded (4 câu): 30 điểm (2 MC + 2 TF)
-- - Manual grading (5 câu): 70 điểm (2 Essay + 2 Short Answer + 1 Coding)
-- 
-- Điểm đạt: 50/100
-- Thời gian: 90 phút
-- ============================================================================

-- Verify exam được tạo thành công
SELECT 
    e.id as exam_id,
    e.title,
    COUNT(eq.id) as total_questions,
    SUM(eq.points) as total_points
FROM exams e
LEFT JOIN exam_questions eq ON e.id = eq.exam_id
WHERE e.id = 101
GROUP BY e.id, e.title;

-- Verify questions trong Question Bank
SELECT 
    q.id,
    q.question_type,
    q.difficulty,
    eq.question_order,
    eq.points,
    LEFT(q.question_text, 50) as question_preview
FROM questions q
INNER JOIN exam_questions eq ON q.id = eq.question_id
WHERE eq.exam_id = 101
ORDER BY eq.question_order;

-- ============================================================================
-- HƯỚNG DẪN SỬ DỤNG
-- ============================================================================
-- 1. Chạy script này để tạo exam:
--    mysql -u root -p
--    mysql> source database/create-exam-with-questions-phase7.sql;
--
-- 2. Verify exam trong database:
--    SELECT * FROM exams WHERE id = 100;
--    SELECT * FROM exam_questions WHERE exam_id = 100;
--    SELECT * FROM questions WHERE id BETWEEN 1001 AND 1009;
--
-- 3. Xóa exam nếu cần (giữ questions trong Question Bank):
--    DELETE FROM exam_questions WHERE exam_id = 100;
--    DELETE FROM exams WHERE id = 100;
--
-- 4. Xóa toàn bộ (bao gồm cả questions):
--    DELETE FROM exam_questions WHERE exam_id = 100;
--    DELETE FROM exams WHERE id = 100;
--    DELETE FROM questions WHERE id BETWEEN 1001 AND 1009;
-- ============================================================================
