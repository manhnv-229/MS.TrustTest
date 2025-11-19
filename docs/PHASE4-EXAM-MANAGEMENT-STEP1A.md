# Phase 4 - Exam Management - Step 1A: Exam Basic CRUD

**Completion Date:** 19/11/2025 08:43
**Status:** ✅ COMPLETED

## Tổng Quan

Step 1A triển khai CRUD cơ bản cho Exam entity với các tính năng:
- Tạo đề thi mới
- Xem danh sách đề thi (có filter và pagination)
- Xem chi tiết đề thi
- Cập nhật đề thi
- Xóa mềm đề thi

## Các Files Đã Tạo/Cập Nhật

### 1. Entity
- ✅ `backend/src/main/java/com/mstrust/exam/entity/Exam.java` (đã refactor)
  - Loại bỏ các fields liên quan đến monitoring (lastAccessedAt, accessCount, etc.)
  - Giữ lại các fields cốt lõi cho quản lý đề thi
  - Thêm computed method `getCurrentStatus()` để tính trạng thái động

### 2. DTOs (4 files)
- ✅ `backend/src/main/java/com/mstrust/exam/dto/ExamDTO.java`
  - Full details response DTO
  - Chứa toàn bộ thông tin exam bao gồm subject class, question count, etc.

- ✅ `backend/src/main/java/com/mstrust/exam/dto/CreateExamRequest.java`
  - Request DTO cho tạo exam mới
  - Validation đầy đủ với Jakarta Validation
  - Default values cho các boolean fields

- ✅ `backend/src/main/java/com/mstrust/exam/dto/UpdateExamRequest.java`
  - Request DTO cho cập nhật exam
  - Tất cả fields optional (partial update)
  - Bắt buộc version để implement optimistic locking

- ✅ `backend/src/main/java/com/mstrust/exam/dto/ExamSummaryDTO.java`
  - Lightweight DTO cho list view
  - Chỉ chứa thông tin cơ bản cần thiết cho hiển thị danh sách

### 3. Service
- ✅ `backend/src/main/java/com/mstrust/exam/service/ExamService.java`
  - `createExam()`: Tạo exam mới với validation
  - `getExams()`: Lấy danh sách với filters (subjectClassId, examPurpose, examFormat, isPublished)
  - `getExamById()`: Lấy chi tiết exam
  - `getExamsBySubjectClass()`: Lấy danh sách theo subject class
  - `updateExam()`: Cập nhật exam với partial update
  - `deleteExam()`: Soft delete (kiểm tra không được xóa exam đã publish)
  - Private helpers: validateTimeConstraints(), validateScores(), mapToDTO(), mapToSummaryDTO()

### 4. Controller
- ✅ `backend/src/main/java/com/mstrust/exam/controller/ExamController.java`
  - `POST /api/exams`: Tạo exam mới
  - `GET /api/exams`: Lấy danh sách với filters và pagination
  - `GET /api/exams/{id}`: Lấy chi tiết
  - `GET /api/exams/subject-class/{subjectClassId}`: Lấy theo subject class
  - `PUT /api/exams/{id}`: Cập nhật
  - `DELETE /api/exams/{id}`: Xóa mềm
  - Authorization: TEACHER, DEPT_MANAGER, ADMIN roles

### 5. Repository
- ✅ `backend/src/main/java/com/mstrust/exam/repository/ExamRepository.java` (đã tồn tại)
  - Đã có sẵn các query methods cần thiết
  - Không cần thay đổi

## Business Rules Implemented

### 1. Time Constraints Validation
```java
- startTime < endTime
- durationMinutes <= (endTime - startTime)
```

### 2. Score Validation
```java
- passingScore <= totalScore
- Both scores trong range [0, 100]
```

### 3. Soft Delete Protection
- Không được xóa exam đã published
- Phải unpublish trước khi xóa

### 4. Optimistic Locking
- Sử dụng version field
- Kiểm tra conflict khi update

## API Endpoints

### POST /api/exams
**Create Exam**
```json
Request:
{
  "title": "Kiểm tra giữa kỳ Toán",
  "description": "Đề thi giữa kỳ môn Toán cao cấp",
  "subjectClassId": 1,
  "examPurpose": "MIDTERM",
  "examFormat": "ONLINE",
  "startTime": "2025-12-01T08:00:00",
  "endTime": "2025-12-01T10:00:00",
  "durationMinutes": 90,
  "passingScore": 50.00,
  "totalScore": 100.00,
  "randomizeQuestions": true,
  "randomizeOptions": true,
  "allowReviewAfterSubmit": true,
  "showCorrectAnswers": false
}

Response: ExamDTO (201 Created)
```

### GET /api/exams
**List Exams with Filters**
```
Query Parameters:
- subjectClassId (optional): Filter by subject class
- examPurpose (optional): PRACTICE, QUIZ, MIDTERM, FINAL, ENTRANCE
- examFormat (optional): ONLINE, OFFLINE, HYBRID
- isPublished (optional): true/false
- page (default: 0)
- size (default: 10)
- sort (default: "createdAt,desc")

Response: Page<ExamSummaryDTO>
```

### GET /api/exams/{id}
**Get Exam Details**
```
Response: ExamDTO (200 OK)
```

### GET /api/exams/subject-class/{subjectClassId}
**List Exams by Subject Class**
```
Query Parameters:
- page, size, sort

Response: Page<ExamSummaryDTO>
```

### PUT /api/exams/{id}
**Update Exam**
```json
Request:
{
  "title": "Updated title",
  "startTime": "2025-12-02T08:00:00",
  "version": 1  // Required for optimistic locking
}

Response: ExamDTO (200 OK)
```

### DELETE /api/exams/{id}
**Soft Delete Exam**
```
Response: 204 No Content
```

## Validation Rules

### CreateExamRequest
- `title`: required, 3-200 chars
- `description`: max 2000 chars
- `subjectClassId`: required
- `examPurpose`: required (enum)
- `examFormat`: required (enum)
- `startTime`: required, @Future
- `endTime`: required
- `durationMinutes`: required, positive, max 480 (8 hours)
- `passingScore`: 0-100
- `totalScore`: 0-100

### UpdateExamRequest
- Tất cả fields optional
- `version`: required (optimistic locking)
- Same validation rules khi có giá trị

## Compilation Status

```
[INFO] BUILD SUCCESS
[INFO] Total time: 17.988 s
[INFO] Finished at: 2025-11-19T08:43:25+07:00
```

✅ No compilation errors
⚠️ 18 Lombok warnings (về @Builder.Default) - không ảnh hưởng chức năng

## Notes

### SubjectClass Relationship
- SubjectClass không có field `name`, chỉ có `code`
- SubjectClass không có `classEntity`, liên kết trực tiếp với Subject
- DTO mapping sử dụng:
  - `subjectClassName` = `subjectClass.code`
  - `subjectName` = `subject.subjectName`
  - `classId`, `className` = null

### Question Count
- Sử dụng `examQuestionRepository.countByExamId()`
- Return type là `long`, cần cast sang `int` cho DTO

### Current User
- Lấy từ `SecurityContextHolder.getContext().getAuthentication()`
- Không dùng `CustomUserDetails` (không tồn tại)
- Map email -> User -> userId

## Next Steps

✅ **Step 1A: Exam Basic CRUD** - COMPLETED
⏭️ **Step 1B: Publish/Unpublish Exam** - NEXT
- Implement publish/unpublish functionality
- Add validation rules
- Update exam status management

---
**Created by:** K24DTCN210-NVMANH
**Date:** 19/11/2025 08:43
