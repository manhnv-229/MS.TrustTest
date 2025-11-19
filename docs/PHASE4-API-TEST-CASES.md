# 🧪 PHASE 4 - API TEST CASES

**Testing Date:** 19/11/2025  
**Base URL:** http://localhost:8080  
**Tester:** K24DTCN210-NVMANH

---

## 📋 PREREQUISITE

### 1. Login để lấy JWT Token

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "teacher1",
    "password": "password123"
}
```

**Expected Response:**
```json
{
    "token": "eyJhbGc...",
    "username": "teacher1",
    "fullName": "Teacher One",
    "role": "TEACHER"
}
```

**Token sẽ được dùng cho các request sau:** `Authorization: Bearer {token}`

---

## 🧪 TEST CASES

### TEST 1: Tạo Câu Hỏi Multiple Choice

**Endpoint:** `POST /api/question-bank`

**Request:**
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

**Expected Response:** Status `201 Created`
```json
{
    "id": 1,
    "subjectId": 1,
    "subjectName": "Mathematics",
    "questionType": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "tags": "[\"Math\", \"Algebra\"]",
    "questionText": "Giải phương trình: 2x + 5 = 11",
    "options": "[\"x = 3\", \"x = 4\", \"x = 5\", \"x = 6\"]",
    "correctAnswer": "x = 3",
    "createdById": 2,
    "createdByName": "Teacher One",
    "createdAt": "2025-11-19T02:20:00",
    "usageCount": 0,
    "version": 0
}
```

---

### TEST 2: Tạo Câu Hỏi Essay

**Endpoint:** `POST /api/question-bank`

**Request:**
```json
{
    "subjectId": 2,
    "questionType": "ESSAY",
    "difficulty": "HARD",
    "tags": "[\"Literature\", \"Analysis\"]",
    "questionText": "Phân tích tác phẩm 'Chiếc lá cuối cùng' của O.Henry",
    "maxWords": 500,
    "minWords": 300,
    "gradingCriteria": "Đánh giá dựa trên: Ý tưởng chính (40%), Luận điểm (30%), Ngôn ngữ (30%)"
}
```

**Expected Response:** Status `201 Created`

---

### TEST 3: Tạo Câu Hỏi Coding

**Endpoint:** `POST /api/question-bank`

**Request:**
```json
{
    "subjectId": 3,
    "questionType": "CODING",
    "difficulty": "HARD",
    "tags": "[\"Algorithm\", \"Sorting\"]",
    "questionText": "Implement QuickSort algorithm in Python",
    "programmingLanguage": "Python",
    "starterCode": "def quicksort(arr):\n    # Your code here\n    pass",
    "testCases": "[{\"input\": [3,6,8,10,1,2,1], \"output\": [1,1,2,3,6,8,10]}, {\"input\": [5,4,3,2,1], \"output\": [1,2,3,4,5]}]",
    "timeLimitSeconds": 5,
    "memoryLimitMb": 128
}
```

**Expected Response:** Status `201 Created`

---

### TEST 4: Get All Questions với Filter

**Endpoint:** `GET /api/question-bank`

**Request 1: Lấy tất cả**
```bash
GET http://localhost:8080/api/question-bank?page=0&size=10&sort=createdAt,desc
Authorization: Bearer {teacher_token}
```

**Request 2: Filter theo Subject**
```bash
GET http://localhost:8080/api/question-bank?subjectId=1&page=0&size=10
Authorization: Bearer {teacher_token}
```

**Request 3: Filter theo Difficulty**
```bash
GET http://localhost:8080/api/question-bank?difficulty=MEDIUM&page=0&size=10
Authorization: Bearer {teacher_token}
```

**Request 4: Filter theo Type**
```bash
GET http://localhost:8080/api/question-bank?type=MULTIPLE_CHOICE&page=0&size=10
Authorization: Bearer {teacher_token}
```

**Request 5: Search theo Keyword**
```bash
GET http://localhost:8080/api/question-bank?keyword=phương trình&page=0&size=10
Authorization: Bearer {teacher_token}
```

**Request 6: Combined Filters**
```bash
GET http://localhost:8080/api/question-bank?subjectId=1&difficulty=MEDIUM&type=MULTIPLE_CHOICE&page=0&size=10
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `200 OK`
```json
{
    "content": [
        {
            "id": 1,
            "subjectId": 1,
            "questionType": "MULTIPLE_CHOICE",
            ...
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10
    },
    "totalPages": 1,
    "totalElements": 3,
    "last": true,
    "first": true
}
```

---

### TEST 5: Get Question By ID

**Endpoint:** `GET /api/question-bank/{id}`

**Request:**
```bash
GET http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `200 OK`
```json
{
    "id": 1,
    "subjectId": 1,
    "subjectName": "Mathematics",
    "questionType": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    ...
}
```

**Test Not Found:**
```bash
GET http://localhost:8080/api/question-bank/999
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `404 Not Found`
```json
{
    "message": "Không tìm thấy câu hỏi với ID: 999",
    "timestamp": "2025-11-19T02:25:00"
}
```

---

### TEST 6: Update Question

**Endpoint:** `PUT /api/question-bank/{id}`

**Request: Update Difficulty và Question Text**
```bash
PUT http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
Content-Type: application/json

{
    "difficulty": "HARD",
    "questionText": "Giải phương trình bậc 2: x² + 5x + 6 = 0",
    "options": "[\"x = -2 hoặc x = -3\", \"x = 2 hoặc x = 3\", \"x = -1 hoặc x = -6\", \"Vô nghiệm\"]",
    "correctAnswer": "x = -2 hoặc x = -3"
}
```

**Expected Response:** Status `200 OK`
```json
{
    "id": 1,
    "difficulty": "HARD",
    "questionText": "Giải phương trình bậc 2: x² + 5x + 6 = 0",
    "updatedById": 2,
    "updatedByName": "Teacher One",
    "updatedAt": "2025-11-19T02:30:00",
    "version": 1
}
```

**Test Update Question In Use (should fail):**
- Trước tiên add question vào 1 exam
- Sau đó thử update question đó

**Expected Response:** Status `400 Bad Request`
```json
{
    "message": "Không thể cập nhật câu hỏi đang được sử dụng trong bài thi"
}
```

---

### TEST 7: Delete Question

**Endpoint:** `DELETE /api/question-bank/{id}`

**Request:**
```bash
DELETE http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `200 OK`
```json
"Xóa câu hỏi thành công"
```

**Verify Deletion:**
```bash
GET http://localhost:8080/api/question-bank/1
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `404 Not Found`

**Test Delete Question In Use (should fail):**

**Expected Response:** Status `400 Bad Request`
```json
{
    "message": "Không thể xóa câu hỏi đang được sử dụng trong bài thi"
}
```

---

### TEST 8: Get Statistics

**Endpoint:** `GET /api/question-bank/statistics/{subjectId}`

**Request:**
```bash
GET http://localhost:8080/api/question-bank/statistics/1
Authorization: Bearer {teacher_token}
```

**Expected Response:** Status `200 OK`
```json
{
    "total": 5,
    "byDifficulty_stats": [
        ["EASY", 2],
        ["MEDIUM", 2],
        ["HARD", 1]
    ],
    "byType_stats": [
        ["MULTIPLE_CHOICE", 3],
        ["ESSAY", 1],
        ["CODING", 1]
    ]
}
```

---

## 🔒 SECURITY TESTS

### TEST 9: Unauthorized Access

**Request: Không có JWT Token**
```bash
GET http://localhost:8080/api/question-bank
```

**Expected Response:** Status `401 Unauthorized`

---

### TEST 10: Invalid Role

**Request: Login as STUDENT**
```json
{
    "username": "student1",
    "password": "password123"
}
```

**Then try to create question:**
```bash
POST http://localhost:8080/api/question-bank
Authorization: Bearer {student_token}
Content-Type: application/json

{
    "questionType": "MULTIPLE_CHOICE",
    "questionText": "Test question"
}
```

**Expected Response:** Status `403 Forbidden`

---

## ✅ VALIDATION TESTS

### TEST 11: Missing Required Fields

**Request: No questionText**
```json
{
    "subjectId": 1,
    "questionType": "MULTIPLE_CHOICE"
}
```

**Expected Response:** Status `400 Bad Request`
```json
{
    "message": "Question text không được để trống"
}
```

---

### TEST 12: Invalid Question Type Data

**Request: CODING without programmingLanguage**
```json
{
    "questionType": "CODING",
    "questionText": "Write code",
    "testCases": "[...]"
}
```

**Expected Response:** Status `400 Bad Request`
```json
{
    "message": "Programming language là bắt buộc cho CODING"
}
```

---

### TEST 13: Non-existent Subject

**Request:**
```json
{
    "subjectId": 999,
    "questionType": "MULTIPLE_CHOICE",
    "questionText": "Test question",
    "options": "[\"A\", \"B\"]",
    "correctAnswer": "A"
}
```

**Expected Response:** Status `404 Not Found`
```json
{
    "message": "Không tìm thấy môn học với ID: 999"
}
```

---

## 📊 TEST SUMMARY

| Test Case | Endpoint | Expected Status | Pass/Fail |
|-----------|----------|-----------------|-----------|
| 1. Create Multiple Choice | POST /api/question-bank | 201 | ⏳ |
| 2. Create Essay | POST /api/question-bank | 201 | ⏳ |
| 3. Create Coding | POST /api/question-bank | 201 | ⏳ |
| 4. Get All (no filter) | GET /api/question-bank | 200 | ⏳ |
| 5. Get All (with filters) | GET /api/question-bank | 200 | ⏳ |
| 6. Get By ID | GET /api/question-bank/{id} | 200 | ⏳ |
| 7. Get By ID (not found) | GET /api/question-bank/999 | 404 | ⏳ |
| 8. Update Question | PUT /api/question-bank/{id} | 200 | ⏳ |
| 9. Update (in use - fail) | PUT /api/question-bank/{id} | 400 | ⏳ |
| 10. Delete Question | DELETE /api/question-bank/{id} | 200 | ⏳ |
| 11. Delete (in use - fail) | DELETE /api/question-bank/{id} | 400 | ⏳ |
| 12. Get Statistics | GET /api/question-bank/statistics/1 | 200 | ⏳ |
| 13. Unauthorized | GET /api/question-bank | 401 | ⏳ |
| 14. Invalid Role | POST /api/question-bank | 403 | ⏳ |
| 15. Missing Fields | POST /api/question-bank | 400 | ⏳ |
| 16. Invalid Type Data | POST /api/question-bank | 400 | ⏳ |
| 17. Non-existent Subject | POST /api/question-bank | 404 | ⏳ |

**Legend:**
- ⏳ Pending
- ✅ Pass
- ❌ Fail

---

## 📝 NOTES

- Tất cả tests cần JWT token từ teacher account
- Database cần có sample data (subjects) trước khi test
- Test order quan trọng: Create → Read → Update → Delete
- Soft delete: Deleted questions vẫn tồn tại trong DB với deletedAt != NULL
