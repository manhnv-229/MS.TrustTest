# Phase 8 - API Testing Guide

**Date:** 23/11/2025 15:00  
**Thunder Client Collection:** `thunder-client-phase8-exam-taking-full.json`  
**Total Tests:** 30 requests (3 auth + 3 exam list + 7 exam taking + 10 errors + 7 edge cases)

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Setup](#setup)
3. [Test Execution Order](#test-execution-order)
4. [Test Categories](#test-categories)
5. [Expected Results](#expected-results)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Backend Requirements
- ✅ Backend server running at `http://localhost:8080`
- ✅ Database populated with test data
- ✅ At least 1 exam with questions available

### Verify Backend is Running
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

### Test Data Required
```sql
-- Students (from V13 migration)
- student1@test.com / password123
- student2@test.com / password123

-- Teacher
- teacher1@test.com / password123

-- Exam with questions (from create-exam-with-questions-phase7.sql)
- At least 1 active exam with multiple question types
```

---

## Setup

### 1. Import Thunder Client Collection

**Via VS Code:**
1. Open Thunder Client extension
2. Click "Collections" tab
3. Click "..." → "Import"
4. Select `docs/thunder-client-phase8-exam-taking-full.json`
5. Collection "Phase 8 - Exam Taking APIs - Full Test Suite" will appear

### 2. Set Environment Variables

Create environment in Thunder Client:

```json
{
  "student_token": "",
  "student2_token": "",
  "teacher_token": "",
  "exam_id": "",
  "submission_id": "",
  "question_id": "",
  "essay_question_id": "",
  "future_exam_id": "",
  "past_exam_id": "",
  "empty_submission_id": ""
}
```

**Note:** Variables will be populated automatically as you run tests sequentially.

---

## Test Execution Order

### Phase 1: Authentication (REQUIRED FIRST)

#### Test 1.1: ✅ Login Student (Get Token)
```
POST /api/auth/login
Body: { "email": "student1@test.com", "password": "password123" }

Expected: 200 OK
Response: { "accessToken": "eyJ...", "user": {...} }

Action: Copy accessToken to {{student_token}} variable
```

#### Test 1.2: ✅ Login Student 2
```
POST /api/auth/login
Body: { "email": "student2@test.com", "password": "password123" }

Expected: 200 OK

Action: Copy accessToken to {{student2_token}} variable
```

#### Test 1.3: ✅ Login Teacher
```
POST /api/auth/login
Body: { "email": "teacher1@test.com", "password": "password123" }

Expected: 200 OK

Action: Copy accessToken to {{teacher_token}} variable
```

---

### Phase 2: Exam List APIs

#### Test 2.1: ✅ Get Available Exams - Success
```
GET /api/exam-taking/available
Headers: Authorization: Bearer {{student_token}}

Expected: 200 OK
Response: [
  {
    "id": 1,
    "title": "...",
    "subjectName": "...",
    "duration": 90,
    "totalQuestions": 10,
    "totalPoints": 10.0,
    "startTime": "...",
    "endTime": "...",
    "status": "ONGOING"
  }
]

Action: Copy first exam id to {{exam_id}} variable
```

#### Test 2.2: ✅ Get Available Exams - Empty List
```
GET /api/exam-taking/available
Headers: Authorization: Bearer {{student2_token}}

Expected: 200 OK
Response: []

Note: student2 has no assigned exams
```

#### Test 2.3: ✅ Filter by Subject Code
```
GET /api/exam-taking/available?subjectCode=PRO192
Headers: Authorization: Bearer {{student_token}}

Expected: 200 OK
Response: Filtered list of exams for PRO192 subject only
```

---

### Phase 3: Exam Taking Flow

#### Test 3.1: ✅ Start Exam - Success
```
POST /api/exam-taking/start/{{exam_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 200 OK
Response: {
  "submissionId": 123,
  "examInfo": {...},
  "questions": [
    {
      "id": 1,
      "orderNumber": 1,
      "content": "...",
      "type": "MULTIPLE_CHOICE",
      "points": 1.0,
      "options": ["A. ...", "B. ...", "C. ...", "D. ..."]
    },
    ...
  ],
  "duration": 90
}

Actions:
1. Copy submissionId to {{submission_id}}
2. Copy first question id to {{question_id}}
3. Copy an essay question id to {{essay_question_id}} (if exists)
```

#### Test 3.2: ✅ Save Answer - Multiple Choice
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: {
  "questionId": {{question_id}},
  "answerText": "A",
  "isMarkedForReview": false
}

Expected: 200 OK
Response: "Answer saved successfully"
```

#### Test 3.3: ✅ Save Answer - Essay
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: {
  "questionId": {{essay_question_id}},
  "answerText": "This is my detailed essay answer...",
  "isMarkedForReview": false
}

Expected: 200 OK
```

#### Test 3.4: ✅ Save Answer - Marked for Review
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: {
  "questionId": {{question_id}},
  "answerText": "B",
  "isMarkedForReview": true
}

Expected: 200 OK
Note: Answer is saved AND marked for review
```

#### Test 3.5: ✅ Save Answer - Update Existing
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: {
  "questionId": {{question_id}},
  "answerText": "C",
  "isMarkedForReview": false
}

Expected: 200 OK
Note: Previous answer "B" is updated to "C"
```

#### Test 3.6: ✅ Submit Exam - Success
```
POST /api/exam-taking/submit/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 200 OK
Response: "Exam submitted successfully"
```

#### Test 3.7: ✅ Get Exam Result
```
GET /api/exam-taking/results/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 200 OK
Response: {
  "submissionId": 123,
  "studentName": "...",
  "examTitle": "...",
  "totalQuestions": 10,
  "answeredQuestions": 8,
  "totalScore": 10.0,
  "earnedScore": 7.5,
  "percentage": 75.0,
  "status": "GRADED",
  "answers": [...]
}
```

---

### Phase 4: Error Cases

#### Test 4.1: ❌ No Authorization Token
```
GET /api/exam-taking/available
Headers: (no Authorization header)

Expected: 401 Unauthorized
```

#### Test 4.2: ❌ Invalid Token
```
GET /api/exam-taking/available
Headers: Authorization: Bearer invalid_token_here

Expected: 401 Unauthorized
```

#### Test 4.3: ❌ Start Exam - Not Found
```
POST /api/exam-taking/start/99999
Headers: Authorization: Bearer {{student_token}}

Expected: 404 Not Found
Error: "Exam not found"
```

#### Test 4.4: ❌ Start Exam - Not Yet Started
```
POST /api/exam-taking/start/{{future_exam_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 400 Bad Request
Error: "Exam has not started yet"

Note: Requires future exam in database
```

#### Test 4.5: ❌ Start Exam - Already Ended
```
POST /api/exam-taking/start/{{past_exam_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 400 Bad Request
Error: "Exam has already ended"

Note: Requires past exam in database
```

#### Test 4.6: ❌ Start Exam - Already Submitted
```
POST /api/exam-taking/start/{{exam_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 400 Bad Request
Error: "You have already submitted this exam"

Note: Run after Test 3.6 (Submit Exam)
```

#### Test 4.7: ❌ Save Answer - Wrong Student
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student2_token}}
Body: { "questionId": {{question_id}}, "answerText": "A", ... }

Expected: 403 Forbidden
Error: "This submission does not belong to you"
```

#### Test 4.8: ❌ Save Answer - Invalid Question ID
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: { "questionId": 99999, "answerText": "A", ... }

Expected: 400 Bad Request
Error: "Question not found in this exam"
```

#### Test 4.9: ❌ Save Answer - Empty Text
```
POST /api/exam-taking/save-answer/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}
Body: { "questionId": {{question_id}}, "answerText": "", ... }

Expected: 400 Bad Request
Error: "Answer text cannot be empty"
```

#### Test 4.10: ❌ Submit - Already Submitted
```
POST /api/exam-taking/submit/{{submission_id}}
Headers: Authorization: Bearer {{student_token}}

Expected: 400 Bad Request
Error: "Exam already submitted"

Note: Run after Test 3.6
```

---

### Phase 5: Edge Cases

#### Test 5.1: ⚠️ Save with Null Answer
```
POST /api/exam-taking/save-answer/{{submission_id}}
Body: { "questionId": {{question_id}}, "answerText": null, ... }

Expected: Behavior depends on backend validation
- If validation: 400 Bad Request
- If allowed: 200 OK (saves as null, marked for review)
```

#### Test 5.2: ⚠️ Save Very Long Answer (5000 chars)
```
POST /api/exam-taking/save-answer/{{submission_id}}
Body: { "questionId": {{question_id}}, "answerText": "Lorem ipsum... (5000 chars)", ... }

Expected: 
- If under limit: 200 OK
- If over limit: 400 Bad Request "Answer too long"
```

#### Test 5.3: ⚠️ Save with Special Characters
```
POST /api/exam-taking/save-answer/{{submission_id}}
Body: { 
  "questionId": {{question_id}}, 
  "answerText": "<script>alert('XSS')</script> & < > \" ' Tiếng Việt", 
  ... 
}

Expected: 200 OK
Note: Backend should sanitize HTML/SQL injection attempts
```

#### Test 5.4: ⚠️ Rapid Save (Concurrency Test)
```
POST /api/exam-taking/save-answer/{{submission_id}}
Body: { "questionId": {{question_id}}, "answerText": "Answer {{$timestamp}}", ... }

Action: Run this request 5-10 times rapidly

Expected: All should return 200 OK
Note: Tests database locking/concurrency handling
```

#### Test 5.5: ⚠️ Submit with Zero Answers
```
POST /api/exam-taking/submit/{{empty_submission_id}}

Expected: 200 OK (submission allowed even with 0 answers)
Note: Requires starting a new exam without saving answers
```

#### Test 5.6: ⚠️ Save Unicode/Emoji Answer
```
POST /api/exam-taking/save-answer/{{submission_id}}
Body: { 
  "questionId": {{question_id}}, 
  "answerText": "Câu trả lời 😀 🎉 ✅ với emoji 中文 日本語", 
  ... 
}

Expected: 200 OK
Note: Tests UTF-8 encoding support
```

---

## Expected Results Summary

### Success Cases (13 tests)
- ✅ All authentication: 3/3 pass
- ✅ All exam list: 3/3 pass
- ✅ All exam taking: 7/7 pass

### Error Cases (10 tests)
- ❌ All return appropriate error codes (401/403/404/400)
- ❌ All return meaningful error messages

### Edge Cases (7 tests)
- ⚠️ Results may vary based on backend implementation
- ⚠️ Test for robustness and security

---

## Troubleshooting

### Issue 1: 401 Unauthorized on all requests
**Cause:** Token expired or invalid  
**Solution:**
1. Re-run authentication tests (1.1, 1.2, 1.3)
2. Copy new tokens to environment variables
3. Retry failed requests

### Issue 2: 404 Exam not found
**Cause:** No exam data in database  
**Solution:**
```bash
# Run test data script
mysql -u root -p < database/create-exam-with-questions-phase7.sql
```

### Issue 3: Empty exam list
**Cause:** No exams assigned to student  
**Solution:** Check database assignments:
```sql
SELECT * FROM exam_student WHERE student_id = 
  (SELECT id FROM users WHERE email = 'student1@test.com');
```

### Issue 4: Connection refused
**Cause:** Backend not running  
**Solution:**
```bash
cd backend
mvn spring-boot:run
```

### Issue 5: Variables not populated
**Cause:** Manual copy required  
**Solution:**
1. After each key request, check response
2. Manually copy required values to environment
3. Update variables in Thunder Client

---

## Test Coverage Matrix

| Category | Tests | Success | Error | Edge |
|----------|-------|---------|-------|------|
| Authentication | 3 | 3 | 0 | 0 |
| Exam List | 3 | 3 | 0 | 0 |
| Start Exam | 5 | 1 | 4 | 0 |
| Save Answer | 11 | 4 | 3 | 4 |
| Submit Exam | 3 | 1 | 1 | 1 |
| Get Result | 1 | 1 | 0 | 0 |
| Security | 4 | 0 | 4 | 0 |
| **TOTAL** | **30** | **13** | **12** | **5** |

---

## Next Steps

After completing API tests:
1. ✅ Verify all success cases work
2. ✅ Confirm error handling is appropriate
3. ✅ Document any bugs found
4. 🔜 Proceed to Phase 8.4 (JavaFX UI Integration)

---

**Created by:** K24DTCN210-NVMANH  
**Date:** 23/11/2025 15:00  
**Status:** Ready for Testing
