# 📋 PHASE 4 - QUESTION BANK APIs Testing Guide

**Ngày tạo:** 19/11/2025 03:19
**Tác giả:** K24DTCN210-NVMANH with Cline

---

## 🎯 MỤC TIÊU TEST

Test đầy đủ 6 APIs của Question Bank Management với các kịch bản:
- ✅ Create questions (5 loại: Multiple Choice, Essay, True/False, Coding, Fill in Blank)
- ✅ Get all questions với filters (subject, difficulty, type, keyword)
- ✅ Get question by ID
- ✅ Update question
- ✅ Delete question (soft delete)
- ✅ Get statistics by subject
- ✅ Security testing (authentication & authorization)

---

## 🔐 TEACHER ACCOUNT

**Email:** `teacher1@mstrust.edu.vn`  
**Password:** `Teacher@123`  
**Role:** ROLE_TEACHER  
**Student Code:** T001

---

## 📁 THUNDER CLIENT SETUP

### Bước 1: Import Collection

1. Mở Thunder Client trong VSCode
2. Click **Collections** → **Import**
3. Chọn file: `docs/thunder-client-phase4-question-bank.json`
4. Collection sẽ có 2 folders:
   - **0. Authentication** (1 request)
   - **1. Question Bank** (16 requests)

### Bước 2: Setup Environment Variable

1. Chạy request **0.1. Login as Teacher**
2. Copy **token** từ response
3. Trong Thunder Client, tạo biến:
   - Name: `token`
   - Value: `<paste token here>`
4. Hoặc manual replace `{{token}}` trong từng request

---

## 🧪 TEST SCENARIOS

### Test 1: Login as Teacher ✅
**Request:** `0.1. Login as Teacher`  
**Expected:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 4,
    "email": "teacher1@mstrust.edu.vn",
    "fullName": "Nguyen Van B - Giao Vien",
    "roles": ["ROLE_TEACHER"]
  }
}
```

---

### Test 2: Create Multiple Choice Question ✅
**Request:** `1.1. Create Multiple Choice Question`  
**Expected:** Status 201 Created
```json
{
  "id": 1,
  "questionType": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "questionText": "Giải phương trình: 2x + 5 = 11",
  "options": ["x = 3", "x = 4", "x = 5", "x = 6"],
  "correctAnswer": "x = 3"
}
```

---

### Test 3: Create Essay Question ✅
**Request:** `1.2. Create Essay Question`  
**Expected:** Status 201 Created
```json
{
  "id": 2,
  "questionType": "ESSAY",
  "difficulty": "HARD",
  "questionText": "Phân tích tác phẩm 'Chiếc lá cuối cùng' của O.Henry",
  "maxWords": 500,
  "minWords": 300,
  "gradingCriteria": "Đánh giá dựa trên: Ý tưởng chính (40%), Luận điểm (30%), Ngôn ngữ (30%)"
}
```

---

### Test 4: Create Coding Question ✅
**Request:** `1.3. Create Coding Question`  
**Expected:** Status 201 Created
```json
{
  "id": 3,
  "questionType": "CODING",
  "difficulty": "HARD",
  "questionText": "Implement QuickSort algorithm in Python",
  "programmingLanguage": "Python",
  "starterCode": "def quicksort(arr):\n    # Your code here\n    pass",
  "testCases": [...]
}
```

---

### Test 5: Get All Questions (No Filter) ✅
**Request:** `1.4. Get All Questions (No Filter)`  
**Expected:** Status 200 OK
```json
{
  "content": [
    { "id": 3, "questionType": "CODING", ... },
    { "id": 2, "questionType": "ESSAY", ... },
    { "id": 1, "questionType": "MULTIPLE_CHOICE", ... }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### Test 6: Get All Questions (Filter by Subject) ✅
**Request:** `1.5. Get All Questions (Filter by Subject)`  
**URL:** `?subjectId=1&page=0&size=10`  
**Expected:** Chỉ trả về questions của subject 1

---

### Test 7: Get All Questions (Filter by Difficulty) ✅
**Request:** `1.6. Get All Questions (Filter by Difficulty)`  
**URL:** `?difficulty=MEDIUM&page=0&size=10`  
**Expected:** Chỉ trả về questions có difficulty MEDIUM

---

### Test 8: Get All Questions (Filter by Type) ✅
**Request:** `1.7. Get All Questions (Filter by Type)`  
**URL:** `?type=MULTIPLE_CHOICE&page=0&size=10`  
**Expected:** Chỉ trả về MULTIPLE_CHOICE questions

---

### Test 9: Get All Questions (Search by Keyword) ✅
**Request:** `1.8. Get All Questions (Search by Keyword)`  
**URL:** `?keyword=phương trình&page=0&size=10`  
**Expected:** Questions có "phương trình" trong questionText

---

### Test 10: Get All Questions (Combined Filters) ✅
**Request:** `1.9. Get All Questions (Combined Filters)`  
**URL:** `?subjectId=1&difficulty=MEDIUM&type=MULTIPLE_CHOICE`  
**Expected:** Questions thỏa mãn TẤT CẢ điều kiện

---

### Test 11: Get Question by ID (Success) ✅
**Request:** `1.10. Get Question by ID`  
**URL:** `/question-bank/1`  
**Expected:** Status 200, trả về question đầy đủ

---

### Test 12: Get Question by ID (Not Found) ❌
**Request:** `1.11. Get Question by ID (Not Found)`  
**URL:** `/question-bank/999`  
**Expected:** Status 404
```json
{
  "message": "Question not found with id: 999"
}
```

---

### Test 13: Update Question ✅
**Request:** `1.12. Update Question`  
**Expected:** Status 200, question được update

---

### Test 14: Delete Question (Soft Delete) ✅
**Request:** `1.13. Delete Question`  
**Expected:** Status 204 No Content  
**Verify:** Question không còn xuất hiện trong list

---

### Test 15: Get Statistics by Subject ✅
**Request:** `1.14. Get Statistics by Subject`  
**URL:** `/question-bank/statistics/1`  
**Expected:** Status 200
```json
{
  "subjectId": 1,
  "totalQuestions": 10,
  "byDifficulty": {
    "EASY": 3,
    "MEDIUM": 5,
    "HARD": 2
  },
  "byType": {
    "MULTIPLE_CHOICE": 4,
    "ESSAY": 2,
    "TRUE_FALSE": 2,
    "CODING": 1,
    "FILL_IN_BLANK": 1
  }
}
```

---

### Test 16: Unauthorized Access (No Token) ❌
**Request:** `1.15. Unauthorized Access (No Token)`  
**Headers:** Không có Authorization  
**Expected:** Status 401 Unauthorized

---

### Test 17: Invalid Role (Student tries to create) ❌
**Request:** `1.16. Invalid Role (Student tries to create)`  
**Note:** Cần login với student account trước  
**Expected:** Status 403 Forbidden

---

## ✅ CHECKLIST KIỂM TRA

### Functional Testing
- [ ] Login thành công với teacher account
- [ ] Tạo được 5 loại câu hỏi khác nhau
- [ ] Get all questions với pagination
- [ ] Filter theo subject, difficulty, type hoạt động
- [ ] Search theo keyword hoạt động
- [ ] Combined filters hoạt động đúng
- [ ] Get by ID trả về đúng data
- [ ] Update question thành công
- [ ] Soft delete hoạt động
- [ ] Statistics tính toán chính xác

### Security Testing
- [ ] 401 khi không có token
- [ ] 403 khi student cố tạo question
- [ ] Token expiration handling
- [ ] CORS headers đúng

### Data Validation
- [ ] Required fields validation
- [ ] Email format validation
- [ ] Enum values validation (QuestionType, Difficulty)
- [ ] JSON fields parsing đúng (options, testCases, etc.)

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: 401 Unauthorized
**Nguyên nhân:** Token hết hạn hoặc không valid  
**Giải pháp:** Login lại và lấy token mới

### Issue 2: 404 Not Found
**Nguyên nhân:** Question ID không tồn tại hoặc đã bị soft delete  
**Giải pháp:** Check database hoặc dùng question ID khác

### Issue 3: 400 Bad Request
**Nguyên nhân:** Request body không đúng format  
**Giải pháp:** Kiểm tra lại JSON format, đặc biệt là các field JSON string

### Issue 4: 500 Internal Server Error
**Nguyên nhân:** Lỗi server hoặc database  
**Giải pháp:** Check console log trong terminal running app

---

## 📊 EXPECTED RESULTS SUMMARY

| Test Case | Expected Status | Description |
|-----------|----------------|-------------|
| Login | 200 OK | Get JWT token |
| Create Questions (1-3) | 201 Created | 5 loại câu hỏi |
| Get All (4-9) | 200 OK | Với/không filter |
| Get by ID (10) | 200 OK | Trả về đúng question |
| Get by ID (11) | 404 Not Found | Question không tồn tại |
| Update (12) | 200 OK | Question updated |
| Delete (13) | 204 No Content | Soft deleted |
| Statistics (14) | 200 OK | Thống kê chính xác |
| No Token (15) | 401 Unauthorized | Security check |
| Wrong Role (16) | 403 Forbidden | Authorization check |

---

## 🎉 COMPLETION CRITERIA

Phase 4 được coi là HOÀN THÀNH khi:
- ✅ Tất cả 16 test cases PASS
- ✅ Không có lỗi 500 Internal Server Error
- ✅ Security checks hoạt động đúng
- ✅ Data validation chính xác
- ✅ Pagination hoạt động
- ✅ Soft delete không ảnh hưởng queries

---

**Good luck with testing! 🚀**
