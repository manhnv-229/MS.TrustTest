# Setup Test Data for Phase 5B Testing

**Author:** K24DTCN210-NVMANH  
**Date:** 21/11/2025 07:53  
**Purpose:** Hướng dẫn setup test data để test Phase 5B APIs

---

## 🎯 Quick Start (3 Steps)

### Step 1: Run SQL Script
```bash
# Option A: Using MCP Tool (Recommended)
- Open Cline
- Use MCP tool: ms-trust-test-server
- Execute queries from: database/test-data-phase5b.sql

# Option B: Using MySQL Workbench
- Open MySQL Workbench
- Connect to MS.TrustTest database
- Open file: database/test-data-phase5b.sql
- Click Execute (⚡ icon)

# Option C: Using Command Line
mysql -u root -p MS.TrustTest < database/test-data-phase5b.sql
```

### Step 2: Verify Setup
```sql
-- Check available exams
SELECT id, title, status, start_time, end_time, max_attempts
FROM exams
WHERE status = 'PUBLISHED'
ORDER BY id DESC;

-- Should see at least 1 exam with status='PUBLISHED'
```

### Step 3: Test API
```bash
curl -X POST 'http://localhost:8080/api/exam-taking/start/1' \
  -H 'Authorization: Bearer YOUR_STUDENT_TOKEN'

# Should return 200 OK with exam started message
```

---

## 📋 What the Script Does

### Option 1: Fix Existing Exam (Fast)
```sql
-- Just update exam 1 to be available
UPDATE exams 
SET 
    start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 7 DAY),
    status = 'PUBLISHED',
    max_attempts = 5
WHERE id = 1;
```

**Use when:** Cụ chỉ muốn fix exam có sẵn

### Option 2: Create New Test Exam (Complete)
Script sẽ tạo:
- ✅ 1 exam mới: "Phase 5B Test Exam"
- ✅ 5 câu hỏi test (Multiple Choice, True/False, Essay)
- ✅ Link câu hỏi vào exam
- ✅ Set thời gian available 30 ngày

**Use when:** Cụ muốn exam riêng để test

---

## 🔧 Detailed Setup Options

### Option A: Minimal Setup (Fix Exam 1 Only)

**Run this query:**
```sql
UPDATE exams 
SET 
    start_time = NOW(),
    end_time = DATE_ADD(NOW(), INTERVAL 7 DAY),
    status = 'PUBLISHED'
WHERE id = 1;
```

**Pros:**
- ✅ Nhanh nhất
- ✅ Không tạo data mới
- ✅ Dùng exam có sẵn

**Cons:**
- ❌ Exam 1 có thể có questions không phù hợp
- ❌ Ảnh hưởng exam production

---

### Option B: Full Setup (Create New Test Exam)

**Run full script:** `database/test-data-phase5b.sql`

**Pros:**
- ✅ Exam riêng cho test
- ✅ Questions được thiết kế cho test
- ✅ Không ảnh hưởng data production
- ✅ Có thể xóa sau khi test

**Cons:**
- ❌ Mất thời gian hơn
- ❌ Tạo nhiều data hơn

---

### Option C: Reset Old Submissions

Nếu cụ đã test trước và muốn test lại:

```sql
-- Delete old submissions for exam 1
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions 
    WHERE student_id = 2 AND exam_id = 1
);

DELETE FROM exam_submissions 
WHERE student_id = 2 AND exam_id = 1;

-- Now you can start exam again
```

---

## 🧪 Verification Queries

### Check Exam Status
```sql
SELECT 
    id,
    title,
    status,
    start_time,
    end_time,
    NOW() as current_time,
    CASE 
        WHEN start_time <= NOW() AND end_time >= NOW() THEN 'AVAILABLE'
        WHEN start_time > NOW() THEN 'NOT_STARTED'
        ELSE 'EXPIRED'
    END as availability
FROM exams
WHERE id = 1;
```

**Expected:**
- `status` = 'PUBLISHED'
- `availability` = 'AVAILABLE'

### Check Questions
```sql
SELECT 
    eq.exam_id,
    COUNT(*) as question_count,
    SUM(eq.points) as total_points
FROM exam_questions eq
WHERE eq.exam_id = 1
GROUP BY eq.exam_id;
```

**Expected:**
- `question_count` > 0
- `total_points` = exam.total_score

### Check Student Eligibility
```sql
SELECT 
    e.id as exam_id,
    e.title,
    e.max_attempts,
    COALESCE(COUNT(es.id), 0) as attempts_made,
    e.max_attempts - COALESCE(COUNT(es.id), 0) as remaining_attempts
FROM exams e
LEFT JOIN exam_submissions es ON e.id = es.exam_id AND es.student_id = 2
WHERE e.id = 1
GROUP BY e.id, e.title, e.max_attempts;
```

**Expected:**
- `remaining_attempts` > 0

---

## 🚨 Common Issues & Solutions

### Issue 1: "Exam not found"
```sql
-- Check if exam exists
SELECT id, title FROM exams WHERE id = 1;
```

**Solution:** Exam không tồn tại, cần tạo exam mới hoặc dùng ID khác

### Issue 2: "Exam is not available yet"
```sql
-- Check timing
SELECT 
    id, 
    title, 
    status,
    start_time,
    end_time,
    NOW() as now
FROM exams WHERE id = 1;
```

**Solution:** Run update query để fix timing:
```sql
UPDATE exams 
SET start_time = NOW(), end_time = DATE_ADD(NOW(), INTERVAL 7 DAY)
WHERE id = 1;
```

### Issue 3: "Maximum attempts reached"
```sql
-- Check attempts
SELECT COUNT(*) as attempts
FROM exam_submissions
WHERE student_id = 2 AND exam_id = 1;
```

**Solution:** Delete old submissions hoặc tăng max_attempts:
```sql
-- Option A: Delete old attempts
DELETE FROM exam_submissions WHERE student_id = 2 AND exam_id = 1;

-- Option B: Increase max_attempts
UPDATE exams SET max_attempts = 10 WHERE id = 1;
```

### Issue 4: "Active submission exists"
```sql
-- Find active submission
SELECT id, status, started_at
FROM exam_submissions
WHERE student_id = 2 AND exam_id = 1 AND status = 'IN_PROGRESS';
```

**Solution:** Submit or expire the active submission:
```sql
-- Option A: Auto-submit
UPDATE exam_submissions 
SET status = 'SUBMITTED', submitted_at = NOW()
WHERE student_id = 2 AND exam_id = 1 AND status = 'IN_PROGRESS';

-- Option B: Mark as expired
UPDATE exam_submissions 
SET status = 'EXPIRED'
WHERE student_id = 2 AND exam_id = 1 AND status = 'IN_PROGRESS';
```

---

## 🧹 Cleanup After Testing

### Delete Test Exam Only
```sql
-- Delete test exam and related data
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions 
    WHERE exam_id IN (
        SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%'
    )
);

DELETE FROM exam_submissions 
WHERE exam_id IN (
    SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%'
);

DELETE FROM exam_questions 
WHERE exam_id IN (
    SELECT id FROM exams WHERE title LIKE '%Phase 5B Test%'
);

DELETE FROM exams 
WHERE title LIKE '%Phase 5B Test%';
```

### Reset Exam 1 Back to Original
```sql
-- If you want to revert exam 1
UPDATE exams 
SET 
    start_time = '2025-01-01 00:00:00',
    end_time = '2025-12-31 23:59:59',
    status = 'DRAFT'
WHERE id = 1;
```

---

## 📊 Testing Workflow

### Full Test Flow:
```
1. Setup Data (Run SQL)
   ↓
2. Login as Student (Get token)
   ↓
3. Check Eligibility
   ↓
4. Start Exam
   ↓
5. Get Questions
   ↓
6. Save Answers
   ↓
7. Test Pause (as Teacher)
   ↓
8. Test Resume (as Teacher)
   ↓
9. Submit Exam
   ↓
10. Get Results
```

### Quick Reset Between Tests:
```sql
-- Quick reset for re-testing
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions WHERE student_id = 2
);

DELETE FROM exam_submissions WHERE student_id = 2;
```

---

## 🎯 Recommended Approach

**For Development:**
1. Use **Option B** (Create new test exam)
2. Keep test exam separate from production data
3. Delete after testing complete

**For Quick Testing:**
1. Use **Option A** (Fix exam 1)
2. Remember to reset afterward

**For Continuous Testing:**
1. Create dedicated test exam once
2. Reset submissions between tests
3. Keep exam available indefinitely

---

## 📖 Related Files

- SQL Script: `database/test-data-phase5b.sql`
- Error Fix Guide: `docs/FIX-EXAM-NOT-AVAILABLE.md`
- Thunder Client: `docs/thunder-client-phase5b-websocket.json`
- Testing Guide: `docs/PHASE5B-TESTING-GUIDE.md`

---

**End of Document**
