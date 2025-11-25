-- Debug: Kiểm tra email của user ID=6
SELECT 
    id,
    email,
    full_name,
    deleted_at,
    is_active,
    CHAR_LENGTH(email) as email_length,
    HEX(email) as email_hex
FROM users
WHERE id = 6;

-- Kiểm tra xem có user nào với email 'giaovien@gmail.com' không
SELECT 
    id,
    email,
    full_name,
    deleted_at,
    is_active
FROM users  
WHERE email = 'giaovien@gmail.com';

-- Check tất cả emails có chứa 'giaovien'
SELECT 
    id,
    email,
    full_name,
    deleted_at,
    is_active
FROM users
WHERE email LIKE '%giaovien%';
