# Bug Fix Documentation: "question_id cannot be null" Error

## 🐛 Problem Description

**Error Message:**
```
could not execute statement [Column 'question_id' cannot be null] 
[insert into student_answers (...) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]
```

**When it occurs:** 
- POST `/api/exam-taking/save-answer/{submissionId}`
- Student trying to save answer during exam

## 🔍 Root Cause Analysis

The issue occurs because `examQuestion.getQuestion()` returns `null` due to JPA lazy loading not initializing the relationship.

### Why `question` is null:

1. **ExamQuestion entity** has `@ManyToOne(fetch = FetchType.LAZY)` for question field
2. When JPA loads ExamQuestion, it creates a proxy for the question relationship
3. If the relationship is not explicitly fetched, calling `.getQuestion()` returns null
4. This null is then passed to `StudentAnswer.setQuestion(null)` 
5. When saving StudentAnswer, Hibernate tries to insert with `question_id = NULL` → **CONSTRAINT VIOLATION**

## ✅ Solutions Applied

### Solution #1: Changed Entity Fetch Type (RECOMMENDED)

**File:** `backend/src/main/java/com/mstrust/exam/entity/ExamQuestion.java`

```java
// BEFORE (WRONG):
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "question_id", nullable = false)
private QuestionBank question;

// AFTER (CORRECT):
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "question_id", nullable = false)  
private QuestionBank question;
```

**Why EAGER is safe:**
- ExamQuestion always needs QuestionBank data when queried
- This is essentially a 1:1 relationship in our use case
- No N+1 problem because we're fetching single ExamQuestion at a time
- Better than JOIN FETCH which requires query changes everywhere

### Solution #2: Added JOIN FETCH in Repository

**File:** `backend/src/main/java/com/mstrust/exam/repository/ExamQuestionRepository.java`

```java
// BEFORE:
Optional<ExamQuestion> findByExamIdAndQuestionId(Long examId, Long questionId);

// AFTER:
@Query("SELECT eq FROM ExamQuestion eq JOIN FETCH eq.question WHERE eq.exam.id = :examId AND eq.question.id = :questionId")
Optional<ExamQuestion> findByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
```

**Note:** This alone is NOT enough if entity has LAZY fetch type!

### Solution #3: Fixed Service Logic

**File:** `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java`

**BEFORE (line 262-268) - WRONG:**
```java
// This was looking up by ExamQuestion.id instead of QuestionBank.id!
QuestionBank question = examQuestionRepository.findById(request.getQuestionId())
    .map(ExamQuestion::getQuestion)
    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
```

**AFTER - CORRECT:**
```java
// Find ExamQuestion first to validate question belongs to this exam
ExamQuestion examQuestion = examQuestionRepository
    .findByExamIdAndQuestionId(submission.getExam().getId(), request.getQuestionId())
    .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));

QuestionBank question = examQuestion.getQuestion();

// Debug log
log.info("ExamQuestion found: id={}, examId={}, questionId={}", 
    examQuestion.getId(), examQuestion.getExam().getId(), 
    examQuestion.getQuestion() != null ? examQuestion.getQuestion().getId() : "NULL");

if (question == null) {
    throw new ResourceNotFoundException("Question relationship is null - lazy loading issue");
}
```

## 🧪 How to Verify the Fix

### Step 1: Check Server Logs

After restarting server and calling save answer API, you should see:

```
INFO  ExamTakingService : ExamQuestion found: id=36, examId=101, questionId=1011
```

**If you DON'T see this log:**
- Server hasn't loaded new code
- Need to fully rebuild: `mvn clean compile` then restart

### Step 2: Check Database

After successful save, check student_answers table:

```sql
SELECT id, submission_id, question_id, answer_text, is_correct, points_earned
FROM student_answers  
WHERE submission_id = <your_submission_id>
ORDER BY id DESC
LIMIT 1;
```

**Expected:** `question_id` should have a value (e.g., 1011), NOT null

### Step 3: API Response

Successful API response should be:

```json
{
  "success": true,
  "message": "Answer saved",
  "isGraded": true,  // for auto-graded questions
  "pointsEarned": 10.00
}
```

## 🔧 Troubleshooting

### Issue: Still getting "question_id cannot be null" error

**Possible causes:**

1. **Server not restarted after code changes**
   ```bash
   # Stop server (Ctrl+C)
   cd backend
   mvn clean compile
   mvn spring-boot:run
   ```

2. **Old compiled classes cached**
   ```bash
   # Force rebuild
   cd backend
   mvn clean
   rm -rf target/
   mvn compile
   mvn spring-boot:run
   ```

3. **Wrong database/table name**
   - Check application.yml points to correct database
   - Verify table name is `questions` not `question_bank`
   
4. **Request sending wrong questionId**
   - questionId must be from `questions` table (e.g., 1011)
   - NOT from `exam_questions` table (e.g., 36)

### Issue: "Question not found in this exam" error

**This is actually GOOD!** It means:
- Code is working
- But the questionId you sent doesn't exist in that exam

**Solution:** Check exam_questions table:
```sql
SELECT eq.id, eq.exam_id, eq.question_id, eq.question_order, eq.points
FROM exam_questions eq
WHERE eq.exam_id = <your_exam_id>;
```

Use the `question_id` from this query (NOT the `eq.id`!)

## 📊 Test Data

Use the provided test data SQL:

```bash
# Run this to set up exam 101 with 5 questions
mysql -u root -p MS.TrustTest < database/create-exam-with-questions-phase7.sql
```

This creates:
- Exam ID: 101  
- Questions: 1011, 1012, 1013, 1014, 1015
- Student: student@example.com / student123

## 📝 Files Changed

1. `backend/src/main/java/com/mstrust/exam/entity/ExamQuestion.java`
   - Changed `question` field from LAZY to EAGER

2. `backend/src/main/java/com/mstrust/exam/repository/ExamQuestionRepository.java`
   - Added JOIN FETCH to findByExamIdAndQuestionId

3. `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java`
   - Fixed question lookup logic
   - Added debug logging
   - Added null check

## ✅ Verification Checklist

Before testing, ensure:

- [ ] Code compiled successfully: `mvn clean compile` shows BUILD SUCCESS
- [ ] Server restarted with new code
- [ ] Test data loaded in database (exam 101, questions 1011-1015)
- [ ] Using correct questionId from questions table (not exam_questions.id)
- [ ] Check server logs show the debug message with actual questionId

## 🎯 Success Criteria

✅ API call succeeds with 200 OK
✅ Server log shows: "ExamQuestion found: id=36, examId=101, questionId=1011"  
✅ Database has new row in student_answers with question_id NOT NULL
✅ Response JSON includes isGraded and pointsEarned

---

**Last Updated:** 22/11/2025 22:58  
**Author:** K24DTCN210-NVMANH  
**Status:** TESTED & VERIFIED
