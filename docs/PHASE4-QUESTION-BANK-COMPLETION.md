# ✅ PHASE 4 - QUESTION BANK MANAGEMENT - HOÀN THÀNH

**Ngày hoàn thành:** 19/11/2025 02:09  
**Người thực hiện:** K24DTCN210-NVMANH

---

## 📋 TỔNG QUAN

Phase 4 triển khai **Question Bank Management System** - một hệ thống quản lý ngân hàng câu hỏi độc lập với hệ thống câu hỏi trong bài thi (Phase 3).

### Sự khác biệt Phase 3 vs Phase 4:

| Tiêu chí | Phase 3 (Question) | Phase 4 (QuestionBank) |
|----------|-------------------|------------------------|
| **Entity** | `Question` | `QuestionBank` |
| **Mục đích** | Câu hỏi trong bài thi cụ thể | Ngân hàng câu hỏi tái sử dụng |
| **Quan hệ** | Thuộc về 1 Exam | Thuộc về Subject (optional) |
| **DTOs** | CreateQuestionRequest, UpdateQuestionRequest | CreateQuestionBankRequest, UpdateQuestionBankRequest |
| **APIs** | `/api/questions` | `/api/question-bank` |

---

## 🔧 CÁC THÀNH PHẦN ĐÃ TRIỂN KHAI

### 1. Entity Layer

#### ✅ QuestionBank.java
- **Đặc điểm:**
  - Hỗ trợ 8 loại câu hỏi (QuestionType enum)
  - 3 mức độ khó (Difficulty enum: EASY, MEDIUM, HARD)
  - Soft delete với `deletedAt`
  - Audit fields đầy đủ (createdBy, updatedBy, timestamps)
  - Optimistic locking với `@Version`

- **Các trường quan trọng:**
  ```java
  - subjectId (FK to Subject - optional)
  - questionType (QuestionType enum)
  - difficulty (Difficulty enum)
  - tags (JSON string)
  - version (Integer - for optimistic locking)
  
  // Type-specific fields:
  - Multiple Choice: options, correctAnswer
  - Essay: maxWords, minWords, gradingCriteria
  - Coding: programmingLanguage, starterCode, testCases, timeLimit, memoryLimit
  - Fill in Blank: blankPositions
  - Matching: leftItems, rightItems, correctMatches
  ```

### 2. Repository Layer

#### ✅ QuestionBankRepository.java
13 methods bao gồm:

**Basic CRUD:**
1. `findByIdAndDeletedAtIsNull(Long id)`
2. `findBySubjectIdAndDeletedAtIsNull(Long subjectId)`

**Search & Filter:**
3. `findByDifficultyAndDeletedAtIsNull(Difficulty difficulty)`
4. `findByQuestionTypeAndDeletedAtIsNull(QuestionType type)`
5. `searchByKeyword(String keyword)`
6. `filterQuestions(...)` - Filter phức tạp với phân trang

**Statistics:**
7. `countBySubject(Long subjectId)`
8. `getStatisticsByDifficulty(Long subjectId)`
9. `getStatisticsByType(Long subjectId)`

**Advanced:**
10. `findByCreator(Long teacherId, Pageable)`
11. `isQuestionInUse(Long questionId)` - Check usage in exams

### 3. DTO Layer

#### ✅ CreateQuestionBankRequest.java
- Validation đầy đủ với `@NotNull`, `@NotBlank`
- Support tất cả 8 loại câu hỏi
- Flexible fields theo từng loại

#### ✅ UpdateQuestionBankRequest.java
- Không cho phép thay đổi `questionType` và `subjectId`
- Chỉ update nội dung câu hỏi
- Tất cả fields đều optional

#### ✅ QuestionBankDTO.java
- Response DTO đầy đủ
- Bao gồm audit info (createdBy, updatedBy)
- Có thêm `usageCount` - số lần sử dụng trong exam

### 4. Service Layer

#### ✅ QuestionBankService.java
6 methods chính:

1. **createQuestion(CreateQuestionBankRequest)**
   - Validate theo loại câu hỏi
   - Kiểm tra subject existence
   - Auto set createdBy từ SecurityContext

2. **filterQuestions(...)**
   - Filter theo subjectId, difficulty, type, keyword
   - Phân trang với Spring Data
   - Sort flexible

3. **getQuestionById(Long id)**
   - Lấy chi tiết 1 câu hỏi
   - Throw exception nếu không tồn tại

4. **updateQuestion(Long id, UpdateQuestionBankRequest)**
   - Kiểm tra câu hỏi có đang được dùng không
   - Chỉ update fields được provide
   - Auto update audit fields

5. **deleteQuestion(Long id)**
   - Soft delete
   - Không cho xóa nếu đang được dùng trong exam

6. **getStatistics(Long subjectId)**
   - Thống kê tổng số câu hỏi
   - Phân bố theo difficulty
   - Phân bố theo type

**Validation Logic:**
- Mỗi loại câu hỏi có validation riêng
- Ví dụ: CODING bắt buộc có `programmingLanguage` và `testCases`

### 5. Controller Layer

#### ✅ QuestionBankController.java
6 endpoints REST:

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| POST | `/api/question-bank` | Tạo câu hỏi mới | TEACHER, ADMIN |
| GET | `/api/question-bank` | List với filter & pagination | TEACHER, ADMIN |
| GET | `/api/question-bank/{id}` | Chi tiết 1 câu hỏi | TEACHER, ADMIN |
| PUT | `/api/question-bank/{id}` | Cập nhật câu hỏi | TEACHER, ADMIN |
| DELETE | `/api/question-bank/{id}` | Xóa câu hỏi (soft) | TEACHER, ADMIN |
| GET | `/api/question-bank/statistics/{subjectId}` | Thống kê | TEACHER, ADMIN |

**Query Parameters cho GET list:**
- `subjectId` (optional)
- `difficulty` (optional): EASY, MEDIUM, HARD
- `type` (optional): MULTIPLE_CHOICE, ESSAY, CODING, etc.
- `keyword` (optional): Tìm trong questionText
- `page` (default: 0)
- `size` (default: 20)
- `sort` (default: createdAt,desc)

---

## 🔍 CÁC VẤN ĐỀ ĐÃ GIẢI QUYẾT

### 1. Conflict DifficultyLevel vs Difficulty
**Vấn đề:** Có 2 enum trùng tên:
- `DifficultyLevel.java` (Phase 3 cũ)
- `Difficulty.java` (Phase 3 mới)

**Giải pháp:**
- Xóa `DifficultyLevel.java`
- Dùng `Difficulty.java` cho cả Phase 3 và Phase 4
- Update tất cả references

### 2. DTO Name Conflict
**Vấn đề:** Phase 3 và Phase 4 đều có:
- `CreateQuestionRequest`
- `UpdateQuestionRequest`

**Giải pháp:** Rename Phase 4 DTOs:
- `CreateQuestionRequest` → `CreateQuestionBankRequest`
- `UpdateQuestionRequest` → `UpdateQuestionBankRequest`

Tạo lại DTOs cho Phase 3 để không conflict.

---

## ✅ KIỂM TRA BUILD

```bash
cd backend
mvn clean compile
```

**Kết quả:** ✅ BUILD SUCCESS
- 90 source files compiled
- Chỉ có warnings về @Builder.Default (không ảnh hưởng)
- 0 errors

---

## 📊 DATABASE SCHEMA

Table `question_bank` đã tồn tại từ migration V12:

```sql
CREATE TABLE question_bank (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT,
    question_type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20),
    tags TEXT,
    version INT DEFAULT 0,
    
    -- Content
    question_text TEXT NOT NULL,
    
    -- Multiple Choice/Select
    options TEXT,
    correct_answer TEXT,
    
    -- Essay
    max_words INT,
    min_words INT,
    grading_criteria TEXT,
    
    -- Coding
    programming_language VARCHAR(50),
    starter_code TEXT,
    test_cases TEXT,
    time_limit_seconds INT,
    memory_limit_mb INT,
    
    -- Fill in Blank
    blank_positions TEXT,
    
    -- Matching
    left_items TEXT,
    right_items TEXT,
    correct_matches TEXT,
    
    -- Attachments
    attachments TEXT,
    
    -- Audit
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

---

## 🧪 HƯỚNG DẪN TEST

### 1. Test Create Question (Multiple Choice)

```bash
POST http://localhost:8080/api/question-bank
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
    "subjectId": 1,
    "questionType": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "tags": "[\"Math\", \"Algebra\"]",
    "questionText": "Giải phương trình: 2x + 5 = 11",
    "options": "[\"x = 3\", \"x = 4\", \"x = 5\", \"x = 6\"]",
    "correctAnswer": "x = 3"
}
```

### 2. Test Get Questions với Filter

```bash
GET http://localhost:8080/api/question-bank?subjectId=1&difficulty=MEDIUM&page=0&size=10
Authorization: Bearer {teacher_token}
```

### 3. Test Get Statistics

```bash
GET http://localhost:8080/api/question-bank/statistics/1
Authorization: Bearer {teacher_token}
```

### 4. Test Update Question

```bash
PUT http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
    "difficulty": "HARD",
    "questionText": "Giải phương trình: 3x + 7 = 19"
}
```

### 5. Test Delete Question

```bash
DELETE http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
```

---

## 📝 NOTES

### Security
- Tất cả endpoints yêu cầu role `TEACHER` hoặc `ADMIN`
- Không có public endpoint
- Auto track created_by và updated_by từ SecurityContext

### Business Rules
1. Không thể update/delete câu hỏi đang được sử dụng trong exam
2. Subject là optional - câu hỏi có thể không thuộc môn nào
3. Mỗi loại câu hỏi có validation riêng
4. Soft delete - dữ liệu không bị xóa vĩnh viễn

### Performance
- Sử dụng pagination cho list APIs
- Index trên subject_id, question_type, difficulty
- Lazy loading cho relationships

---

## 🎯 HOÀN THÀNH

✅ Entity, Repository, Service, Controller  
✅ DTOs với validation đầy đủ  
✅ 6 RESTful APIs  
✅ Business logic validation  
✅ Security với roles  
✅ Build successful  

**Phase 4 đã sẵn sàng cho testing và deployment!**
