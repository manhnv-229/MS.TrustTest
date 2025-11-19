# Phase 4 - Step 2: Exam-Question Association Management

**Completion Date:** 19/11/2025 09:21  
**Status:** ✅ COMPLETED

## 📋 Overview

Step 2 hoàn thành việc quản lý liên kết giữa Exam và Question (từ QuestionBank), cho phép teacher thêm/xóa/sắp xếp câu hỏi trong bài thi.

## 🎯 Objectives Achieved

1. ✅ Thêm câu hỏi vào bài thi từ QuestionBank
2. ✅ Xóa câu hỏi khỏi bài thi
3. ✅ Sắp xếp lại thứ tự câu hỏi
4. ✅ Cập nhật điểm số cho từng câu hỏi
5. ✅ Lấy danh sách câu hỏi trong bài thi

## 📦 Components Created

### 1. DTOs (Data Transfer Objects)

#### AddQuestionToExamRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToExamRequest {
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotNull(message = "Question order is required")
    @Min(value = 1, message = "Question order must be at least 1")
    private Integer questionOrder;
    
    @NotNull(message = "Points is required")
    @DecimalMin(value = "0.0", message = "Points must be non-negative")
    private BigDecimal points;
}
```

#### ExamQuestionDTO.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    private Long examQuestionId;
    private Integer questionOrder;
    private BigDecimal points;
    
    // Question info
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private String difficulty;
    
    // Subject info
    private Long subjectId;
    private String subjectName;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### UpdateQuestionScoreRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionScoreRequest {
    @NotNull(message = "Points is required")
    @DecimalMin(value = "0.0", message = "Points must be non-negative")
    private BigDecimal points;
}
```

#### ReorderQuestionsRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderQuestionsRequest {
    @NotNull(message = "Questions list is required")
    @Size(min = 1, message = "Questions list must not be empty")
    private List<QuestionOrder> questions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOrder {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        @NotNull(message = "New order is required")
        @Min(value = 1, message = "Order must be at least 1")
        private Integer newOrder;
    }
}
```

### 2. Service Methods (ExamService.java)

#### addQuestionToExam()
- **Purpose:** Thêm câu hỏi từ QuestionBank vào Exam
- **Business Rules:**
  - Exam phải tồn tại và chưa bị xóa
  - Không thêm được vào exam ONGOING hoặc COMPLETED
  - Question phải tồn tại trong QuestionBank
  - Không được duplicate question trong cùng exam
- **Parameters:** examId, AddQuestionToExamRequest, currentUserId
- **Returns:** ExamQuestionDTO

#### removeQuestionFromExam()
- **Purpose:** Xóa câu hỏi khỏi bài thi
- **Business Rules:**
  - Không xóa được từ exam ONGOING hoặc COMPLETED
  - Tự động recalculate questionOrder cho các câu còn lại
- **Parameters:** examId, questionId, currentUserId
- **Returns:** void

#### reorderQuestions()
- **Purpose:** Sắp xếp lại thứ tự câu hỏi
- **Business Rules:**
  - Chỉ reorder được khi exam chưa bắt đầu (không ONGOING/COMPLETED)
  - Tất cả questions trong request phải tồn tại trong exam
- **Parameters:** examId, ReorderQuestionsRequest, currentUserId
- **Returns:** List<ExamQuestionDTO>

#### updateQuestionScore()
- **Purpose:** Cập nhật điểm số của câu hỏi
- **Business Rules:**
  - Có thể update cả khi exam đã published (để điều chỉnh)
- **Parameters:** examId, questionId, UpdateQuestionScoreRequest, currentUserId
- **Returns:** ExamQuestionDTO

#### getExamQuestions()
- **Purpose:** Lấy danh sách câu hỏi trong bài thi
- **Parameters:** examId
- **Returns:** List<ExamQuestionDTO> (ordered by questionOrder)

### 3. Controller Endpoints (ExamController.java)

| Method | Endpoint | Purpose | Auth Required |
|--------|----------|---------|---------------|
| POST | `/api/exams/{examId}/questions` | Thêm câu hỏi vào exam | TEACHER, DEPT_MANAGER, ADMIN |
| DELETE | `/api/exams/{examId}/questions/{questionId}` | Xóa câu hỏi khỏi exam | TEACHER, DEPT_MANAGER, ADMIN |
| PUT | `/api/exams/{examId}/questions/reorder` | Sắp xếp lại thứ tự | TEACHER, DEPT_MANAGER, ADMIN |
| PUT | `/api/exams/{examId}/questions/{questionId}` | Cập nhật điểm số | TEACHER, DEPT_MANAGER, ADMIN |
| GET | `/api/exams/{examId}/questions` | Lấy danh sách câu hỏi | TEACHER, DEPT_MANAGER, ADMIN |

## 🔄 Business Logic Flow

### 1. Add Question to Exam Flow
```
1. Validate exam exists và chưa xóa
2. Check exam status (không ONGOING/COMPLETED)
3. Validate question exists trong QuestionBank
4. Check duplicate (question đã có trong exam chưa)
5. Create ExamQuestion relationship
6. Update exam metadata (updatedBy, updatedAt)
7. Return ExamQuestionDTO
```

### 2. Remove Question Flow
```
1. Validate exam exists
2. Check exam status (không ONGOING/COMPLETED)
3. Find ExamQuestion relationship
4. Delete ExamQuestion
5. Recalculate questionOrder cho remaining questions
6. Update exam metadata
```

### 3. Reorder Questions Flow
```
1. Validate exam exists
2. Check exam status (không ONGOING/COMPLETED)
3. Validate all questions exist trong exam
4. Update questionOrder for each question
5. Update exam metadata
6. Return updated list ordered by new order
```

## 🧪 Testing Scenarios

### 1. Add Question to Exam

**Test Case 1: Thêm câu hỏi thành công**
```http
POST /api/exams/1/questions
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
  "questionId": 5,
  "questionOrder": 1,
  "points": 10.0
}

Expected: 201 Created
Response: ExamQuestionDTO
```

**Test Case 2: Thêm duplicate question**
```http
POST /api/exams/1/questions
{
  "questionId": 5,  // Already exists
  "questionOrder": 2,
  "points": 10.0
}

Expected: 400 Bad Request
Error: "Question already exists in this exam"
```

**Test Case 3: Thêm vào exam ONGOING**
```http
POST /api/exams/2/questions  // exam is ONGOING
{
  "questionId": 6,
  "questionOrder": 1,
  "points": 10.0
}

Expected: 400 Bad Request
Error: "Cannot modify ongoing exam"
```

### 2. Remove Question from Exam

**Test Case 1: Xóa câu hỏi thành công**
```http
DELETE /api/exams/1/questions/5
Authorization: Bearer {teacher_token}

Expected: 204 No Content
Side effect: Remaining questions reordered
```

**Test Case 2: Xóa từ exam COMPLETED**
```http
DELETE /api/exams/3/questions/7  // exam is COMPLETED

Expected: 400 Bad Request
Error: "Cannot remove questions from completed exam"
```

### 3. Reorder Questions

**Test Case 1: Sắp xếp lại thành công**
```http
PUT /api/exams/1/questions/reorder
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
  "questions": [
    {"questionId": 5, "newOrder": 3},
    {"questionId": 6, "newOrder": 1},
    {"questionId": 7, "newOrder": 2}
  ]
}

Expected: 200 OK
Response: List<ExamQuestionDTO> ordered by newOrder
```

**Test Case 2: Reorder exam ONGOING**
```http
PUT /api/exams/2/questions/reorder  // exam is ONGOING
{
  "questions": [...]
}

Expected: 400 Bad Request
Error: "Cannot reorder questions in ongoing or completed exam"
```

### 4. Update Question Score

**Test Case 1: Cập nhật điểm thành công**
```http
PUT /api/exams/1/questions/5
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
  "points": 15.0
}

Expected: 200 OK
Response: Updated ExamQuestionDTO
```

**Test Case 2: Invalid points**
```http
PUT /api/exams/1/questions/5
{
  "points": -5.0
}

Expected: 400 Bad Request
Error: "Points must be non-negative"
```

### 5. Get Exam Questions

**Test Case 1: Lấy danh sách thành công**
```http
GET /api/exams/1/questions
Authorization: Bearer {teacher_token}

Expected: 200 OK
Response: [
  {
    "examQuestionId": 1,
    "questionOrder": 1,
    "points": 10.0,
    "questionId": 5,
    "questionText": "What is Java?",
    "questionType": "MULTIPLE_CHOICE",
    "difficulty": "EASY",
    "subjectId": 1,
    "subjectName": "Programming",
    "createdAt": "2025-11-19T09:00:00",
    "updatedAt": "2025-11-19T09:00:00"
  },
  // ... more questions
]
```

**Test Case 2: Exam không tồn tại**
```http
GET /api/exams/999/questions

Expected: 404 Not Found
Error: "Exam not found with id: 999"
```

## 🔍 Key Technical Details

### 1. ExamQuestion Entity Relationship
```
ExamQuestion
├── @ManyToOne Exam (exam)
├── @ManyToOne QuestionBank (question)
├── Integer questionOrder
├── BigDecimal points
└── Timestamps (createdAt, updatedAt)
```

### 2. Business Rules Summary

| Operation | DRAFT | PUBLISHED (future) | ONGOING | COMPLETED |
|-----------|-------|-------------------|---------|-----------|
| Add Question | ✅ | ✅ | ❌ | ❌ |
| Remove Question | ✅ | ✅ | ❌ | ❌ |
| Reorder Questions | ✅ | ✅ | ❌ | ❌ |
| Update Score | ✅ | ✅ | ✅* | ✅* |
| Get Questions | ✅ | ✅ | ✅ | ✅ |

*Update score được phép để teacher có thể điều chỉnh

### 3. Auto-recalculate Order
Khi xóa question, hệ thống tự động recalculate questionOrder:
```java
// Example: Before delete
Q1 (order=1), Q2 (order=2), Q3 (order=3)

// Delete Q2
examQuestionRepository.delete(Q2);

// After recalculate
Q1 (order=1), Q3 (order=2)
```

## 📊 Implementation Statistics

- **Files Created:** 4 DTOs
- **Service Methods:** 5 new methods + 1 mapper
- **Controller Endpoints:** 5 endpoints
- **Business Rules:** 15+ validation rules
- **Lines of Code:** ~400 lines
- **Build Status:** ✅ SUCCESS (0 errors, 18 warnings - Lombok @Builder)

## 🐛 Issues Resolved

### Issue 1: ExamStatus.UPCOMING not found
**Problem:** Code reference `ExamStatus.UPCOMING` nhưng enum không có value này.

**Solution:** Refactor logic để check `ExamStatus.ONGOING || ExamStatus.COMPLETED` thay vì check `!= UPCOMING`.

```java
// Before (ERROR)
if (currentStatus != ExamStatus.UPCOMING) { ... }

// After (FIXED)
if (currentStatus == ExamStatus.ONGOING || currentStatus == ExamStatus.COMPLETED) {
    throw new BadRequestException("Cannot reorder questions in ongoing or completed exam");
}
```

## 📝 Notes

1. **Question Duplication Check:** Sử dụng `examQuestionRepository.existsByExamIdAndQuestionId()` để prevent duplicate
2. **Order Recalculation:** Tự động reorder khi delete để maintain consecutive order (1,2,3,...)
3. **Flexible Score Update:** Teacher có thể update điểm ngay cả khi exam đã published
4. **Read-Only Query:** getExamQuestions() dùng `@Transactional(readOnly = true)` để optimize

## ✅ Completion Checklist

- [x] Tạo 4 DTOs với validation annotations
- [x] Implement 5 service methods với business rules
- [x] Add 5 REST endpoints với authorization
- [x] Build thành công (no compilation errors)
- [x] Document business logic flow
- [x] Create comprehensive test scenarios
- [x] Document technical details

## 🎯 Next Steps

**Step 3:** Advanced Exam Features (dự kiến)
- Bulk add questions
- Import questions from template
- Exam statistics
- Question preview
- Exam duplication

---

**Author:** K24DTCN210-NVMANH  
**Date:** 19/11/2025 09:21  
**Build:** ✅ SUCCESS  
**Tests:** Ready for manual testing
