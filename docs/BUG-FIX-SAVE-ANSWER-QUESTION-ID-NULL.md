# Bug Fix: question_id Cannot Be Null - Save Answer API

**Date:** 23/11/2025 05:31  
**Fixed By:** K24DTCN210-NVMANH  
**Issue:** Lỗi `Column 'question_id' cannot be null` khi gọi API save answer

---

## 🐛 Bug Description

**API Endpoint:**
```
POST /api/exam-taking/save-answer/{submissionId}
```

**Error Message:**
```json
{
  "status": 500,
  "message": "could not execute statement [Column 'question_id' cannot be null]"
}
```

**Request Body:**
```json
{
  "questionId": "1031",
  "answerText": "B"
}
```

---

## 🔍 Root Cause Analysis

### Problem

Entity `StudentAnswer` có 2 cách mapping cho `question_id`:

```java
// Cách 1: ManyToOne relationship (JPA managed)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "question_id", nullable = false, 
            insertable = false, updatable = false)  // ❌ insertable=false!
private QuestionBank question;

// Cách 2: Direct column mapping (manual managed)
@Column(name = "question_id", nullable = false)
private Long questionId;
```

**Vấn đề:**
- `insertable = false` nghĩa là JPA KHÔNG insert giá trị vào DB khi set `answer.setQuestion(question)`
- Phải set `answer.setQuestionId(questionId)` thay vì chỉ set relationship

### Original Code (ExamTakingService.java - dòng 300-310)

```java
// Set basic info
if (isNewAnswer) {
    answer.setSubmission(submission);
    answer.setQuestion(question);  // ❌ SAI - Không insert vào DB vì insertable=false
    answer.setFirstSavedAt(now);
    answer.setSavedCount(0);
}
```

---

## ✅ Solution

### Bug #1: question_id NULL - Fixed Code

```java
// Set basic info
if (isNewAnswer) {
    answer.setSubmission(submission);
    answer.setQuestionId(request.getQuestionId());  // ✅ FIX: Set questionId directly
    answer.setQuestion(question);  // Keep for JPA reference
    answer.setFirstSavedAt(now);
    answer.setSavedCount(0);
}
```

### Bug #2: isAutoSave NULL - Fixed Code

**Location:** Method `saveAnswer()`, dòng 345

```java
Map<String, Object> result = new HashMap<>();
result.put("success", true);
// Handle null isAutoSave - default to false (manual save)
boolean isAutoSave = request.getIsAutoSave() != null && request.getIsAutoSave();
result.put("message", isAutoSave ? "Answer auto-saved" : "Answer saved");
result.put("isGraded", answer.getIsCorrect() != null);
result.put("pointsEarned", answer.getPointsEarned());
```

**Why:** Field `isAutoSave` trong `SubmitAnswerRequest` là `Boolean` (nullable), nếu client không gửi thì sẽ null. Code cũ dùng trực tiếp `request.getIsAutoSave()` trong ternary operator gây NullPointerException.

### Why This Works

**Pattern 1: Bidirectional (có insertable/updatable = false)**
```java
@ManyToOne
@JoinColumn(name = "question_id", insertable = false, updatable = false)
private QuestionBank question;

@Column(name = "question_id")
private Long questionId;  // ✅ Phải manually manage field này
```

**Pattern 2: Unidirectional (không có insertable/updatable flags)**
```java
@ManyToOne
@JoinColumn(name = "question_id")
private QuestionBank question;  // JPA auto-manages the FK column
// Không cần questionId field riêng
```

StudentAnswer entity đang dùng **Pattern 1**, nên phải set `questionId` manually.

---

## 📝 Changes Made

### File: `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java`

**Change #1:** Method `saveAnswer()`, dòng 304

```diff
  // Set basic info
  if (isNewAnswer) {
      answer.setSubmission(submission);
+     answer.setQuestionId(request.getQuestionId());  // Fix: Set questionId directly
      answer.setQuestion(question);  // Keep for JPA reference
      answer.setFirstSavedAt(now);
      answer.setSavedCount(0);
  }
```

**Change #2:** Method `saveAnswer()`, dòng 345

```diff
  Map<String, Object> result = new HashMap<>();
  result.put("success", true);
- result.put("message", request.getIsAutoSave() ? "Answer auto-saved" : "Answer saved");
+ // Handle null isAutoSave - default to false (manual save)
+ boolean isAutoSave = request.getIsAutoSave() != null && request.getIsAutoSave();
+ result.put("message", isAutoSave ? "Answer auto-saved" : "Answer saved");
  result.put("isGraded", answer.getIsCorrect() != null);
  result.put("pointsEarned", answer.getPointsEarned());
```

---

## 🧪 Testing

### Test Case 1: Save New Answer

**Request:**
```bash
curl -X POST \
  'http://localhost:8080/api/exam-taking/save-answer/15' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "questionId": "1031",
    "answerText": "B"
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "message": "Answer saved",
  "isGraded": true,
  "pointsEarned": 1.0
}
```

### Test Case 2: Update Existing Answer

**Request:**
```bash
curl -X POST \
  'http://localhost:8080/api/exam-taking/save-answer/15' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "questionId": "1031",
    "answerText": "C",
    "isAutoSave": false
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "message": "Answer saved",
  "isGraded": true,
  "pointsEarned": 0.0
}
```

---

## 📊 Database Verification

```sql
-- Check student_answers table after fix
SELECT 
    id,
    submission_id,
    question_id,  -- ✅ Should NOT be NULL
    answer_text,
    is_correct,
    points_earned
FROM student_answers
WHERE submission_id = 15
ORDER BY id DESC
LIMIT 5;
```

**Expected:**
- `question_id` column có giá trị (NOT NULL)
- `answer_text` chứa câu trả lời của student
- `is_correct` được auto-grade (nếu là multiple choice)

---

## 🎯 Impact Analysis

**Severity:** HIGH  
**Impact:** Critical bug - Students không thể save answers

**Affected APIs:**
- `POST /api/exam-taking/save-answer/{submissionId}` ✅ FIXED

**Risk Level:** LOW  
- Chỉ thêm 1 dòng code
- Không ảnh hưởng existing logic
- Fix cả new answer và existing answer

---

## 📚 Related Documentation

- Entity mapping: `docs/PHASE7-ENTITY-MAPPING.md`
- Database schema: `backend/src/main/resources/db/migration/V15__Create_Exam_Submissions_And_Student_Answers.sql`
- Testing guide: `docs/PHASE7-TESTING-GUIDE.md`

---

## ✅ Status

- [x] Root cause identified (2 bugs)
- [x] Fix implemented (2 fixes)
- [x] Code compiled successfully
- [x] Server restarted successfully
- [x] API tested manually - WORKING! ✅
- [x] Documentation updated

**Note:** Cả 2 bugs đã được fix và test thành công!

---

**Author:** K24DTCN210-NVMANH  
**Created:** 23/11/2025 05:31  
**Last Updated:** 23/11/2025 10:54
