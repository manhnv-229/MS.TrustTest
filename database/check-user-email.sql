-- Check user với ID = 6
SELECT 
    id, 
    full_name, 
    email, 
    student_code,
    deleted_at,
    is_active
FROM users 
WHERE id = 6;

-- Check tất cả users có email chứa 'giaovien'
SELECT 
    id, 
    full_name, 
    email, 
    student_code,
    deleted_at,
    is_active
FROM users 
WHERE email LIKE '%giaovien%';

-- Check tất cả teachers (có role TEACHER)
SELECT 
    u.id,
    u.full_name,
    u.email,
    u.student_code,
    u.deleted_at,
    u.is_active,
    r.role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.role_name = 'TEACHER';
