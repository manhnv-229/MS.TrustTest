# Phase 8.6 BugFix: Auto-Save Detailed Logging - COMPLETE

## 📋 Tổng Quan
**Thời gian**: 24/11/2025 15:20 - 15:28
**Mục tiêu**: Thêm detailed logging cho auto-save để debug vấn đề dữ liệu không được lưu

## 🎯 Vấn Đề
- Auto-save service chạy nhưng không thấy dữ liệu trong database
- Cần detailed logs để track:
  - AutoSaveService có gọi API không?
  - Request body gửi lên backend là gì?
  - Backend có nhận được request không?
  - Response từ backend là gì?

## ✅ Các Thay Đổi Đã Thực Hiện

### 1. AutoSaveService.java - Enhanced Logging
**File**: `client-javafx/src/main/java/com/mstrust/client/exam/service/AutoSaveService.java`

```java
// Added detailed logging at key points:

// 1. Check empty queue (unchanged)
logger.debug("[AutoSave] Queue size: {}", answerQueue.size());

// 2. Before API call
SaveAnswerRequest answer = answerQueue.poll();
logger.info("[AutoSave] Processing answer - QuestionId: {}, AnswerText: '{}', AnswerJson: '{}', AutoSave: true", 
    answer.getQuestionId(), 
    answer.getAnswerText() != null ? answer.getAnswerText().substring(0, Math.min(50, answer.getAnswerText().length())) : "null",
    answer.getAnswer());

// 3. After successful save
apiClient.saveAnswer(submissionId, answer);
logger.info("[AutoSave] Successfully saved answer for QuestionId: {}", answer.getQuestionId());

// 4. After error
catch (Exception e) {
    logger.error("[AutoSave] Failed to save answer for QuestionId: {}. Error: {}", 
        answer.getQuestionId(), e.getMessage(), e);
    answerQueue.offer(answer); // Re-queue
}
```

### 2. ExamApiClient.java - Detailed API Logging
**File**: `client-javafx/src/main/java/com/mstrust/client/exam/api/ExamApiClient.java`

```java
public void saveAnswer(Long submissionId, SaveAnswerRequest request) 
                      throws IOException, InterruptedException {
    String jsonBody = gson.toJson(request);
    
    // Log request details
    logger.info("[API] Saving answer - SubmissionId: {}, QuestionId: {}, AutoSave: {}", 
        submissionId, request.getQuestionId(), request.getIsAutoSave());
    logger.debug("[API] Request body: {}", jsonBody);
    
    HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/exam-taking/save-answer/" + submissionId))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    
    HttpResponse<String> response = httpClient.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
    
    // Log response
    if (response.statusCode() == 200) {
        logger.info("[API] Save answer SUCCESS - Status: 200, QuestionId: {}", 
            request.getQuestionId());
        logger.debug("[API] Response body: {}", response.body());
    } else {
        logger.error("[API] Save answer FAILED - Status: {}, QuestionId: {}, Body: {}", 
                response.statusCode(), request.getQuestionId(), response.body());
        throw new IOException("Failed to save answer: " + response.statusCode());
    }
}
```

### 3. Bug Fix: Lombok Getter Method
**Vấn đề**: Field `isAutoSave` trong SaveAnswerRequest được Lombok generate getter là `getIsAutoSave()`, không phải `isAutoSave()`

**Solution**:
```java
// Before (❌ SAI)
logger.info("... AutoSave: {}", request.isAutoSave());

// After (✅ ĐÚNG)
logger.info("... AutoSave: {}", request.getIsAutoSave());
```

## 📊 Log Levels Được Sử Dụng

### INFO Level (Default - sẽ hiện trong console)
- Queue processing start/end
- Individual answer being saved (with preview of data)
- API call success
- Error summary

### DEBUG Level (Chi tiết - cần enable)
- Queue size checks
- Full JSON request body
- Full response body

### ERROR Level
- Failed API calls với full stack trace
- Queue re-add operations

## 🧪 Testing Guide

### Bước 1: Khởi Động Ứng Dụng
```bash
cd client-javafx
.\run-exam-client.bat
```

### Bước 2: Mở Console/Terminal Window
- Giữ terminal window mở để xem logs real-time
- Tất cả logs sẽ print ra console (System.out/err)

### Bước 3: Test Auto-Save
1. Login với student account
2. Bắt đầu làm bài thi
3. Trả lời một câu hỏi (nhập text hoặc chọn option)
4. Đợi 5 giây (auto-save interval)

### Bước 4: Quan Sát Logs

**Expected Logs cho Auto-Save Success:**
```
[AutoSave] Starting auto-save check...
[AutoSave] Queue size: 1
[AutoSave] Processing answer - QuestionId: 3065, AnswerText: 'Hello World', AnswerJson: '...', AutoSave: true
[API] Saving answer - SubmissionId: 31, QuestionId: 3065, AutoSave: true
[API] Save answer SUCCESS - Status: 200, QuestionId: 3065
[AutoSave] Successfully saved answer for QuestionId: 3065
[AutoSave] Completed auto-save check
```

**Expected Logs cho Auto-Save Failure:**
```
[AutoSave] Starting auto-save check...
[AutoSave] Processing answer - QuestionId: 3065, ...
[API] Saving answer - SubmissionId: 31, QuestionId: 3065, AutoSave: true
[API] Save answer FAILED - Status: 403, QuestionId: 3065, Body: {...}
[AutoSave] Failed to save answer for QuestionId: 3065. Error: Failed to save answer: 403
java.io.IOException: Failed to save answer: 403
    at com.mstrust.client.exam.api.ExamApiClient.saveAnswer(...)
[AutoSave] Re-queuing answer for QuestionId: 3065
```

### Bước 5: Verify Database
```sql
-- Check student_answers table
SELECT * FROM student_answers 
WHERE submission_id = 31 
  AND question_id = 3065;

-- Check exam_submissions auto_save_count
SELECT id, auto_save_count, last_saved_at 
FROM exam_submissions 
WHERE id = 31;
```

## 🔍 Debug Scenarios

### Scenario 1: Không Thấy Logs
**Nguyên nhân**: Log level quá cao hoặc logger not initialized
**Solution**: 
- Check logback.xml hoặc logging configuration
- Verify SLF4J binding

### Scenario 2: Auto-Save Không Chạy
**Logs Expected**: Không có "[AutoSave] Starting..." logs
**Debug**:
1. Check AutoSaveService có được start() không?
2. Check scheduledExecutor có running không?
3. Check answerQueue có empty không?

### Scenario 3: API 403 Forbidden
**Logs Expected**: "[API] Save answer FAILED - Status: 403"
**Debug**:
1. Check JWT token còn valid không?
2. Check user có permission không?
3. Check submission_id có thuộc về user không?

### Scenario 4: API 400 Bad Request
**Logs Expected**: "[API] Save answer FAILED - Status: 400"
**Debug**:
1. Check request body JSON format
2. Check questionId có valid không?
3. Check answer format có đúng với question type không?

## 📝 Next Steps

### Nếu Vẫn Không Thấy Dữ Liệu Được Lưu:
1. **Check Backend Logs**: Xem backend có nhận request không?
2. **Check Database Transactions**: Có rollback không?
3. **Check Field Mapping**: Answer data có match với backend DTO không?
4. **Check Permissions**: User có quyền save answer không?

### Enable DEBUG Level (Optional)
Để xem full request/response body:
```java
// In ExamClientApplication.java hoặc logging config
Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
root.setLevel(Level.DEBUG);
```

## ✅ Compilation Status
- **Status**: BUILD SUCCESS ✓
- **Files Compiled**: 39 files
- **Time**: 10.169s
- **Warnings**: 1 (system modules path - safe to ignore)

## 📦 Files Changed
1. `client-javafx/src/main/java/com/mstrust/client/exam/service/AutoSaveService.java`
2. `client-javafx/src/main/java/com/mstrust/client/exam/api/ExamApiClient.java`

## 🎓 Lessons Learned
1. **Lombok Getter Naming**: Field `isXxx` (Boolean) → getter `getIsXxx()`, not `isXxx()`
2. **Detailed Logging**: Log ở nhiều levels để dễ debug
3. **Request/Response Logging**: Always log API request body và response
4. **Error Context**: Log enough context để reproduce issue

---
**Status**: ✅ COMPLETE  
**Next**: Test auto-save với detailed logs và verify database
**Author**: K24DTCN210-NVMANH  
**Date**: 24/11/2025 15:28
