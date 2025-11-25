# Phase 8.6 - BugFix: Auto-Save Transaction Rollback Issue

**Ngày**: 24/11/2025 16:04
**Trạng thái**: ĐÃ SỬA - CHỜ TESTING
**Mức độ**: 🔴 CRITICAL

## 📋 Vấn Đề

### Hiện Tượng
- Client logs show: API call SUCCESS (HTTP 200)
- Backend logs show: INSERT queries executed
- **Database query returns EMPTY** - Data không persist!
- Auto-save và manual save đều bị ảnh hưởng

### Root Cause Analysis

```
CLIENT → API SUCCESS 200
   ↓
BACKEND → answerRepository.save() → INSERT executed
   ↓
BACKEND → submissionRepository.save() → EXCEPTION thrown
   ↓
TRANSACTION ROLLBACK → All changes lost
   ↓
DATABASE → Empty (no data persisted)
```

**Nguyên nhân**: Trong method `ExamTakingService.saveAnswer()`:

```java
// Line 228-238
answer = answerRepository.save(answer);  // ✅ Save thành công

// Update submission tracking
submission.setLastSavedAt(now);
Integer currentCount = submission.getAutoSaveCount();
submission.setAutoSaveCount(currentCount != null ? currentCount + 1 : 1);
submission = submissionRepository.save(submission);  // ❌ FAIL → ROLLBACK TRANSACTION
```

Exception có thể do:
- Optimistic Locking Failure
- Null pointer trong submission tracking fields
- Concurrent modification issues

## 🔧 Giải Pháp Implemented

### 1. Added Exception Handling

Wrap submission update trong try-catch để:
- Answer vẫn được save ngay cả khi submission tracking fail
- Log chi tiết lỗi để debug
- Không rollback toàn bộ transaction

```java
answer = answerRepository.save(answer);
log.info("[SaveAnswer] StudentAnswer saved successfully - ID: {}, QuestionId: {}", 
    answer.getId(), answer.getQuestion().getId());

// Update submission tracking với exception handling
try {
    submission.setLastSavedAt(now);
    Integer currentCount = submission.getAutoSaveCount();
    submission.setAutoSaveCount(currentCount != null ? currentCount + 1 : 1);
    submission = submissionRepository.save(submission);
    log.info("[SaveAnswer] ExamSubmission updated - ID: {}, AutoSaveCount: {}", 
        submission.getId(), submission.getAutoSaveCount());
} catch (Exception e) {
    log.error("[SaveAnswer] CRITICAL: Failed to update submission tracking - ID: {}. " +
        "Answer was saved but submission tracking failed! Error: {}", 
        submission.getId(), e.getMessage(), e);
    // Don't throw - answer is already saved, just tracking failed
}
```

### 2. Added Detailed Logging

Để track transaction flow:
- Log khi answer save thành công (với ID)
- Log khi submission update thành công  
- Log ERROR nếu submission update fail (với stack trace)

## 📊 Testing Required

### Test Steps

1. **Start Backend với Logging Mới**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Test Auto-Save trên Client**
   - Làm bài thi
   - Để auto-save chạy hoặc click "Lưu câu trả lời"
   - Xem backend logs

3. **Check Backend Logs**
   Tìm các dòng:
   ```
   [SaveAnswer] StudentAnswer saved successfully - ID: xxx, QuestionId: xxx
   [SaveAnswer] ExamSubmission updated - ID: xxx, AutoSaveCount: xxx
   ```
   
   Hoặc nếu có lỗi:
   ```
   [SaveAnswer] CRITICAL: Failed to update submission tracking...
   ```

4. **Verify Database**
   ```sql
   SELECT * FROM student_answers 
   WHERE submission_id = xxx 
   ORDER BY last_saved_at DESC;
   ```

### Expected Results

✅ **Success Case**:
- Backend logs show both save operations successful
- Database có data persistent
- Auto-save count tăng đều

⚠️ **Partial Success Case** (nếu submission tracking fail):
- Backend logs show CRITICAL error
- Database CÓ student_answers (vì answer save thành công)
- Database KHÔNG CÓ updated autoSaveCount (tracking field)
- Application vẫn hoạt động bình thường

## 📝 Files Modified

1. `backend/src/main/java/com/mstrust/exam/service/ExamTakingService.java`
   - Added exception handling cho submission update
   - Added detailed logging
   - Prevent transaction rollback

## 🎯 Next Steps

1. ✅ Backend compiled successfully
2. ✅ Server running với logging mới
3. ⏳ **CHỜ**: Cụ Mạnh test và provide backend logs
4. ⏳ Analyze logs để confirm root cause chính xác
5. ⏳ Apply final fix nếu cần

## 🔍 Debug Info

Nếu vẫn thấy transaction rollback sau fix này, check:

1. **ExamSubmission Entity** - có @Version field không?
2. **Concurrent Updates** - nhiều requests cùng update submission?
3. **Database Constraints** - có constraint nào bị vi phạm?
4. **Spring Transaction Config** - propagation level?

---
**Author**: K24DTCN210-NVMANH  
**Status**: Waiting for test results from Cụ Mạnh
