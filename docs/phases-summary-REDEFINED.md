# Tổng Hợp Các Phase - MS.TrustTest (REDEFINED)

**Document Type**: Project Roadmap  
**Status**: 🔄 ACTIVE REDEFINITION  
**Created**: 13/11/2025  
**Redefined**: 21/11/2025 01:30  
**Author**: K24DTCN210-NVMANH

---

## 🎯 EXECUTIVE SUMMARY

### Current Status (21/11/2025)
- **Backend Progress**: 85% Complete ✅
- **Frontend Progress**: 0% Complete ❌
- **Overall Progress**: ~42% (Backend heavy)
- **Time Spent**: ~30 hours (Phases 1-5A Backend)
- **Estimated Remaining**: 10 tuần (2.5 tháng)

### Key Achievements
✅ 107 REST APIs hoạt động  
✅ 16 database migrations  
✅ JWT Authentication & RBAC  
✅ Complete business logic  
✅ Auto-grading engine  
✅ Manual grading workflow  

### Critical Gap Identified
❌ **Không có JavaFX client** → Cần 4-5 tuần cho UI development  
❌ **Thiếu real-time features** → Cần WebSocket infrastructure  
❌ **Anti-cheat monitors chưa có** → Core feature chưa implement  

---

## 📊 REDEFINED PHASES STRUCTURE

### Phase Organization
```
TRACK 1: BACKEND COMPLETION (Phases 5B, 6A)
└── Duration: 2 tuần
    └── WebSocket, Monitoring APIs, Real-time features

TRACK 2: JAVAFX CLIENT (Phases 7-13)
└── Duration: 7 tuần
    ├── Foundation & Auth (1 tuần)
    ├── Exam Taking UI (1.5 tuần) ⭐ Critical
    ├── Management UIs (2 tuần)
    ├── Anti-Cheat Monitors (1.5 tuần) ⭐ Core Feature
    ├── Monitoring Dashboard (1 tuần)
    └── Admin Dashboard (1 tuần)

TRACK 3: TESTING & DEPLOYMENT (Week 10)
└── Integration testing, Bug fixes, Documentation
```

---

## ✅ COMPLETED PHASES (Backend Only)

### Phase 1: Setup & Database Schema ✅
**Status**: COMPLETED  
**Duration**: 2 hours  
**Completed**: 13/11/2025

**Deliverables:**
- ✅ Maven multi-module structure
- ✅ 16 database tables
- ✅ Flyway migrations (V1-V3)
- ✅ Spring Boot configuration
- ✅ MCP Server setup

**Documentation**: `docs/PHASE1-COMPLETED.md`

---

### Phase 2: Authentication & Authorization (Backend) ✅
**Status**: COMPLETED (Backend Only - Missing Login UI)  
**Backend Progress**: 100% ✅  
**UI Progress**: 0% ❌  
**Overall**: 85%  
**Duration**: ~5 hours  
**Completed**: 14/11/2025

**Backend Deliverables:**
- ✅ 16 REST APIs (Auth + User management)
- ✅ JWT token system (24h access, 7d refresh)
- ✅ Multi-login support (student_code/email/phone)
- ✅ RBAC with 5 roles
- ✅ BCrypt password hashing
- ✅ Spring Security configuration
- ✅ Custom UserDetails service
- ✅ Exception handling

**Missing UI:**
- ❌ Login screen (JavaFX)
- ❌ Session management UI
- ❌ User profile UI

**Documentation**: `docs/PHASE2-COMPLETED.md`

---

### Phase 3: Organization Management (Backend) ✅
**Status**: COMPLETED (Backend Only - Missing Management UI)  
**Backend Progress**: 100% ✅  
**UI Progress**: 0% ❌  
**Overall**: 80%  
**Duration**: 2 days  
**Completed**: 15/11/2025

**Backend Deliverables:**
- ✅ 61 REST APIs (Department, Class, Subject, SubjectClass, Users)
- ✅ Complete CRUD operations
- ✅ N:M relationships (Student enrollment)
- ✅ Teacher assignments
- ✅ Advanced search & filtering
- ✅ Statistics APIs
- ✅ Soft delete pattern
- ✅ 11 database migrations (V1-V11)

**Missing UI:**
- ❌ Department management screen
- ❌ Class management screen
- ❌ Subject management screen
- ❌ Student enrollment UI
- ❌ Teacher assignment UI

**Documentation**: 
- `docs/phases/phase-3-organization-management.md`
- `docs/PHASE3-STEP6-COMPLETION-REPORT.md`

---

### Phase 4: Exam Management (Backend) ✅
**Status**: COMPLETED (Backend Only - Missing Exam Creation UI)  
**Backend Progress**: 100% ✅  
**UI Progress**: 0% ❌  
**Overall**: 70%  
**Duration**: 6.5 hours  
**Completed**: 19/11/2025

**Backend Deliverables:**
- ✅ Question Bank system với 8 question types
- ✅ 19 REST APIs (Question Bank + Exam Management)
- ✅ Exam CRUD with computed status
- ✅ Question-Exam association
- ✅ Publish/Unpublish workflow
- ✅ Exam configuration (time, attempts, monitoring level)
- ✅ Database migrations V12-V14

**Question Types Supported:**
1. MULTIPLE_CHOICE - Trắc nghiệm
2. MULTIPLE_SELECT - Nhiều lựa chọn
3. TRUE_FALSE - Đúng/Sai
4. ESSAY - Tự luận
5. SHORT_ANSWER - Câu trả lời ngắn
6. CODING - Lập trình
7. FILL_IN_BLANK - Điền khuyết
8. MATCHING - Nối cặp

**Missing UI:**
- ❌ Question Bank management screen
- ❌ Question editor (rich text, multi-type)
- ❌ Exam creation wizard
- ❌ Exam preview
- ❌ Import/Export UI

**Documentation**: 
- `docs/PHASE4-COMPLETE-SUMMARY.md`
- `docs/PHASE4-TESTING-GUIDE.md`

---

### Phase 5A: Exam Taking & Grading (Backend) ✅
**Status**: COMPLETED (Backend Only - Missing Exam Taking UI)  
**Backend Progress**: 100% ✅  
**UI Progress**: 0% ❌  
**Overall**: 40%  
**Duration**: 8 hours  
**Completed**: 21/11/2025

**Backend Deliverables:**
- ✅ 9 REST APIs (5 exam taking + 4 grading)
- ✅ ExamSubmission & StudentAnswer entities
- ✅ Auto-grading engine (5 question types)
- ✅ Manual grading workflow
- ✅ Randomization system (reproducible seeds)
- ✅ Business rules validation
- ✅ Database migrations V15-V16

**Auto-Grading Support:**
- ✅ MULTIPLE_CHOICE - Compare selected option
- ✅ TRUE_FALSE - Compare boolean
- ✅ MULTIPLE_SELECT - Order-independent comparison
- ✅ FILL_IN_BLANK - Case-insensitive text match
- ✅ MATCHING - Order-independent pair matching

**Manual Grading Required:**
- ESSAY - Teacher review
- SHORT_ANSWER - Teacher review
- CODING - Teacher review

**Missing Critical Features:**
- ❌ Exam taking UI (JavaFX) ⭐
- ❌ Timer countdown component ⭐
- ❌ Auto-save mechanism (30s) ⭐
- ❌ Network reconnection handling ⭐
- ❌ Full-screen exam mode ⭐
- ❌ Question palette UI
- ❌ Progress indicator
- ❌ Grading UI for teachers

**Documentation**: 
- `docs/PHASE5-COMPLETE-SUMMARY.md`
- `docs/PHASE5-EXAM-TAKING-STEP1-COMPLETION.md`
- `docs/PHASE5-GRADING-STEP2.1-COMPLETION.md`

---

## 🚀 UPCOMING PHASES (Backend Completion)

### Phase 5B: Backend Enhanced Features
**Status**: ⏳ NEXT (Ready to Start)  
**Duration**: 1 tuần  
**Priority**: 🔴 Critical  
**Dependencies**: Phase 5A ✅

#### Objectives
Bổ sung các APIs cần thiết cho JavaFX client:
1. WebSocket infrastructure (real-time)
2. Auto-save API
3. Time tracking API
4. Resume exam API
5. Session management

#### Deliverables

**1. WebSocket Configuration**
- Spring WebSocket setup
- STOMP protocol config
- Message broker configuration
- Connection/Disconnection handlers
- Authentication for WebSocket

**2. Real-time APIs**
```
GET  /api/exam-taking/{submissionId}/time-remaining
POST /api/exam-taking/{submissionId}/auto-save
POST /api/exam-taking/{submissionId}/resume
GET  /api/exam-taking/{submissionId}/progress
POST /api/exam-taking/{submissionId}/heartbeat

WS   /topic/exam/{examId}/updates
WS   /queue/student/{studentId}/notifications
WS   /topic/alerts (for teachers)
```

**3. Additional Features**
- In-memory session tracking
- Heartbeat mechanism (keep-alive)
- Session timeout handling
- Progress calculation
- Live statistics

**4. Files to Create** (~15 files)
- `WebSocketConfig.java`
- `WebSocketController.java`
- `ExamSessionTracker.java`
- `HeartbeatScheduler.java`
- `SessionTimeoutHandler.java`
- `TimeRemainingDTO.java`
- `AutoSaveRequest.java`
- `ProgressDTO.java`
- Additional DTOs and services

#### Success Criteria
- ✅ WebSocket connects successfully
- ✅ Real-time messages delivered < 100ms
- ✅ Auto-save works every 30s
- ✅ Session tracking accurate
- ✅ Heartbeat prevents timeout
- ✅ Resume exam after disconnect

**Documentation**: TBD `docs/PHASE5B-BACKEND-ENHANCED.md`

---

### Phase 6A: Monitoring Backend Infrastructure
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🔴 Critical  
**Dependencies**: Phase 5B ✅

#### Objectives
Backend infrastructure cho anti-cheat monitoring system

#### Deliverables

**1. Screenshot Management**
```
POST /api/monitoring/screenshot/upload
GET  /api/monitoring/screenshot/{submissionId}
GET  /api/monitoring/screenshot/{submissionId}/{screenshotId}
```
- File storage service (local/cloud)
- Image compression (JPEG 70%)
- Metadata storage

**2. Activity Logging**
```
POST /api/monitoring/activity/log
GET  /api/monitoring/activity/{submissionId}
GET  /api/monitoring/activity/summary/{submissionId}
```
- Activity types: WINDOW_FOCUS, PROCESS_DETECTED, CLIPBOARD, KEYSTROKE
- Batch upload support
- Timeline queries

**3. Alert System**
```
POST /api/monitoring/alert/create
GET  /api/monitoring/alerts
GET  /api/monitoring/alerts/{id}
PUT  /api/monitoring/alert/{id}/review
DELETE /api/monitoring/alert/{id}

WS   /topic/alerts (real-time to teachers)
WS   /queue/teacher/{teacherId}/alerts
```
- Alert levels: LOW, MEDIUM, HIGH, CRITICAL
- Auto-evaluation rules
- Alert aggregation

**4. Database Schema**
```sql
V17: monitoring_screenshots
  - id, submission_id, file_path, timestamp, metadata

V18: activity_logs  
  - id, submission_id, activity_type, details, timestamp

V19: monitoring_alerts
  - id, submission_id, alert_type, severity, description, 
    reviewed, reviewed_by, reviewed_at
```

**5. Files to Create** (~20 files)
- Entities: `Screenshot.java`, `ActivityLog.java`, `Alert.java`
- Repositories, Services, Controllers
- `FileStorageService.java`
- `AlertEvaluationService.java`
- `MonitoringWebSocketController.java`
- Migrations V17-V19

#### Success Criteria
- ✅ Screenshot upload < 2s
- ✅ Activity logs saved reliably
- ✅ Alerts delivered real-time
- ✅ Storage system scalable
- ✅ Query performance < 500ms

**Documentation**: TBD `docs/PHASE6A-MONITORING-BACKEND.md`

---

## 📱 JAVAFX CLIENT DEVELOPMENT

### Phase 7: JavaFX Foundation & Authentication UI
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🔴 Critical  
**Dependencies**: Phase 5B ✅

#### Objectives
Setup JavaFX project và implement Login UI

#### Deliverables

**1. Project Structure**
```
client/
├── pom.xml (JavaFX 21+, ControlsFX, JFoenix)
├── src/main/java/com/mstrust/client/
│   ├── MsTrustClientApp.java (main entry)
│   ├── config/
│   │   ├── ApiClient.java (HTTP client with JWT)
│   │   ├── WebSocketClient.java (STOMP client)
│   │   └── SessionManager.java (token storage)
│   ├── controller/
│   │   ├── LoginController.java
│   │   └── MainLayoutController.java
│   ├── view/
│   │   ├── LoginView.fxml
│   │   └── MainLayout.fxml
│   ├── model/
│   │   ├── User.java
│   │   └── Session.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── ApiService.java (base class)
│   └── util/
│       ├── AlertUtil.java
│       └── ValidationUtil.java
└── src/main/resources/
    ├── css/
    │   ├── styles.css
    │   └── theme.css
    ├── fxml/
    │   ├── LoginView.fxml
    │   └── MainLayout.fxml
    └── images/
        └── logo.png
```

**2. Login Screen Features**
- Multi-login support (student_code/email/phone)
- Password field với show/hide toggle
- Remember me checkbox (secure token storage)
- Login validation (client-side)
- Loading indicator
- Error messages
- Forgot password link

**3. Main Layout**
- Top bar với user info, role badge, logout button
- Side navigation menu (role-based):
  - Student: Available Exams, My Results
  - Teacher: Exams, Question Bank, Grading, Monitoring
  - Admin: Dashboard, Users, Organizations, System Config
- Content area (central panel)
- Status bar (connection status, notifications)

**4. Core Services**
- `ApiClient`: REST API calls với JWT auto-refresh
- `WebSocketClient`: STOMP connection management
- `SessionManager`: Token storage, auto-login
- `AuthService`: Login, logout, token refresh

#### Files to Create (~15 files)
- Main app + 2 controllers
- 2 FXML layouts
- 4 service classes
- 3 utility classes
- 2 CSS files
- Model classes

#### Success Criteria
- ✅ Login successful với JWT
- ✅ Role-based navigation works
- ✅ Token auto-refresh
- ✅ Remember me works
- ✅ Responsive UI
- ✅ Error handling

**Documentation**: TBD `docs/PHASE7-JAVAFX-FOUNDATION.md`

---

### Phase 8: Exam Taking UI ⭐ CRITICAL
**Status**: ⏳ PLANNED  
**Duration**: 1.5 tuần  
**Priority**: 🔴 CRITICAL (Core Feature)  
**Dependencies**: Phase 7 ✅, Phase 5B ✅

#### Objectives
Implement full-featured exam taking interface cho students

#### Deliverables

**1. Exam List Screen**
```
ExamListView.fxml + ExamListController.java
```
Features:
- Grid/List view của available exams
- Filter: Subject, Status (UPCOMING/ONGOING)
- Sort: Start time, Title
- Exam cards showing:
  - Title, Subject, Class
  - Start/End time
  - Duration
  - Max attempts
  - Status badge
  - "Start Exam" button (if ONGOING)
- Countdown for UPCOMING exams
- Attempted exams history

**2. Exam Taking Interface** ⭐ CRITICAL
```
ExamTakingView.fxml + ExamTakingController.java
```

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ [FULL-SCREEN MODE - F11 to exit]                       │
│ Timer: 01:23:45  |  Student: Nguyễn Văn A  |  [Submit] │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────────────┬───────────────────────────────┐ │
│ │ Question Palette    │ Question Display              │ │
│ │ ┌───┬───┬───┬───┐   │                               │ │
│ │ │ 1 │ 2 │ 3 │ 4 │   │ Câu 1: [Question content]     │ │
│ │ └───┴───┴───┴───┘   │                               │ │
│ │ ┌───┬───┬───┬───┐   │ [Answer options/input area]   │ │
│ │ │ 5 │ 6 │ 7 │ 8 │   │                               │ │
│ │ └───┴───┴───┴───┘   │                               │ │
│ │                     │                               │ │
│ │ Legend:             │                               │ │
│ │ 🟢 Answered         │                               │ │
│ │ 🟡 Marked           │                               │ │
│ │ ⚪ Unanswered       │                               │ │
│ │                     │ [Previous] [Mark] [Next]      │ │
│ └─────────────────────┴───────────────────────────────┘ │
│ Progress: ████████░░ 8/10 answered                      │
└─────────────────────────────────────────────────────────┘
```

**Key Features:**

**A. Timer Component** ⭐
```java
TimerComponent.java
```
- Countdown display (HH:MM:SS)
- Color coding:
  - 🟢 Green: > 50% time left
  - 🟡 Yellow: 20-50% time left
  - 🔴 Red: < 20% time left
- Warnings at 10min, 5min, 1min
- Auto-submit at 00:00:00
- Sync với server time

**B. Question Display** ⭐
```java
QuestionDisplayComponent.java
AnswerInputFactory.java
```
- Rich text display (HTML support)
- Image display (if question has images)
- Code syntax highlighting (for coding questions)
- Answer inputs per question type:
  1. **Multiple Choice**: Radio buttons
  2. **Multiple Select**: Checkboxes
  3. **True/False**: Two buttons
  4. **Essay**: Large text area với rich text
  5. **Short Answer**: Single line text field
  6. **Coding**: Code editor với syntax highlight
  7. **Fill in Blank**: Multiple text fields
  8. **Matching**: Drag-drop or dropdowns

**C. Question Palette (Sidebar)** ⭐
```java
QuestionPaletteComponent.java
```
- Grid of question numbers
- Color coding:
  - 🟢 Green: Answered
  - 🟡 Yellow: Marked for review
  - ⚪ Gray: Unanswered
  - 🔵 Blue: Current question
- Click to jump to question
- "Mark for Review" toggle

**D. Navigation**
- Previous/Next buttons
- Mark for Review button
- Submit button (với confirmation)
- Progress bar (questions answered)

**E. Auto-Save Mechanism** ⭐
```java
AutoSaveService.java
```
- Auto-save every 30 seconds
- Save on question change
- Visual indicator: "Saving..." → "Saved ✓"
- Queue saves if offline
- Retry failed saves

**F. Network Handling** ⭐
```java
NetworkMonitor.java
ConnectionRecoveryService.java
```
- Detect connection loss
- Show offline indicator
- Queue answers locally (SQLite)
- Auto-reconnect with exponential backoff
- Resume exam on reconnect:
  - Sync local answers
  - Update timer
  - Show reconnection success

**G. Full-Screen Mode**
- F11 to toggle
- Prevent alt+tab (attempt to)
- Show exit warning
- Minimize button disabled

**3. Submit Confirmation Dialog**
```java
SubmitConfirmationDialog.java
```
- Review summary:
  - Total questions
  - Answered count
  - Unanswered count
  - Marked for review count
- Warning for unanswered questions
- "Are you sure?" confirmation
- Cannot return after submit

**4. Result View** (after submit)
```java
ExamResultView.fxml
```
- Auto-graded score (if no manual questions)
- "Pending grading" message (if has manual questions)
- Correct answers (if exam allows show_correct_answers)
- Time spent
- Attempt number
- Pass/Fail status

#### Files to Create (~25 files)
- 4 FXML layouts
- 8 Controllers
- 7 Component classes
- 4 Service classes
- 2 Utility classes
- CSS styling

#### Technical Challenges
1. **Timer accuracy**: Sync with server, handle network delays
2. **Auto-save reliability**: Queue system, retry logic
3. **Network reconnection**: State recovery, data sync
4. **Full-screen enforcement**: Platform-specific hacks
5. **Rich text editing**: HTML rendering in JavaFX
6. **Code editor**: Syntax highlighting for multiple languages

#### Success Criteria
- ✅ Timer counts down accurately (±1s)
- ✅ Auto-save works every 30s
- ✅ All 8 question types render correctly
- ✅ Network reconnection successful < 30s
- ✅ No data loss on crash/disconnect
- ✅ Full-screen mode works (best effort)
- ✅ Submit confirmation prevents accidents
- ✅ Performance: Smooth scrolling, < 100ms response

**Documentation**: TBD `docs/PHASE8-EXAM-TAKING-UI.md`

---

### Phase 9: Exam Management UI
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🟡 High  
**Dependencies**: Phase 8 ✅

#### Objectives
Teacher interface for managing questions và exams

#### Deliverables

**1. Question Bank Management**
- List view với filters (subject, difficulty, type)
- Create/Edit question dialog
- Question type selector
- Rich text editor for question content
- Options management (add/remove/reorder)
- Correct answer marking
- Points assignment
- Tags input
- Import questions (Excel/JSON)
- Export questions
- Bulk delete

**2. Exam Creation Wizard**
```
ExamWizard.fxml (multi-step)
```
- **Step 1**: Basic Info
  - Title, Description
  - Subject, Class selection
  - Exam purpose dropdown
  - Start/End datetime pickers
- **Step 2**: Questions
  - Search question bank
  - Select questions (drag-drop)
  - Reorder questions
  - Set points per question
  - Preview question
- **Step 3**: Settings
  - Duration (minutes)
  - Max attempts
  - Shuffle questions toggle
  - Shuffle options toggle
  - Show correct answers toggle
  - Allow review toggle
  - Monitoring level (Low/Medium/High)
- **Step 4**: Assign to Classes
  - Select multiple classes
  - Preview assigned students count
- **Step 5**: Review & Publish
  - Summary view
  - Publish button

**3. Exam List Screen**
- Grid view của exams
- Filter: Subject, Status, Class
- Sort: Created date, Start time
- Exam cards showing status badges
- Actions: Edit, Delete, Publish, Unpublish, Duplicate

#### Files to Create (~20 files)

#### Success Criteria
- ✅ Create exam < 5 minutes
- ✅ Question search fast (< 200ms)
- ✅ Wizard validates each step
- ✅ Rich text editor works
- ✅ Import/Export successful

**Documentation**: TBD `docs/PHASE9-EXAM-MANAGEMENT-UI.md`

---

### Phase 10: Grading UI
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🟡 High  
**Dependencies**: Phase 9 ✅

#### Objectives
Teacher interface for grading submissions

#### Deliverables

**1. Submissions List**
- Table view of submissions
- Columns: Student, Exam, Score, Status, Submitted At
- Filter: Exam, Student, Status (SUBMITTED/GRADED)
- Sort: Name, Score, Date
- Quick stats panel:
  - Average score
  - Pass rate
  - Pending grading count
- Export to Excel

**2. Grading Interface**
```
GradingView.fxml
```
Layout:
```
┌─────────────────────────────────────────────────────┐
│ Student: [Name] | Exam: [Title] | Score: __/100    │
├─────────────────────────────────────────────────────┤
│ ┌─────────────────────┬─────────────────────────┐  │
│ │ Questions List      │ Answer Display          │  │
│ │ Q1: [Title] ✓ 10/10 │                         │  │
│ │ Q2: [Title] ⏳ 0/15  │ Question: [Content]     │  │
│ │ Q3: [Title] ✓ 5/10  │                         │  │
│ │                     │ Student Answer:          │  │
│ │ Legend:             │ [Answer text/content]    │  │
│ │ ✓ Graded            │                         │  │
│ │ ⏳ Pending           │ Points: [___] / 15      │  │
│ │                     │                         │  │
│ │                     │ Feedback:               │  │
│ │                     │ [Text area]             │  │
│ │                     │                         │  │
│ │                     │ [Save] [Next Question]  │  │
│ └─────────────────────┴─────────────────────────┘  │
│ [Previous Student] [Next Student] [Finalize Grading]│
└─────────────────────────────────────────────────────┘
```

Features:
- Student info panel (name, class, photo)
- Question-by-question navigation
- Answer display:
  - Auto-graded answers (read-only với score)
  - Manual answers (editable points + feedback)
- Points input với validation (0 to max_points)
- Feedback text area (optional)
- Save progress button
- Previous/Next student navigation
- Finalize grading button (validates all graded)

**3. Results Summary View**
- Score breakdown by question
- Pass/Fail indicator
- Comparison to class average
- Time spent
- Export to PDF

#### Files to Create (~15 files)

#### Success Criteria
- ✅ Grade answer < 30s
- ✅ Navigation smooth
- ✅ Points validation works
- ✅ Finalize validates all graded
- ✅ Export successful

**Documentation**: TBD `docs/PHASE10-GRADING-UI.md`

---

### Phase 11: Anti-Cheat Client Monitors ⭐ CORE FEATURE
**Status**: ⏳ PLANNED  
**Duration**: 1.5 tuần  
**Priority**: 🔴 CRITICAL (Core Feature)  
**Dependencies**: Phase 8 ✅, Phase 6A ✅

#### Objectives
Implement 5 client-side monitors cho anti-cheat system

#### Architecture
```java
MonitoringCoordinator.java
├── ScreenCaptureMonitor.java
├── WindowFocusMonitor.java
├── ProcessMonitor.java
├── ClipboardMonitor.java
└── KeystrokeAnalyzer.java
```

#### Deliverables

**1. ScreenCaptureMonitor** ⭐
```java
ScreenCaptureMonitor.java
```
Features:
- Random screenshots (interval: 30-120s configurable)
- Platform-specific screen capture:
  - Windows: Robot API
  - macOS: screencapture command
  - Linux: scrot/gnome-screenshot
- JPEG compression (70% quality)
- Resolution scaling (max 1920x1080)
- Upload to backend API
- Retry on failure
- Show countdown to student (10s warning)
- Metadata: timestamp, screen resolution, window title

**2. WindowFocusMonitor** ⭐
```java
WindowFocusMonitor.java (uses JNA)
```
Features:
- Detect window focus change (alt+tab)
- Track active window title
- Platform-specific hooks:
  - Windows: Win32 SetWinEventHook
  - macOS: NSWorkspace notifications
  - Linux: X11 events
- Detect forbidden apps:
  - Browsers: Chrome, Firefox, Edge, Safari
  - AI: ChatGPT, Claude, Copilot
  - IDEs: VS Code, IntelliJ, PyCharm
  - Messaging: Telegram, WhatsApp, Discord
- Log every focus change
- Create alert on forbidden app

**3. ProcessMonitor** ⭐
```java
ProcessMonitor.java
```
Features:
- Scan running processes every 10s
- Platform-specific process listing:
  - Windows: tasklist command
  - macOS: ps command
  - Linux: ps command
- Blacklist matching:
  - ChatGPT Desktop
  - Claude Desktop
  - GitHub Copilot
  - VS Code, Cursor, Sublime
  - Postman, Insomnia
  - Python/Node REPL
- Configurable blacklist from server
- Create alert on blacklisted process
- Option to force-kill (if enabled)

**4. ClipboardMonitor** ⭐
```java
ClipboardMonitor.java
```
Features:
- Monitor clipboard changes
- Detect copy events (Ctrl+C)
- Detect paste events (Ctrl+V)
- Log clipboard content (optional, configurable)
- Alert on large paste (> 100 characters)
- Alert on paste from external source
- Cross-platform clipboard access

**5. KeystrokeAnalyzer** ⭐
```java
KeystrokeAnalyzer.java
```
Features:
- Track typing speed (WPM)
- Detect abnormal speed spikes
- Detect paste vs typing:
  - Typing: Gradual key events
  - Paste: Instant text appearance
- Calculate average typing speed
- Alert on suspicious patterns:
  - Speed > 2x average
  - Long answers appearing instantly
- Statistical analysis

**6. MonitoringCoordinator** ⭐
```java
MonitoringCoordinator.java
```
Responsibilities:
- Start/Stop all monitors
- Collect monitoring data
- Batch upload to backend (every 30s)
- Handle upload failures (queue + retry)
- Manage monitoring lifecycle:
  - Start on exam begin
  - Stop on exam submit
  - Pause/Resume on disconnect
- Configuration from server:
  - Enable/Disable monitors
  - Intervals
  - Blacklist rules

#### Cross-Platform Support

**Dependencies:**
```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.13.0</version>
</dependency>
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.13.0</version>
</dependency>
```

**Platform Detection:**
```java
PlatformUtil.java
- isWindows()
- isMacOS()
- isLinux()
- getProcessList()
- captureScreen()
- etc.
```

#### Files to Create (~15 files)
- 5 Monitor classes
- 1 Coordinator class
- 3 Platform utility classes
- 2 Data models (MonitoringData, AlertData)
- 2 Service classes (UploadService, ConfigService)
- 2 Configuration classes

#### Technical Challenges
1. **JNA Integration**: Native hooks per platform
2. **Permission Handling**: Screen capture, process access
3. **Performance**: Minimize CPU/Memory usage
4. **Network Resilience**: Queue + retry mechanism
5. **Privacy**: Encrypt sensitive data
6. **False Positives**: Reduce alert noise

#### Success Criteria
- ✅ All 5 monitors work on Windows/macOS/Linux
- ✅ Screenshot capture < 2s
- ✅ Window focus detection < 100ms
- ✅ Process scan < 500ms
- ✅ CPU usage < 5%
- ✅ Memory usage < 100MB
- ✅ Batch upload successful
- ✅ No crashes or freezes
- ✅ Configurable from server

**Documentation**: TBD `docs/PHASE11-ANTI-CHEAT-MONITORS.md`

---

### Phase 12: Monitoring Dashboard (Teacher/Admin)
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🟡 High  
**Dependencies**: Phase 11 ✅

#### Objectives
Real-time monitoring dashboard for teachers/admins

#### Deliverables

**1. Live Monitoring View**
```
MonitoringDashboard.fxml
```
Layout:
```
┌─────────────────────────────────────────────────────┐
│ Exam: [Dropdown] | Status: 🟢 ONGOING | Students: 25│
├─────────────────────────────────────────────────────┤
│ ┌─────────────────────┬─────────────────────────┐  │
│ │ Students Grid       │ Alert Feed (Real-time)  │  │
│ │ [Grid of 25 cards]  │ 🔴 HIGH: Student A -    │  │
│ │                     │    Process detected     │  │
│ │ Each card shows:    │ 🟡 MED: Student B -     │  │
│ │ - Name              │    Window switch        │  │
│ │ - Latest screenshot │ 🟢 LOW: Student C -     │  │
│ │ - Status indicator  │    Normal activity      │  │
│ │ - Alert badge       │                         │  │
│ │                     │ [Filter] [Clear All]    │  │
│ └─────────────────────┴─────────────────────────┘  │
│ Quick Actions: [Refresh] [View All Screenshots]    │
└─────────────────────────────────────────────────────┘
```

Features:
- Exam selector dropdown
- Students grid (4-6 columns)
- Student card:
  - Name + photo
  - Latest screenshot (thumbnail, click to enlarge)
  - Status: 🟢 Active / 🟡 Suspicious / 🔴 Alert / ⚪ Offline
  - Alert badge count
  - Click to view detail
- Real-time alert feed (WebSocket)
- Alert severity color coding
- Alert notification sound (optional)
- Auto-refresh (every 10s)

**2. Screenshot Viewer**
```
ScreenshotViewer.fxml
```
Features:
- Gallery view (grid of thumbnails)
- Timeline slider
- Full-screen viewer
- Zoom in/out
- Filter: Student, Time range
- Download screenshot
- Metadata display (timestamp, window title)

**3. Student Detail View**
```
StudentDetailView.fxml
```
Features:
- Student info
- Activity timeline (visual)
- Screenshot gallery
- Alert history
- Window focus changes log
- Process detections log
- Clipboard activity
- Typing speed graph
- Actions:
  - Flag student
  - Send warning message
  - Invalidate submission

**4. Alert Management**
```
AlertManagement.fxml
```
Features:
- Alert list table
- Columns: Student, Type, Severity, Time, Status
- Filter: Severity, Type, Status (NEW/REVIEWED/DISMISSED)
- Sort: Time, Severity
- Review actions:
  - Review (mark as reviewed)
  - Dismiss (false positive)
  - Escalate (flag for admin)
- Bulk actions
- Export to PDF report

**5. Activity Logs Viewer**
```
ActivityLogsViewer.fxml
```
Features:
- Filterable log table
- Columns: Student, Activity Type, Details, Timestamp
- Filter: Student, Activity Type, Time range
- Search by keyword
- Timeline visualization
- Export to CSV/Excel

#### Files to Create (~18 files)

#### WebSocket Integration
```java
MonitoringWebSocketClient.java
```
- Subscribe to `/topic/alerts`
- Subscribe to `/topic/monitoring/{examId}/updates`
- Handle reconnection
- Update UI in real-time

#### Success Criteria
- ✅ Real-time alerts < 1s delay
- ✅ Screenshot gallery smooth scrolling
- ✅ Student grid updates automatically
- ✅ WebSocket reconnects on failure
- ✅ No UI freezes
- ✅ Performance: Handle 100+ students

**Documentation**: TBD `docs/PHASE12-MONITORING-DASHBOARD.md`

---

### Phase 13: Admin Dashboard & System Config
**Status**: ⏳ PLANNED  
**Duration**: 1 tuần  
**Priority**: 🟢 Medium  
**Dependencies**: Phase 12 ✅

#### Objectives
Complete admin functionality và system configuration

#### Deliverables

**1. Dashboard Overview**
```
AdminDashboard.fxml
```
Widgets:
- Statistics cards:
  - Total users (Students/Teachers)
  - Active exams today
  - Total submissions
  - Alerts count (last 7 days)
- System health panel:
  - CPU usage
  - Memory usage
  - Disk usage
  - Database connections
- Charts:
  - Exams per day (line chart)
  - Pass rate trend (line chart)
  - Alert distribution (pie chart)
- Recent activities feed
- Quick actions panel

**2. User Management**
```
UserManagement.fxml
```
Features:
- User list table với pagination
- Columns: Name, Email, Role, Status, Last Login
- Create user dialog
- Edit user dialog
- Bulk import (Excel/CSV):
  - Template download
  - Validation
  - Error reporting
- Bulk operations:
  - Assign role
  - Enroll students to class
  - Deactivate accounts
  - Send notification
- Export users to Excel
- Advanced search

**3. Organization Management**
- Department management (CRUD)
- Class management (CRUD)
- Subject management (CRUD)
- Teacher-Class assignments
- Student enrollment

**4. System Configuration**
```
SystemConfig.fxml
```
Tabs:
- **Monitoring Settings:**
  - Screenshot interval
  - Screenshot compression quality
  - Enable/Disable monitors
  - Process blacklist editor
  - Alert thresholds
- **Exam Settings:**
  - Default duration
  - Default max attempts
  - Auto-grading rules
- **Email Settings:**
  - SMTP configuration
  - Email templates (HTML editor)
  - Test email button
- **Security Settings:**
  - JWT expiration
  - Password policy
  - Session timeout
  - Login attempts limit
- **Maintenance:**
  - Maintenance mode toggle
  - System backup
  - Clear cache
  - View logs

**5. Reports**
```
ReportsView.fxml
```
Report types:
- Exam statistics report
- Student performance report
- Teacher activity report
- Monitoring summary report
- System usage report

Export formats: PDF, Excel, CSV

#### Files to Create (~25 files)

#### Success Criteria
- ✅ Dashboard loads < 2s
- ✅ User bulk import successful
- ✅ Configuration saved correctly
- ✅ Reports generate < 5s
- ✅ Export successful

**Documentation**: TBD `docs/PHASE13-ADMIN-DASHBOARD.md`

---

## 📅 REVISED TIMELINE

### Overview (10 Tuần)
```
Week 1:   Phase 5B - Backend Enhanced Features
Week 2:   Phase 6A - Monitoring Backend
Week 3:   Phase 7 - JavaFX Foundation + Auth UI
Week 4-5: Phase 8 - Exam Taking UI ⭐ (1.5 tuần)
Week 6:   Phase 9 - Exam Management UI
Week 7:   Phase 10 - Grading UI
Week 8-9: Phase 11 - Anti-Cheat Monitors ⭐ (1.5 tuần)
Week 9:   Phase 12 - Monitoring Dashboard
Week 10:  Phase 13 - Admin Dashboard + Integration Testing
```

### Critical Path
```
Phase 5B → Phase 7 → Phase 8 ⭐ EXAM TAKING UI
                            ↓
Phase 6A ────────────────→ Phase 11 ⭐ MONITORS
                            ↓
                       Phase 12 → Phase 13
```

### Parallel Work Possible
- Phase 9 & 10 can be parallel after Phase 8
- Phase 13 can start anytime after Phase 7

---

## 📊 RESOURCE ESTIMATES

### Files to Create
- **Backend (Phases 5B, 6A)**: ~35 files
- **JavaFX Client (Phases 7-13)**: ~150-170 files
- **Documentation**: ~20 documents
- **Total**: ~205-225 files

### Lines of Code (Estimated)
- **Backend**: ~3,000 lines
- **JavaFX Client**: ~15,000-20,000 lines
- **Total New Code**: ~18,000-23,000 lines

### Dependencies to Add
```xml
<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>

<!-- JNA for native hooks -->
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.13.0</version>
</dependency>

<!-- WebSocket Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

## ✅ SUCCESS CRITERIA (Overall)

### Functional
- ✅ Complete exam workflow (create → take → grade → results)
- ✅ Real-time monitoring with 5 monitors
- ✅ Teacher can view live monitoring
- ✅ Auto-grading accurate (100%)
- ✅ Network resilience (auto-reconnect)
- ✅ Cross-platform (Windows, macOS, Linux)

### Performance
- ✅ API response < 500ms
- ✅ WebSocket latency < 100ms
- ✅ UI responsive (60 FPS)
- ✅ Monitor CPU < 5%
- ✅ Screenshot capture < 2s
- ✅ Support 100+ concurrent exams

### Security
- ✅ JWT authentication secure
- ✅ API authorization working
- ✅ Encrypted monitoring data
- ✅ No XSS/SQL injection
- ✅ Secure token storage

### Quality
- ✅ Unit test coverage > 70%
- ✅ Integration tests pass
- ✅ No critical bugs
- ✅ Code documented
- ✅ User manuals complete

---

## 📝 DOCUMENTATION PLAN

### Per Phase
- Technical specification
- Implementation guide
- Testing guide
- API documentation (if backend)
- UI mockups (if frontend)

### Final Documentation
1. **User Manuals:**
   - Student guide
   - Teacher guide
   - Admin guide

2. **Technical Documentation:**
   - Architecture overview
   - API reference
   - Database schema
   - Deployment guide

3. **Developer Documentation:**
   - Setup guide
   - Code structure
   - Contribution guide
   - Troubleshooting

---

## 🎯 NEXT IMMEDIATE ACTIONS

1. ✅ Review và approve redefined phases
2. ⏳ Begin Phase 5B (Backend Enhanced Features)
3. ⏳ Update memory-bank with new plan
4. ⏳ Create detailed Phase 5B specification
5. ⏳ Setup JavaFX project structure (prep for Phase 7)

---

**Document Status**: ACTIVE PLAN  
**Last Updated**: 21/11/2025 01:35  
**Next Review**: After Phase 5B completion  
**Author**: K24DTCN210-NVMANH with Cline AI

---

**🎉 Ready to Complete MS.TrustTest with Clear Roadmap! 🎉**
