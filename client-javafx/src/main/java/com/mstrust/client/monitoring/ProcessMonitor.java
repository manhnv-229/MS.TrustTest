package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityData;
import com.mstrust.client.util.PlatformUtil;
import com.mstrust.client.util.ProcessDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Monitor quét processes đang chạy và phát hiện blacklisted
 * Scan mỗi 10s, tạo alert khi phát hiện blacklisted process
 * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
 * --------------------------------------------------- */
public class ProcessMonitor implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(ProcessMonitor.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private final Consumer<ActivityData> activityCallback;
    private ScheduledExecutorService scheduler;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private final Set<String> detectedBlacklistedProcesses = ConcurrentHashMap.newKeySet();
    private final Set<String> allDetectedProcesses = ConcurrentHashMap.newKeySet();

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @param activityCallback Callback để gửi activities
     * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
     * --------------------------------------------------- */
    public ProcessMonitor(MonitoringApiClient apiClient, Consumer<ActivityData> activityCallback) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.activityCallback = activityCallback;
        this.scheduler = null;
    }

    @Override
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("ProcessMonitor already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        detectedBlacklistedProcesses.clear();
        allDetectedProcesses.clear();
        
        // Create scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Process-Monitor-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Scan processes every 10 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning) {
                    return;
                }
                scanProcesses();
            } catch (Exception e) {
                logger.error("Error in process scan", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
        
        logger.info("ProcessMonitor started for submission: {}", submissionId);
    }

    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Shutdown scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("ProcessMonitor stopped");
    }

    @Override
    public void shutdown() {
        stop();
        logger.info("ProcessMonitor shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return "ProcessMonitor";
    }

    /* ---------------------------------------------------
     * Scan running processes và phát hiện blacklisted
     * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
     * --------------------------------------------------- */
    private void scanProcesses() {
        try {
            // Get all running processes
            List<String> runningProcesses = PlatformUtil.getProcessList();
            if (runningProcesses.isEmpty()) {
                // Fallback to ProcessDetector
                runningProcesses = ProcessDetector.getRunningProcesses();
            }
            
            // Update all detected processes
            allDetectedProcesses.addAll(runningProcesses);
            
            // Check for blacklisted processes
            List<String> blacklisted = ProcessDetector.getBlacklistedProcesses();
            
            for (String process : blacklisted) {
                if (!detectedBlacklistedProcesses.contains(process)) {
                    // New blacklisted process detected
                    detectedBlacklistedProcesses.add(process);
                    onBlacklistedProcessDetected(process);
                }
            }
            
            logger.debug("Process scan completed. Total: {}, Blacklisted: {}", 
                    runningProcesses.size(), blacklisted.size());
            
        } catch (Exception e) {
            logger.error("Error scanning processes", e);
        }
    }

    /* ---------------------------------------------------
     * Handle blacklisted process detected
     * @param processName Tên process
     * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
     * --------------------------------------------------- */
    private void onBlacklistedProcessDetected(String processName) {
        logger.error("Blacklisted process detected: {}", processName);
        
        // Create activity
        ActivityData activity = ActivityData.processDetected(processName);
        if (activityCallback != null) {
            activityCallback.accept(activity);
        }
        
        // Alert will be created by AlertDetectionService
    }

    /* ---------------------------------------------------
     * Get danh sách blacklisted processes đã phát hiện
     * @returns Set process names
     * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
     * --------------------------------------------------- */
    public Set<String> getDetectedBlacklistedProcesses() {
        return new HashSet<>(detectedBlacklistedProcesses);
    }

    /* ---------------------------------------------------
     * Get tổng số processes đã phát hiện
     * @returns int count
     * @author: K24DTCN210-NVMANH (01/12/2025 10:30)
     * --------------------------------------------------- */
    public int getTotalProcessesDetected() {
        return allDetectedProcesses.size();
    }
}

