# Phase 5B Testing Guide - WebSocket & Enhanced APIs

**Author:** K24DTCN210-NVMANH  
**Date:** 21/11/2025 02:15  
**Purpose:** Step-by-step guide để test Phase 5B features

---

## 📋 Prerequisites

### 1. Server Running
```bash
cd backend
mvn spring-boot:run
```
✅ Server phải chạy trên: `http://localhost:8080`

### 2. Thunder Client Extension
- Install Thunder Client trong VS Code
- Import collection: `docs/thunder-client-phase5b-websocket.json`

### 3. Authentication Tokens
Cần có sẵn 2 tokens:
- `{{teacherToken}}` - Token của teacher (có quyền pause/resume)
- `{{studentToken}}` - Token của student (để start exam)

---

## 🧪 Test Scenarios

### Scenario 1: Get Active Sessions (Empty State)

**Mục đích:** Verify API works khi chưa có active sessions

**Steps:**
1. Mở Thunder Client
2. Chọn request: "Get Active Sessions"
3. Click Send

**Expected Result:**
```json
[]
```
Status: 200 OK

**Notes:** Lúc đầu sẽ empty vì chưa có student nào start exam

---

### Scenario 2: Student Start Exam

**Mục đích:** Tạo active session để test các APIs khác

**Steps:**
1. Chọn request: "Student Start Exam (Setup)"
2. Đảm bảo có `{{studentToken}}` và exam ID=1 tồn tại
3. Click Send

**Expected Result:**
```json
{
  "submissionId": 1,
  "examId": 1,
  "studentId": 10,
  "status": "IN_PROGRESS",
  "startedAt": "2025-11-21T02:00:00",
  "questions": [...]
}
```
Status: 200 OK

**Notes:** 
- Lưu lại `submissionId` để dùng cho tests tiếp theo
- Thunder Client sẽ tự động set `{{testSubmissionId}}`

---

### Scenario 3: Get Active Sessions (With Data)

**Mục đích:** Verify API trả về active sessions

**Steps:**
1. Sau khi student đã start exam
2. Chọn request: "Get Active Sessions"
3. Click Send

**Expected Result:**
```json
[
  {
    "submissionId": 1,
    "status": "IN_PROGRESS",
    "studentId": 10,
    "studentName": "Nguyễn Văn A",
    "studentEmail": "student1@example.com",
    "examId": 1,
    "examTitle": "Midterm Exam",
    "durationMinutes": 90,
    "startedAt": "2025-11-21T02:00:00",
    "remainingMinutes": 90,
    "lastActivity": "2025-11-21T02:00:00",
    "autoSaveCount": 0,
    "totalQuestions": 20,
    "answeredQuestions": 0,
    "progressPercentage": 0.0,
    "isInactive": false
  }
]
```
Status: 200 OK

**Verify:**
- ✅ Array có ít nhất 1 item
- ✅ `status` = "IN_PROGRESS"
- ✅ `progressPercentage` >= 0
- ✅ `remainingMinutes` > 0

---

### Scenario 4: Get Teacher Live View

**Mục đích:** Test real-time monitoring cho teacher

**Steps:**
1. Chọn request: "Get Teacher Live View"
2. URL: `/exam-sessions/live/1` (examId = 1)
3. Click Send

**Expected Result:**
```json
{
  "examId": 1,
  "examTitle": "Midterm Exam",
  "totalActiveSessions": 1,
  "sessions": [
    {
      "submissionId": 1,
      "studentName": "Nguyễn Văn A",
      "progressPercentage": 0.0,
      "remainingMinutes": 90,
      "answeredQuestions": 0,
      "totalQuestions": 20,
      "lastActivity": "2025-11-21T02:00:00",
      "isInactive": false
    }
  ],
  "statistics": {
    "averageProgress": 0.0,
    "averageTimeSpent": 0.0,
    "totalStarted": 1,
    "totalInProgress": 1,
    "totalSubmitted": 0
  },
  "alerts": [],
  "lastUpdated": "2025-11-21T02:00:00"
}
```
Status: 200 OK

**Verify:**
- ✅ `totalActiveSessions` = số student đang làm bài
- ✅ `sessions` array có data
- ✅ `statistics` có đầy đủ fields
- ✅ `lastUpdated` là thời gian hiện tại

---

### Scenario 5: Pause Exam

**Mục đích:** Teacher pause exam của student

**Steps:**
1. Chọn request: "Pause Exam"
2. Update request body với submissionId đúng:
```json
{
  "submissionId": 1,
  "reason": "Technical issue - student laptop crashed",
  "pauseDurationMinutes": 10
}
```
3. Click Send

**Expected Result:**
```json
{
  "success": true,
  "message": "Exam paused successfully",
  "submissionId": 1,
  "newStatus": "PAUSED",
  "pausedAt": "2025-11-21T02:05:00",
  "resumeBy": "2025-11-21T02:15:00"
}
```
Status: 200 OK

**Verify:**
- ✅ `success` = true
- ✅ `newStatus` = "PAUSED"
- ✅ `pausedAt` là thời gian hiện tại
- ✅ `resumeBy` = pausedAt + pauseDurationMinutes

**WebSocket Event:**
Student nhận được notification qua `/queue/exam/1`:
```json
{
  "type": "EXAM_PAUSED",
  "submissionId": 1,
  "reason": "Technical issue - student laptop crashed",
  "timestamp": "2025-11-21T02:05:00"
}
```

---

### Scenario 6: Verify Paused Status

**Mục đích:** Confirm exam đã paused

**Steps:**
1. Chọn request: "Get Active Sessions"
2. Click Send

**Expected Result:**
```json
[]
```

**Why empty?** Vì query chỉ lấy `IN_PROGRESS`, không lấy `PAUSED`

**Alternative Test:**
Query database trực tiếp:
```sql
SELECT * FROM exam_submissions WHERE id = 1;
-- status should be 'PAUSED'
```

---

### Scenario 7: Resume Exam

**Mục đích:** Teacher resume exam

**Steps:**
1. Chọn request: "Resume Exam"
2. Update request body:
```json
{
  "submissionId": 1,
  "additionalMinutes": 5
}
```
3. Click Send

**Expected Result:**
```json
{
  "success": true,
  "message": "Exam resumed successfully",
  "submissionId": 1,
  "newStatus": "IN_PROGRESS",
  "additionalTime": 5,
  "newEndTime": "2025-11-21T03:20:00"
}
```
Status: 200 OK

**Verify:**
- ✅ `success` = true
- ✅ `newStatus` = "IN_PROGRESS"
- ✅ `additionalTime` = 5
- ✅ `newEndTime` đã được cộng thêm 5 phút

**WebSocket Event:**
Student nhận được notification:
```json
{
  "type": "EXAM_RESUMED",
  "submissionId": 1,
  "additionalMinutes": 5,
  "timestamp": "2025-11-21T02:10:00"
}
```

---

### Scenario 8: Verify Resumed Status

**Mục đích:** Confirm exam đã resume

**Steps:**
1. Chọn request: "Get Active Sessions"
2. Click Send

**Expected Result:**
Submission xuất hiện lại trong list:
```json
[
  {
    "submissionId": 1,
    "status": "IN_PROGRESS",
    ...
  }
]
```

---

## 🔍 Error Cases Testing

### Test 1: Pause Non-Existent Submission
```json
POST /exam-sessions/999/pause
{
  "submissionId": 999,
  "reason": "Test",
  "pauseDurationMinutes": 10
}
```

**Expected:** 404 Not Found
```json
{
  "error": "Submission not found"
}
```

---

### Test 2: Pause Already Paused Exam
```json
POST /exam-sessions/1/pause
// (khi exam đã PAUSED)
```

**Expected:** 400 Bad Request
```json
{
  "error": "Can only pause IN_PROGRESS exams"
}
```

---

### Test 3: Resume Non-Paused Exam
```json
POST /exam-sessions/1/resume
// (khi exam đang IN_PROGRESS)
```

**Expected:** 400 Bad Request
```json
{
  "error": "Can only resume PAUSED exams"
}
```

---

### Test 4: Student Try to Pause (Authorization Test)
```json
POST /exam-sessions/1/pause
Authorization: Bearer {{studentToken}}
```

**Expected:** 403 Forbidden
```json
{
  "error": "Access denied"
}
```

---

## 🌐 WebSocket Testing

### Setup WebSocket Client

**Option 1: Browser Console**
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to timer updates
    stompClient.subscribe('/queue/timer/1', function(message) {
        console.log('Timer update:', JSON.parse(message.body));
    });
    
    // Subscribe to exam notifications
    stompClient.subscribe('/queue/exam/1', function(message) {
        console.log('Exam notification:', JSON.parse(message.body));
    });
    
    // Subscribe to progress updates
    stompClient.subscribe('/topic/exam/1/progress', function(message) {
        console.log('Progress update:', JSON.parse(message.body));
    });
});
```

**Option 2: WebSocket Testing Tool**
- Use: https://www.websocket.org/echo.html
- Or: Postman WebSocket feature

---

### Test WebSocket Events

#### 1. Timer Sync Event (Auto every 5 seconds)
**Expected Message on `/queue/timer/1`:**
```json
{
  "submissionId": 1,
  "examId": 1,
  "startedAt": "2025-11-21T02:00:00",
  "durationMinutes": 90,
  "remainingMinutes": 85,
  "status": "IN_PROGRESS",
  "syncTime": "2025-11-21T02:05:00"
}
```

#### 2. Pause Event
**Trigger:** POST /exam-sessions/1/pause  
**Expected Message on `/queue/exam/1`:**
```json
{
  "type": "EXAM_PAUSED",
  "submissionId": 1,
  "reason": "Technical issue",
  "timestamp": "2025-11-21T02:05:00"
}
```

#### 3. Resume Event
**Trigger:** POST /exam-sessions/1/resume  
**Expected Message on `/queue/exam/1`:**
```json
{
  "type": "EXAM_RESUMED",
  "submissionId": 1,
  "additionalMinutes": 5,
  "timestamp": "2025-11-21T02:10:00"
}
```

---

## ✅ Testing Checklist

### REST APIs
- [ ] Get Active Sessions (empty)
- [ ] Student Start Exam
- [ ] Get Active Sessions (with data)
- [ ] Get Teacher Live View
- [ ] Pause Exam
- [ ] Verify paused status
- [ ] Resume Exam
- [ ] Verify resumed status

### Error Handling
- [ ] Pause non-existent submission
- [ ] Pause already paused exam
- [ ] Resume non-paused exam
- [ ] Student try to pause (403)

### WebSocket
- [ ] Timer sync events (every 5s)
- [ ] Pause notification
- [ ] Resume notification
- [ ] Connection/disconnection

### Performance
- [ ] Multiple active sessions (load test)
- [ ] Concurrent pause/resume requests
- [ ] WebSocket connection stability

---

## 🐛 Common Issues & Solutions

### Issue 1: Port 8080 already in use
**Solution:**
```bash
# Kill process on port 8080
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```

### Issue 2: WebSocket connection failed
**Causes:**
- CORS policy
- Server not running
- Wrong endpoint URL

**Solution:**
- Check server logs
- Verify WebSocket endpoint: `/ws`
- Check browser console for errors

### Issue 3: 401 Unauthorized
**Causes:**
- Token expired
- Token not set in environment

**Solution:**
- Re-login to get fresh token
- Update `{{teacherToken}}` and `{{studentToken}}` in Thunder Client

---

## 📊 Test Results Template

```
Test Date: 21/11/2025
Tester: K24DTCN210-NVMANH
Environment: Local Development

| Test Case | Status | Notes |
|-----------|--------|-------|
| Get Active Sessions (empty) | ✅ PASS | Returns [] |
| Student Start Exam | ✅ PASS | submissionId=1 |
| Get Active Sessions (data) | ✅ PASS | 1 session found |
| Get Teacher Live View | ✅ PASS | Statistics correct |
| Pause Exam | ✅ PASS | Status changed to PAUSED |
| Resume Exam | ✅ PASS | Status back to IN_PROGRESS |
| WebSocket Timer Sync | ✅ PASS | Events every 5s |
| Pause Notification | ✅ PASS | Student received |
| Resume Notification | ✅ PASS | Student received |

Overall: 9/9 tests passed (100%)
```

---

**End of Testing Guide**
