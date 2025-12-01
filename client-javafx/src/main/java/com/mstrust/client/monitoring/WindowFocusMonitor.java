package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityData;
import com.mstrust.client.util.WindowDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Monitor phát hiện window focus changes (Alt+Tab)
 * Sử dụng JNA hooks cho Windows
 * Detect forbidden apps và tạo alerts
 * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
 * --------------------------------------------------- */
public class WindowFocusMonitor implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(WindowFocusMonitor.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private final Consumer<ActivityData> activityCallback;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private String lastWindowTitle = "";
    private final Queue<String> windowSwitchHistory = new ConcurrentLinkedQueue<>();
    
    // Forbidden apps list
    private static final Set<String> FORBIDDEN_APPS = Set.of(
        // Browsers
        "chrome", "firefox", "edge", "safari", "opera", "brave",
        // AI Tools
        "chatgpt", "claude", "copilot", "bard",
        // IDEs
        "code", "cursor", "intellij", "pycharm", "eclipse", "sublime",
        // Messaging
        "telegram", "whatsapp", "discord", "skype", "zoom", "slack",
        // Other
        "teamviewer", "anydesk", "postman", "insomnia"
    );

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @param activityCallback Callback để gửi activities
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    public WindowFocusMonitor(MonitoringApiClient apiClient, Consumer<ActivityData> activityCallback) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.activityCallback = activityCallback;
    }

    @Override
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("WindowFocusMonitor already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.lastWindowTitle = "";
        windowSwitchHistory.clear();
        
        // Start monitoring
        if (WindowDetector.isAvailable()) {
            startMonitoring();
            logger.info("WindowFocusMonitor started for submission: {}", submissionId);
        } else {
            logger.warn("WindowDetector not available on this platform");
            // Fallback: Poll-based detection
            startPolling();
        }
    }

    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        logger.info("WindowFocusMonitor stopped");
    }

    @Override
    public void shutdown() {
        stop();
        logger.info("WindowFocusMonitor shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return "WindowFocusMonitor";
    }

    /* ---------------------------------------------------
     * Start monitoring với JNA hooks (Windows)
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    private void startMonitoring() {
        // Windows: Sử dụng polling vì JNA hooks phức tạp
        // Có thể implement native hooks sau nếu cần
        startPolling();
    }

    /* ---------------------------------------------------
     * Start polling-based detection (fallback)
     * Poll mỗi 500ms để detect window changes
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    private void startPolling() {
        Thread monitorThread = new Thread(() -> {
            while (isRunning) {
                try {
                    String currentWindow = WindowDetector.getActiveWindowTitle();
                    
                    if (!currentWindow.equals(lastWindowTitle) && !lastWindowTitle.isEmpty()) {
                        // Window changed
                        onWindowSwitch(currentWindow);
                    }
                    
                    lastWindowTitle = currentWindow;
                    
                    Thread.sleep(500); // Poll every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in window focus monitoring", e);
                }
            }
        }, "WindowFocus-Monitor-Thread");
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /* ---------------------------------------------------
     * Handle window switch event
     * @param windowTitle Tiêu đề cửa sổ mới
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    private void onWindowSwitch(String windowTitle) {
        if (!isRunning) {
            return;
        }
        
        logger.debug("Window switched to: {}", windowTitle);
        
        // Add to history
        windowSwitchHistory.add(windowTitle);
        
        // Keep only last 100 entries
        while (windowSwitchHistory.size() > 100) {
            windowSwitchHistory.poll();
        }
        
        // Create activity
        ActivityData activity = ActivityData.windowFocus(windowTitle);
        if (activityCallback != null) {
            activityCallback.accept(activity);
        }
        
        // Check if forbidden app
        if (isForbiddenApp(windowTitle)) {
            logger.warn("Forbidden app detected: {}", windowTitle);
            // Alert will be created by AlertDetectionService
        }
    }

    /* ---------------------------------------------------
     * Kiểm tra window title có phải forbidden app không
     * @param windowTitle Tiêu đề cửa sổ
     * @returns true nếu là forbidden app
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    private boolean isForbiddenApp(String windowTitle) {
        if (windowTitle == null || windowTitle.isEmpty()) {
            return false;
        }
        
        String lowerTitle = windowTitle.toLowerCase();
        return FORBIDDEN_APPS.stream()
            .anyMatch(app -> lowerTitle.contains(app));
    }

    /* ---------------------------------------------------
     * Get window switch history
     * @returns List window titles
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    public List<String> getWindowSwitchHistory() {
        return new ArrayList<>(windowSwitchHistory);
    }

    /* ---------------------------------------------------
     * Get số lần switch window
     * @returns int count
     * @author: K24DTCN210-NVMANH (01/12/2025 10:20)
     * --------------------------------------------------- */
    public int getSwitchCount() {
        return windowSwitchHistory.size();
    }
}

