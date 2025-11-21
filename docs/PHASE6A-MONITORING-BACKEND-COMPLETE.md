# Phase 6A: Monitoring Backend Infrastructure - HOÀN THÀNH

**Ngày hoàn thành:** 21/11/2025  
**Người thực hiện:** K24DTCN210-NVMANH

---

## 📋 Tổng Quan

Phase 6A đã hoàn thành việc xây dựng backend infrastructure cho hệ thống monitoring và ghi nhận hành vi sinh viên trong quá trình thi. Hệ thống bao gồm:

1. **Screenshot Capture & Storage** - Lưu trữ ảnh chụp màn hình lên FTP server
2. **Activity Logging** - Ghi nhận các hoạt động như window switch, clipboard, keystroke
3. **Alert System** - Tạo và quản lý cảnh báo hành vi bất thường
4. **Monitoring Summary** - Tổng hợp dữ liệu monitoring với risk assessment

---

## 🗄️ Database Schema

### 1. Screenshots Table (V17)
```sql
CREATE TABLE screenshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    timestamp DATETIME NOT NULL,
    screen_resolution VARCHAR(50),
    window_title VARCHAR(255),
    metadata TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id)
);
```

**Mục đích:**
- Lưu metadata của screenshots (file thực tế lưu trên FTP)
- Hỗ trợ soft delete
- Track thông tin màn hình và cửa sổ đang active

### 2. Activity Logs Table (V18)
```sql
CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    details TEXT,
    timestamp DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id),
    INDEX idx_submission_type (submission_id, activity_type),
    INDEX idx_timestamp (timestamp)
);
```

**Activity Types:**
- `WINDOW_FOCUS` - Chuyển cửa sổ (Alt+Tab)
- `PROCESS_DETECTED` - Phát hiện process đáng ngờ
- `CLIPBOARD` - Copy/paste operations
- `KEYSTROKE` - Keystroke pattern analysis

### 3. Monitoring Alerts Table (V19)
```sql
CREATE TABLE monitoring_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    reviewed BOOLEAN DEFAULT FALSE,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    review_note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES exam_submissions(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);
```

**Alert Severities:**
- `LOW` - Thông tin, không cần hành động
- `MEDIUM` - Cần xem xét
- `HIGH` - Cần review ngay
- `CRITICAL` - Vi phạm nghiêm trọng

---

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────┐
│                    JavaFX Client                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Screenshot   │  │ Activity     │  │ Alert        │     │
│  │ Capture      │  │ Monitor      │  │ Detection    │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │                  │                  │
          │ HTTP POST        │ HTTP POST        │ HTTP POST
          │ Multipart        │ JSON Batch       │ JSON
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (Port 8080)                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │            MonitoringController                       │  │
│  │  • POST /api/monitoring/screenshots                  │  │
│  │  • POST /api/monitoring/activities                   │  │
│  │  • POST /api/monitoring/alerts                       │  │
│  │  • GET  /api/monitoring/summary/{submissionId}       │  │
│  └───────────────────┬──────────────────────────────────┘  │
│                      │                                      │
│  ┌───────────────────┴──────────────────────────────────┐  │
│  │            Service Layer                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │  │
│  │  │Screenshot    │  │Activity      │  │Alert       │ │  │
│  │  │Service       │  │LogService    │  │Service     │ │  │
│  │  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘ │  │
│  │         │                 │                 │        │  │
│  │         │                 │                 │        │  │
│  │  ┌──────▼─────────────────▼─────────────────▼──────┐ │  │
│  │  │       FtpStorageService (Image Upload)          │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  └────────────────────┬──────────────────────────────────┘  │
└───────────────────────┼──────────────────────────────────────┘
                        │
         ┌──────────────┼──────────────┐
         │              │              │
         ▼              ▼              ▼
    ┌────────┐    ┌─────────┐    ┌─────────┐
    │ MySQL  │    │   FTP   │    │Teacher  │
    │Database│    │ Server  │    │Dashboard│
    └────────┘    └─────────┘    └─────────┘
```

---

## 📦 Code Structure

### Entities (3 files)
```
backend/src/main/java/com/mstrust/exam/entity/
├── Screenshot.java          # Screenshot metadata entity
├── ActivityLog.java         # Activity log entries
├── MonitoringAlert.java     # Alert records
├── ActivityType.java        # Enum: WINDOW_FOCUS, CLIPBOARD, etc.
└── AlertSeverity.java       # Enum: LOW, MEDIUM, HIGH, CRITICAL
```

### Repositories (3 files)
```
backend/src/main/java/com/mstrust/exam/repository/
├── ScreenshotRepository.java
│   ├── findBySubmissionId()
│   ├── findBySubmissionIdAndTimestampBetween()
│   ├── countBySubmissionId()
│   └── softDeleteOlderThan()
│
├── ActivityLogRepository.java
│   ├── findBySubmissionId()
│   ├── findBySubmissionIdAndActivityType()
│   └── countWindowSwitchesInTimeRange()
│
└── MonitoringAlertRepository.java
    ├── findBySubmissionId()
    ├── findUnreviewedBySubmissionId()
    ├── findUnreviewedByExamId()
    └── countBySubmissionIdAndSeverity()
```

### DTOs (8 files)
```
backend/src/main/java/com/mstrust/exam/dto/monitoring/
├── ScreenshotDTO.java              # Screenshot response
├── ScreenshotUploadRequest.java    # (Không dùng - dùng MultipartFile)
├── ActivityLogDTO.java             # Activity log response
├── ActivityLogRequest.java         # Batch activity logging
├── AlertDTO.java                   # Alert response
├── AlertCreateRequest.java         # Create alert request
├── AlertReviewRequest.java         # Teacher review alert
└── MonitoringSummaryDTO.java       # Comprehensive monitoring summary
```

### Services (4 files)
```
backend/src/main/java/com/mstrust/exam/service/
├── FtpStorageService.java
│   ├── uploadScreenshot() - Upload & compress image to FTP
│   ├── deleteFile() - Delete from FTP
│   └── compressImage() - Resize & compress (max 1920x1080, JPEG 70%)
│
├── ScreenshotService.java
│   ├── uploadScreenshot() - Upload & save metadata
│   ├── getScreenshotsBySubmission()
│   ├── getLatestScreenshot()
│   └── cleanupOldScreenshots() - Delete >90 days old
│
├── ActivityLogService.java
│   ├── logActivities() - Batch logging
│   ├── getActivitiesBySubmission()
│   ├── getActivitiesByType()
│   └── countWindowSwitchesInLastMinutes()
│
└── AlertService.java
    ├── createAlert()
    ├── getUnreviewedAlerts()
    ├── reviewAlert() - Teacher review
    └── getMonitoringSummary() - Complete summary with risk assessment
```

### Controllers (2 files)
```
backend/src/main/java/com/mstrust/exam/controller/
├── MonitoringController.java (/api/monitoring)
│   ├── POST   /screenshots              [STUDENT]
│   ├── GET    /screenshots/{id}         [ALL]
│   ├── POST   /activities               [STUDENT]
│   ├── GET    /activities/{id}          [ALL]
│   ├── POST   /alerts                   [STUDENT, ADMIN]
│   └── GET    /summary/{submissionId}   [TEACHER]
│
└── AlertController.java (/api/alerts)
    ├── GET    /submission/{id}          [TEACHER]
    ├── GET    /submission/{id}/unreviewed [TEACHER]
    ├── GET    /exam/{examId}/unreviewed [TEACHER]
    └── POST   /{alertId}/review         [TEACHER]
```

---

## 🔧 FTP Configuration

### application.yml
```yaml
ftp:
  server: 153.92.11.239
  port: 21
  username: u341775345.admin
  password: '!M@nh1989'
  base-path: /screenshots
```

### Directory Structure on FTP
```
/screenshots/
├── 2025-11/                    # Tổ chức theo tháng
│   ├── 1/                      # submission_id = 1
│   │   ├── screenshot_20251121_101234_001.jpg
│   │   ├── screenshot_20251121_101239_002.jpg
│   │   └── ...
│   ├── 2/                      # submission_id = 2
│   │   └── ...
│   └── ...
├── 2025-12/
│   └── ...
```

### Image Processing
- **Max Resolution:** 1920x1080 (auto-resize nếu lớn hơn)
- **Format:** JPEG
- **Quality:** 70%
- **Compression:** Scalr.Method.QUALITY với anti-aliasing

---

## 📡 API Endpoints

### 1. Upload Screenshot
```http
POST /api/monitoring/screenshots
Authorization: Bearer <student-token>
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- submissionId: Long (required)
- screenResolution: String (optional, e.g., "1920x1080")
- windowTitle: String (optional)
- metadata: String (optional, JSON format)

Response: 201 Created
{
  "id": 1,
  "submissionId": 7,
  "filePath": "/screenshots/2025-11/7/screenshot_20251121_101234_001.jpg",
  "fileSize": 245678,
  "timestamp": "2025-11-21T10:12:34",
  "screenResolution": "1920x1080",
  "windowTitle": "MS.TrustTest Exam",
  "metadata": "{}",
  "createdAt": "2025-11-21T10:12:35"
}
```

### 2. Log Activities (Batch)
```http
POST /api/monitoring/activities
Authorization: Bearer <student-token>
Content-Type: application/json

{
  "submissionId": 7,
  "activities": [
    {
      "activityType": "WINDOW_FOCUS",
      "details": "Switched to Chrome browser",
      "timestamp": "2025-11-21T10:10:00"
    },
    {
      "activityType": "CLIPBOARD",
      "details": "Copied text: 'Hello World'",
      "timestamp": "2025-11-21T10:11:00"
    }
  ]
}

Response: 201 Created
[
  {
    "id": 1,
    "submissionId": 7,
    "activityType": "WINDOW_FOCUS",
    "details": "Switched to Chrome browser",
    "timestamp": "2025-11-21T10:10:00",
    "createdAt": "2025-11-21T10:10:01"
  },
  ...
]
```

### 3. Create Alert
```http
POST /api/monitoring/alerts
Authorization: Bearer <student-token>
Content-Type: application/json

{
  "submissionId": 7,
  "alertType": "MULTIPLE_WINDOW_SWITCHES",
  "severity": "HIGH",
  "description": "Detected 15 window switches in 5 minutes"
}

Response: 201 Created
{
  "id": 1,
  "submissionId": 7,
  "studentName": "Nguyễn Văn A",
  "studentCode": "SV001",
  "alertType": "MULTIPLE_WINDOW_SWITCHES",
  "severity": "HIGH",
  "description": "Detected 15 window switches in 5 minutes",
  "reviewed": false,
  "reviewedBy": null,
  "reviewedByName": null,
  "reviewedAt": null,
  "reviewNote": null,
  "createdAt": "2025-11-21T10:15:00"
}
```

### 4. Get Monitoring Summary
```http
GET /api/monitoring/summary/7
Authorization: Bearer <teacher-token>

Response: 200 OK
{
  "submissionId": 7,
  "studentName": "Nguyễn Văn A",
  "studentCode": "SV001",
  "totalScreenshots": 45,
  "totalActivities": 128,
  "windowSwitchCount": 8,
  "clipboardActivityCount": 3,
  "keystrokeAnomalies": 0,
  "totalAlerts": 2,
  "unreviewedAlerts": 1,
  "alertsBySeverity": {
    "HIGH": 1,
    "MEDIUM": 1
  },
  "latestScreenshot": {
    "id": 45,
    "filePath": "/screenshots/2025-11/7/screenshot_20251121_103045_045.jpg",
    "timestamp": "2025-11-21T10:30:45",
    ...
  },
  "riskLevel": "MEDIUM",
  "riskDescription": "Phát hiện 8 lần chuyển cửa sổ trong 30 phút qua. Có 1 cảnh báo chưa được xem xét."
}
```

### 5. Review Alert
```http
POST /api/alerts/1/review
Authorization: Bearer <teacher-token>
Content-Type: application/json

{
  "reviewNote": "Đã kiểm tra, sinh viên chuyển cửa sổ để tham khảo tài liệu được phép"
}

Response: 200 OK
{
  "id": 1,
  "reviewed": true,
  "reviewedBy": 2,
  "reviewedByName": "Giáo viên Nguyễn Thị B",
  "reviewedAt": "2025-11-21T10:20:00",
  "reviewNote": "Đã kiểm tra, sinh viên chuyển cửa sổ để tham khảo tài liệu được phép",
  ...
}
```

---

## 🧪 Testing

### Build Status
```bash
cd backend && mvn clean compile
# Result: BUILD SUCCESS ✅
# Warnings: 31 warnings (Lombok @Builder defaults - không ảnh hưởng)
# Errors: 0
```

### Dependencies Added
```xml
<!-- Apache Commons Net (FTP Client) -->
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.10.0</version>
</dependency>

<!-- Image Scaling Library -->
<dependency>
    <groupId>org.imgscalr</groupId>
    <artifactId>imgscalr-lib</artifactId>
    <version>4.2</version>
</dependency>
```

---

## 🎯 Risk Assessment Algorithm

### Risk Levels
Hệ thống tự động đánh giá mức độ rủi ro dựa trên:

1. **CRITICAL:**
   - Có alerts với severity = CRITICAL
   - Hoặc >20 window switches trong 30 phút

2. **HIGH:**
   - Có alerts với severity = HIGH
   - Hoặc >5 unreviewed alerts

3. **MEDIUM:**
   - Có alerts với severity = MEDIUM
   - Hoặc >5 window switches trong 30 phút

4. **LOW:**
   - Các trường hợp còn lại

---

## 📝 Next Steps

### Phase 6B: JavaFX Client Development (Upcoming)
1. **Screenshot Capture Module**
   - Screen capture với java.awt.Robot
   - Background upload mỗi 30 giây
   - Queue management cho offline mode

2. **Activity Monitor Module**
   - JNativeHook cho global keyboard/mouse events
   - Window focus detection
   - Clipboard monitoring
   - Process detection

3. **Alert Detection Module**
   - Window switch counter (threshold: 10 in 5 minutes)
   - Suspicious process detector
   - Excessive clipboard usage
   - Auto-create alerts

4. **UI Components**
   - Monitoring status indicator
   - Screenshot preview
   - Activity log viewer
   - Alert notifications

---

## ✅ Phase 6A Completion Checklist

- [x] Database migrations (V17, V18, V19)
- [x] Entities & Enums (5 files)
- [x] Repositories with custom queries (3 files)
- [x] DTOs for request/response (8 files)
- [x] Services with business logic (4 files)
- [x] Controllers with REST APIs (2 files)
- [x] FTP storage configuration
- [x] Image compression logic
- [x] Risk assessment algorithm
- [x] Batch activity logging
- [x] Soft delete support
- [x] Build success verification
- [x] Documentation complete

---

## 📚 Related Documentation

- [Phase 5B: WebSocket & Enhanced APIs](./PHASE5B-WEBSOCKET-ENHANCED-APIS.md)
- [Phase 5: Exam Taking & Grading](./PHASE5-COMPLETE-SUMMARY.md)
- [Database Migrations](../backend/src/main/resources/db/migration/)
- [Thunder Client Collection](./thunder-client-phase6a-monitoring.json) *(To be created)*

---

**Phase 6A Status:** ✅ **HOÀN THÀNH**  
**Next Phase:** Phase 6B - JavaFX Client Monitoring Implementation

---
*Document created: 21/11/2025 10:18*  
*Last updated: 21/11/2025 10:18*  
*Author: K24DTCN210-NVMANH*
