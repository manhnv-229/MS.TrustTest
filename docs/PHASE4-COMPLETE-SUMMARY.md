# Phase 4: Question Bank & Exam Management - Complete Summary

**Status**: ✅ **COMPLETED**  
**Started**: 19/11/2025 08:00  
**Completed**: 19/11/2025 14:36  
**Duration**: 6.5 hours  
**Author**: K24DTCN210-NVMANH with Cline AI

---

## Executive Summary

Phase 4 successfully implemented a comprehensive Question Bank and Exam Management system with 19 REST APIs, supporting 8 question types and full CRUD operations for exams with advanced features.

### Key Achievements

✅ **Question Bank System**: 6 APIs with advanced filtering  
✅ **Exam Management**: 13 APIs with business logic validation  
✅ **Bug Fixes**: 4 critical issues resolved  
✅ **Documentation**: 5 detailed documentation files  
✅ **Testing**: All 19 APIs tested and verified  

---

## Part A: Question Bank System

### Overview
Independent question bank system allowing teachers to create, manage, and reuse questions across multiple exams.

### Database Schema
```sql
-- questions table (Question Bank)
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20),
    question_text TEXT NOT NULL,
    tags JSON,
    options JSON,
    correct_answer VARCHAR(255),
    -- Essay fields
    max_words INT,
    min_words INT,
    grading_criteria TEXT,
    -- Coding fields
    programming_language VARCHAR(50),
    starter_code TEXT,
    test_cases JSON,
    time_limit_seconds INT,
    memory_limit_mb INT,
    -- Fill in blank fields
    blank_positions JSON,
    -- Matching fields
    left_items JSON,
    right_items JSON,
    correct_matches JSON,
    -- Common fields
    attachments JSON,
    version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
```

### Supported Question Types

1. **MULTIPLE_CHOICE** - Trắc nghiệm một đáp án
2. **MULTIPLE_SELECT** - Chọn nhiều đáp án
3. **TRUE_FALSE** - Đúng/Sai
4. **ESSAY** - Tự luận
5. **SHORT_ANSWER** - Câu trả lời ngắn
6. **CODING** - Lập trình (with test cases)
7. **FILL_IN_BLANK** - Điền chỗ trống
8. **MATCHING** - Nối cặp

### API Endpoints (6 endpoints)

#### 1. Create Question
```http
POST /api/question-bank
Authorization: Bearer {token}
Content-Type: application/json

{
  "subjectId": 1,
  "questionType": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "questionText": "What is polymorphism?",
  "tags": ["Java", "OOP"],
  "options": [
    {"key": "A", "text": "Inheritance"},
    {"key": "B", "text": "Many forms"},
    {"key": "C", "text": "Encapsulation"},
    {"key": "D", "text": "Abstraction"}
  ],
  "correctAnswer": "B"
}
```

**Response**: `201 Created` with QuestionBankDTO

#### 2. List Questions with Filters
```http
GET /api/question-bank?subjectId=1&difficulty=MEDIUM&type=MULTIPLE_CHOICE&keyword=java&page=0&size=10
Authorization: Bearer {token}
```

**Features**:
- Filter by subject, difficulty, type
- Keyword search in question text
- Pagination & sorting
- Returns Page<QuestionBankDTO>

#### 3. Get Question by ID
```http
GET /api/question-bank/{id}
Authorization: Bearer {token}
```

**Response**: `200 OK` with full QuestionBankDTO

#### 4. Update Question
```http
PUT /api/question-bank/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "subjectId": 1,
  "questionType": "MULTIPLE_CHOICE",
  "questionText": "Updated question text",
  ...
}
```

**Features**:
- Optimistic locking with version check
- Full or partial updates
- Audit trail (updated_by, updated_at)

#### 5. Delete Question (Soft Delete)
```http
DELETE /api/question-bank/{id}
Authorization: Bearer {token}
```

**Response**: `204 No Content`

**Business Rules**:
- Soft delete (sets deleted_at timestamp)
- Questions used in exams cannot be hard deleted
- Can be restored by setting deleted_at = NULL

#### 6. Get Statistics
```http
GET /api/question-bank/statistics/{subjectId}
Authorization: Bearer {token}
```

**Response**:
```json
{
  "totalQuestions": 45,
  "byDifficulty": {
    "EASY": 15,
    "MEDIUM": 20,
    "HARD": 10
  },
  "byType": {
    "MULTIPLE_CHOICE": 25,
    "ESSAY": 10,
    "CODING": 10
  }
}
```

### Key Files Created

**Entities**:
- `QuestionBank.java` - Main entity with 8 question type support
- `QuestionType.java` - Enum for question types
- `DifficultyLevel.java` - Enum for difficulty levels

**DTOs**:
- `QuestionBankDTO.java` - Response DTO
- `CreateQuestionBankRequest.java` - Create request
- `UpdateQuestionBankRequest.java` - Update request
- `CreateQuestionRequest.java` - Unified create request
- `UpdateQuestionRequest.java` - Unified update request

**Repository**:
- `QuestionBankRepository.java` - Custom queries with filters

**Service**:
- `QuestionBankService.java` - Business logic & validation

**Controller**:
- `QuestionBankController.java` - 6 REST endpoints

**Migrations**:
- `V12__Refactor_Questions_To_Question_Bank.sql` - Schema refactor
- `V13__Insert_Teacher_And_Student_Users.sql` - Test data

---

## Part B: Exam Management System

### Overview
Comprehensive exam management with status tracking, publish workflow, and question association.

### Database Schema
```sql
-- exams table
CREATE TABLE exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject_class_id BIGINT NOT NULL,
    exam_purpose VARCHAR(50) NOT NULL,
    exam_format VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    passing_score DECIMAL(5,2),
    total_score DECIMAL(5,2),
    -- Settings
    randomize_questions BOOLEAN DEFAULT FALSE,
    randomize_options BOOLEAN DEFAULT FALSE,
    allow_review_after_submit BOOLEAN DEFAULT TRUE,
    show_correct_answers BOOLEAN DEFAULT FALSE,
    allow_code_execution BOOLEAN DEFAULT FALSE,
    programming_language VARCHAR(50),
    is_published BOOLEAN DEFAULT FALSE,
    -- Audit
    version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    FOREIGN KEY (subject_class_id) REFERENCES subject_classes(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- exam_questions table (Join table)
CREATE TABLE exam_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    points DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    UNIQUE KEY uk_exam_question (exam_id, question_id),
    UNIQUE KEY uk_exam_order (exam_id, question_order)
);
```

### Exam Status (Computed)

Exam status được tính toán dựa trên thời gian hiện tại:

```java
public ExamStatus getCurrentStatus() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime start = startTime.toLocalDateTime();
    LocalDateTime end = endTime.toLocalDateTime();
    
    if (!isPublished) return ExamStatus.DRAFT;
    if (now.isBefore(start)) return ExamStatus.UPCOMING;
    if (now.isAfter(end)) return ExamStatus.COMPLETED;
    return ExamStatus.ONGOING;
}
```

**Status Values**:
- `DRAFT` - Chưa publish
- `UPCOMING` - Đã publish nhưng chưa đến giờ thi
- `ONGOING` - Đang diễn ra
- `COMPLETED` - Đã kết thúc

### API Endpoints (13 endpoints)

#### Step 1A: Basic CRUD (6 endpoints)

##### 1. Create Exam
```http
POST /api/exams
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Midterm Exam - OOP Java",
  "description": "Kiểm tra giữa kỳ môn OOP",
  "subjectClassId": 1,
  "examPurpose": "MIDTERM",
  "examFormat": "ONLINE",
  "startTime": "2025-12-01T09:00:00",
  "endTime": "2025-12-01T11:00:00",
  "durationMinutes": 90,
  "passingScore": 5.0,
  "totalScore": 10.0,
  "randomizeQuestions": true,
  "randomizeOptions": true,
  "allowReviewAfterSubmit": false,
  "showCorrectAnswers": false
}
```

**Business Rules**:
- `startTime` < `endTime`
- `durationMinutes` <= time window
- `passingScore` <= `totalScore`
- Auto-set `isPublished = false`

##### 2. List Exams with Filters
```http
GET /api/exams?subjectClassId=1&examPurpose=MIDTERM&examFormat=ONLINE&isPublished=true&page=0&size=10&sort=startTime,desc
Authorization: Bearer {token}
```

**Features**:
- Filter by subject class, purpose, format, published status
- Pagination & sorting
- Returns Page<ExamSummaryDTO> (lightweight)

##### 3. Get Exam by ID
```http
GET /api/exams/{id}
Authorization: Bearer {token}
```

**Response**: Full ExamDTO with:
- Current status (computed)
- Question count
- Subject & class information
- All settings

##### 4. Get Exams by Subject Class
```http
GET /api/exams/subject-class/{subjectClassId}?page=0&size=10
Authorization: Bearer {token}
```

**Use Case**: List all exams cho một lớp học cụ thể

##### 5. Update Exam
```http
PUT /api/exams/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated title",
  "startTime": "2025-12-02T09:00:00",
  "version": 0
}
```

**Features**:
- Optimistic locking (version check)
- Partial updates (only provided fields)
- Validates time & score constraints
- Cannot update published exams (except unpublish first)

##### 6. Delete Exam (Soft Delete)
```http
DELETE /api/exams/{id}
Authorization: Bearer {token}
```

**Business Rules**:
- Cannot delete published exams
- Soft delete only (sets deleted_at)
- Preserves exam-question relationships

#### Step 1B: Publish/Unpublish (2 endpoints)

##### 7. Publish Exam
```http
POST /api/exams/{id}/publish
Authorization: Bearer {token}
```

**Business Rules**:
- ✅ Exam must have at least 1 question
- ✅ `startTime` must be in future
- ✅ Cannot re-publish published exam
- ✅ Sets `isPublished = true`

**Response**: ExamDTO with `isPublished = true`

##### 8. Unpublish Exam
```http
POST /api/exams/{id}/unpublish
Authorization: Bearer {token}
```

**Business Rules**:
- ❌ Cannot unpublish ONGOING exam
- ✅ Can unpublish UPCOMING or COMPLETED
- ✅ Sets `isPublished = false`

#### Step 2: Exam-Question Association (5 endpoints)

##### 9. Add Question to Exam
```http
POST /api/exams/{examId}/questions
Authorization: Bearer {token}
Content-Type: application/json

{
  "questionId": 5,
  "questionOrder": 1,
  "points": 1.0
}
```

**Business Rules**:
- Question must exist in QuestionBank
- No duplicate questions in same exam
- Cannot modify ONGOING or COMPLETED exams
- Auto-creates ExamQuestion relationship

##### 10. Remove Question from Exam
```http
DELETE /api/exams/{examId}/questions/{questionId}
Authorization: Bearer {token}
```

**Business Rules**:
- Cannot modify ONGOING or COMPLETED exams
- Auto-recalculates displayOrder for remaining questions
- Soft delete ExamQuestion

##### 11. Reorder Questions
```http
PUT /api/exams/{examId}/questions/reorder
Authorization: Bearer {token}
Content-Type: application/json

{
  "questions": [
    {"questionId": 10, "newOrder": 1},
    {"questionId": 8, "newOrder": 2},
    {"questionId": 9, "newOrder": 3}
  ]
}
```

**Technical Implementation**:
```java
// Step 1: Set temporary negative orders to avoid unique constraint
for (QuestionOrder qo : request.getQuestions()) {
    examQuestion.setQuestionOrder(-qo.getNewOrder());
}
examQuestionRepository.saveAllAndFlush(questionsToUpdate);

// Step 2: Convert back to positive orders
for (ExamQuestion eq : questionsToUpdate) {
    eq.setQuestionOrder(-eq.getQuestionOrder());
}
examQuestionRepository.saveAllAndFlush(questionsToUpdate);
```

**Key Fix**: Used `saveAllAndFlush()` to ensure DB commits between steps, avoiding unique constraint violations on `uk_exam_order`.

##### 12. Update Question Points
```http
PUT /api/exams/{examId}/questions/{questionId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "points": 2.0
}
```

**Use Case**: Điều chỉnh điểm số cho từng câu hỏi

##### 13. List Exam Questions
```http
GET /api/exams/{examId}/questions
Authorization: Bearer {token}
```

**Response**: List<ExamQuestionDTO> ordered by `question_order`

### Key Files Created

**Entities**:
- `Exam.java` - Main exam entity với computed status
- `ExamQuestion.java` - Join table entity
- `ExamStatus.java` - Enum cho status
- `ExamPurpose.java` - Enum (MIDTERM, FINAL, QUIZ, ASSIGNMENT)
- `ExamFormat.java` - Enum (ONLINE, OFFLINE, HYBRID)

**DTOs**:
- `ExamDTO.java` - Full exam details
- `ExamSummaryDTO.java` - Lightweight for listing
- `CreateExamRequest.java` - Create request
- `UpdateExamRequest.java` - Update request
- `AddQuestionToExamRequest.java` - Add question request
- `UpdateQuestionScoreRequest.java` - Update points request
- `ReorderQuestionsRequest.java` - Reorder request
- `ExamQuestionDTO.java` - Exam-question response

**Repository**:
- `ExamRepository.java` - Custom search queries
- `ExamQuestionRepository.java` - Join table queries

**Service**:
- `ExamService.java` - Business logic (600+ lines)

**Controller**:
- `ExamController.java` - 13 REST endpoints

---

## Bug Fixes

### Bug 1: SubjectClass Entity Issues
**Problem**: 
```java
String className = sc.getClassEntity().getName(); // ❌ No such method
```

**Root Cause**: SubjectClass không có `ClassEntity` relationship, chỉ có `code` field

**Fix**:
```java
String className = sc.getCode(); // ✅ Use code instead
```

### Bug 2: Question Count Type Mismatch
**Problem**:
```java
int count = examQuestionRepository.countByExamId(examId); // ❌ long → int
```

**Fix**:
```java
long count = examQuestionRepository.countByExamId(examId);
int questionCount = (int) count; // ✅ Explicit cast
```

### Bug 3: Reorder Unique Constraint Violation
**Problem**:
```
Duplicate entry '4-2' for key 'exam_questions.uk_exam_order'
```

**Root Cause**: Khi swap orders, intermediate state vi phạm unique constraint

**Fix**: Two-step approach với temporary negative orders
```java
// Step 1: Negative orders (-1, -2, -3)
examQuestionRepository.saveAllAndFlush(questionsToUpdate);

// Step 2: Positive orders (1, 2, 3)
examQuestionRepository.saveAllAndFlush(questionsToUpdate);
```

### Bug 4: Controllers Not Recognized
**Problem**: APIs returning "No static resource" error

**Root Cause**: Server không restart sau compile, controllers chưa được load

**Fix**: Created `restart-server.bat` utility
```batch
taskkill /F /IM java.exe
mvn clean compile -f backend/pom.xml
mvn spring-boot:run -f backend/pom.xml
```

---

## Documentation Files

1. **PHASE4-QUESTION-BANK-COMPLETION.md** - Question Bank implementation details
2. **PHASE4-EXAM-MANAGEMENT-STEP1A.md** - Exam CRUD documentation
3. **PHASE4-EXAM-MANAGEMENT-STEP1B.md** - Publish/Unpublish documentation
4. **PHASE4-EXAM-MANAGEMENT-STEP2.md** - Exam-Question association documentation
5. **PHASE4-API-TEST-CASES.md** - Complete API test suite
6. **PHASE4-TESTING-GUIDE.md** - Testing instructions
7. **PHASE4-COMPLETE-SUMMARY.md** - This file

### Thunder Client Collections

1. **thunder-client-phase4-question-bank.json** - Question Bank APIs
2. **thunder-client-exam-workflow-FINAL.json** - Complete exam workflow
3. **thunder-client-complete-workflow.json** - Full integration test

---

## Code Quality & Patterns

### Patterns Used

1. **Repository Pattern**: JPA repositories with custom queries
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: Request/Response separation from entities
4. **Builder Pattern**: Lombok @Builder for DTOs
5. **Soft Delete Pattern**: deleted_at timestamp
6. **Optimistic Locking**: @Version for concurrent updates
7. **Audit Pattern**: created_by, updated_by, timestamps

### Security

- ✅ JWT Authentication required
- ✅ Role-based authorization (TEACHER, DEPT_MANAGER, ADMIN)
- ✅ Input validation with @Valid
- ✅ SQL injection prevention (Parameterized queries)
- ✅ Business logic validation

### Code Statistics

- **Total Files**: 35+ Java files
- **Lines of Code**: ~3,500 lines
- **Comments**: Full Vietnamese comments with author tags
- **API Endpoints**: 19 endpoints
- **Test Coverage**: Manual testing (100% endpoints verified)

---

## Performance Considerations

### Database Indexes
```sql
-- Exam queries optimization
CREATE INDEX idx_exam_subject_class ON exams(subject_class_id);
CREATE INDEX idx_exam_published ON exams(is_published);
CREATE INDEX idx_exam_start_time ON exams(start_time);
CREATE INDEX idx_exam_deleted ON exams(deleted_at);

-- Question queries optimization
CREATE INDEX idx_question_subject ON questions(subject_id);
CREATE INDEX idx_question_type ON questions(question_type);
CREATE INDEX idx_question_difficulty ON questions(difficulty);
CREATE INDEX idx_question_deleted ON questions(deleted_at);

-- Exam-Question relationship
CREATE INDEX idx_exam_question_exam ON exam_questions(exam_id);
CREATE INDEX idx_exam_question_question ON exam_questions(question_id);
```

### Query Optimization

1. **Pagination**: All list endpoints support pagination
2. **Lazy Loading**: OneToMany relationships use LAZY fetch
3. **Projection**: ExamSummaryDTO for lightweight listing
4. **Filtering**: Database-level filtering vs application filtering

---

## Testing Results

### Manual Testing Status

✅ **Question Bank APIs** (6/6 passed)
- Create question: ✅ PASS
- List with filters: ✅ PASS  
- Get by ID: ✅ PASS
- Update question: ✅ PASS
- Delete question: ✅ PASS
- Get statistics: ✅ PASS

✅ **Exam CRUD APIs** (6/6 passed)
- Create exam: ✅ PASS
- List exams: ✅ PASS
- Get by ID: ✅ PASS
- Get by subject class: ✅ PASS
- Update exam: ✅ PASS
- Delete exam: ✅ PASS

✅ **Exam Publish APIs** (2/2 passed)
- Publish exam: ✅ PASS
- Unpublish exam: ✅ PASS

✅ **Exam-Question APIs** (5/5 passed)
- Add question: ✅ PASS
- Remove question: ✅ PASS
- Reorder questions: ✅ PASS (after fix)
- Update points: ✅ PASS
- List questions: ✅ PASS

**Total**: 19/19 APIs PASSED ✅

---

## Lessons Learned

### Technical Lessons

1. **Unique Constraints**: Need careful handling when swapping values
   - Solution: Temporary negative values + flush between steps

2. **Entity Relationships**: Always verify relationship existence before accessing
   - Don't assume `ClassEntity` exists if not in schema

3. **Type Casting**: Repository methods return `long` for counts
   - Need explicit cast to `int` if required

4. **Server Restart**: Code changes require full server restart
   - Created utility script for clean restart

5. **JPA Flush**: Use `saveAllAndFlush()` when order matters
   - Ensures DB persistence before next operation

### Process Lessons

1. **Incremental Testing**: Test each step before moving to next
2. **Documentation First**: Write docs while code is fresh
3. **Bug Tracking**: Document all issues and solutions
4. **Code Comments**: Vietnamese comments help team collaboration
5. **Git Commits**: Frequent commits with clear messages

---

## Future Enhancements (Not in Phase 4)

### Potential Improvements

1. **Exam Templates**: Save exam as template for reuse
2. **Exam Duplication**: Clone existing exam
3. **Advanced Statistics**: 
   - Pass rate by class
   - Average scores
   - Question difficulty analysis
4. **Bulk Operations**: 
   - Import questions from CSV/Excel
   - Export exam to PDF
5. **Question Validation**:
   - Automated grading for coding questions
   - Plagiarism detection
6. **Real-time Features**:
   - Live exam monitoring
   - Real-time submission count

These features are planned for future phases or iterations.

---

## Dependencies & Prerequisites

### Runtime Dependencies
```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### Database Requirements
- MySQL 8.0+
- Database: MS.TrustTest
- Flyway migrations V1-V13 applied

### Prerequisites from Previous Phases
- ✅ Phase 1: Database schema
- ✅ Phase 2: Authentication system
- ✅ Phase 3: Organization management (Subject, SubjectClass)

---

## Conclusion

Phase 4 successfully delivered a robust Question Bank and Exam Management system with:

- ✅ 19 production-ready REST APIs
- ✅ 8 question types support
- ✅ Advanced filtering and search
- ✅ Publish workflow with business rules
- ✅ Optimistic locking for concurrent updates
- ✅ Comprehensive error handling
- ✅ Full audit trail
- ✅ Soft delete pattern
- ✅ Complete documentation

The system is now ready for Phase 5 (Exam Taking Interface) where students will be able to:
- View published exams
- Take exams online
- Submit answers
- View results (after exam completion)

**Next Phase**: Phase 5 - Exam Taking Interface  
**Estimated Start**: After review and approval  
**Estimated Duration**: 2 weeks

---

**Document Version**: 1.0  
**Last Updated**: 19/11/2025 14:45  
**Author**: K24DTCN210-NVMANH  
**Reviewed By**: Pending
