# Fix "Exam is not available yet" Error

**Author:** K24DTCN210-NVMANH  
**Date:** 21/11/2025 02:44  
**Issue:** Error 400 - "Exam is not available yet" khi call `POST /api/exam-taking/start/1`

---

## 🐛 Root Cause Analysis

### Error Message
```json
{
  "status": 400,
  "message": "Exam is not available yet",
  "timestamp": "2025-11-21T02:41:35.8141907"
}
```

### Source Code (ExamTakingService.java, line 111-116)
```java
// Check 1: Exam status
ExamStatus status = exam.getCurrentStatus();
if (status != ExamStatus.PUBLISHED && status != ExamStatus.ONGOING) {
    result.put("isEligible", false);
    result.put("reason", "Exam is not available yet");
    return result;
}
```

### Validation Rules
Exam must meet ALL conditions:
1. ✅ **Status:** `PUBLISHED` hoặc `ONGOING`
2. ✅ **Time:** Current time trong khoảng `start_time` đến `end_time`
3. ✅ **Attempts:** Student chưa vượt quá `max_attempts`
4. ✅ **No Active Submission:** Student không có submission `IN_PROGRESS`

---

## 🔍 Diagnosis Steps

### Step 1: Check Exam Status & Timing
```sql
SELECT 
    id,
    title,
    start_time,
    end_time,
    status,
    duration_minutes,
    max_attempts
FROM exams 
WHERE id = 1;
```

**Expected:**
- `status` = 'PUBLISHED' (or 'ONGOING')
- `start_time` <= NOW()
- `end_time` >= NOW()

### Step 2: Check Student Attempts
```sql
SELECT 
    id,
    student_id,
    exam_id,
    attempt_number,
    status,
    started_at,
    submitted_at
FROM exam_submissions
WHERE student_id = 2 AND exam_id = 1
ORDER BY attempt_number DESC;
```

**Check:**
- Count attempts < max_attempts
- No submission with status = 'IN_PROGRESS'

---

## ✅ Solutions

### Solution 1: Update Exam Timing (Recommended)
```sql
-- Set exam to be available NOW for 2 hours
UPDATE exams 
SET 
    start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 2 HOUR),
    status = 'PUBLISHED'
WHERE id = 1;
```

### Solution 2: Create New Test Exam
```sql
-- Create a new exam that's immediately available
INSERT INTO exams (
    title,
    description,
    subject_class_id,
    created_by,
    start_time,
    end_time,
    duration_minutes,
    total_score,
    passing_score,
    status,
    exam_purpose,
    exam_format,
    max_attempts,
    randomize_questions,
    randomize_options,
    show_results_after_submit,
    show_correct_answers,
    allow_review_after_submit,
    show_score_only,
    allow_code_execution,
    created_at
) VALUES (
    'Test Exam - Phase 5B',
    'Test exam for Phase 5B WebSocket features',
    1,  -- subject_class_id
    1,  -- created_by (teacher)
    NOW(),  -- Available now
    DATE_ADD(NOW(), INTERVAL 7 DAY),  -- Available for 7 days
    60,  -- 60 minutes duration
    100,
    50,
    'PUBLISHED',
    'MIDTERM',
    'ONLINE',
    3,  -- Max 3 attempts
    false,
    false,
    true,
    true,
    true,
    false,
    false,
    NOW()
);

-- Get the new exam ID
SELECT LAST_INSERT_ID() as new_exam_id;
```

### Solution 3: Fix Existing Exam Data
```sql
-- If exam has wrong status, update it
UPDATE exams 
SET status = 'PUBLISHED'
WHERE id = 1 AND status IN ('DRAFT', 'SCHEDULED');

-- If timing is past, extend it
UPDATE exams 
SET end_time = DATE_ADD(NOW(), INTERVAL 2 HOUR)
WHERE id = 1 AND end_time < NOW();

-- If not started yet, set start time to now
UPDATE exams
SET start_time = NOW()
WHERE id = 1 AND start_time > NOW();
```

---

## 🧪 Testing After Fix

### Test 1: Check Eligibility
```bash
curl -X GET \
  'http://localhost:8080/api/exam-taking/check-eligibility/1' \
  -H 'Authorization: Bearer YOUR_STUDENT_TOKEN'
```

**Expected Response:**
```json
{
  "isEligible": true,
  "attemptsMade": 0,
  "remainingAttempts": 3
}
```

### Test 2: Start Exam
```bash
curl -X POST \
  'http://localhost:8080/api/exam-taking/start/1' \
  -H 'Authorization: Bearer YOUR_STUDENT_TOKEN'
```

**Expected Response:**
```json
{
  "submissionId": 1,
  "examId": 1,
  "examTitle": "...",
  "startedAt": "2025-11-21T02:45:00",
  "durationMinutes": 60,
  "remainingSeconds": 3600,
  "message": "Exam started successfully. Good luck!"
}
```

---

## 📋 Common Scenarios & Solutions

### Scenario 1: Exam in DRAFT status
**Error:** "Exam is not available yet"  
**Fix:**
```sql
UPDATE exams SET status = 'PUBLISHED' WHERE id = 1;
```

### Scenario 2: Exam timing expired
**Error:** "Exam is not available yet"  
**Fix:**
```sql
UPDATE exams 
SET start_time = NOW(), 
    end_time = DATE_ADD(NOW(), INTERVAL 2 HOUR)
WHERE id = 1;
```

### Scenario 3: Exam hasn't started
**Error:** "Exam is not available yet"  
**Fix:**
```sql
UPDATE exams SET start_time = NOW() WHERE id = 1;
```

### Scenario 4: Max attempts reached
**Error:** "Maximum attempts reached (3)"  
**Fix:**
```sql
-- Delete old attempts (DANGEROUS - only for testing!)
DELETE FROM exam_submissions WHERE student_id = 2 AND exam_id = 1;

-- Or increase max_attempts
UPDATE exams SET max_attempts = 5 WHERE id = 1;
```

### Scenario 5: Active submission exists
**Error:** "You have an active submission in progress"  
**Fix:**
```sql
-- Find active submission
SELECT id, status FROM exam_submissions 
WHERE student_id = 2 AND exam_id = 1 AND status = 'IN_PROGRESS';

-- Option A: Submit it
UPDATE exam_submissions 
SET status = 'SUBMITTED', submitted_at = NOW()
WHERE id = <submission_id>;

-- Option B: Expire it
UPDATE exam_submissions 
SET status = 'EXPIRED'
WHERE id = <submission_id>;
```

---

## 🎯 Best Practice for Testing

### Create Dedicated Test Exam
```sql
-- Always use a separate test exam for development
INSERT INTO exams (...) VALUES (
    'TEST - Phase 5B',
    'FOR TESTING ONLY - Can be deleted',
    ...,
    NOW(),  -- start_time
    DATE_ADD(NOW(), INTERVAL 30 DAY),  -- Long duration for testing
    ...
);
```

### Reset Test Data Script
```sql
-- Script to reset test exam data
-- Run this before each testing session

-- 1. Delete all submissions for test exam
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions WHERE exam_id = <test_exam_id>
);

DELETE FROM exam_submissions WHERE exam_id = <test_exam_id>;

-- 2. Reset exam timing
UPDATE exams 
SET start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 30 DAY),
    status = 'PUBLISHED'
WHERE id = <test_exam_id>;
```

---

## 📖 Related Documentation

- **ExamStatus Enum Values:**
  - `DRAFT` - Not available
  - `SCHEDULED` - Not available yet
  - `PUBLISHED` - Available (in time range)
  - `ONGOING` - Currently active
  - `COMPLETED` - Finished
  - `CANCELLED` - Not available

- **SubmissionStatus Enum Values:**
  - `NOT_STARTED` - Initial state
  - `IN_PROGRESS` - Student làm bài
  - `PAUSED` - Tạm dừng
  - `SUBMITTED` - Đã nộp
  - `GRADED` - Đã chấm điểm
  - `EXPIRED` - Hết giờ

---

**End of Document**
