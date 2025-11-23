-- ===============================================
-- Script: Gán Role STUDENT cho User ID=7
-- Purpose: Fix lỗi 403 khi user 7 gọi API /exam-taking/available
-- Root Cause: User 7 không có role nào trong bảng user_roles
-- Author: K24DTCN210-NVMANH
-- Date: 23/11/2025 16:41
-- ===============================================

-- BƯỚC 1: Xem thông tin user hiện tại
SELECT 
    'BEFORE FIX' AS status,
    u.id,
    u.full_name,
    u.email,
    u.student_code,
    GROUP_CONCAT(r.name) AS current_roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.id = 7
GROUP BY u.id, u.full_name, u.email, u.student_code;

-- BƯỚC 2: Lấy role_id của ROLE_STUDENT
SET @student_role_id = (SELECT id FROM roles WHERE name = 'ROLE_STUDENT' LIMIT 1);

SELECT @student_role_id AS student_role_id;

-- BƯỚC 3: Gán role STUDENT cho user 7
INSERT INTO user_roles (user_id, role_id)
SELECT 7, @student_role_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = 7 
    AND role_id = @student_role_id
);

-- BƯỚC 4: Verify - Xem kết quả sau khi gán
SELECT 
    'AFTER FIX' AS status,
    u.id,
    u.full_name,
    u.email,
    u.student_code,
    GROUP_CONCAT(r.name) AS current_roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.id = 7
GROUP BY u.id, u.full_name, u.email, u.student_code;

-- ===============================================
-- BƯỚC 5: TEST - User cần login lại để lấy token mới
-- ===============================================
-- POST /api/auth/login
-- {
--   "email": "sinhvien09@gmail.com",
--   "password": "student123"
-- }
-- 
-- Token mới sẽ có: "roles":"STUDENT"
-- Sau đó test API:
-- GET /api/exam-taking/available (với token mới)
-- → Sẽ trả về 200 OK thay vì 403
-- ===============================================

-- ROLLBACK (nếu cần)
/*
DELETE FROM user_roles 
WHERE user_id = 7 
AND role_id = @student_role_id;
*/
