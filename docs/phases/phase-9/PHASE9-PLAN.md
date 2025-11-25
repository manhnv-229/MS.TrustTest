# Phase 9: Exam Management UI - KẾ HOẠCH TỔNG THỂ

**Document Type**: Implementation Plan  
**Status**: 🚀 ACTIVE DEVELOPMENT  
**Created**: 25/11/2025 21:00  
**Author**: K24DTCN210-NVMANH

---

## 🎯 OVERVIEW

**Phase 9** triển khai giao diện quản lý đề thi và ngân hàng câu hỏi cho **Teacher và Admin**.

### Objectives
1. Question Bank Management UI (Quản lý Ngân hàng Câu hỏi)
2. Exam Creation Wizard (Tạo đề thi 5 bước)
3. Exam List & Management (Danh sách đề thi)

### Scope
- **Duration**: 7 ngày (1 tuần)
- **Priority**: 🟡 HIGH
- **Dependencies**: Phase 8 ✅ (Exam Taking UI complete)
- **Target Users**: Teacher, Admin

---

## 📋 IMPLEMENTATION STEPS

### Bước 1: Question Bank Management (2-3 ngày)
**Status**: ⏳ IN PROGRESS

#### 1.1. Main Layout & Navigation
**Files to Create**:
- `teacher-main.fxml` - Main layout với sidebar navigation
- `TeacherMainController.java` - Navigation logic
- `teacher-styles.css` - Teacher-specific styling

**Layout Design**:
```
┌─────────────────────────────────────────────────────────────────┐
│ MS.TrustTest - Teacher Dashboard             [User] [Logout]   │
├─────────────────────────────────────────────────────────────────┤
│ ┌───────────┬─────────────────────────────────────────────────┐ │
│ │ MENU      │ CONTENT AREA                                    │ │
│ │           │                                                 │ │
│ │ 📚 Quản lý │ (Dynamic content loaded here)                   │ │
│ │ Ngân hàng  │                                                 │ │
│ │ Câu hỏi   │                                                 │ │
│ │           │                                                 │ │
│ │ 📝 Quản lý │                                                 │ │
│ │ Đề thi    │                                                 │ │
│ │           │                                                 │ │
│ │ ✍️ Chấm bài│                                                 │ │
│ │           │                                                 │ │
│ │ 📊 Giám sát│                                                 │ │
│ │ Thi       │                                                 │ │
│ │           │                                                 │ │
│ │ ⚙️ Cài đặt │                                                 │ │
│ │ (Admin)   │                                                 │ │
│ └───────────┴─────────────────────────────────────────────────┘ │
│ Status: Connected | Last sync: 21:00                           │
└─────────────────────────────────────────────────────────────────┘
```

**Menu Items**:
- **Teacher Role**: Question Bank, Exams, Grading, Monitoring
- **Admin Role**: + Users, Organizations, System Config

#### 1.2. Question List View
**Files to Create**:
- `question-bank.fxml` - Question list layout
- `QuestionBankController.java` - List controller
- `QuestionBankService.java` - API service

**Features**:
- Table view với columns: ID, Content (preview), Type, Subject, Difficulty, Points
- Filters: Subject, Type, Difficulty
- Search box (keyword)
- Pagination (20 items/page)
- Toolbar: [+ New Question] [Refresh] [Search]
- Row actions: [Edit] [Delete] [Preview]

#### 1.3. Question Editor Dialog
**Files to Create**:
- `question-editor-dialog.fxml` - Editor layout
- `QuestionEditorController.java` - Editor logic
- `QuestionTypeSelector.java` - Type selector component
- `QuestionOptionsEditor.java` - Dynamic options editor

**Features**:
- Question Type selector (8 types)
- **HTMLEditor** for question content
- Dynamic options based on type:
  - MULTIPLE_CHOICE: 4 options, radio select correct
  - MULTIPLE_SELECT: 4+ options, checkboxes
  - TRUE_FALSE: 2 options
  - ESSAY: No options
  - SHORT_ANSWER: No options
  - CODING: Test cases area
  - FILL_IN_BLANK: Blanks input
  - MATCHING: Pairs editor
- Subject selector
- Difficulty selector (EASY/MEDIUM/HARD)
- Points input
- Tags input
- Preview button
- Save/Cancel buttons

**Validation**:
- Content not empty
- At least 1 correct answer (for MCQ types)
- Points > 0
- Subject selected

---

### Bước 2: Exam Creation Wizard (3 ngày)
**Status**: ⏳ PENDING

#### 2.1. Wizard Container
**Files to Create**:
- `exam-wizard.fxml` - Container layout
- `ExamWizardController.java` - Wizard controller
- `ExamWizardData.java` - Data holder

**Features**:
- 5-step navigation (Progress indicator)
- Back/Next/Cancel buttons
- Data persistence across steps
- Validation per step

#### 2.2. Step 1: Basic Info
**File**: `exam-wizard-step1.fxml`

**Fields**:
- Title (required)
- Description
- Subject (ComboBox)
- SubjectClass (ComboBox)
- Exam Purpose (MIDTERM/FINAL/QUIZ/PRACTICE)
- Start Date/Time
- End Date/Time

#### 2.3. Step 2: Questions
**File**: `exam-wizard-step2.fxml`

**Layout**: Split view
- Left: Search questions
- Right: Selected questions

**Features**:
- Question search với filters
- Drag-drop reordering (or ↑↓ buttons)
- Edit points per question
- Total points display
- Minimum 1 question validation

#### 2.4. Step 3: Settings
**File**: `exam-wizard-step3.fxml`

**Fields**:
- Duration (minutes)
- Max Attempts (1-5)
- Shuffle Questions (checkbox)
- Shuffle Options (checkbox)
- Show Correct Answers (checkbox)
- Allow Review (checkbox)
- Monitoring Level (LOW/MEDIUM/HIGH)

#### 2.5. Step 4: Assign to Classes
**File**: `exam-wizard-step4.fxml`

**Features**:
- Checkbox list of classes
- Student count per class
- Total students preview
- Minimum 1 class required

#### 2.6. Step 5: Review & Publish
**File**: `exam-wizard-step5.fxml`

**Features**:
- Summary của tất cả info
- [Save as Draft] button
- [Publish Now] button

---

### Bước 3: Exam List & Management (2 ngày)
**Status**: ⏳ PENDING

#### 3.1. Exam List View
**Files to Create**:
- `exam-list.fxml` - Grid layout
- `ExamListController.java` - List controller
- `exam-card.fxml` - Card template
- `ExamCardController.java` - Card controller

**Features**:
- Grid view (3-4 columns)
- Filters: Subject, Status, Class
- Sort: Created date, Start time
- Status badges (DRAFT/UPCOMING/ONGOING/COMPLETED)

#### 3.2. Exam Card
**Display**:
- Title, Subject, Class
- Start date/time, Duration
- Status badge
- Student count, Question count
- Actions: Edit, Duplicate, Publish, Delete

#### 3.3. Edit Exam
**Reuse**: ExamWizardController với EDIT mode
- Load existing data
- Pre-fill all steps
- Update instead of create

---

## 📁 FILE STRUCTURE

```
client-javafx/src/main/java/com/mstrust/client/
├── teacher/
│   ├── controller/
│   │   ├── TeacherMainController.java
│   │   ├── QuestionBankController.java
│   │   ├── QuestionEditorController.java
│   │   ├── ExamWizardController.java
│   │   ├── ExamListController.java
│   │   └── ExamCardController.java
│   ├── component/
│   │   ├── QuestionTypeSelector.java
│   │   ├── QuestionOptionsEditor.java
│   │   ├── QuestionSearchPanel.java
│   │   └── ExamCardComponent.java
│   ├── service/
│   │   ├── QuestionBankService.java
│   │   ├── ExamManagementService.java
│   │   └── ValidationService.java
│   └── dto/
│       ├── ExamWizardData.java
│       └── ExamCardData.java
│
└── resources/
    ├── view/
    │   ├── teacher-main.fxml
    │   ├── question-bank.fxml
    │   ├── question-editor-dialog.fxml
    │   ├── exam-wizard.fxml
    │   ├── exam-wizard-step1.fxml
    │   ├── exam-wizard-step2.fxml
    │   ├── exam-wizard-step3.fxml
    │   ├── exam-wizard-step4.fxml
    │   ├── exam-wizard-step5.fxml
    │   ├── exam-list.fxml
    │   └── exam-card.fxml
    └── css/
        └── teacher-styles.css
```

**Total Files**: ~26 files

---

## 🔗 BACKEND APIs (Already Exists)

### Question Bank APIs (Phase 4)
- GET `/api/question-bank` - List with filters
- POST `/api/question-bank` - Create
- GET `/api/question-bank/{id}` - Get by ID
- PUT `/api/question-bank/{id}` - Update
- DELETE `/api/question-bank/{id}` - Delete
- GET `/api/question-bank/statistics/{subjectId}` - Stats

### Exam Management APIs (Phase 4)
- GET `/api/exams` - List with filters
- POST `/api/exams` - Create
- GET `/api/exams/{id}` - Get by ID
- PUT `/api/exams/{id}` - Update
- DELETE `/api/exams/{id}` - Delete
- POST `/api/exams/{id}/publish` - Publish
- POST `/api/exams/{id}/unpublish` - Unpublish
- POST `/api/exams/{examId}/questions` - Add questions
- PUT `/api/exams/{examId}/questions/reorder` - Reorder

### Organization APIs (Phase 3)
- GET `/api/subjects` - List subjects
- GET `/api/subject-classes` - List classes

---

## ✅ SUCCESS CRITERIA

### Functional
- ✅ Teacher/Admin có thể tạo câu hỏi với 8 types
- ✅ HTMLEditor hoạt động tốt
- ✅ Wizard validate đầy đủ
- ✅ Exam list hiển thị đúng
- ✅ Edit exam works
- ✅ Publish/Unpublish works

### Performance
- ✅ Question list < 500ms
- ✅ Search < 200ms
- ✅ Wizard step < 100ms
- ✅ Save exam < 1s

### UX
- ✅ Consistent styling
- ✅ Loading indicators
- ✅ Validation feedback
- ✅ Confirmation dialogs

---

## 📅 TIMELINE

**Week 1**:
- Day 1-2: Main Layout + Question List
- Day 3-4: Question Editor
- Day 5-6: Exam Wizard (Steps 1-3)
- Day 7: Exam Wizard (Steps 4-5) + Exam List

**Total**: 7 ngày

---

## 🎨 UI/UX DESIGN

### Color Scheme
- Primary: #2196F3 (Blue)
- Success: #4CAF50 (Green)
- Warning: #FF9800 (Orange)
- Danger: #F44336 (Red)
- Sidebar: #263238 (Dark gray)

### Spacing
- Padding: 10px, 15px, 20px
- Margin: 5px, 10px
- Border radius: 5px

### Icons
- Unicode emoji + Font Awesome style
- Consistent với Phase 8

---

## 📝 NOTES

### NOT IN SCOPE (Phase 9)
- ❌ Import/Export questions (Enhancement sau)
- ❌ Advanced question templates
- ❌ Question analytics
- ❌ Exam templates

### Future Enhancements
- Import từ Excel/JSON
- Export to Word/PDF
- Question duplication
- Bulk operations
- Advanced filtering

---

**Status**: 🚀 READY TO START  
**Last Updated**: 25/11/2025 21:00  
**Author**: K24DTCN210-NVMANH
