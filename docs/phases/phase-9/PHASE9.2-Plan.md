### Phase 9.2 - Exam Creation Wizard

__Approach__: Incremental development với testing sau mỗi component

### STAGE 1: Foundation & Data Models (Test sau mỗi file)

__Files__: 4 files

- ExamWizardData.java - Data holder với validation methods
- ExamCreateRequest.java - DTO for API
- ExamQuestionMapping.java - Question + points + order
- ExamClassAssignment.java - Class assignments

__Testing__: Unit test validation methods

### STAGE 2: API Client (Test với Thunder Client)

__Files__: 1 file

- ExamManagementApiClient.java

__Endpoints sử dụng__:

```java
POST /api/exams                    - Create exam
POST /api/exams/{id}/publish       - Publish
POST /api/exams/{examId}/questions - Add questions
GET  /api/subjects                 - List subjects
GET  /api/subject-classes          - List classes  
GET  /api/question-bank            - Search questions
```

__Testing__: Test từng method với Thunder Client

### STAGE 3: Step 1 - Basic Info (Test UI + validation)

__Files__: 2 files

- exam-wizard-step1.fxml
- ExamWizardStep1Controller.java

__Features__:

- Title, Description TextFields
- Subject, Class ComboBoxes (load from API)
- Exam Purpose ComboBox (enum)
- DatePicker + TimeField (custom) cho Start/End
- Validation: Required fields, End > Start

__Testing__:

- Load form → ✓
- Fill valid data → ✓
- Try invalid data → ✓
- Validation messages → ✓

---

### STAGE 4: Step 2 - Questions với Drag-Drop (COMPLEX - Test kỹ)

__Files__: 3 files

- exam-wizard-step2.fxml (SplitPane layout)
- ExamWizardStep2Controller.java
- DraggableQuestionCell.java (Custom ListCell)

__Layout__:

```javascript
┌─────────────────────────────────────────────┐
│ Search: [_________] [🔍] Filters: [Subject]│
├──────────────────┬──────────────────────────┤
│ Available        │ Selected Questions       │
│ Questions        │                          │
│ ┌──────────────┐ │ ┌──────────────────────┐│
│ │□ Q1 (5pts)   ││ ││1.Q3 (10pts) [↑][↓]││
│ │□ Q2 (10pts)  ││ ││2.Q1 (5pts)  [↑][↓]││
│ │□ Q3 (10pts)  ││ ││                      ││
│ │              ││ ││ Total: 15 points     ││
│ └──────────────┘ │ └──────────────────────┘│
│                  │                          │
│  [Add Selected →]│     [← Remove Selected]  │
└──────────────────┴──────────────────────────┘
```

__Drag-Drop Implementation__:

```java
// Custom Cell cho drag-drop
public class DraggableQuestionCell extends ListCell<QuestionMapping> {
    
    @Override
    protected void updateItem(QuestionMapping item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        
        // Setup drag detected
        setOnDragDetected(event -> {
            if (item != null) {
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(getIndex()));
                db.setContent(content);
                event.consume();
            }
        });
        
        // Setup drag over
        setOnDragOver(event -> {
            if (event.getGestureSource() != this && 
                event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        // Setup drag dropped
        setOnDragDropped(event -> {
            // Reorder logic here
            event.setDropCompleted(true);
            event.consume();
        });
    }
}
```

__Testing__:

- Load available questions → ✓
- Search/Filter works → ✓
- Add questions (checkboxes + button) → ✓
- Drag-drop reordering → ✓
- Edit points inline → ✓
- Total points updates → ✓
- Remove questions → ✓

---

### STAGE 5: Step 3 - Settings (Test UI + defaults)

__Files__: 2 files

- exam-wizard-step3.fxml
- ExamWizardStep3Controller.java

__Controls__:

- Duration Spinner (30-300 minutes, default 60)
- Max Attempts Spinner (1-5, default 1)
- Checkboxes: Shuffle Questions, Shuffle Options, Show Answers, Allow Review
- MonitoringLevel ComboBox (LOW/MEDIUM/HIGH)

__Testing__:

- All controls render → ✓
- Default values set → ✓
- Spinners work → ✓
- Checkboxes toggle → ✓

---

### STAGE 6: Step 4 - Assign Classes (Test API + UI)

__Files__: 2 files

- exam-wizard-step4.fxml
- ExamWizardStep4Controller.java

__Features__:

- Load classes from SubjectApiClient
- CheckBox ListView
- Display student count per class (if available)
- Total students summary
- Validation: At least 1 class selected

__Testing__:

- Classes load → ✓
- Multi-select works → ✓
- Student count displays → ✓
- Validation works → ✓

---

### STAGE 7: Step 5 - Review & Publish (Test summary display)

__Files__: 2 files

- exam-wizard-step5.fxml
- ExamWizardStep5Controller.java

__Summary Sections__:

1.Basic Info (Title, Subject, Class, Times)
2.Questions (Count, Total Points, List)
3.Settings (Duration, Attempts, Flags)
4.Classes (Assigned classes, Total students)

__Actions__:

- [Save as Draft] → POST /api/exams (published=false)
- [Publish Now] → POST /api/exams + POST /api/exams/{id}/publish

__Testing__:

- Summary accurate → ✓
- Save Draft works → ✓
- Publish works → ✓

---

### STAGE 8: Main Wizard Container (Test navigation)

__Files__: 2 files

- exam-wizard.fxml
- ExamWizardController.java

__Features__:

- Progress indicator (1→2→3→4→5)
- Step loading (dynamic content)
- Navigation (Back/Next/Cancel/Finish)
- Per-step validation
- Data persistence across steps

__Navigation Logic__:

```java
public class ExamWizardController {
    private int currentStep = 1;
    private ExamWizardData wizardData = new ExamWizardData();
    
    @FXML
    private void handleNext() {
        // Validate current step
        if (! validateCurrentStep()) {
            return;
        }
        
        // Save current step data
        saveCurrentStepData();
        
        // Move to next
        currentStep++;
        if (currentStep <= 5) {
            loadStep(currentStep);
            updateProgressIndicator();
            updateButtons();
        }
    }
    
    private void loadStep(int step) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-wizard-step" + step + ".fxml")
            );
            Node stepView = loader.load();
            
            // Get controller and pass data
            Object controller = loader.getController();
            if (controller instanceof WizardStepController) {
                ((WizardStepController) controller).setWizardData(wizardData);
            }
            
            contentArea.getChildren().setAll(stepView);
        } catch (IOException e) {
            showError("Không thể load bước " + step);
        }
    }
}
```

__Testing__:

- All 5 steps load → ✓
- Navigation works → ✓
- Back button works → ✓
- Progress updates → ✓
- Data persists → ✓
- Validation blocks navigation → ✓

---

### STAGE 9: Integration (Test menu + launch)

__Files__: 3 files modified

- teacher-main.fxml (add menu item)
- TeacherMainController.java (add handler)
- teacher-styles.css (wizard styles)

__Menu Integration__:

```java
@FXML
private void handleExamWizardClick() {
    try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/exam-wizard.fxml")
        );
        Parent wizardView = loader.load();
        
        ExamWizardController controller = loader.getController();
        controller.initialize(
            examApiClient,
            subjectApiClient, 
            questionBankApiClient,
            primaryStage
        );
        
        contentArea.getChildren().setAll(wizardView);
        highlightSelectedMenu(examWizardButton);
    } catch (IOException e) {
        showError("Không thể mở Exam Wizard", e.getMessage());
    }
}
```

__Testing__:

- Menu item appears → ✓
- Click opens wizard → ✓
- Wizard initializes → ✓

---

### STAGE 10: End-to-End Testing

__Full Workflow Test__:

1.Login as Teacher → ✓
2.Click "Tạo Đề Thi" menu → ✓
3.__Step 1__: Fill basic info → Next → ✓
4.__Step 2__: Search questions, add 5 questions, drag reorder, set points → Next → ✓
5.__Step 3__: Set duration 90min, attempts 2, enable shuffle → Next → ✓
6.__Step 4__: Select 2 classes → Next → ✓
7.__Step 5__: Review summary → Save Draft → ✓
8.Verify exam created in database → ✓
9.Re-open wizard → Complete step 5 → Publish → ✓
10.Verify exam published → ✓

__Bug Fixes__: Fix any issues found

---

### STAGE 11: Documentation

__Files__: 1 file

- docs/phases/phase-9/PHASE9.2-EXAM-WIZARD-COMPLETE.md

__Content__:

- Implementation summary
- Features completed
- Testing results
- Known issues
- Future enhancements


## FINAL DELIVERABLES

### Files Created: ~25-28 files

__DTOs (4)__:

- ExamWizardData.java
- ExamCreateRequest.java
- ExamQuestionMapping.java
- ExamClassAssignment.java

__API (1)__:

- ExamManagementApiClient.java

__FXML Views (6)__:

- exam-wizard.fxml
- exam-wizard-step1.fxml
- exam-wizard-step2.fxml
- exam-wizard-step3.fxml
- exam-wizard-step4.fxml
- exam-wizard-step5.fxml

__Controllers (7)__:

- ExamWizardController.java
- ExamWizardStep1Controller.java
- ExamWizardStep2Controller.java
- ExamWizardStep3Controller.java
- ExamWizardStep4Controller.java
- ExamWizardStep5Controller.java
- DraggableQuestionCell.java

__Modified (3)__:

- teacher-main.fxml
- TeacherMainController.java
- teacher-styles.css

__Documentation (1)__:

- PHASE9.2-EXAM-WIZARD-COMPLETE.md

---

## ⚙️ TECHNICAL DETAILS

### Drag-Drop trong JavaFX:

Con sẽ implement đúng chuẩn JavaFX với:

- `setOnDragDetected()` - Bắt đầu drag
- `setOnDragOver()` - Hover effect
- `setOnDragDropped()` - Drop và reorder
- `Dragboard` + `ClipboardContent` - Data transfer
- Visual feedback khi drag (shadow effect)

### Time Input Solution:

```java
// Custom TimeField component
public class TimeField extends HBox {
    private Spinner<Integer> hourSpinner;  // 0-23
    private Spinner<Integer> minuteSpinner; // 0-59
    
    public LocalTime getTime() {
        return LocalTime.of(
            hourSpinner.getValue(),
            minuteSpinner.getValue()
        );
    }
}
```

### Validation Strategy:

- Client-side validation trước khi Next
- Visual feedback (red borders + error labels)
- Alert dialogs cho critical errors
- Backend validation khi save/publish

