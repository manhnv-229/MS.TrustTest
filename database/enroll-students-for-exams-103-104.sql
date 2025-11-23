-- ===============================================
-- Script: Enroll Students to View Exams 103 & 104
-- Purpose: Cho phép user 2 và 5 xem/làm exam 103, 104
-- Author: K24DTCN210-NVMANH
-- Date: 23/11/2025 16:20
-- ===============================================

-- BƯỚC 1: Kiểm tra exams có tồn tại không
SELECT 
    e.id AS exam_id,
    e.title,
    e.subject_class_id,
    sc.code AS class_code,
    sc.subject_id
FROM exams e
LEFT JOIN subject_classes sc ON e.subject_class_id = sc.id
WHERE e.id IN (103, 104);

-- BƯỚC 2: Kiểm tra users có tồn tại không
SELECT 
    u.id AS user_id,
    u.full_name,
    u.email,
    GROUP_CONCAT(r.name) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.id IN (2, 5)
GROUP BY u.id, u.full_name, u.email;

-- BƯỚC 3: Xem current enrollment status
SELECT 
    scs.subject_class_id,
    scs.student_id,
    u.full_name AS student_name,
    sc.code AS class_code,
    scs.status,
    scs.enrolled_at
FROM subject_class_students scs
LEFT JOIN users u ON scs.student_id = u.id
LEFT JOIN subject_classes sc ON scs.subject_class_id = sc.id
WHERE scs.student_id IN (2, 5)
ORDER BY scs.student_id, scs.subject_class_id;

-- ===============================================
-- BƯỚC 4: INSERT ENROLLMENT RECORDS
-- ===============================================

-- Lấy subject_class_id của exam 103
SET @class_id_103 = (SELECT subject_class_id FROM exams WHERE id = 103);

-- Lấy subject_class_id của exam 104  
SET @class_id_104 = (SELECT subject_class_id FROM exams WHERE id = 104);

-- In ra để kiểm tra
SELECT 
    103 AS exam_id, 
    @class_id_103 AS subject_class_id,
    'Exam 103 belongs to this class' AS note
UNION ALL
SELECT 
    104 AS exam_id,
    @class_id_104 AS subject_class_id,
    'Exam 104 belongs to this class' AS note;

-- ===============================================
-- ENROLL USER 2 vào classes của exam 103, 104
-- ===============================================

-- Check nếu chưa enroll thì insert
-- User 2 -> Class của Exam 103
INSERT INTO subject_class_students (
    subject_class_id,
    student_id,
    status,
    enrolled_at
)
SELECT 
    @class_id_103,
    2,
    'ENROLLED',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM subject_class_students 
    WHERE subject_class_id = @class_id_103 
    AND student_id = 2
);

-- User 2 -> Class của Exam 104
INSERT INTO subject_class_students (
    subject_class_id,
    student_id,
    status,
    enrolled_at
)
SELECT 
    @class_id_104,
    2,
    'ENROLLED',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM subject_class_students 
    WHERE subject_class_id = @class_id_104 
    AND student_id = 2
)
AND @class_id_104 IS NOT NULL  -- Chỉ insert nếu exam 104 thuộc class khác 103
AND @class_id_104 != @class_id_103;

-- ===============================================
-- ENROLL USER 5 vào classes của exam 103, 104
-- ===============================================

-- User 5 -> Class của Exam 103
INSERT INTO subject_class_students (
    subject_class_id,
    student_id,
    status,
    enrolled_at
)
SELECT 
    @class_id_103,
    5,
    'ENROLLED',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM subject_class_students 
    WHERE subject_class_id = @class_id_103 
    AND student_id = 5
);

-- User 5 -> Class của Exam 104
INSERT INTO subject_class_students (
    subject_class_id,
    student_id,
    status,
    enrolled_at
)
SELECT 
    @class_id_104,
    5,
    'ENROLLED',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM subject_class_students 
    WHERE subject_class_id = @class_id_104 
    AND student_id = 5
)
AND @class_id_104 IS NOT NULL
AND @class_id_104 != @class_id_103;

-- ===============================================
-- BƯỚC 5: VERIFICATION - Kiểm tra kết quả
-- ===============================================

-- Xem enrollments vừa tạo
SELECT 
    'ENROLLMENT SUMMARY' AS info,
    scs.student_id,
    u.full_name AS student_name,
    scs.subject_class_id,
    sc.code AS class_code,
    sc.subject_id,
    s.subject_name,
    scs.status,
    scs.enrolled_at
FROM subject_class_students scs
JOIN users u ON scs.student_id = u.id
JOIN subject_classes sc ON scs.subject_class_id = sc.id
JOIN subjects s ON sc.subject_id = s.id
WHERE scs.student_id IN (2, 5)
AND scs.subject_class_id IN (@class_id_103, @class_id_104)
ORDER BY scs.student_id, scs.subject_class_id;

-- Xem exams mà user 2 có thể làm
SELECT 
    'EXAMS USER 2 CAN SEE' AS info,
    e.id AS exam_id,
    e.title,
    e.subject_class_id,
    sc.code AS class_code,
    e.is_published,
    e.start_time,
    e.end_time,
    e.duration_minutes
FROM exams e
JOIN subject_classes sc ON e.subject_class_id = sc.id
WHERE e.subject_class_id IN (
    SELECT subject_class_id 
    FROM subject_class_students 
    WHERE student_id = 2 
    AND status = 'ENROLLED'
)
AND e.id IN (103, 104)
ORDER BY e.id;

-- Xem exams mà user 5 có thể làm
SELECT 
    'EXAMS USER 5 CAN SEE' AS info,
    e.id AS exam_id,
    e.title,
    e.subject_class_id,
    sc.code AS class_code,
    e.is_published,
    e.start_time,
    e.end_time,
    e.duration_minutes
FROM exams e
JOIN subject_classes sc ON e.subject_class_id = sc.id
WHERE e.subject_class_id IN (
    SELECT subject_class_id 
    FROM subject_class_students 
    WHERE student_id = 5 
    AND status = 'ENROLLED'
)
AND e.id IN (103, 104)
ORDER BY e.id;

-- ===============================================
-- CLEANUP (Nếu cần rollback)
-- ===============================================
-- Uncomment để xóa enrollments vừa tạo:
/*
DELETE FROM subject_class_students 
WHERE student_id IN (2, 5) 
AND subject_class_id IN (@class_id_103, @class_id_104);
*/

-- ===============================================
-- NOTES
-- ===============================================
-- 1. Script này dùng INSERT ... SELECT ... WHERE NOT EXISTS
--    để tránh duplicate entries
-- 2. Nếu exam 103 và 104 cùng 1 class thì chỉ insert 1 record
-- 3. Sau khi chạy script, test bằng API:
--    GET /api/exam-taking/available
--    với token của user 2 hoặc 5
-- ===============================================
