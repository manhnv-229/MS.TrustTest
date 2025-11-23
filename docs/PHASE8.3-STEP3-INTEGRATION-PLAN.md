# Phase 8.3 - Step 3: Integration & Testing Plan

**Date:** 23/11/2025 14:18  
**Status:** 🔄 IN PROGRESS

---

## 🎯 Objectives

### Step 3A: Integration (Kết nối ExamListController → ExamTakingController)
1. Add navigation method trong ExamListController
2. Load exam-taking.fxml khi click "Bắt đầu làm bài"
3. Pass examId + authToken sang ExamTakingController
4. Handle Scene transition

### Step 3B: Testing
1. Manual UI testing (nếu có backend ready)
2. Component unit tests (optional)
3. Integration testing checklist
4. Document test results

---

## 📋 Step 3A: Integration Tasks

### Task 1: Update ExamListController
**File:** `client-javafx/src/main/java/com/mstrust/client/exam/controller/ExamListController.java`

**Changes Needed:**
```java
// Add import
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Add method to handle "Bắt đầu làm bài" button
private void startExamSession(ExamInfoDTO exam) {
    try {
        // 1. Load FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/exam-taking.fxml")
        );
        Parent root = loader.load();
        
        // 2. Get controller
        ExamTakingController controller = loader.getController();
        
        // 3. Initialize exam
        controller.initializeExam(exam.getId(), this.authToken);
        
        // 4. Switch scene
        Stage stage = (Stage) container.getScene().getWindow();
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(
            getClass().getResource("/css/exam-common.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setTitle("Làm bài thi: " + exam.getTitle());
        
    } catch (Exception e) {
        showError("Lỗi", "Không thể mở giao diện làm bài: " + e.getMessage());
        e.printStackTrace();
    }
}
```

**Update existing button handler:**
```java
// In createExamCard() method, update the startButton onAction:
startButton.setOnAction(e -> {
    // Show confirmation dialog
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Xác nhận");
    confirm.setHeaderText("Bắt đầu làm bài thi?");
    confirm.setContentText(
        "Đề thi: " + exam.getTitle() + "\n" +
        "Thời gian: " + exam.getDurationMinutes() + " phút\n\n" +
        "Bạn có chắc chắn muốn bắt đầu?"
    );
    
    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        startExamSession(exam);
    }
});
```

### Task 2: Add CSS for exam-taking screen
**File:** Already exists in `exam-common.css`, verify it's loaded

### Task 3: Test Scene Navigation
- Click exam card → Confirmation dialog
- Confirm → Load exam-taking screen
- Timer starts automatically
- Questions loaded
- Palette shows all questions

---

## 📋 Step 3B: Testing Checklist

### 1. Component-Level Testing

#### TimerComponent
- [ ] Timer counts down correctly
- [ ] Color changes at thresholds (50%, 20%)
- [ ] Warning alerts at 10min, 5min, 1min
- [ ] Auto-submit triggers at 00:00:00
- [ ] Can be paused/resumed (if implemented)

#### QuestionPaletteComponent
- [ ] All questions displayed in grid
- [ ] Click jumps to correct question
- [ ] Status updates when answer saved
- [ ] Current question highlighted
- [ ] Marked questions show orange

#### QuestionDisplayComponent
- [ ] Question content renders correctly
- [ ] All 8 question types display properly:
  - [ ] MULTIPLE_CHOICE (RadioButton)
  - [ ] MULTIPLE_SELECT (CheckBox)
  - [ ] TRUE_FALSE (2 buttons)
  - [ ] ESSAY (TextArea)
  - [ ] SHORT_ANSWER (TextField)
  - [ ] CODING (RichTextFX)
  - [ ] FILL_IN_BLANK (Multiple TextFields)
  - [ ] MATCHING (ComboBox pairs)
- [ ] Mark for review checkbox works
- [ ] Answer persists when navigating away

#### AnswerInputFactory
- [ ] Correct widget created for each type
- [ ] Can extract answer values
- [ ] Handles empty/null answers
- [ ] Pre-fills cached answers

### 2. Controller-Level Testing

#### ExamTakingController
- [ ] Exam initializes successfully
- [ ] Questions load from API
- [ ] Components created and injected
- [ ] Navigation buttons work (Previous/Next)
- [ ] Jump to question works
- [ ] Manual save works
- [ ] Auto-save runs every 30s
- [ ] Submit with confirmation
- [ ] Time expiry triggers auto-submit

### 3. Integration Testing

#### ExamListController → ExamTakingController
- [ ] Scene transition works
- [ ] AuthToken passed correctly
- [ ] ExamId passed correctly
- [ ] No memory leaks
- [ ] Can return to exam list (future)

#### API Integration
- [ ] `POST /api/exam-taking/start/{examId}` succeeds
- [ ] `GET /api/exam-taking/questions/{submissionId}` returns questions
- [ ] `POST /api/exam-taking/save-answer/{submissionId}` saves answers
- [ ] `POST /api/exam-taking/submit/{submissionId}` submits exam
- [ ] Error handling for network failures
- [ ] Token authentication works

### 4. UI/UX Testing

#### Visual
- [ ] Layout responsive to window resize
- [ ] Colors match design (Material Design inspired)
- [ ] Fonts readable
- [ ] Buttons have hover effects
- [ ] Loading states visible

#### Interaction
- [ ] Smooth transitions
- [ ] No UI freezing during API calls
- [ ] Dialogs appear centered
- [ ] Keyboard shortcuts work (if implemented)
- [ ] Focus management correct

### 5. Error Handling

- [ ] Network timeout handled
- [ ] Invalid token handled
- [ ] Exam not found handled
- [ ] Already submitted handled
- [ ] Server error (500) handled
- [ ] Graceful degradation

---

## 🧪 Test Scenarios

### Scenario 1: Happy Path
1. User sees exam list
2. Clicks "Bắt đầu làm bài"
3. Confirms in dialog
4. Exam-taking screen loads
5. Timer starts counting down
6. User answers questions
7. User navigates between questions
8. Manual save works
9. Auto-save works every 30s
10. User submits exam
11. Success message shown

### Scenario 2: Time Runs Out
1. User starts exam
2. Timer reaches 10min → Warning
3. Timer reaches 5min → Warning
4. Timer reaches 1min → Warning
5. Timer reaches 00:00 → Auto-submit
6. Dialog shown: "Hết giờ"
7. Exam auto-submitted

### Scenario 3: Network Error
1. User answers question
2. Click save
3. Network timeout occurs
4. Error message shown
5. Answer cached locally
6. Retry available

### Scenario 4: Navigation
1. User on question 1
2. Clicks Next → Question 2
3. Clicks Previous → Question 1
4. Clicks palette button 5 → Question 5
5. All navigation smooth
6. Answers preserved

---

## 📊 Test Results Template

### Test Execution Log
```
Date: ___________
Tester: ___________
Environment: Local / Staging / Production

Component Tests:
- TimerComponent: PASS / FAIL
- QuestionPaletteComponent: PASS / FAIL
- QuestionDisplayComponent: PASS / FAIL
- AnswerInputFactory: PASS / FAIL

Controller Tests:
- ExamTakingController: PASS / FAIL
- ExamListController integration: PASS / FAIL

API Integration:
- Start exam: PASS / FAIL
- Load questions: PASS / FAIL
- Save answer: PASS / FAIL
- Submit exam: PASS / FAIL

UI/UX:
- Visual design: PASS / FAIL
- Interaction: PASS / FAIL
- Error handling: PASS / FAIL

Overall Result: PASS / FAIL

Notes:
___________________________________________
___________________________________________
```

---

## 🚀 Implementation Order

### Now (Step 3A)
1. ✅ Update ExamListController with navigation
2. ✅ Test scene transition
3. ✅ Verify components initialize

### Later (Step 3B)
1. Manual testing with backend
2. Document test results
3. Fix any bugs found
4. Create test report

---

## 📝 Notes

- Testing có thể thực hiện manual hoặc automated
- Nếu backend chưa sẵn sàng, có thể mock data
- Focus vào component integration trước
- Performance testing sau khi stable

---

**Next Action:** Implement ExamListController navigation method
**Estimated Time:** 30 minutes
**Dependencies:** None (all components ready)
