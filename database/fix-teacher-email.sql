-- Fix teacher email để match với login
-- Kiểm tra user hiện tại
SELECT id, full_name, email, student_code FROM users WHERE id = 6;

-- Update email của teacher (user ID = 6) để match với login
UPDATE users 
SET email = 'giaovien@gmail.com'
WHERE id = 6;

-- Verify sau khi update
SELECT id, full_name, email, student_code FROM users WHERE id = 6;
