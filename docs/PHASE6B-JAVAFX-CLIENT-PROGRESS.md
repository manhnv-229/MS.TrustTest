# Phase 6B: JavaFX Client Monitoring - Progress Report

**Ngày tạo:** 21/11/2025 10:47  
**Người thực hiện:** K24DTCN210-NVMANH  
**Trạng thái:** 🟡 IN PROGRESS (40% hoàn thành)

---

## 1. Tổng Quan

Phase 6B tập trung xây dựng **JavaFX Client** với khả năng giám sát học sinh trong quá trình thi. Client sẽ:
- Chụp màn hình tự động mỗi 30 giây
- Giám sát các hoạt động (Alt+Tab, clipboard, processes)
- Phát hiện và tạo cảnh báo tự động
- Upload dữ liệu lên backend qua REST APIs
- Xử lý offline mode với queue mechanism

---

## 2. Đã Hoàn Thành ✅

### 2.1. Project Structure
```
client-javafx/
├── pom.xml                          ✅ Created
├── src/main/
│   ├── java/
│   │   ├── module-info.java         ✅ Created
│   │   └── com/mstrust/client/
│   │       ├── config/
│   │       │   └── AppConfig.java   ✅ Created
│   │       ├── dto/
│   │       │   ├── ActivityType.java        ✅ Created
│   │       │   ├── AlertSeverity.java       ✅ Created
│   │       │   ├── ActivityData.java        ✅ Created
│   │       │   ├── ActivityLogRequest.java  ✅ Created
│   │       │   └── AlertCreateRequest.java  ✅ Created
│   │       └── api/
│   │           └── MonitoringApiClient.java ✅ Created
│   └── resources/
│       └── config.properties        ✅ Created
```

### 2.2. Dependencies Configured (pom.xml)
```xml
<!-- Core Dependencies -->
- JavaFX 21 (controls, fxml)
- JNativeHook 2.2.2 (global keyboard/mouse events)
- JNA 5.13.0 (Windows API access)
- Gson 2.10.1 (JSON processing)
- Lombok 1.18.30
- SLF4J 2.0.9 (logging)

<!-- Build Plugins -->
- Maven Compiler Plugin 3.11.0
- JavaFX Maven Plugin 0.0.8
- Maven Shade Plugin 3.5.1 (executable JAR)
```

### 2.3. Configuration Management

**AppConfig.java** - Singleton pattern để quản lý cấu hình:
```java
// API Configuration
- baseUrl: http://localhost:8080
- timeout: 30 seconds

// Monitoring Configuration
- Screenshot interval: 30 seconds
- Activity batch interval: 60 seconds
- Screenshot max size: 1920x1080
- JPEG quality: 0.7

// Alert Thresholds
- Window switch threshold: 10 lần trong 5 phút
- Clipboard threshold: 20 lần trong 10 phút

// Blacklisted Processes
- teamviewer, anydesk, chrome, firefox, safari, edge
- discord, telegram, skype, zoom, slack

// Network Queue
- Max queue size: 1000
- Retry attempts: 3
- Retry delay: 5 seconds
```

**Method quan trọng:**
```java
public boolean isProcessBlacklisted(String processName)
```

### 2.4. Data Transfer Objects (DTOs)

**ActivityType.java** - Enum định nghĩa loại hoạt động:
```java
public enum ActivityType {
    WINDOW_FOCUS,      // Chuyển cửa sổ (Alt+Tab)
    PROCESS_DETECTED,  // Phát hiện process đáng ngờ
    CLIPBOARD,         // Thao tác clipboard
    KEYSTROKE          // Keystroke pattern
}
```

**AlertSeverity.java** - Enum mức độ nghiêm trọng:
```java
public enum AlertSeverity {
    LOW,       // Thông tin
    MEDIUM,    // Cần xem xét
    HIGH,      // Xem xét ngay
    CRITICAL   // Vi phạm nặng
}
```

**ActivityData.java** - DTO chứa một hoạt động:
```java
@Data @Builder
public class ActivityData {
    private ActivityType activityType;
    private String details;
    private LocalDateTime timestamp;
    
    // Factory methods
    public static ActivityData windowFocus(String windowTitle)
    public static ActivityData processDetected(String processName)
    public static ActivityData clipboard(String operation)
    public static ActivityData keystroke(String pattern)
}
```

**ActivityLogRequest.java** - Batch request:
```java
@Data @Builder
public class ActivityLogRequest {
    private Long submissionId;
    private List<ActivityData> activities;
    
    public static ActivityLogRequest of(Long submissionId, List<ActivityData> activities)
}
```

**AlertCreateRequest.java** - Tạo cảnh báo:
```java
@Data @Builder
public class AlertCreateRequest {
    private Long submissionId;
    private AlertSeverity severity;
    private String alertType;
    private String description;
    
    // Factory methods cho các loại alert
    public static AlertCreateRequest windowSwitchAlert(Long submissionId, int switchCount)
    public static AlertCreateRequest suspiciousProcessAlert(Long submissionId, String processName)
    public static AlertCreateRequest clipboardAlert(Long submissionId, int count)
}
```

### 2.5. API Client

**MonitoringApiClient.java** - HTTP client giao tiếp với backend:

**Main Methods:**
```java
public void setAuthToken(String token)

public boolean uploadScreenshot(
    Path imagePath, 
    Long submissionId, 
    String screenResolution, 
    String windowTitle
)

public boolean logActivities(ActivityLogRequest request)

public boolean createAlert(AlertCreateRequest request)

public boolean testConnection()
```

**Features:**
- Sử dụng Java 11+ HttpClient
- Multipart form-data upload cho screenshots
- JSON serialization/deserialization với Gson
- Custom LocalDateTime adapter
- Connection timeout configuration
- Bearer token authentication
- Error logging với SLF4J

**API Endpoints Used:**
```
POST /api/monitoring/screenshots (multipart)
POST /api/monitoring/activities (JSON)
POST /api/monitoring/alerts (JSON)
GET  /api/health (health check)
```

---

## 3. Chưa Hoàn Thành ⏳

### 3.1. Core Monitoring Services

#### Screenshot Capture Service
**File cần tạo:** `ScreenshotCaptureService.java`

**Chức năng:**
- Chụp màn hình tự động mỗi 30 giây
- Sử dụng `java.awt.Robot` để capture
- Compress ảnh về max 1920x1080, JPEG 70%
- Lấy screen resolution hiện tại
- Lấy active window title
- Upload qua MonitoringApiClient
- Xử lý multi-monitor setup
- Queue failed uploads cho retry

**Threading:**
- Sử dụng `ScheduledExecutorService`
- Background thread không block UI
- Graceful shutdown

#### Activity Monitor Service
**File cần tạo:** `ActivityMonitorService.java`

**Chức năng:**
- Implement JNativeHook listeners
- Monitor keyboard events (Alt+Tab, Ctrl+C/V)
- Monitor window focus changes
- Monitor clipboard operations
- Detect running processes
- Buffer activities trong memory
- Batch upload mỗi 60 giây

**Libraries:**
- JNativeHook for global hooks
- JNA for Windows API calls
- `ProcessHandle` API for process detection

#### Alert Detection Service
**File cần tạo:** `AlertDetectionService.java`

**Chức năng:**
- Count window switches trong timeframe
- Detect blacklisted processes
- Count clipboard operations
- Auto-create alerts khi vượt threshold
- Severity level calculation
- Alert deduplication

**Thresholds:**
```java
Window switches: 10 trong 5 phút → HIGH/CRITICAL
Clipboard ops: 20 trong 10 phút → MEDIUM
Blacklisted process: → CRITICAL
```

### 3.2. Network & Queue Management

#### Network Queue Manager
**File cần tạo:** `NetworkQueueManager.java`

**Chức năng:**
- Manage failed uploads trong queue
- Retry mechanism với exponential backoff
- Persist queue to disk (optional)
- Priority queue (alerts > activities > screenshots)
- Max queue size limit (1000 items)
- Background worker thread

**Queue Types:**
```java
LinkedBlockingQueue<ScreenshotUploadTask>
LinkedBlockingQueue<ActivityBatchTask>
LinkedBlockingQueue<AlertTask>
```

### 3.3. Windows API Utilities (JNA)

#### WindowDetector.java
**Chức năng:**
- Get active window title
- Get window handle (HWND)
- Detect window focus changes
- Support multiple monitors

**Windows API Calls:**
```c
GetForegroundWindow()
GetWindowText()
GetWindowThreadProcessId()
```

#### ProcessDetector.java
**Chức năng:**
- List running processes
- Get process name from PID
- Check if process is blacklisted
- Monitor process start/stop

**API Approaches:**
1. Java `ProcessHandle` API (Java 9+)
2. JNA Windows API (if needed)
3. WMI queries (advanced)

#### ClipboardMonitor.java
**Chức năng:**
- Monitor clipboard changes
- Detect Copy (Ctrl+C) operations
- Detect Paste (Ctrl+V) operations
- Prevent excessive clipboard usage

**Java APIs:**
```java
java.awt.datatransfer.Clipboard
java.awt.datatransfer.DataFlavor
```

### 3.4. UI Components

#### MonitoringStatusBar.java
**Chức năng:**
- Show connection status (green/red indicator)
- Display last screenshot time
- Show activity count
- Display alert notifications (non-intrusive)
- Minimize to system tray option

**JavaFX Components:**
```java
HBox layout
Circle statusIndicator
Label lastScreenshotLabel
Label activityCountLabel
Label connectionLabel
```

#### AlertNotification.java
**Chức năng:**
- Show toast notifications for alerts
- Auto-dismiss after 5 seconds
- Color-coded by severity
- Click to view details

### 3.5. Main Application

#### ExamClientApplication.java
**Chức năng:**
- JavaFX Application entry point
- Initialize all services
- Coordinate service lifecycle
- Handle authentication
- Show main exam UI
- Shutdown hook for cleanup

**Lifecycle:**
```java
1. start() - Launch JavaFX
2. initialize() - Setup services
3. startMonitoring(submissionId) - Begin exam
4. stopMonitoring() - End exam
5. stop() - Cleanup resources
```

#### MonitoringCoordinator.java
**Chức năng:**
- Coordinate tất cả monitoring services
- Single point of control
- Service dependency management
- Error handling & recovery
- Logging & metrics

---

## 4. Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│           ExamClientApplication (JavaFX)            │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │      MonitoringCoordinator (Orchestrator)     │ │
│  └───────────────┬───────────────────────────────┘ │
│                  │                                   │
│  ┌───────────────┴────────────┬─────────────────┐  │
│  │                            │                  │  │
│  ▼                            ▼                  ▼  │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────┐ │
│  │ Screenshot   │  │   Activity     │  │  Alert   │ │
│  │   Capture    │  │   Monitor      │  │ Detection│ │
│  │   Service    │  │   Service      │  │  Service │ │
│  └──────┬───────┘  └───────┬────────┘  └────┬─────┘ │
│         │                  │                 │       │
│         │    ┌─────────────┴─────────────────┘       │
│         │    │                                        │
│         ▼    ▼                                        │
│  ┌──────────────────────────────────────────────┐   │
│  │      NetworkQueueManager (Retry Logic)       │   │
│  └──────────────────┬───────────────────────────┘   │
│                     │                                │
│                     ▼                                │
│  ┌──────────────────────────────────────────────┐   │
│  │      MonitoringApiClient (HTTP Client)       │   │
│  └──────────────────┬───────────────────────────┘   │
└────────────────────│─────────────────────────────────┘
                     │
                     ▼
          ┌──────────────────────┐
          │   Backend REST APIs  │
          │ (Spring Boot Server) │
          └──────────────────────┘
```

---

## 5. Implementation Strategy

### Phase 1: Core Services (Next)
1. ✅ ScreenshotCaptureService
2. ✅ Basic WindowDetector (JNA)
3. ✅ Test screenshot upload

### Phase 2: Activity Monitoring
1. ✅ Setup JNativeHook
2. ✅ ActivityMonitorService
3. ✅ ProcessDetector
4. ✅ ClipboardMonitor
5. ✅ Test activity batch upload

### Phase 3: Alert System
1. ✅ AlertDetectionService
2. ✅ Threshold checking logic
3. ✅ Test alert creation

### Phase 4: Queue & Resilience
1. ✅ NetworkQueueManager
2. ✅ Retry mechanism
3. ✅ Test offline mode

### Phase 5: UI & Integration
1. ✅ MonitoringStatusBar
2. ✅ AlertNotification
3. ✅ MonitoringCoordinator
4. ✅ ExamClientApplication
5. ✅ Integration testing

### Phase 6: Testing & Documentation
1. ✅ Unit tests
2. ✅ Integration tests
3. ✅ User manual
4. ✅ Developer documentation

---

## 6. Technical Challenges

### 6.1. JNativeHook Setup
**Issue:** Native library loading, antivirus warnings  
**Solution:** 
- Proper library configuration in pom.xml
- Code signing for production
- User documentation about antivirus

### 6.2. Screenshot Performance
**Issue:** Large images, memory usage  
**Solution:**
- Image compression before upload
- Max resolution limit (1920x1080)
- JPEG quality 70%
- Cleanup temp files

### 6.3. Multi-threading
**Issue:** Thread safety, resource cleanup  
**Solution:**
- Use ScheduledExecutorService
- Proper shutdown hooks
- Thread-safe collections
- Concurrent queue management

### 6.4. Windows API (JNA)
**Issue:** Platform-specific code  
**Solution:**
- Check OS before JNA calls
- Fallback mechanisms
- Graceful degradation on non-Windows

---

## 7. Testing Plan

### 7.1. Unit Tests
```java
AppConfigTest - Configuration loading
ActivityDataTest - Factory methods
AlertCreateRequestTest - Alert creation logic
```

### 7.2. Integration Tests
```java
MonitoringApiClientTest - Backend API calls
ScreenshotCaptureTest - Capture & upload
ActivityMonitorTest - Event detection
AlertDetectionTest - Threshold checking
```

### 7.3. Manual Tests
1. Start monitoring → verify screenshots uploaded
2. Switch windows → verify activity logged
3. Copy/paste → verify clipboard detected
4. Run blacklisted process → verify alert created
5. Disconnect network → verify queue works
6. Reconnect network → verify retry succeeds

---

## 8. Files Created (13/30+)

### ✅ Completed (13 files)
1. `client-javafx/pom.xml`
2. `client-javafx/src/main/java/module-info.java`
3. `client-javafx/src/main/resources/config.properties`
4. `client-javafx/src/main/java/com/mstrust/client/config/AppConfig.java`
5. `client-javafx/src/main/java/com/mstrust/client/dto/ActivityType.java`
6. `client-javafx/src/main/java/com/mstrust/client/dto/AlertSeverity.java`
7. `client-javafx/src/main/java/com/mstrust/client/dto/ActivityData.java`
8. `client-javafx/src/main/java/com/mstrust/client/dto/ActivityLogRequest.java`
9. `client-javafx/src/main/java/com/mstrust/client/dto/AlertCreateRequest.java`
10. `client-javafx/src/main/java/com/mstrust/client/api/MonitoringApiClient.java`
11. `docs/PHASE6B-JAVAFX-CLIENT-PROGRESS.md`

### ⏳ Pending (17+ files)
12. `com/mstrust/client/monitoring/ScreenshotCaptureService.java`
13. `com/mstrust/client/monitoring/ActivityMonitorService.java`
14. `com/mstrust/client/monitoring/AlertDetectionService.java`
15. `com/mstrust/client/monitoring/MonitoringCoordinator.java`
16. `com/mstrust/client/monitoring/NetworkQueueManager.java`
17. `com/mstrust/client/util/WindowDetector.java`
18. `com/mstrust/client/util/ProcessDetector.java`
19. `com/mstrust/client/util/ClipboardMonitor.java`
20. `com/mstrust/client/ui/MonitoringStatusBar.java`
21. `com/mstrust/client/ui/AlertNotification.java`
22. `com/mstrust/client/ExamClientApplication.java`
23. `src/main/resources/views/main-window.fxml` (optional)
24. `src/main/resources/styles.css` (optional)
25. Test files...

---

## 9. Next Steps

**Khi tiếp tục Phase 6B, thực hiện theo thứ tự:**

1. **ScreenshotCaptureService** - Core functionality
2. **WindowDetector** (JNA) - Dependency cho screenshot
3. **Test screenshot** - Verify hoạt động
4. **ActivityMonitorService** - Monitoring engine
5. **ProcessDetector** - Detect blacklisted apps
6. **ClipboardMonitor** - Clipboard tracking
7. **Test activity logging** - Verify batch upload
8. **AlertDetectionService** - Alert logic
9. **Test alerts** - Verify alert creation
10. **NetworkQueueManager** - Offline handling
11. **Test queue** - Verify retry logic
12. **UI Components** - Status bar, notifications
13. **MonitoringCoordinator** - Orchestration
14. **ExamClientApplication** - Main entry point
15. **Integration testing** - End-to-end tests
16. **Documentation** - User guide, API docs

---

## 10. References

### Backend APIs (Phase 6A)
- `POST /api/monitoring/screenshots` - Upload screenshot
- `POST /api/monitoring/activities` - Batch log activities
- `POST /api/monitoring/alerts` - Create alert

### Key Libraries
- **JavaFX 21:** https://openjfx.io/
- **JNativeHook:** https://github.com/kwhat/jnativehook
- **JNA:** https://github.com/java-native-access/jna
- **Gson:** https://github.com/google/gson

### Documentation
- Phase 6A: `docs/PHASE6A-MONITORING-BACKEND-COMPLETE.md`
- Project Brief: `memory-bank/projectbrief.md`
- System Patterns: `memory-bank/systemPatterns.md`

---

**Status:** 🟡 **40% Complete** - Foundation done, core services pending  
**Next Session:** Continue with ScreenshotCaptureService implementation  
**Estimated Time Remaining:** 4-6 hours for full completion  

---
**CreatedBy:** K24DTCN210-NVMANH (21/11/2025 10:47)
