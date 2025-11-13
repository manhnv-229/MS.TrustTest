# Tổng Hợp Các Phase - MS.TrustTest

## Phase 1: Setup & Database Schema ✅ (Đã hoàn thành chi tiết)

Xem file: `docs/phases/phase-1-setup.md`

**Highlights**:
- 16 bảng database với schema đầy đủ
- Maven multi-module structure
- Flyway migration scripts
- Setup instructions chi tiết

---

## Phase 2: Authentication & Authorization

**Thời gian**: 1 tuần  
**Priority**: 🔴 Critical

### Mục Tiêu Chính
1. Implement hệ thống đăng nhập đa hình thức (mã SV/email/SĐT)
2. Mã hóa mật khẩu với BCrypt
3. JWT token authentication
4. RBAC với 5 roles
5. Spring Security configuration
6. Login UI (JavaFX)

### Deliverables
- User Entity, Repository, Service
- AuthController với endpoints: `/login`, `/register`, `/refresh-token`
- JwtTokenProvider
- JwtAuthenticationFilter
- Spring Security config với role-based access
- Login screen (JavaFX)
- Unit tests

---

## Phase 3: Organization Management

**Thời gian**: 1-2 tuần  
**Priority**: 🟡 High

### Mục Tiêu Chính
1. CRUD cho Department (Khoa)
2. CRUD cho Class (Lớp chung)
3. CRUD cho SubjectClass (Lớp môn học)
4. Quản lý sinh viên
5. Phân công giáo viên - môn - lớp
6. Permission checking

### Deliverables
- Department, Class, Subject, SubjectClass entities
- CRUD Controllers và Services
- Management UI screens
- Permission-based access control
- Integration tests

### API Endpoints
```
POST   /api/departments
GET    /api/departments
GET    /api/departments/{id}
PUT    /api/departments/{id}
DELETE /api/departments/{id}

POST   /api/classes
GET    /api/classes
POST   /api/classes/{id}/students
DELETE /api/classes/{id}/students/{studentId}

POST   /api/subject-classes
GET    /api/subject-classes
POST   /api/subject-classes/{id}/students (bulk enroll)
```

---

## Phase 4: Exam Management

**Thời gian**: 2 tuần  
**Priority**: 🔴 Critical

### Mục Tiêu Chính
1. Tạo và quản lý bài thi
2. Tạo câu hỏi (trắc nghiệm, tự luận)
3. Ngân hàng câu hỏi
4. Gán bài thi cho lớp
5. Cấu hình thời gian và monitoring level
6. Exam creation UI

### Deliverables
- Exam, Question entities
- ExamController, QuestionController
- Question Bank service
- Exam creation wizard (JavaFX)
- Question editor UI
- Import/Export questions (optional)

### Key Features
- **Exam Purpose (Mục đích)**:
  - QUICK_TEST: Kiểm tra nhanh (15 phút)
  - PROGRESS_TEST: Kiểm tra tiến độ
  - MIDTERM: Thi giữa kỳ
  - FINAL: Thi cuối kỳ
  - MODULE_COMPLETION: Kết thúc học phần
  - MAKEUP: Thi lại
  - ASSIGNMENT: Bài tập về nhà
  - PRACTICE: Luyện tập

- **Exam Format (Hình thức)**:
  - MULTIPLE_CHOICE_ONLY: Chỉ trắc nghiệm
  - ESSAY_ONLY: Chỉ tự luận
  - CODING_ONLY: Chỉ lập trình
  - MIXED: Hỗn hợp

- **Question Types (8 loại)**:
  - MULTIPLE_CHOICE: Trắc nghiệm (1 đáp án)
  - MULTIPLE_SELECT: Chọn nhiều đáp án
  - TRUE_FALSE: Đúng/Sai
  - ESSAY: Tự luận
  - SHORT_ANSWER: Trả lời ngắn
  - CODING: Lập trình (với test cases)
  - FILL_IN_BLANK: Điền chỗ trống
  - MATCHING: Nối câu

- **Settings**: 
  - Time constraints
  - Monitoring level (Low/Medium/High)
  - Randomization
  - Review options
  - Show answers
  - Code execution (for coding exams)

### API Endpoints
```
POST   /api/exams
GET    /api/exams
GET    /api/exams/{id}
PUT    /api/exams/{id}
DELETE /api/exams/{id}
POST   /api/exams/{id}/publish
POST   /api/exams/{id}/classes (assign to multiple classes)

POST   /api/questions
GET    /api/questions?examId={examId}
PUT    /api/questions/{id}
DELETE /api/questions/{id}
POST   /api/questions/import
GET    /api/questions/export?examId={examId}
```

---

## Phase 5: Exam Taking Interface

**Thời gian**: 2 tuần  
**Priority**: 🔴 Critical

### Mục Tiêu Chính
1. Exam session management
2. Exam taking UI (JavaFX)
3. Timer countdown
4. Auto-save (30s)
5. Answer submission
6. Network reconnection handling
7. Exam validation

### Deliverables
- ExamSessionController
- Exam taking screen (full-screen mode)
- Timer component
- Auto-save mechanism
- Answer storage
- Submission confirmation dialog
- Resume exam after disconnect

### Key Features
- **Validation**: Check time, check attempts, check permissions
- **Auto-save**: Every 30 seconds
- **Timer**: Countdown with visual warnings
- **Navigation**: Previous/Next question, question list
- **Submit**: Confirmation dialog, final submission

### API Endpoints
```
POST   /api/exam-sessions/start (start exam)
GET    /api/exam-sessions/{id}
POST   /api/exam-sessions/{id}/save (auto-save)
POST   /api/exam-sessions/{id}/submit (final submit)
GET    /api/exams/{examId}/questions (get questions for exam)
```

---

## Phase 6: Anti-Cheat Monitoring 🌟 (Core Feature)

**Thời gian**: 3 tuần  
**Priority**: 🔴 Critical

### Mục Tiêu Chính
1. Client-side monitors (5 loại)
2. Backend monitoring service
3. WebSocket real-time alerts
4. Screenshot capture và storage
5. Admin monitoring dashboard
6. Cross-platform support

### Client Monitors

#### 1. ScreenCaptureMonitor
```java
- Chụp màn hình ngẫu nhiên (configurable interval)
- Compress ảnh (JPEG 70%)
- Upload qua API
- Hiển thị countdown cho sinh viên
```

#### 2. WindowFocusMonitor
```java
- Detect alt+tab, window switch
- Track active window title
- Detect browser, ChatGPT, IDE
- Log mỗi lần chuyển cửa sổ
```

#### 3. ProcessMonitor
```java
- Scan running processes
- Blacklist: ChatGPT, Claude, Copilot, VS Code, Cursor
- Detect browser extensions
- Alert khi phát hiện forbidden process
```

#### 4. ClipboardMonitor
```java
- Monitor clipboard changes
- Detect copy/paste
- Log clipboard content (configurable)
- Alert on large paste
```

#### 5. KeystrokeAnalyzer
```java
- Analyze typing speed
- Detect abnormal patterns
- Detect paste vs typing
```

### Backend Services

#### MonitoringService
```java
- Receive monitoring data from client
- Store screenshots
- Analyze patterns
- Generate alerts
```

#### AlertService
```java
- Evaluate severity
- Create alerts
- Notify admins via WebSocket
- Store alert history
```

### Admin Dashboard
- Live monitoring view (real-time)
- Active exams list
- Students list với status
- Alert feed (real-time)
- Screenshot viewer
- Activity logs viewer
- Monitoring configuration panel

### WebSocket Events
```
/topic/alerts -> Broadcast to all admins
/queue/alerts/{userId} -> Private alerts
/topic/monitoring/{examId} -> Exam-specific updates
```

### API Endpoints
```
POST   /api/monitoring/screenshot (upload screenshot)
POST   /api/monitoring/log (send monitoring log)
POST   /api/monitoring/alert (manual alert)
GET    /api/monitoring/sessions/{sessionId}/logs
GET    /api/monitoring/sessions/{sessionId}/screenshots
GET    /api/monitoring/alerts?status=NEW
PUT    /api/monitoring/alerts/{id}/review
```

### Deliverables
- 5 monitor classes (Client)
- MonitoringCoordinator (Client)
- MonitoringService, AlertService (Backend)
- WebSocket configuration
- Admin dashboard (JavaFX)
- Cross-platform testing
- Performance benchmarks

---

## Phase 7: Grading & Results

**Thời gian**: 1 tuần  
**Priority**: 🟡 Medium

### Mục Tiêu Chính
1. Auto-grading cho multiple choice
2. Manual grading UI cho essay
3. Result calculation
4. Student result view
5. Teacher grading interface
6. Result history

### Deliverables
- GradingService
- Auto-grading algorithm
- Manual grading UI (Teacher)
- Result view UI (Student)
- Report generation
- Grade export

### Key Features
- **Auto-grading**: Chấm tự động trắc nghiệm
- **Manual grading**: Interface chấm tự luận
- **Result view**: Sinh viên xem điểm, đáp án
- **Statistics**: Phân tích điểm, ranking
- **Feedback**: Giáo viên comment cho sinh viên

### API Endpoints
```
POST   /api/grading/auto/{submissionId} (auto-grade)
GET    /api/grading/submissions?examId={examId}&status=SUBMITTED
POST   /api/grading/manual/{submissionId} (manual grade)
GET    /api/results/student/{studentId}
GET    /api/results/exam/{examId}/statistics
```

---

## Phase 8: Admin Dashboard & Configuration

**Thời gian**: 1 tuần  
**Priority**: 🟢 Medium

### Mục Tiêu Chính
1. Admin dashboard overview
2. System health monitoring
3. User management (bulk operations)
4. System configuration
5. Logs và audit trail
6. Performance monitoring

### Deliverables
- Admin dashboard (comprehensive)
- System health checks
- User bulk import/export
- Configuration panel
- Logs viewer
- Performance metrics
- Final integration testing

### Dashboard Widgets
- Active exams count
- Online users count
- Total submissions today
- Alert summary
- System health (CPU, Memory, Disk)
- Recent activities
- Quick actions

### Bulk Operations
- Import users từ Excel/CSV
- Export users
- Bulk assign roles
- Bulk enroll students
- Bulk notifications

### Configuration Settings
- Monitoring rules
- Screenshot settings
- Alert thresholds
- Email templates
- System maintenance mode

### API Endpoints
```
GET    /api/admin/dashboard/stats
GET    /api/admin/health
GET    /api/admin/users?page=1&size=20
POST   /api/admin/users/import
GET    /api/admin/users/export
POST   /api/admin/config
GET    /api/admin/logs?level=ERROR&date=2025-01-13
GET    /api/admin/audit-trail
```

---

## Timeline Overview

```
Week 1-2:   Phase 1 + Phase 2 (Setup + Auth)
Week 3-4:   Phase 3 (Organization)
Week 5-6:   Phase 4 (Exam Management)
Week 7-8:   Phase 5 (Exam Taking)
Week 9-11:  Phase 6 (Monitoring) ⭐ Core Feature
Week 12:    Phase 7 (Grading)
Week 13:    Phase 8 (Admin + Final Testing)
```

## Testing Strategy

### Unit Tests
- Service layer logic
- Utility functions
- Validation rules

### Integration Tests
- API endpoints
- Database operations
- WebSocket communication

### End-to-End Tests
- Complete exam flow
- Monitoring flow
- Grading flow

### Performance Tests
- Load testing (500+ concurrent)
- Screenshot upload stress test
- WebSocket scalability
- Database query optimization

### Security Tests
- Authentication bypass attempts
- SQL injection
- XSS prevention
- JWT token validation

### Cross-Platform Tests
- Windows compatibility
- macOS compatibility
- Linux compatibility
- JNA functionality

---

## Success Criteria

Hệ thống được coi là hoàn thành khi:

✅ Tất cả 8 phases đã implement  
✅ Unit test coverage > 80%  
✅ All integration tests pass  
✅ Performance targets met (API < 500ms, WebSocket < 100ms)  
✅ Security audit passed  
✅ Cross-platform tested  
✅ Documentation complete  
✅ User acceptance testing done  
✅ Ready for deployment  

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:05  
**Last Updated**: 13/11/2025 14:05

**Note**: Chi tiết từng Phase sẽ được expand khi bắt đầu implementation. Phase 1 đã có document đầy đủ tại `docs/phases/phase-1-setup.md`.
