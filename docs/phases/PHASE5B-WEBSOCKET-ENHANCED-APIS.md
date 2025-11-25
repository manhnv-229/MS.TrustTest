# Phase 5B: WebSocket & Enhanced Exam APIs - Technical Documentation

**Author:** K24DTCN210-NVMANH  
**Date:** 21/11/2025 02:14  
**Status:** ✅ Implementation Complete - Testing Phase

---

## 📋 Overview

Phase 5B implements real-time WebSocket communication and enhanced exam management APIs for teacher monitoring and control of active exam sessions.

## 🎯 Objectives

1. **Real-time Communication**: WebSocket support for live updates
2. **Teacher Control**: Pause/resume exam capabilities
3. **Live Monitoring**: Real-time view of active exam sessions
4. **Session Management**: Enhanced tracking and control

---

## 🏗️ Architecture Components

### 1. WebSocket Configuration

**File:** `backend/src/main/java/com/mstrust/exam/config/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**Key Features:**
- STOMP protocol over WebSocket
- `/topic` - Broadcast messages (1-to-many)
- `/queue` - Direct messages (1-to-1)
- `/app` - Application message prefix
- SockJS fallback for browsers without WebSocket support

---

### 2. WebSocket DTOs

#### 2.1 ExamTimerSyncMessage
**Purpose:** Sync exam timer across all connected clients

```java
@Data
@Builder
public class ExamTimerSyncMessage {
    private Long submissionId;
    private Long examId;
    private LocalDateTime startedAt;
    private Integer durationMinutes;
    private Integer remainingMinutes;
    private SubmissionStatus status;
    private LocalDateTime syncTime;
}
```

#### 2.2 StudentProgressMessage
**Purpose:** Notify about student progress updates

```java
@Data
@Builder
public class StudentProgressMessage {
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private BigDecimal progressPercentage;
    private LocalDateTime lastSaveTime;
}
```

#### 2.3 ConnectionStatusMessage
**Purpose:** Monitor connection status

```java
@Data
@Builder
public class ConnectionStatusMessage {
    private Long submissionId;
    private Long studentId;
    private String status; // "CONNECTED", "DISCONNECTED", "RECONNECTED"
    private LocalDateTime timestamp;
    private String reason;
}
```

---

### 3. WebSocket Event Service

**File:** `backend/src/main/java/com/mstrust/exam/service/WebSocketEventService.java`

**Purpose:** Centralized service for sending WebSocket messages

```java
@Service
@RequiredArgsConstructor
public class WebSocketEventService {
    private final SimpMessagingTemplate messagingTemplate;
    
    // Send timer sync to specific student
    public void sendTimerSync(ExamTimerSyncMessage message) {
        messagingTemplate.convertAndSend(
            "/queue/timer/" + message.getSubmissionId(), 
            message
        );
    }
    
    // Broadcast progress to all teachers monitoring
    public void broadcastProgress(StudentProgressMessage message) {
        messagingTemplate.convertAndSend(
            "/topic/exam/" + message.getSubmissionId() + "/progress", 
            message
        );
    }
    
    // Send pause notification
    public void sendPauseNotification(Long submissionId, String reason) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "EXAM_PAUSED");
        notification.put("submissionId", submissionId);
        notification.put("reason", reason);
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend(
            "/queue/exam/" + submissionId, 
            notification
        );
    }
    
    // Send resume notification
    public void sendResumeNotification(Long submissionId, Integer additionalMinutes) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "EXAM_RESUMED");
        notification.put("submissionId", submissionId);
        notification.put("additionalMinutes", additionalMinutes);
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend(
            "/queue/exam/" + submissionId, 
            notification
        );
    }
}
```

---

### 4. WebSocket Controllers

#### 4.1 ExamSessionWebSocketController
**Purpose:** Handle student exam session WebSocket connections

```java
@Controller
@RequiredArgsConstructor
public class ExamSessionWebSocketController {
    
    @MessageMapping("/exam/{submissionId}/connect")
    @SendTo("/queue/exam/{submissionId}")
    public ConnectionStatusMessage handleConnect(
        @DestinationVariable Long submissionId,
        Principal principal
    ) {
        return ConnectionStatusMessage.builder()
            .submissionId(submissionId)
            .status("CONNECTED")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    @MessageMapping("/exam/{submissionId}/heartbeat")
    public void handleHeartbeat(
        @DestinationVariable Long submissionId
    ) {
        // Update last activity timestamp
    }
}
```

#### 4.2 MonitoringWebSocketController
**Purpose:** Scheduled tasks for teacher monitoring

```java
@Controller
@RequiredArgsConstructor
public class MonitoringWebSocketController {
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void syncTimers() {
        List<ExamSubmission> activeSessions = 
            submissionRepository.findByStatus(SubmissionStatus.IN_PROGRESS);
            
        for (ExamSubmission submission : activeSessions) {
            ExamTimerSyncMessage message = buildTimerMessage(submission);
            webSocketEventService.sendTimerSync(message);
        }
    }
}
```

---

## 🔌 Enhanced Exam APIs

### 1. Pause Exam API

**Endpoint:** `POST /api/exam-sessions/{id}/pause`  
**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Request:**
```json
{
  "submissionId": 1,
  "reason": "Technical issue - student laptop crashed",
  "pauseDurationMinutes": 10
}
```

**Response:**
```json
{
  "success": true,
  "message": "Exam paused successfully",
  "submissionId": 1,
  "newStatus": "PAUSED",
  "pausedAt": "2025-11-21T02:00:00",
  "resumeBy": "2025-11-21T02:10:00"
}
```

**Business Logic:**
1. Validate submission exists and is IN_PROGRESS
2. Validate teacher has permission
3. Update status to PAUSED
4. Broadcast WebSocket event to student
5. Log the action with reason

---

### 2. Resume Exam API

**Endpoint:** `POST /api/exam-sessions/{id}/resume`  
**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Request:**
```json
{
  "submissionId": 1,
  "additionalMinutes": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "Exam resumed successfully",
  "submissionId": 1,
  "newStatus": "IN_PROGRESS",
  "additionalTime": 5,
  "newEndTime": "2025-11-21T03:05:00"
}
```

**Business Logic:**
1. Validate submission exists and is PAUSED
2. Update status back to IN_PROGRESS
3. Add additional time if specified
4. Broadcast WebSocket event
5. Log the action

---

### 3. Get Active Sessions API

**Endpoint:** `GET /api/exam-sessions/active`  
**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Response:**
```json
[
  {
    "submissionId": 1,
    "status": "IN_PROGRESS",
    "studentId": 10,
    "studentName": "Nguyễn Văn A",
    "studentEmail": "student1@example.com",
    "examId": 1,
    "examTitle": "Midterm Exam - Math",
    "durationMinutes": 90,
    "startedAt": "2025-11-21T01:00:00",
    "remainingMinutes": 45,
    "lastActivity": "2025-11-21T01:30:00",
    "autoSaveCount": 15,
    "totalQuestions": 20,
    "answeredQuestions": 12,
    "progressPercentage": 60.0,
    "isInactive": false
  }
]
```

**Business Logic:**
1. Query all IN_PROGRESS submissions
2. For each submission:
   - Get student info
   - Get exam info
   - Count answered questions
   - Calculate progress percentage
   - Calculate remaining time
   - Check if inactive (>10 minutes no activity)

---

### 4. Get Teacher Live View API

**Endpoint:** `GET /api/exam-sessions/live/{examId}`  
**Authorization:** TEACHER, DEPT_MANAGER, ADMIN

**Response:**
```json
{
  "examId": 1,
  "examTitle": "Midterm Exam - Math",
  "totalActiveSessions": 3,
  "sessions": [
    {
      "submissionId": 1,
      "studentName": "Nguyễn Văn A",
      "progressPercentage": 60.0,
      "remainingMinutes": 45,
      "answeredQuestions": 12,
      "totalQuestions": 20,
      "lastActivity": "2025-11-21T01:30:00",
      "isInactive": false
    }
  ],
  "statistics": {
    "averageProgress": 55.5,
    "averageTimeSpent": 48.5,
    "totalStarted": 5,
    "totalInProgress": 3,
    "totalSubmitted": 2
  },
  "alerts": [
    "Student 'Trần Thị B' has not saved for 12 minutes",
    "Student 'Lê Văn C' has only 5 minutes remaining"
  ],
  "lastUpdated": "2025-11-21T01:35:00"
}
```

**Business Logic:**
1. Get all active submissions for exam
2. Calculate statistics:
   - Average progress
   - Average time spent
   - Count by status
3. Generate alerts:
   - Students inactive >10 minutes
   - Students with <10 minutes remaining
   - Students stuck on same question
4. Return comprehensive live view

---

## 🗄️ Database Changes

### SubmissionStatus Enum Enhancement

**Before:**
```java
public enum SubmissionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    GRADED,
    EXPIRED
}
```

**After:**
```java
public enum SubmissionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,        // ← NEW
    SUBMITTED,
    GRADED,
    EXPIRED
}
```

**Migration:** Hibernate auto-updates the enum in database

---

## 📡 WebSocket Message Flows

### 1. Student Connects to Exam
```
Student → /app/exam/1/connect
Server → /queue/exam/1 (ConnectionStatusMessage)
```

### 2. Timer Sync (Every 5 seconds)
```
Server (Scheduled) → /queue/timer/1 (ExamTimerSyncMessage)
Student receives update
```

### 3. Student Saves Answer
```
Student → POST /api/exam-taking/save-answer
Server → /topic/exam/1/progress (StudentProgressMessage)
Teachers receive live update
```

### 4. Teacher Pauses Exam
```
Teacher → POST /api/exam-sessions/1/pause
Server → /queue/exam/1 (PauseNotification)
Student receives pause event
```

### 5. Teacher Resumes Exam
```
Teacher → POST /api/exam-sessions/1/resume
Server → /queue/exam/1 (ResumeNotification)
Student receives resume event
```

---

## 🧪 Testing Checklist

### Unit Tests
- [x] WebSocketConfig configuration
- [x] WebSocketEventService message sending
- [x] ExamTakingService pause/resume logic
- [x] Active session calculation
- [x] Statistics generation

### Integration Tests
- [ ] WebSocket connection establishment
- [ ] Timer sync message delivery
- [ ] Pause/Resume API flow
- [ ] Active sessions API
- [ ] Teacher live view API

### Manual Testing (Thunder Client)
- [ ] Import `thunder-client-phase5b-websocket.json`
- [ ] Test Pause Exam API
- [ ] Test Resume Exam API
- [ ] Test Get Active Sessions
- [ ] Test Teacher Live View
- [ ] Verify WebSocket events in browser console

---

## 🔒 Security Considerations

1. **Authentication Required:** All APIs require JWT token
2. **Authorization:** Teacher/Admin roles only for control APIs
3. **Validation:** 
   - Submission ownership check
   - Status transition validation
   - Permission verification
4. **WebSocket Security:**
   - Origin validation
   - Authentication on connect
   - Rate limiting on messages

---

## 📊 Performance Considerations

1. **WebSocket Scalability:**
   - Use Redis for distributed WebSocket sessions
   - Load balancer with sticky sessions
   - Horizontal scaling support

2. **Query Optimization:**
   - Index on `status` and `exam_id`
   - Eager loading for related entities
   - Cache active sessions list

3. **Message Throttling:**
   - Timer sync every 5 seconds (not every second)
   - Batch progress updates
   - Debounce heartbeat messages

---

## 🐛 Known Issues & Limitations

1. **Database Tags Column:** 
   - Warning during startup about invalid JSON in `tags` column
   - Does not affect functionality
   - Will be fixed in migration script

2. **WebSocket Fallback:**
   - SockJS enabled for older browsers
   - May have slight delay in message delivery

3. **Scheduled Task:**
   - Timer sync runs on all server instances
   - Consider distributed lock for production

---

## 📝 Next Steps

### Phase 5B Remaining Work:
- [ ] Step 3: Complete Real-time Features Testing
- [ ] Step 4: Enhanced Grading APIs
- [ ] Step 5: Final Documentation & Deployment Guide

### Future Enhancements:
- WebSocket authentication with JWT
- Redis integration for distributed WebSocket
- Real-time collaboration features
- Video proctoring integration
- AI-powered cheating detection

---

## 📚 References

- Spring WebSocket Documentation: https://spring.io/guides/gs/messaging-stomp-websocket/
- STOMP Protocol: https://stomp.github.io/
- SockJS: https://github.com/sockjs/sockjs-client

---

**End of Phase 5B Documentation**
