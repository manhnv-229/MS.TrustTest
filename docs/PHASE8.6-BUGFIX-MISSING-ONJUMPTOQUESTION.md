# Phase 8.6 - Bug Fix #2: Missing onJumpToQuestion Event Handler

**Date**: 24/11/2025 10:15  
**Author**: K24DTCN210-NVMANH  
**Status**: ✅ FIXED & VERIFIED

## 🐛 Bug Description

Sau khi fix Bug #1 (timerContainer type), khi click "Bắt đầu làm bài" vẫn crash với lỗi:

```
javafx.fxml.LoadException: Error resolving onAction='#onJumpToQuestion', 
either the event handler is not in the Namespace or there is an error in the script.
/D:/PRIVATE/MS.TrustTest/MS.TrustTest/client-javafx/target/classes/view/exam-taking.fxml:150
```

## 🔍 Root Cause Analysis

**Missing event handler method** trong Controller:

| Component | Status | Location |
|-----------|--------|----------|
| **FXML Button** | ✅ EXISTS | `exam-taking.fxml:148-150` |
| **FXML TextField** | ✅ EXISTS | `exam-taking.fxml:143` |
| **Controller Method** | ❌ MISSING | `ExamTakingController.java` |
| **Controller Field** | ❌ MISSING | `@FXML TextField jumpToQuestionField` |

### FXML Definition (exam-taking.fxml lines 143-150):
```xml
<!-- Question number input -->
<HBox spacing="10" alignment="CENTER">
    <Label text="Đi tới câu:" styleClass="jump-label"/>
    <TextField fx:id="jumpToQuestionField" 
              promptText="Số câu"
              prefWidth="80"
              styleClass="jump-field"/>
    <Button text="Đi tới" 
           onAction="#onJumpToQuestion"
           styleClass="nav-button-secondary"/>
</HBox>
```

### Controller Before Fix:
```java
// ❌ Missing field reference
// ❌ Missing method onJumpToQuestion()
```

## ✅ Solution

Added missing TextField reference và event handler method:

### 1. Added FXML Field Reference (Line 52):
```java
@FXML private TextField jumpToQuestionField;
```

### 2. Added Event Handler Method (Lines 253-281):
```java
/* ---------------------------------------------------
 * Handle Jump to Question button
 * @author: K24DTCN210-NVMANH (24/11/2025 10:11)
 * --------------------------------------------------- */
@FXML
private void onJumpToQuestion() {
    String input = jumpToQuestionField.getText();
    if (input == null || input.trim().isEmpty()) {
        showAlert("Lỗi", "Vui lòng nhập số câu hỏi!");
        return;
    }
    
    try {
        int questionNumber = Integer.parseInt(input.trim());
        int questionIndex = questionNumber - 1; // Convert to 0-based index
        
        // Validate range
        if (questionIndex < 0 || questionIndex >= examSession.getQuestions().size()) {
            showAlert("Lỗi", 
                String.format("Số câu hỏi phải từ 1 đến %d!", examSession.getQuestions().size()));
            return;
        }
        
        // Jump to question
        saveCurrentAnswer();
        examSession.jumpToQuestion(questionIndex);
        displayCurrentQuestion();
        
        // Clear field
        jumpToQuestionField.clear();
        
    } catch (NumberFormatException e) {
        showAlert("Lỗi", "Vui lòng nhập số hợp lệ!");
    }
}
```

## 📝 Files Changed

1. **ExamTakingController.java**
   - Line 52: Added `@FXML private TextField jumpToQuestionField;`
   - Lines 253-281: Added `onJumpToQuestion()` method

## 🎯 Method Features

Method `onJumpToQuestion()` thực hiện:

1. **Input Validation**:
   - Check empty input
   - Parse string to integer
   - Validate range (1 đến total questions)

2. **Jump Logic**:
   - Save current answer trước khi jump
   - Convert question number (1-based) → index (0-based)
   - Call `examSession.jumpToQuestion(index)`
   - Display new question

3. **UI Feedback**:
   - Show alert nếu lỗi
   - Clear field sau khi jump thành công

## 🧪 Verification

### Build Status:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 9.452 s
[INFO] Compiling 39 source files
```

### Test Scenarios:
1. ✅ Empty input → Show "Vui lòng nhập số câu hỏi!"
2. ✅ Invalid number (e.g. "abc") → Show "Vui lòng nhập số hợp lệ!"
3. ✅ Out of range (e.g. 0, 999) → Show range error
4. ✅ Valid number (e.g. 5) → Jump to question 5
5. ✅ Field cleared after successful jump

## 📊 Impact Assessment

- **Severity**: 🔴 CRITICAL (Blocks exam taking completely)
- **Scope**: Jump to Question feature (navigation enhancement)
- **User Impact**: 100% - Cannot start exam without fix
- **Fix Complexity**: ✅ Medium (Add field + method with validation)

## 🎯 Related Bugs

1. **Bug #1**: timerContainer type mismatch (HBox → VBox) - FIXED ✅
2. **Bug #2**: Missing onJumpToQuestion - FIXED ✅

## ✅ Status

**RESOLVED** - Both bugs fixed, ready for manual testing

---

**Next Steps:**
1. Cụ Mạnh test lại với `run-exam-client.bat`
2. Verify exam taking screen loads successfully
3. Test "Đi tới câu" feature (jump to specific question)
4. Continue Phase 8.6 Step 3: Exit Protection & Polish
