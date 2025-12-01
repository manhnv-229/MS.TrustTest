# Hướng Dẫn Test Monitors - Phase 11

## Cách 1: Sử dụng MonitorTestApplication (Khuyến nghị)

### Chạy Test Application:

```bash
# Từ thư mục client-javafx
mvn javafx:run -Djavafx.mainClass=com.mstrust.client.monitoring.test.MonitorTestApplication
```

Hoặc compile và chạy trực tiếp:

```bash
cd client-javafx
mvn clean compile
java --module-path <javafx-path> --add-modules javafx.controls,javafx.fxml -cp target/classes com.mstrust.client.monitoring.test.MonitorTestApplication
```

### Test Steps:

1. **Start Monitoring:**
   - Nhập Submission ID (ví dụ: 1)
   - Nhập Auth Token (có thể dùng "test-token" nếu chưa có backend)
   - Click "Start Monitoring"

2. **Test ScreenCaptureMonitor:**
   - Chờ 30-120 giây (random interval)
   - Screenshot sẽ tự động được chụp
   - Xem số lượng captures trong stats

3. **Test WindowFocusMonitor:**
   - Nhấn Alt+Tab để switch windows
   - Xem số lượng window switches tăng trong stats
   - Thử mở Chrome/Firefox (forbidden app)

4. **Test ProcessMonitor:**
   - Mở một blacklisted process (Chrome, TeamViewer, etc.)
   - Xem trong stats có hiển thị blacklisted process không
   - Process được scan mỗi 10 giây

5. **Test ClipboardMonitor:**
   - Copy text (Ctrl+C)
   - Paste text (Ctrl+V)
   - Xem số lượng operations tăng trong stats
   - Thử paste text dài (> 100 chars) để trigger alert

6. **Test KeystrokeAnalyzer:**
   - Gõ phím bình thường
   - Xem WPM (Words Per Minute) được tính
   - Thử paste text (Ctrl+V) - sẽ detect instant text
   - Xem có alert về suspicious paste pattern không

7. **Stop Monitoring:**
   - Click "Stop Monitoring"
   - Xem final stats

---

## Cách 2: Test trong ExamTakingController

### Tích hợp vào Exam Taking Flow:

1. Khi student bắt đầu làm bài, gọi:
```java
monitoringCoordinator.startMonitoring(submissionId, authToken);
```

2. Khi submit bài, gọi:
```java
monitoringCoordinator.stopMonitoring();
```

3. Xem stats:
```java
String stats = monitoringCoordinator.getStats();
System.out.println(stats);
```

---

## Cách 3: Unit Tests (Nếu cần)

Tạo test class trong `src/test/java`:

```java
@Test
public void testScreenCaptureMonitor() {
    ScreenCaptureMonitor monitor = new ScreenCaptureMonitor(apiClient);
    monitor.start(1L);
    // Wait và verify
    assertTrue(monitor.isRunning());
    monitor.stop();
}
```

---

## Expected Results:

### ScreenCaptureMonitor:
- ✅ Screenshot được chụp mỗi 30-120s (random)
- ✅ File được upload lên backend
- ✅ Capture count tăng dần

### WindowFocusMonitor:
- ✅ Detect window switches khi Alt+Tab
- ✅ Log mọi window change
- ✅ Detect forbidden apps

### ProcessMonitor:
- ✅ Scan processes mỗi 10s
- ✅ Detect blacklisted processes
- ✅ Tạo alert khi phát hiện

### ClipboardMonitor:
- ✅ Detect copy/paste operations
- ✅ Track clipboard history
- ✅ Alert khi paste lớn

### KeystrokeAnalyzer:
- ✅ Track keystrokes
- ✅ Calculate WPM
- ✅ Detect paste vs typing
- ✅ Alert khi suspicious patterns

---

## Troubleshooting:

### Monitors không start:
- Check logs trong console
- Verify API client có auth token
- Check backend đang chạy (nếu test với real backend)

### KeystrokeAnalyzer không hoạt động:
- Cần native hook permissions
- Trên macOS/Linux có thể cần sudo
- Check JNativeHook dependencies

### Screenshot không upload:
- Check network connection
- Verify backend API endpoint
- Check FTP configuration (nếu dùng FTP)

---

## Performance Checks:

- CPU usage < 5%
- Memory usage < 100MB
- No memory leaks (test start/stop nhiều lần)
- No crashes khi stop/start liên tục

---

**Author**: K24DTCN210-NVMANH  
**Date**: 01/12/2025

