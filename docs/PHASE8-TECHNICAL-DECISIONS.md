# Phase 8: Exam Taking UI - Technical Decisions
## Tài Liệu Quyết Định Kỹ Thuật

**Author:** K24DTCN210-NVMANH  
**Date:** 23/11/2025  
**Version:** 1.0

---

## 1. ⏱️ Timer Synchronization Strategy

### Decision: Sử dụng WebSocket Timer Sync (ĐÃ CÓ SẴN)

**Phân Tích:**
- Backend đã implement `ExamTimerSyncMessage` DTO trong Phase 5B
- WebSocket topic: `/topic/exam/{examId}/timer`
- Server broadcast timer updates real-time

**Implementation:**
```java
// Client subscribe:
stompClient.subscribe("/topic/exam/" + examId + "/timer", message -> {
    ExamTimerSyncMessage timerSync = gson.fromJson(message.getPayload(), 
        ExamTimerSyncMessage.class);
    updateTimerUI(timerSync.getRemainingSeconds());
});
```

**Advantages:**
- ✅ Server-side time authority (không thể cheat bằng system clock)
- ✅ Tự động sync tất cả clients
- ✅ Handle pause/resume từ giáo viên
- ✅ Đã được test trong Phase 5B

**No Need for:**
- ❌ KHÔNG cần NTP client
- ❌ KHÔNG cần REST API `/api/server-time`

---

## 2. 💾 Local Queue & Offline Storage Strategy

### Decision: In-Memory Queue + File-based Backup (KHÔNG dùng SQLite)

**Rationale:**
- SQLite là overkill cho use case này
- Exam sessions thường ngắn (1-2 giờ)
- Offline scenario rất hiếm (mất mạng vài phút)

**Implementation:**

**A. In-Memory Queue:**
```java
public class AnswerQueue {
    private final ConcurrentLinkedQueue<AnswerData> pendingAnswers;
    private final ScheduledExecutorService retryService;
    
    // Retry every 5 seconds
    private void startRetryLoop() {
        retryService.scheduleAtFixedRate(() -> {
            if (isOnline()) {
                flushQueue();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
```

**B. File-based Backup (JSON):**
```java
// Persist to file khi có answers pending
private void persistToFile() {
    Path backupFile = Paths.get(System.getProperty("user.home"), 
        ".mstrust", "answers_backup.json");
    Files.writeString(backupFile, gson.toJson(pendingAnswers));
}

// Load on startup
private void loadFromFile() {
    // Restore unsent answers nếu app crash
}
```

**Advantages:**
- ✅ Simple implementation
- ✅ No external dependencies
- ✅ Fast in-memory operations
- ✅ File backup cho crash recovery

---

## 3. 📝 Code Editor Component

### Decision: RichTextFX với Basic Syntax Highlighting

**Library:** `org.fxmisc.richtext:richtextfx:0.11.2` (MIT License, FREE)

**Why RichTextFX:**
- ✅ Native JavaFX integration
- ✅ Lightweight (~500KB)
- ✅ MIT License (free commercial use)
- ✅ Good documentation
- ✅ Syntax highlighting support

**Alternative Considered:**
- ❌ Plain TextArea - Too basic, no syntax highlighting
- ❌ JCodeEditor - Unmaintained since 2018
- ❌ RSyntaxTextArea - Swing component, không fit với JavaFX

**Implementation:**
```java
// Simple Java syntax highlighting
CodeArea codeEditor = new CodeArea();
codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));

// Basic keyword highlighting
codeEditor.textProperty().addListener((obs, oldText, newText) -> {
    codeEditor.setStyleSpans(0, computeHighlighting(newText));
});
```

**Supported Languages (Phase 8):**
- Java
- Python
- C/C++
- JavaScript

---

## 4. 🔒 Full-Screen Mode & Alt+Tab Blocking

### Decision: Configurable Full-Screen với Optional Blocking

**Implementation Strategy:**

**A. Full-Screen Mode (JavaFX Native):**
```java
stage.setFullScreen(true);
stage.setFullScreenExitHint(""); // Hide exit hint
stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Disable ESC
```

**B. Alt+Tab Blocking (Optional, JNA-based):**
```java
public class FullScreenHelper {
    private boolean blockAltTab = false; // Configurable
    
    public void enableStrictMode() {
        if (blockAltTab && isWindows()) {
            // Hook low-level keyboard (JNA)
            blockAltTabWindows();
        }
    }
    
    private void blockAltTabWindows() {
        // Use JNativeHook (already in dependencies)
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_TAB 
                    && (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
                    // Consume event (block Alt+Tab)
                    try {
                        GlobalScreen.removeNativeKeyListener(this);
                        GlobalScreen.addNativeKeyListener(this);
                    } catch (Exception ex) {
                        // Best effort
                    }
                }
            }
        });
    }
}
```

**Configuration (config.properties):**
```properties
exam.fullscreen.enabled=true
exam.fullscreen.block_alt_tab=false  # Default: Allow escape
exam.fullscreen.exit_warning=true
```

**Warning Levels:**
1. **Soft Mode (Default):** Full-screen, but can exit
2. **Strict Mode (Optional):** Block Alt+Tab on Windows
3. **Exit Warning:** Dialog khi sinh viên thoát full-screen

---

## 5. 🌐 Network Handling & Reconnection

### Decision: Exponential Backoff với Connection Status Indicator

**Implementation:**

**A. Network Monitor:**
```java
public class NetworkMonitor {
    private final ScheduledExecutorService healthChecker;
    private boolean isOnline = true;
    
    public void startMonitoring() {
        healthChecker.scheduleAtFixedRate(() -> {
            boolean wasOnline = isOnline;
            isOnline = checkConnection();
            
            if (wasOnline && !isOnline) {
                onConnectionLost();
            } else if (!wasOnline && isOnline) {
                onConnectionRestored();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
    
    private boolean checkConnection() {
        try {
            URL url = new URL(apiBaseUrl + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
```

**B. Connection Recovery Service:**
```java
public class ConnectionRecoveryService {
    private final ExponentialBackoff backoff = new ExponentialBackoff(
        1000,  // Initial delay: 1s
        30000, // Max delay: 30s
        2.0    // Multiplier
    );
    
    public void attemptReconnect() {
        int attempt = 0;
        while (!isConnected() && attempt < 10) {
            try {
                Thread.sleep(backoff.getDelay(attempt));
                reconnectWebSocket();
                flushPendingAnswers();
                attempt++;
            } catch (Exception e) {
                log.error("Reconnect failed: {}", e.getMessage());
            }
        }
    }
}
```

**C. UI Indicators:**
```
🟢 Connected - Green indicator
🟡 Reconnecting... - Yellow indicator with spinner
🔴 Offline - Red indicator with retry button
```

---

## 6. 📦 Additional Dependencies

### Thêm vào `client-javafx/pom.xml`:

```xml
<!-- RichTextFX for code editor -->
<dependency>
    <groupId>org.fxmisc.richtext</groupId>
    <artifactId>richtextfx</artifactId>
    <version>0.11.2</version>
</dependency>

<!-- OkHttp for better HTTP client -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- WebSocket client (Tyrus) -->
<dependency>
    <groupId>org.glassfish.tyrus.bundles</groupId>
    <artifactId>tyrus-standalone-client</artifactId>
    <version>2.1.3</version>
</dependency>

<!-- Apache Commons Lang (utilities) -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>
```

---

## 7. 🎨 Question Type Rendering Strategy

### Factory Pattern cho Answer Inputs

```java
public class AnswerInputFactory {
    
    public Node createInput(QuestionType type, String existingAnswer) {
        return switch (type) {
            case MULTIPLE_CHOICE -> createRadioButtons();
            case MULTIPLE_SELECT -> createCheckBoxes();
            case TRUE_FALSE -> createTrueFalseButtons();
            case ESSAY -> createTextArea(10);
            case SHORT_ANSWER -> createTextField();
            case CODING -> createCodeEditor();
            case FILL_IN_BLANK -> createMultipleTextFields();
            case MATCHING -> createMatchingInterface();
        };
    }
    
    private Node createCodeEditor() {
        CodeArea codeArea = new CodeArea();
        codeArea.setPrefHeight(400);
        // Syntax highlighting setup
        return codeArea;
    }
    
    private Node createMatchingInterface() {
        // Two-column layout với drag-drop
        VBox container = new VBox(10);
        // Left column: Questions
        // Right column: Answers (draggable)
        return container;
    }
}
```

---

## 8. 💾 Auto-Save Strategy

### Tiered Auto-Save với Debouncing

```java
public class AutoSaveService {
    private final ScheduledExecutorService scheduler;
    private final Map<Long, ScheduledFuture<?>> saveTasks;
    
    // Strategy 1: Periodic auto-save (every 30s)
    public void startPeriodicSave(Long submissionId) {
        saveTasks.put(submissionId, scheduler.scheduleAtFixedRate(
            () -> saveAllAnswers(submissionId),
            30, 30, TimeUnit.SECONDS
        ));
    }
    
    // Strategy 2: On question change (debounced)
    private final Map<Long, Long> lastSaveTime = new ConcurrentHashMap<>();
    
    public void saveOnQuestionChange(Long questionId, String answer) {
        long now = System.currentTimeMillis();
        Long lastSave = lastSaveTime.get(questionId);
        
        // Debounce: Min 2 seconds between saves
        if (lastSave == null || (now - lastSave) > 2000) {
            saveAnswer(questionId, answer, true); // isAutoSave=true
            lastSaveTime.put(questionId, now);
        }
    }
}
```

---

## 9. 📊 Performance Targets

| Metric | Target | Critical |
|--------|--------|----------|
| Timer accuracy | ±1 second | ✅ |
| Auto-save frequency | Every 30s | ✅ |
| Question navigation | < 100ms | ✅ |
| Network reconnect | < 30s | ✅ |
| Memory usage | < 500MB | ⚠️ |
| Startup time | < 3s | ⚠️ |

---

## 10. 🔄 Migration from Phase 6B

### Reuse Existing Components:

**From Monitoring Client:**
- ✅ `MonitoringApiClient` pattern
- ✅ JWT authentication flow
- ✅ Config loading (`AppConfig`)
- ✅ Error handling patterns

**New for Exam Taking:**
- ⭐ WebSocket client (STOMP)
- ⭐ Answer queue system
- ⭐ Timer component
- ⭐ Question rendering

---

## Summary of Decisions

1. **Timer Sync:** WebSocket-based (existing infrastructure)
2. **Offline Storage:** In-memory queue + JSON file backup
3. **Code Editor:** RichTextFX (MIT, free)
4. **Full-Screen:** Configurable with optional Alt+Tab blocking (JNativeHook)
5. **Network Recovery:** Exponential backoff + health check
6. **Dependencies:** 4 new libraries (all free, well-maintained)
7. **Question Rendering:** Factory pattern
8. **Auto-Save:** Tiered strategy (periodic + on-change)

---

**Approval Status:** ✅ READY FOR IMPLEMENTATION

**Next Step:** Create project structure and begin Phase 8.1
