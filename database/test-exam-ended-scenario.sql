/* ---------------------------------------------------
 * SQL Script: Test "Start Exam - Already Ended" Scenario
 * Set exam 103 end_time to past để test validation
 * @author: K24DTCN210-NVMANH (23/11/2025 17:06)
 * --------------------------------------------------- */

-- Backup current data first
SELECT 
    id, 
    title,
    start_time,
    end_time,
    CASE 
        WHEN NOW() < start_time THEN 'UPCOMING'
        WHEN NOW() BETWEEN start_time AND end_time THEN 'ONGOING'
        WHEN NOW() > end_time THEN 'ENDED'
    END as current_status
FROM exams 
WHERE id = 103;

-- Update exam 103 to ENDED (set end_time to 1 hour ago)
UPDATE exams
SET 
    end_time = DATE_SUB(NOW(), INTERVAL 1 HOUR),
    start_time = DATE_SUB(NOW(), INTERVAL 3 HOUR)
WHERE id = 103;

-- Verify the change
SELECT 
    id, 
    title,
    start_time,
    end_time,
    CASE 
        WHEN NOW() < start_time THEN 'UPCOMING'
        WHEN NOW() BETWEEN start_time AND end_time THEN 'ONGOING'
        WHEN NOW() > end_time THEN 'ENDED'
    END as current_status
FROM exams 
WHERE id = 103;

-- Test API: POST /api/exam-taking/start/103
-- Expected Response: 
-- {
--   "status": 400,
--   "message": "Exam is not available yet"
-- }

-- RESTORE DATA (run this after testing)
/*
UPDATE exams
SET 
    start_time = '2025-11-24 08:00:00',
    end_time = '2025-11-24 10:00:00'
WHERE id = 103;
*/
