package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityData;
import com.mstrust.client.dto.ActivityLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * Coordinator điều phối tất cả monitoring monitors
 * Single point of control cho monitoring system
 * Quản lý lifecycle của 5 monitors: ScreenCapture, WindowFocus, Process, Clipboard, Keystroke
 * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
 * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Refactor để sử dụng 5 monitors mới
 * --------------------------------------------------- */
public class MonitoringCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringCoordinator.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    
    // 5 Monitors (Phase 11)
    private final ScreenCaptureMonitor screenCaptureMonitor;
    private final WindowFocusMonitor windowFocusMonitor;
    private final ProcessMonitor processMonitor;
    private final ClipboardMonitor clipboardMonitor;
    private final KeystrokeAnalyzer keystrokeAnalyzer;
    
    // Alert Detection Service (giữ lại để xử lý alerts)
    private final AlertDetectionService alertService;
    
    // Activity buffer
    private final List<ActivityData> activityBuffer = new ArrayList<>();
    
    // Scheduler cho batch upload
    private ScheduledExecutorService activityUploadScheduler;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private LocalDateTime startTime;

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Initialize 5 monitors
     * --------------------------------------------------- */
    public MonitoringCoordinator(MonitoringApiClient apiClient) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        
        // Initialize 5 monitors với activity callback
        this.screenCaptureMonitor = new ScreenCaptureMonitor(apiClient);
        this.windowFocusMonitor = new WindowFocusMonitor(apiClient, this::onActivity);
        this.processMonitor = new ProcessMonitor(apiClient, this::onActivity);
        this.clipboardMonitor = new ClipboardMonitor(apiClient, this::onActivity);
        this.keystrokeAnalyzer = new KeystrokeAnalyzer(apiClient, this::onActivity);
        
        // Initialize alert service
        this.alertService = new AlertDetectionService(apiClient);
        
        // Scheduler will be created when monitoring starts
        this.activityUploadScheduler = null;
        
        logger.info("MonitoringCoordinator initialized with 5 monitors");
    }
    
    /* ---------------------------------------------------
     * Activity callback từ các monitors
     * @param activity ActivityData từ monitor
     * @author: K24DTCN210-NVMANH (01/12/2025 11:00)
     * --------------------------------------------------- */
    private synchronized void onActivity(ActivityData activity) {
        if (!isRunning) {
            return;
        }
        
        activityBuffer.add(activity);
        
        // Forward to alert service nếu cần
        switch (activity.getActivityType()) {
            case WINDOW_FOCUS:
                // Extract window title từ details
                String windowTitle = activity.getDetails().replace("Window switched to: ", "");
                alertService.recordWindowSwitch(windowTitle);
                break;
            case CLIPBOARD:
                // Extract operation từ details
                String operation = activity.getDetails().replace("Thao tác clipboard: ", "");
                alertService.recordClipboardOperation(operation);
                break;
            case PROCESS_DETECTED:
                // Process alert sẽ được tạo bởi ProcessMonitor
                break;
            case KEYSTROKE:
                // Keystroke alerts được xử lý bởi KeystrokeAnalyzer
                break;
        }
        
        logger.debug("Activity recorded: {}", activity.getActivityType());
    }

    /* ---------------------------------------------------
     * Bắt đầu monitoring
     * @param submissionId ID bài làm
     * @param authToken JWT token
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Start 5 monitors
     * --------------------------------------------------- */
    public void startMonitoring(Long submissionId, String authToken) {
        if (isRunning) {
            logger.warn("Monitoring already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.startTime = LocalDateTime.now();
        
        // Set auth token
        apiClient.setAuthToken(authToken);
        
        // Create activity upload scheduler
        this.activityUploadScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Activity-Upload-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Start all 5 monitors
        screenCaptureMonitor.start(submissionId);
        windowFocusMonitor.start(submissionId);
        processMonitor.start(submissionId);
        clipboardMonitor.start(submissionId);
        keystrokeAnalyzer.start(submissionId);
        
        // Start alert service
        alertService.start(submissionId);
        
        // Schedule activity upload (every 30 seconds - Phase 11 requirement)
        int activityInterval = 30; // Phase 11: batch upload every 30s
        activityUploadScheduler.scheduleAtFixedRate(
            this::uploadActivityBatch,
            activityInterval,
            activityInterval,
            TimeUnit.SECONDS
        );
        
        logger.info("Monitoring started for submission: {} with 5 monitors", submissionId);
    }

    /* ---------------------------------------------------
     * Dừng monitoring
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Stop 5 monitors
     * --------------------------------------------------- */
    public void stopMonitoring() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Upload remaining activities
        uploadActivityBatch();
        
        // Stop all 5 monitors
        screenCaptureMonitor.stop();
        windowFocusMonitor.stop();
        processMonitor.stop();
        clipboardMonitor.stop();
        keystrokeAnalyzer.stop();
        
        // Stop alert service
        alertService.stop();
        
        // Shutdown scheduler
        if (activityUploadScheduler != null && !activityUploadScheduler.isShutdown()) {
            activityUploadScheduler.shutdown();
            try {
                if (!activityUploadScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    activityUploadScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                activityUploadScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Monitoring stopped. Duration: {} minutes", 
            java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes());
    }

    /* ---------------------------------------------------
     * Shutdown hoàn toàn
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Shutdown 5 monitors
     * --------------------------------------------------- */
    public void shutdown() {
        // stopMonitoring already stops monitors
        stopMonitoring();
        
        // Final cleanup - shutdown all monitors
        screenCaptureMonitor.shutdown();
        windowFocusMonitor.shutdown();
        processMonitor.shutdown();
        clipboardMonitor.shutdown();
        keystrokeAnalyzer.shutdown();
        
        logger.info("MonitoringCoordinator shutdown complete");
    }

    /* ---------------------------------------------------
     * Get monitor instances (for external access nếu cần)
     * @author: K24DTCN210-NVMANH (01/12/2025 11:00)
     * --------------------------------------------------- */
    public ScreenCaptureMonitor getScreenCaptureMonitor() {
        return screenCaptureMonitor;
    }
    
    public WindowFocusMonitor getWindowFocusMonitor() {
        return windowFocusMonitor;
    }
    
    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }
    
    public ClipboardMonitor getClipboardMonitor() {
        return clipboardMonitor;
    }
    
    public KeystrokeAnalyzer getKeystrokeAnalyzer() {
        return keystrokeAnalyzer;
    }

    /* ---------------------------------------------------
     * Upload activity batch
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    private synchronized void uploadActivityBatch() {
        if (activityBuffer.isEmpty()) {
            return;
        }
        
        // Create request
        ActivityLogRequest request = ActivityLogRequest.of(
            currentSubmissionId, 
            new ArrayList<>(activityBuffer)
        );
        
        // Upload
        boolean success = apiClient.logActivities(request);
        
        if (success) {
            logger.info("Activity batch uploaded: {} activities", activityBuffer.size());
            activityBuffer.clear();
        } else {
            logger.error("Failed to upload activity batch");
            // Keep activities in buffer for retry
        }
    }

    /* ---------------------------------------------------
     * Get monitoring stats
     * @returns String mô tả stats
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 11:00) - Include stats từ 5 monitors
     * --------------------------------------------------- */
    public String getStats() {
        if (!isRunning) {
            return "Monitoring not running";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Monitoring Stats ===\n");
        sb.append("Submission ID: ").append(currentSubmissionId).append("\n");
        sb.append("Duration: ").append(
            java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes()
        ).append(" minutes\n");
        sb.append("\n--- Monitors ---\n");
        sb.append("ScreenCapture: ").append(screenCaptureMonitor.getCaptureCount()).append(" captures\n");
        sb.append("WindowFocus: ").append(windowFocusMonitor.getSwitchCount()).append(" switches\n");
        sb.append("Process: ").append(processMonitor.getTotalProcessesDetected()).append(" processes detected\n");
        sb.append("Clipboard: ").append(clipboardMonitor.getOperationCount()).append(" operations\n");
        sb.append("Keystroke: ").append(keystrokeAnalyzer.getTotalKeystrokes())
          .append(" keystrokes, ").append(String.format("%.2f", keystrokeAnalyzer.getAverageWPM())).append(" WPM\n");
        sb.append("\nActivity Buffer: ").append(activityBuffer.size()).append("\n");
        sb.append("Alert Stats: ").append(alertService.getStats()).append("\n");
        sb.append("=======================");
        
        return sb.toString();
    }

    /* ---------------------------------------------------
     * Check monitoring đang chạy
     * @returns true nếu đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return isRunning;
    }

    /* ---------------------------------------------------
     * Get submission ID hiện tại
     * @returns Long submission ID
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public Long getCurrentSubmissionId() {
        return currentSubmissionId;
    }
}
