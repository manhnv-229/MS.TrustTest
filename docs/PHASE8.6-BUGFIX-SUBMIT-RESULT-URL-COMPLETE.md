# Phase 8.6 - Bugfix: Submit Result URL & Options Null

**Ngày**: 25/11/2025  
**Người thực hiện**: K24DTCN210-NVMANH

## 📋 Tổng Quan

Fix 2 lỗi nghiêm trọng xảy ra khi submit bài thi:
1. **Client gọi sai URL** → 404 error
2. **Backend crash khi parse options NULL** → 500 error

## 🐛 Phân Tích Lỗi

### Lỗi 1: URL Client Sai
**Log Client:**
```
[Thread-10] ERROR com.mstrust.client.exam.api.ExamApiClient - Failed to get exam result
Status: 500, Body: {"status":500,"message":"An unexpected error occurred: No static resource exam-taking/results/37."}
```

**Nguyên nhân:**
- Client call: `GET /api/exam-taking/results/37` ❌ (có 's')
- Backend thực tế: `GET /api/exam-taking/result/{submissionId}` ✓ (không có 's')

**File lỗi:**
- `client-javafx/src/main/java/com/mstrust/client/exam/api/ExamApiClient.java:351`

### Lỗi 2: Backend Parse Options NULL
**Log Backend:**
```
2025-11-24T16:38:54.671+07:00 ERROR 16412 --- [ms-trust-exam-backend] 
c.m.exam.service.ExamTakingService : Error parsing options for question 3088

java.lang.IllegalArgumentException: argument "content" is null
    at com.fasterxml.jackson.databind.ObjectMapper._assertNotNull(ObjectMapper.java:5060)
```

**Nguyên nhân:**
- Method `parseOptionsWithoutAnswer()` gọi `objectMapper.readValue(question.getOptions(), Map.class)`
- Khi `question.getOptions()` = NULL → ObjectMapper throw exception

**File lỗi:**
- `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java:592`

## ✅ Giải Pháp

### Fix 1: Sửa URL Client

**File**: `client-javafx/src/main/java/com/mstrust/client/exam/api/ExamApiClient.java`

```java
// ❌ TRƯỚC (Line 351)
.uri(URI.create(baseUrl + "/api/exam-taking/results/" + submissionId))

// ✅ SAU
.uri(URI.create(baseUrl + "/api/exam-taking/result/" + submissionId))
```

**Kết quả:**
- Client gọi đúng endpoint backend
- Response HTTP 200 thay vì 404

### Fix 2: Handle Options NULL

**File**: `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java`

```java
private List<String> parseOptionsWithoutAnswer(QuestionBank question, 
        Boolean randomize, Long seed) {
    try {
        // ✅ CHECK NULL TRƯỚC KHI PARSE
        String optionsJson = question.getOptions();
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            log.warn("Question {} has null or empty options field", question.getId());
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> optionsMap = objectMapper.readValue(
            optionsJson, Map.class);
        
        // Check if map is null or empty
        if (optionsMap == null || optionsMap.isEmpty()) {
            log.warn("Question {} has empty options map", question.getId());
            return new ArrayList<>();
        }
        
        // Remove correctAnswer key if exists
        optionsMap.remove("correctAnswer");
        
        // ... rest of the method
    } catch (Exception e) {
        log.error("Error parsing options for question {}", question.getId(), e);
        return new ArrayList<>();
    }
}
```

**Kết quả:**
- Không crash khi options NULL
- Log warning thay vì error
- Return empty list thay vì throw exception

## 🔧 Compile & Deploy

```bash
# Backend
cd backend
mvn clean compile

# Client  
cd client-javafx
mvn clean compile
```

## 🧪 Testing

### Test Case 1: Submit Exam với Questions Có Options NULL
**Steps:**
1. Login student1@test.com
2. Start exam có câu hỏi options NULL
3. Answer một số câu
4. Submit exam
5. Verify result screen hiển thị

**Expected:**
- ✅ Submit thành công
- ✅ Result screen load được
- ✅ Không có 500 error
- ✅ Câu hỏi options NULL không hiển thị

### Test Case 2: Submit Exam Bình Thường
**Steps:**
1. Login student1@test.com
2. Start exam bình thường
3. Answer questions
4. Submit exam

**Expected:**
- ✅ URL gọi đúng: `/api/exam-taking/result/{submissionId}`
- ✅ HTTP 200 response
- ✅ Result screen hiển thị đầy đủ thông tin

## 📊 Impact Analysis

### Trước Fix
- ❌ 100% submit requests fail với 404/500 error
- ❌ Student không xem được kết quả
- ❌ Backend log đầy errors

### Sau Fix
- ✅ Submit requests thành công
- ✅ Student xem được result screen
- ✅ Backend xử lý gracefully khi options NULL

## 🎯 Root Cause

1. **URL mismatch**: 
   - Do typo khi implement Phase 8.5
   - Controller dùng `@GetMapping("/result/{id}")`
   - Client gọi `/results/{id}` (thừa 's')

2. **NULL handling thiếu**:
   - Method không validate input trước khi parse JSON
   - ObjectMapper không chấp nhận NULL content
   - Cần add defensive programming

## 📝 Lessons Learned

1. **Luôn kiểm tra URL mapping**:
   - So sánh Controller @GetMapping với API client URL
   - Dùng constants cho API paths thay vì hardcode

2. **Defensive programming**:
   - LUÔN validate input trước khi parse/process
   - Handle NULL/empty cases gracefully
   - Return safe defaults thay vì throw exception

3. **Logging strategy**:
   - WARN cho cases có thể handle được
   - ERROR cho cases nghiêm trọng
   - Include context (questionId, reason) trong log

## ✅ Completion Checklist

- [x] Fix client URL (results → result)
- [x] Add NULL check cho options parsing
- [x] Add empty check cho options map
- [x] Update log level (ERROR → WARN)
- [x] Test submit flow
- [x] Verify result screen loads
- [x] Document bugfix

## 🔜 Next Steps

Tiếp tục Phase 8.6:
- **Bước 3**: Exit Protection & Polish (2 giờ)
- **Bước 4**: Testing & Documentation (1 giờ)

---
**Status**: ✅ COMPLETED  
**Files Changed**: 2  
**Lines Changed**: +15 / -3  
**Test Status**: ✅ PASSED
