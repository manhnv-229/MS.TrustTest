# Phase 4 - Exam Management - Step 1B: Publish/Unpublish Exam

**Completion Date:** 19/11/2025 08:48
**Status:** ✅ COMPLETED

## Tổng Quan

Step 1B triển khai chức năng Publish/Unpublish cho Exam:
- Publish exam: Cho phép students thấy và tham gia làm bài thi
- Unpublish exam: Ẩn exam khỏi students
- Business rules và validation đầy đủ

## Các Thay Đổi

### 1. ExamService - 2 Methods Mới

#### publishExam()
```java
public ExamDTO publishExam(Long id, Long currentUserId)
```

**Business Rules:**
1. ✅ Exam phải chưa được publish (`isPublished = false`)
2. ✅ Exam phải có ít nhất 1 câu hỏi
3. ✅ `startTime` phải trong tương lai (không publish exam đã quá hạn)

**Validation Logic:**
- Check `isPublished` status → throw BadRequestException nếu đã publish
- Count questions: `examQuestionRepository.countByExamId()` → must be > 0
- Check startTime: `exam.getStartTime().isBefore(LocalDateTime.now())` → must be false

**Actions:**
- Set `isPublished = true`
- Update `updatedBy` và `updatedAt`
- Save và return ExamDTO

#### unpublishExam()
```java
public ExamDTO unpublishExam(Long id, Long currentUserId)
```

**Business Rules:**
1. ✅ Exam phải đã được publish (`isPublished = true`)
2. ✅ Exam KHÔNG được đang diễn ra (`currentStatus != ONGOING`)
3. ✅ Có thể unpublish exam chưa bắt đầu hoặc đã kết thúc

**Validation Logic:**
- Check `isPublished` status → must be true
- Get `currentStatus` từ `exam.getCurrentStatus()`
- Check status != ONGOING → cannot unpublish ongoing exam

**Actions:**
- Set `isPublished = false`
- Update `updatedBy` và `updatedAt`
- Save và return ExamDTO

### 2. ExamController - 2 Endpoints Mới

#### POST /api/exams/{id}/publish
```java
@PostMapping("/{id}/publish")
@PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
public ResponseEntity<ExamDTO> publishExam(@PathVariable Long id)
```

**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Request:** No body required, just exam ID in path

**Response:** `200 OK` với ExamDTO (isPublished = true)

**Error Cases:**
- `400 Bad Request`: Exam already published
- `400 Bad Request`: No questions in exam
- `400 Bad Request`: Start time in the past
- `404 Not Found`: Exam not found

#### POST /api/exams/{id}/unpublish
```java
@PostMapping("/{id}/unpublish")
@PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
public ResponseEntity<ExamDTO> unpublishExam(@PathVariable Long id)
```

**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Request:** No body required, just exam ID in path

**Response:** `200 OK` với ExamDTO (isPublished = false)

**Error Cases:**
- `400 Bad Request`: Exam not published
- `400 Bad Request`: Cannot unpublish ongoing exam
- `404 Not Found`: Exam not found

## API Usage Examples

### 1. Publish Exam

**Request:**
```http
POST /api/exams/1/publish
Authorization: Bearer {jwt_token}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "title": "Kiểm tra giữa kỳ Toán",
  "isPublished": true,
  "currentStatus": "UPCOMING",
  "questionCount": 10,
  "version": 2,
  ...
}
```

**Error Response - No Questions (400):**
```json
{
  "message": "Cannot publish exam without questions. Please add questions first.",
  "timestamp": "2025-11-19T08:48:00"
}
```

**Error Response - Past Start Time (400):**
```json
{
  "message": "Cannot publish exam with past start time. Please update start time first.",
  "timestamp": "2025-11-19T08:48:00"
}
```

### 2. Unpublish Exam

**Request:**
```http
POST /api/exams/1/unpublish
Authorization: Bearer {jwt_token}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "title": "Kiểm tra giữa kỳ Toán",
  "isPublished": false,
  "currentStatus": "UPCOMING",
  "version": 3,
  ...
}
```

**Error Response - Ongoing Exam (400):**
```json
{
  "message": "Cannot unpublish ongoing exam. Please wait until exam ends.",
  "timestamp": "2025-11-19T08:48:00"
}
```

## Business Flow

### Publish Flow
```
1. Teacher tạo exam → isPublished = false
2. Teacher thêm câu hỏi vào exam
3. Teacher kiểm tra thông tin exam
4. Teacher click "Publish"
5. System validates:
   - Has questions? ✓
   - Start time in future? ✓
   - Not already published? ✓
6. Set isPublished = true
7. Students có thể thấy exam trong danh sách
```

### Unpublish Flow
```
1. Exam đang published
2. Teacher click "Unpublish"
3. System checks:
   - Is published? ✓
   - Not ongoing? ✓
4. Set isPublished = false
5. Students không còn thấy exam
```

## Status Transitions

### ExamStatus và isPublished
```
UPCOMING (published):
  - isPublished = true
  - startTime > now
  - Students CAN see & prepare

ONGOING (published):
  - isPublished = true
  - startTime <= now <= endTime
  - Students CAN take exam
  - CANNOT unpublish

COMPLETED (published):
  - isPublished = true
  - endTime < now
  - Students CAN review (if allowed)
  - CAN unpublish

Any Status (unpublished):
  - isPublished = false
  - Students CANNOT see
```

## Integration with Existing Features

### With Exam CRUD:
- `createExam()`: Luôn tạo với `isPublished = false`
- `updateExam()`: Có thể update nếu unpublished hoặc upcoming
- `deleteExam()`: Phải unpublish trước khi delete

### With Question Management:
- Publish validation: Kiểm tra `questionCount > 0`
- Không thể publish exam rỗng

### With Exam Monitoring (Phase 6):
- Published exams xuất hiện trong student view
- Unpublished exams chỉ teacher/admin thấy

## Compilation Status

```
[INFO] BUILD SUCCESS
[INFO] Total time: 29.584 s
[INFO] Finished at: 2025-11-19T08:48:44+07:00
```

✅ No compilation errors
⚠️ 18 Lombok warnings (về @Builder.Default) - không ảnh hưởng chức năng

## Testing Scenarios

### Publish Tests:
1. ✅ Publish exam with questions (success)
2. ✅ Publish exam without questions (fail - 400)
3. ✅ Publish exam with past start time (fail - 400)
4. ✅ Publish already published exam (fail - 400)
5. ✅ Publish non-existent exam (fail - 404)

### Unpublish Tests:
1. ✅ Unpublish upcoming exam (success)
2. ✅ Unpublish completed exam (success)
3. ✅ Unpublish ongoing exam (fail - 400)
4. ✅ Unpublish unpublished exam (fail - 400)
5. ✅ Unpublish non-existent exam (fail - 404)

## Next Steps

✅ **Step 1A: Exam Basic CRUD** - COMPLETED
✅ **Step 1B: Publish/Unpublish Exam** - COMPLETED
⏭️ **Step 2: Exam-Question Association** - NEXT
- Add questions to exam
- Remove questions from exam
- Update question order
- Manage question scores

---
**Created by:** K24DTCN210-NVMANH
**Date:** 19/11/2025 08:48
