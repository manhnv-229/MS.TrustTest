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
 * Coordinator điều phối tất cả monitoring services
 * Single point of control cho monitoring system
 * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
 * --------------------------------------------------- */
public class MonitoringCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringCoordinator.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    
    // Services
    private final ScreenshotCaptureService screenshotService;
    private final AlertDetectionService alertService;
    
    // Activity buffer
    private final List<ActivityData> activityBuffer = new ArrayList<>();
    
    // Schedulers
    private ScheduledExecutorService activityUploadScheduler;
    private ScheduledExecutorService processCheckScheduler;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private LocalDateTime startTime;

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public MonitoringCoordinator(MonitoringApiClient apiClient) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        
        // Initialize services
        this.screenshotService = new ScreenshotCaptureService(apiClient);
        this.alertService = new AlertDetectionService(apiClient);
        
        // Schedulers will be created when monitoring starts
        this.activityUploadScheduler = null;
        this.processCheckScheduler = null;
        
        logger.info("MonitoringCoordinator initialized");
    }

    /* ---------------------------------------------------
     * Bắt đầu monitoring
     * @param submissionId ID bài làm
     * @param authToken JWT token
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
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
        
        // Create new schedulers (in case they were shutdown before)
        this.activityUploadScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Activity-Upload-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        this.processCheckScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Process-Check-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Start services
        screenshotService.start(submissionId);
        alertService.start(submissionId);
        
        // Schedule activity upload (every 60 seconds)
        int activityInterval = config.getActivityBatchIntervalSeconds();
        activityUploadScheduler.scheduleAtFixedRate(
            this::uploadActivityBatch,
            activityInterval,
            activityInterval,
            TimeUnit.SECONDS
        );
        
        // Schedule process check (every 30 seconds)
        processCheckScheduler.scheduleAtFixedRate(
            alertService::checkBlacklistedProcesses,
            30,
            30,
            TimeUnit.SECONDS
        );
        
        logger.info("Monitoring started for submission: {}", submissionId);
    }

    /* ---------------------------------------------------
     * Dừng monitoring
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (21/11/2025 12:46) - Shutdown schedulers khi stop
     * --------------------------------------------------- */
    public void stopMonitoring() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Upload remaining activities
        uploadActivityBatch();
        
        // Stop services
        screenshotService.stop();
        alertService.stop();
        
        // Shutdown schedulers to stop all background tasks
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
        
        if (processCheckScheduler != null && !processCheckScheduler.isShutdown()) {
            processCheckScheduler.shutdown();
            try {
                if (!processCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    processCheckScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                processCheckScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Monitoring stopped. Duration: {} minutes", 
            java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes());
    }

    /* ---------------------------------------------------
     * Shutdown hoàn toàn
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * EditBy: K24DTCN210-NVMANH (21/11/2025 12:46) - stopMonitoring đã shutdown schedulers
     * --------------------------------------------------- */
    public void shutdown() {
        // stopMonitoring already shuts down schedulers
        stopMonitoring();
        
        // Final cleanup
        screenshotService.shutdown();
        
        logger.info("MonitoringCoordinator shutdown complete");
    }

    /* ---------------------------------------------------
     * Ghi nhận window switch event
     * @param windowTitle Tiêu đề window
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public synchronized void onWindowSwitch(String windowTitle) {
        if (!isRunning) {
            return;
        }
        
        // Log activity
        ActivityData activity = ActivityData.windowFocus(windowTitle);
        activityBuffer.add(activity);
        
        // Alert detection
        alertService.recordWindowSwitch(windowTitle);
        
        logger.debug("Window switch recorded: {}", windowTitle);
    }

    /* ---------------------------------------------------
     * Ghi nhận clipboard operation
     * @param operation "COPY" hoặc "PASTE"
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public synchronized void onClipboardOperation(String operation) {
        if (!isRunning) {
            return;
        }
        
        // Log activity
        ActivityData activity = ActivityData.clipboard(operation);
        activityBuffer.add(activity);
        
        // Alert detection
        alertService.recordClipboardOperation(operation);
        
        logger.debug("Clipboard operation recorded: {}", operation);
    }

    /* ---------------------------------------------------
     * Ghi nhận process detected
     * @param processName Tên process
     * @author: K24DTCN210-NVMANH (21/11/2025 11:35)
     * --------------------------------------------------- */
    public synchronized void onProcessDetected(String processName) {
        if (!isRunning) {
            return;
        }
        
        // Log activity
        ActivityData activity = ActivityData.processDetected(processName);
        activityBuffer.add(activity);
        
        logger.info("Process detected: {}", processName);
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
        sb.append("Screenshots: ").append(screenshotService.getCaptureCount()).append("\n");
        sb.append("Activity Buffer: ").append(activityBuffer.size()).append("\n");
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
