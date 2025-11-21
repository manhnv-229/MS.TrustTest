package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.AlertCreateRequest;
import com.mstrust.client.dto.AlertSeverity;
import com.mstrust.client.util.ProcessDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/* ---------------------------------------------------
 * Service phát hiện vi phạm và tạo alerts tự động
 * Theo dõi thresholds: window switches, clipboard, processes
 * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
 * --------------------------------------------------- */
public class AlertDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(AlertDetectionService.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    
    // Window switch tracking
    private final Queue<LocalDateTime> windowSwitchTimes = new ConcurrentLinkedQueue<>();
    
    // Clipboard tracking
    private final Queue<LocalDateTime> clipboardOperations = new ConcurrentLinkedQueue<>();
    
    // Detected blacklisted processes
    private final Set<String> detectedBlacklistedProcesses = Collections.synchronizedSet(new HashSet<>());
    
    // Created alerts tracking (để tránh duplicate)
    private final Set<String> createdAlerts = Collections.synchronizedSet(new HashSet<>());

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client để tạo alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public AlertDetectionService(MonitoringApiClient apiClient) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
    }

    /* ---------------------------------------------------
     * Bắt đầu alert detection
     * @param submissionId ID bài làm
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void start(Long submissionId) {
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        
        // Clear previous data
        windowSwitchTimes.clear();
        clipboardOperations.clear();
        detectedBlacklistedProcesses.clear();
        createdAlerts.clear();
        
        logger.info("Alert detection started for submission: {}", submissionId);
    }

    /* ---------------------------------------------------
     * Dừng alert detection
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        logger.info("Alert detection stopped");
    }

    /* ---------------------------------------------------
     * Ghi nhận window switch event
     * @param windowTitle Tiêu đề window
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void recordWindowSwitch(String windowTitle) {
        if (!isRunning) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        windowSwitchTimes.add(now);
        
        // Cleanup old entries (older than 5 minutes)
        LocalDateTime cutoff = now.minusMinutes(5);
        windowSwitchTimes.removeIf(time -> time.isBefore(cutoff));
        
        // Check threshold
        int recentSwitches = windowSwitchTimes.size();
        int threshold = config.getWindowSwitchThreshold();
        
        if (recentSwitches >= threshold) {
            createWindowSwitchAlert(recentSwitches);
        }
        
        logger.debug("Window switched to: {}. Recent switches: {}/{}", 
                windowTitle, recentSwitches, threshold);
    }

    /* ---------------------------------------------------
     * Ghi nhận clipboard operation
     * @param operation "COPY" hoặc "PASTE"
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void recordClipboardOperation(String operation) {
        if (!isRunning) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        clipboardOperations.add(now);
        
        // Cleanup old entries (older than 10 minutes)
        LocalDateTime cutoff = now.minusMinutes(10);
        clipboardOperations.removeIf(time -> time.isBefore(cutoff));
        
        // Check threshold
        int recentOps = clipboardOperations.size();
        int threshold = config.getClipboardThreshold();
        
        if (recentOps >= threshold) {
            createClipboardAlert(recentOps);
        }
        
        logger.debug("Clipboard {}: Recent operations: {}/{}", 
                operation, recentOps, threshold);
    }

    /* ---------------------------------------------------
     * Check blacklisted processes
     * Nên gọi định kỳ (mỗi 30 giây)
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void checkBlacklistedProcesses() {
        if (!isRunning) {
            return;
        }
        
        List<String> blacklisted = ProcessDetector.getBlacklistedProcesses();
        
        for (String process : blacklisted) {
            if (!detectedBlacklistedProcesses.contains(process)) {
                // New blacklisted process detected
                detectedBlacklistedProcesses.add(process);
                createBlacklistedProcessAlert(process);
            }
        }
    }

    /* ---------------------------------------------------
     * Tạo alert cho window switch
     * @param count Số lần switch
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    private void createWindowSwitchAlert(int count) {
        String alertKey = "WINDOW_SWITCH_" + (count / 5); // Group by 5
        
        if (createdAlerts.contains(alertKey)) {
            return; // Already created
        }
        
        AlertCreateRequest alert = AlertCreateRequest.windowSwitchAlert(
                currentSubmissionId, count);
        
        boolean success = apiClient.createAlert(alert);
        
        if (success) {
            createdAlerts.add(alertKey);
            logger.warn("Window switch alert created: {} switches", count);
        }
    }

    /* ---------------------------------------------------
     * Tạo alert cho clipboard
     * @param count Số lần operations
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    private void createClipboardAlert(int count) {
        String alertKey = "CLIPBOARD_" + (count / 10); // Group by 10
        
        if (createdAlerts.contains(alertKey)) {
            return;
        }
        
        AlertCreateRequest alert = AlertCreateRequest.clipboardAlert(
                currentSubmissionId, count);
        
        boolean success = apiClient.createAlert(alert);
        
        if (success) {
            createdAlerts.add(alertKey);
            logger.warn("Clipboard alert created: {} operations", count);
        }
    }

    /* ---------------------------------------------------
     * Tạo alert cho blacklisted process
     * @param processName Tên process
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    private void createBlacklistedProcessAlert(String processName) {
        String alertKey = "PROCESS_" + processName;
        
        if (createdAlerts.contains(alertKey)) {
            return;
        }
        
        AlertCreateRequest alert = AlertCreateRequest.suspiciousProcessAlert(
                currentSubmissionId, processName);
        
        boolean success = apiClient.createAlert(alert);
        
        if (success) {
            createdAlerts.add(alertKey);
            logger.error("Blacklisted process alert created: {}", processName);
        }
    }

    /* ---------------------------------------------------
     * Get thống kê
     * @returns Map chứa stats
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("windowSwitches", windowSwitchTimes.size());
        stats.put("clipboardOps", clipboardOperations.size());
        stats.put("blacklistedProcesses", detectedBlacklistedProcesses.size());
        stats.put("alertsCreated", createdAlerts.size());
        stats.put("isRunning", isRunning);
        return stats;
    }

    /* ---------------------------------------------------
     * Reset tất cả counters
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public void reset() {
        windowSwitchTimes.clear();
        clipboardOperations.clear();
        detectedBlacklistedProcesses.clear();
        createdAlerts.clear();
        logger.info("Alert detection counters reset");
    }

    /* ---------------------------------------------------
     * Check service đang chạy không
     * @returns true nếu đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:34)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return isRunning;
    }
}
