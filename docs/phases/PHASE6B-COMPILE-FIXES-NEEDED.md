# Phase 6B: Compile Errors và Cách Fix

## Tổng Quan

Phase 6B đã hoàn thành **17/20 files** (85%). Có 5 lỗi compile cần fix trước khi chạy được.

## ✅ Files Đã Hoàn Thành (17 files)

### Foundation (4 files)
1. ✅ pom.xml
2. ✅ module-info.java (đã fix)
3. ✅ config.properties
4. ✅ AppConfig.java

### DTOs (5 files)
5. ✅ ActivityType.java
6. ✅ AlertSeverity.java
7. ✅ ActivityData.java
8. ✅ ActivityLogRequest.java
9. ✅ AlertCreateRequest.java

### API (1 file)
10. ✅ MonitoringApiClient.java

### Utilities (2 files)
11. ✅ WindowDetector.java
12. ✅ ProcessDetector.java

### Core Services (4 files)
13. ✅ ScreenshotCaptureService.java
14. ✅ AlertDetectionService.java
15. ✅ MonitoringCoordinator.java

### Main App (1 file)
16. ✅ ExamMonitoringApplication.java

### Docs (1 file)
17. ✅ PHASE6B-JAVAFX-CLIENT-PROGRESS.md

## ❌ Lỗi Compile Cần Fix (5 lỗi)

### 1. AppConfig.java - Thiếu 2 methods

**File:** `client-javafx/src/main/java/com/mstrust/client/config/AppConfig.java`

**Lỗi:**
```
cannot find symbol: method getWindowSwitchThreshold()
cannot find symbol: method getClipboardThreshold()
```

**Fix:** Thêm 2 methods vào AppConfig.java:

```java
/* ---------------------------------------------------
 * Lấy threshold cho window switches (alerts)
 * @returns int threshold (mặc định: 10)
 * @author: K24DTCN210-NVMANH (21/11/2025 11:40)
 * --------------------------------------------------- */
public int getWindowSwitchThreshold() {
    String value = properties.getProperty("monitoring.window_switch_threshold", "10");
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        logger.warn("Invalid window_switch_threshold, using default: 10");
        return 10;
    }
}

/* ---------------------------------------------------
 * Lấy threshold cho clipboard operations (alerts)
 * @returns int threshold (mặc định: 20)
 * @author: K24DTCN210-NVMANH (21/11/2025 11:40)
 * --------------------------------------------------- */
public int getClipboardThreshold() {
    String value = properties.getProperty("monitoring.clipboard_threshold", "20");
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        logger.warn("Invalid clipboard_threshold, using default: 20");
        return 20;
    }
}
```

Và thêm vào `config.properties`:
```properties
# Alert thresholds
monitoring.window_switch_threshold=10
monitoring.clipboard_threshold=20
```

### 2. WindowDetector.java - Thiếu isWindows() method

**File:** `client-javafx/src/main/java/com/mstrust/client/util/WindowDetector.java`

**Lỗi:**
```
cannot find symbol: method isWindows()
```

**Fix:** Thêm method vào WindowDetector.java:

```java
/* ---------------------------------------------------
 * Kiểm tra OS có phải Windows không
 * @returns true nếu là Windows
 * @author: K24DTCN210-NVMANH (21/11/2025 11:40)
 * --------------------------------------------------- */
public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("win");
}
```

### 3. WindowDetector.java - JNA IntByReference issue

**File:** `client-javafx/src/main/java/com/mstrust/client/util/WindowDetector.java`
**Line:** 75

**Lỗi:**
```
incompatible types: int[] cannot be converted to com.sun.jna.ptr.IntByReference
```

**Fix:** Thay đổi line 75:

```java
// SAI:
int[] pid = new int[1];
User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);

// ĐÚNG:
IntByReference pid = new IntByReference();
User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
return pid.getValue();
```

Import thêm:
```java
import com.sun.jna.ptr.IntByReference;
```

## 🔧 Các Bước Fix

### Bước 1: Fix AppConfig.java
1. Mở file `client-javafx/src/main/java/com/mstrust/client/config/AppConfig.java`
2. Thêm 2 methods: `getWindowSwitchThreshold()` và `getClipboardThreshold()`
3. Cập nhật `client-javafx/src/main/resources/config.properties`

### Bước 2: Fix WindowDetector.java
1. Mở file `client-javafx/src/main/java/com/mstrust/client/util/WindowDetector.java`
2. Thêm method `isWindows()`
3. Fix line 75: Đổi `int[]` thành `IntByReference`
4. Add import: `import com.sun.jna.ptr.IntByReference;`

### Bước 3: Compile lại
```bash
cd client-javafx
mvn clean compile
```

## 📊 Tình Trạng Sau Fix

Sau khi fix 5 lỗi trên, project sẽ:
- ✅ Compile thành công
- ✅ Sẵn sàng chạy với: `mvn javafx:run`
- ✅ Có thể package thành JAR

## 🎯 Chức Năng Đã Implement

### Screenshot Capture
- ✅ Auto capture every 30s
- ✅ Compress & upload to backend
- ✅ Temp file management
- ✅ Get screen resolution & window title

### Alert Detection
- ✅ Window switch threshold (10 in 5 minutes)
- ✅ Clipboard threshold (20 in 10 minutes)
- ✅ Blacklisted process detection
- ✅ Auto-create alerts via API

### Activity Logging
- ✅ Batch upload every 60s
- ✅ Window focus tracking
- ✅ Clipboard operations
- ✅ Process detection

### Monitoring Coordinator
- ✅ Centralized control
- ✅ Start/stop monitoring
- ✅ Activity buffer management
- ✅ Statistics tracking

### UI Application
- ✅ JavaFX GUI
- ✅ Start/stop controls
- ✅ Status indicator
- ✅ Activity log display

## 📝 Testing Steps (Sau khi fix)

### 1. Compile
```bash
cd client-javafx
mvn clean compile
```

### 2. Run Application
```bash
mvn javafx:run
```

### 3. Test Workflow
1. Start backend server (port 8080)
2. Login to get JWT token
3. Enter Submission ID & Token in UI
4. Click "Start Monitoring"
5. Verify:
   - Screenshots uploading every 30s
   - Activities logging every 60s
   - Alerts created when thresholds exceeded

## 🚀 Next Steps (Optional Enhancements)

1. **Network Queue Manager** - Offline mode support
2. **Clipboard Monitor** - Real-time clipboard tracking
3. **Activity Monitor với JNativeHook** - Global keyboard/mouse hooks
4. **UI Enhancements** - Charts, notifications
5. **Configuration UI** - Settings panel
6. **Logging Panel** - View uploaded data

## 📚 Documentation Created

1. ✅ PHASE6A-MONITORING-BACKEND-COMPLETE.md
2. ✅ PHASE6B-JAVAFX-CLIENT-PROGRESS.md
3. ✅ PHASE6B-COMPILE-FIXES-NEEDED.md (this file)

---
**Status:** 85% Complete - 5 compilation errors remaining
**Author:** K24DTCN210-NVMANH
**Date:** 21/11/2025 11:40
