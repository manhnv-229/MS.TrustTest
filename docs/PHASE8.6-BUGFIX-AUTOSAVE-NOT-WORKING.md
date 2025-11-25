# 🐛 BUG FIX: Auto-Save Không Hoạt Động

**Ngày**: 24/11/2025 14:50  
**Người thực hiện**: K24DTCN210-NVMANH  
**Mức độ**: 🔴 CRITICAL

## 📋 Mô Tả Bug

User báo 2 vấn đề:
1. ❌ Auto-save không hoạt động (không thấy periodic saves)
2. ❌ Nhấn "Lưu câu trả lời" nhưng data không được lưu vào database

## 🔍 Root Cause Analysis

### Bug 1: AutoSaveService Không Được Notify
```java
// ExamTakingController.java - saveCurrentAnswer()
private void saveCurrentAnswer() {
    // ... cache locally ...
    
    // ❌ PROBLEM: Gọi API trực tiếp, KHÔNG notify AutoSaveService!
    new Thread(() -> {
        apiClient.saveAnswer(examSession.getSubmissionId(), request);
    }).start();
}
```

**Nguyên nhân**: 
- AutoSaveService có method `onAnswerChanged()` để trigger debounced save
- Nhưng controller KHÔNG BAO GIỜ gọi method này!
- Do đó AutoSaveService không biết answer đã thay đổi
- Queue luôn empty → periodic save không có gì để save

### Bug 2: Không Có Answer Change Listeners
- QuestionDisplayComponent render TextField/TextArea để user nhập
- Nhưng KHÔNG có TextProperty listener để detect changes
- Khi user nhập xong, controller phải:
  1. Lắng nghe answer changes
  2. Notify AutoSaveService via `onAnswerChanged()`

## 🔧 Solution Design

### Fix 1: Thêm Answer Change Listener
```java
// ExamTakingController.java
private void displayCurrentQuestion() {
    // ... existing code ...
    
    // NEW: Listen for answer changes
    questionDisplayComponent.setOnAnswerChanged((answer) -> {
        if (autoSaveService != null && autoSaveService.isRunning()) {
            Long questionId = questionDisplayComponent.getCurrentQuestion().getId();
            autoSaveService.onAnswerChanged(questionId, answer);
        }
    });
}
```

### Fix 2: Update QuestionDisplayComponent
```java
// QuestionDisplayComponent.java
private Consumer<String> onAnswerChanged;

public void setOnAnswerChanged(Consumer<String> callback) {
    this.onAnswerChanged = callback;
}

private void setupAnswerInputListener(Node inputNode) {
    if (inputNode instanceof TextField) {
        TextField field = (TextField) inputNode;
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (onAnswerChanged != null) {
                onAnswerChanged.accept(newVal);
            }
        });
    }
    // Similar for TextArea, etc.
}
```

### Fix 3: Update saveCurrentAnswer() Logic
```java
// ExamTakingController.java
private void saveCurrentAnswer() {
    QuestionDTO currentQuestion = questionDisplayComponent.getCurrentQuestion();
    if (currentQuestion == null) return;
    
    String answer = questionDisplayComponent.getCurrentAnswer();
    
    // Cache locally
    if (answer != null && !answer.isEmpty()) {
        answersCache.put(currentQuestion.getId(), answer);
    }
    
    // ✅ NEW: Notify AutoSaveService (will handle queueing & saving)
    if (autoSaveService != null && autoSaveService.isRunning()) {
        autoSaveService.onAnswerChanged(currentQuestion.getId(), answer);
    }
    
    // Update palette
    updatePaletteStatus();
    
    // ❌ REMOVE: Direct API call (AutoSaveService handles this now)
    // apiClient.saveAnswer(...) 
}
```

## 📝 Implementation Steps

1. ✅ Update QuestionDisplayComponent - Add listener callback
2. ✅ Update ExamTakingController - Wire up answer change events  
3. ✅ Update saveCurrentAnswer() - Use AutoSaveService instead of direct API
4. ✅ Test auto-save every 30s
5. ✅ Test debounced save after 3s of typing
6. ✅ Verify database has answers

## ⚠️ Important Notes

- AutoSaveService ĐÃ HOẠT ĐỘNG ĐÚNG - code logic OK
- Vấn đề là controller KHÔNG kết nối với service
- Sau fix, flow sẽ là:
  ```
  User types → TextField listener → onAnswerChanged callback → 
  AutoSaveService.onAnswerChanged() → Queue → Debounced/Periodic save → API
  ```

---
**Status**: 🔄 IN PROGRESS  
**Next**: Implement fixes
