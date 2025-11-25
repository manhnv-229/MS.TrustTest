# Phase 8.6 - Bugfix: Double API Call (COMPLETE)

**Date:** 24/11/2025 13:43  
**Author:** K24DTCN210-NVMANH

## 🐛 Vấn Đề

Khi student click "Bắt đầu làm bài", hệ thống gọi `startExam()` API **2 LẦN**:

1. **Lần 1:** ExamListController.startExamSession() → SUCCESS
2. **Lần 2:** ExamTakingController.initializeExam() → ERROR "Maximum attempts reached"

### Log Minh Chứng

```
[Thread-6] INFO  - Calling startExam API for exam: 100
[Thread-6] INFO  - Started exam 100. SubmissionId: 25  ← SUCCESS lần 1
[JavaFX]  INFO  - Successfully navigated to exam taking screen
[Thread-7] ERROR - Failed to start exam. Status: 400    ← LỖI lần 2
                  "Maximum attempts reached (1)"
```

### Root Cause

**ExamListController** đã call `startExam()` API và nhận được `StartExamResponse`, nhưng sau đó:

```java
// ExamListController - Line 305
controller.initializeExam(exam.getExamId(), authToken);
```

**ExamTakingController.initializeExam()** lại call `startExam()` API lần nữa:

```java
// ExamTakingController - Line 62
StartExamResponse response = apiClient.startExam(examId);  // ← Double call!
```

## ✅ Giải Pháp

### 1. ExamListController - Pass StartExamResponse

```java
// BEFORE (Line 305)
controller.initializeExam(exam.getExamId(), authToken);

// AFTER
controller.initializeExamWithResponse(response, authToken);
```

### 2. ExamTakingController - New Method

Thêm method `initializeExamWithResponse()` nhận `StartExamResponse` đã có:

```java
/* ---------------------------------------------------
 * Initialize exam với StartExamResponse ĐÃ CÓ từ ExamListController
 * NEW method để tránh double API call (Phase 8.6 bugfix)
 * @param response StartExamResponse from ExamListController's API call
 * @param authToken Bearer token
 * @author: K24DTCN210-NVMANH (24/11/2025 13:42)
 * --------------------------------------------------- */
public void initializeExamWithResponse(StartExamResponse response, String authToken) {
    this.apiClient = new ExamApiClient(authToken);
    
    // Show loading
    showLoading("Đang tải câu hỏi...");
    
    new Thread(() -> {
        try {
            // 1. SKIP startExam() API - đã có response rồi!
            
            // 2. Load questions (GET /api/exam-taking/questions/{submissionId})  
            List<QuestionDTO> questions = apiClient.getQuestionsForSubmission(
                response.getSubmissionId()
            );
            
            // 3. Create ExamSession model
            examSession = new ExamSession();
            examSession.setSubmissionId(response.getSubmissionId());
            examSession.setExamTitle(response.getExamTitle());
            examSession.setQuestions(questions);
            examSession.setRemainingSeconds(response.getRemainingSeconds().longValue());
            examSession.setCurrentQuestionIndex(0);
            
            // 4. Initialize UI on JavaFX thread
            Platform.runLater(() -> {
                try {
                    initializeComponents(response);
                    initializeAutoSaveServices(); // Phase 8.4
                    initializeFullScreenSecurity(); // Phase 8.6
                    displayCurrentQuestion();
                    hideLoading();
                } catch (Exception e) {
                    showError("Lỗi khởi tạo UI", e.getMessage());
                }
            });
            
        } catch (IOException e) {
            Platform.runLater(() -> {
                showError("Lỗi tải câu hỏi", e.getMessage());
                hideLoading();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> {
                showError("Lỗi tải câu hỏi", "Bị gián đoạn: " + e.getMessage());
                hideLoading();
            });
        }
    }).start();
}
```

### 3. Deprecate Old Method

```java
@Deprecated
public void initializeExam(Long examId, String authToken) {
    // Keep for backward compatibility but mark as deprecated
}
```

## 📊 Flow Cải Thiện

### BEFORE (❌ Double Call)
```
ExamListController
  ↓
  startExam() API ← Call lần 1
  ↓
  navigate to ExamTakingController
  ↓
ExamTakingController.initializeExam()
  ↓
  startExam() API ← Call lần 2 (ERROR!)
```

### AFTER (✅ Single Call)
```
ExamListController
  ↓
  startExam() API ← Call DUY NHẤT
  ↓
  receive StartExamResponse
  ↓
  navigate to ExamTakingController
  ↓
ExamTakingController.initializeExamWithResponse(response)
  ↓
  Use existing response (NO API call)
  ↓
  Load questions only
```

## 🔧 Files Modified

### 1. `ExamListController.java`

**Line 305:** Changed method call

```java
// Line 305 in navigateToExamScreen()
controller.initializeExamWithResponse(response, authToken);
```

### 2. `ExamTakingController.java`

**New method added:** `initializeExamWithResponse()`

**Line 100-150:** Complete new method implementation

## ✅ Benefits

1. **Performance:** Giảm 1 API call không cần thiết
2. **Reliability:** Tránh lỗi "Maximum attempts reached"
3. **Logic:** ExamListController call API 1 lần → pass response
4. **Clean:** Separation of concerns rõ ràng

## 🧪 Testing

### Scenario 1: Normal Flow
1. Login → Exam List
2. Click "Bắt đầu làm bài"
3. ✅ API called 1 lần duy nhất
4. ✅ Navigate to exam screen thành công

### Scenario 2: Max Attempts Error
1. Student đã thi 1 lần (maxAttempts = 1)
2. Click "Bắt đầu làm bài" lần 2
3. ✅ Error dialog hiển thị ngay tại Exam List
4. ✅ KHÔNG navigate to exam screen

### Scenario 3: Active Submission
1. Student có submission đang active
2. Click "Bắt đầu làm bài"
3. ✅ Warning dialog hiển thị
4. ✅ KHÔNG navigate to exam screen

## 📝 Compilation Result

```bash
[INFO] BUILD SUCCESS
[INFO] Compiling 39 source files
[INFO] Total time: 7.039 s
```

## 🎯 Kết Luận

Bug **double API call** đã được fix hoàn toàn:
- ✅ ExamListController call `startExam()` API 1 lần
- ✅ Pass `StartExamResponse` cho ExamTakingController
- ✅ ExamTakingController dùng response có sẵn
- ✅ Không còn error "Maximum attempts reached" do double call

---

**Status:** ✅ COMPLETE  
**Next:** Continue Phase 8.6 - Exit Protection & Polish
