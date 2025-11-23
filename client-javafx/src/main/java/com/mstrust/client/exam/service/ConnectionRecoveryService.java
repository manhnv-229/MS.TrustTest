package com.mstrust.client.exam.service;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/* ---------------------------------------------------
 * Connection Recovery Service - Xử lý reconnection và recovery
 * 
 * Features:
 * - Listen to network status changes
 * - Auto-flush queued answers on reconnect
 * - Notify UI về connection status
 * - Handle partial success scenarios
 * 
 * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
 * --------------------------------------------------- */
public class ConnectionRecoveryService implements NetworkMonitor.NetworkStatusListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionRecoveryService.class);
    
    // Dependencies
    private final AutoSaveService autoSaveService;
    private final AnswerQueue answerQueue;
    
    // State
    private boolean isRecovering = false;
    private ConnectionStatus currentStatus = ConnectionStatus.CONNECTED;
    
    // Callbacks for UI updates
    private Consumer<ConnectionStatus> onStatusChanged;

    /* ---------------------------------------------------
     * Constructor
     * @param autoSaveService AutoSaveService instance
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public ConnectionRecoveryService(AutoSaveService autoSaveService) {
        this.autoSaveService = autoSaveService;
        this.answerQueue = new AnswerQueue(); // Use same queue instance
    }

    /* ---------------------------------------------------
     * Constructor với explicit queue
     * @param autoSaveService AutoSaveService instance
     * @param answerQueue AnswerQueue instance (shared)
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public ConnectionRecoveryService(AutoSaveService autoSaveService, AnswerQueue answerQueue) {
        this.autoSaveService = autoSaveService;
        this.answerQueue = answerQueue;
    }

    /* ---------------------------------------------------
     * Handle disconnection event
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    @Override
    public void onDisconnected() {
        logger.warn("Network disconnected detected");
        
        currentStatus = ConnectionStatus.DISCONNECTED;
        notifyUI(ConnectionStatus.DISCONNECTED);
        
        Platform.runLater(() -> {
            logger.info("Showing disconnection warning to user");
            // UI will handle showing warning dialog
        });
    }

    /* ---------------------------------------------------
     * Handle reconnection event
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    @Override
    public void onConnected() {
        logger.info("Network reconnected detected");
        
        // Prevent concurrent recovery
        if (isRecovering) {
            logger.warn("Recovery already in progress, skipping");
            return;
        }
        
        startRecovery();
    }

    /* ---------------------------------------------------
     * Start recovery process
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    private void startRecovery() {
        isRecovering = true;
        currentStatus = ConnectionStatus.RECONNECTING;
        notifyUI(ConnectionStatus.RECONNECTING);
        
        logger.info("Starting connection recovery...");
        
        Platform.runLater(() -> {
            logger.info("Showing reconnecting message to user");
        });
        
        // Run recovery in background thread
        new Thread(() -> {
            try {
                performRecovery();
            } catch (Exception e) {
                logger.error("Error during recovery: {}", e.getMessage(), e);
                currentStatus = ConnectionStatus.RECOVERY_FAILED;
                notifyUI(ConnectionStatus.RECOVERY_FAILED);
            } finally {
                isRecovering = false;
            }
        }, "ConnectionRecovery").start();
    }

    /* ---------------------------------------------------
     * Perform recovery operations
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    private void performRecovery() {
        int pendingCount = answerQueue.getPendingCount();
        
        if (pendingCount == 0) {
            logger.info("No pending answers to recover");
            recoveryComplete(true, 0, 0);
            return;
        }
        
        logger.info("Attempting to flush {} pending answers", pendingCount);
        
        // Trigger auto-save to flush all pending
        autoSaveService.saveAllPendingAnswers();
        
        // Wait a moment for save to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // Check results
        int remainingCount = answerQueue.getPendingCount();
        int successCount = pendingCount - remainingCount;
        int failCount = remainingCount;
        
        boolean fullSuccess = (remainingCount == 0);
        
        logger.info("Recovery complete - Success: {}, Failed: {}, Remaining: {}", 
            successCount, failCount, remainingCount);
        
        recoveryComplete(fullSuccess, successCount, failCount);
    }

    /* ---------------------------------------------------
     * Handle recovery completion
     * @param fullSuccess true nếu tất cả pending đều save thành công
     * @param successCount Số answers save thành công
     * @param failCount Số answers vẫn pending
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    private void recoveryComplete(boolean fullSuccess, int successCount, int failCount) {
        if (fullSuccess) {
            currentStatus = ConnectionStatus.CONNECTED;
            logger.info("Recovery successful - All pending answers saved");
            
            Platform.runLater(() -> {
                logger.info("Showing recovery success message");
            });
            
        } else {
            currentStatus = ConnectionStatus.PARTIAL_RECOVERY;
            logger.warn("Recovery partial success - {} answers still pending", failCount);
            
            Platform.runLater(() -> {
                logger.info("Showing partial recovery warning");
            });
        }
        
        notifyUI(currentStatus);
    }

    /* ---------------------------------------------------
     * Notify UI về status change
     * @param status ConnectionStatus mới
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    private void notifyUI(ConnectionStatus status) {
        if (onStatusChanged != null) {
            Platform.runLater(() -> onStatusChanged.accept(status));
        }
    }

    /* ---------------------------------------------------
     * Set callback khi connection status thay đổi
     * @param callback Consumer nhận ConnectionStatus
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public void setOnStatusChanged(Consumer<ConnectionStatus> callback) {
        this.onStatusChanged = callback;
    }

    /* ---------------------------------------------------
     * Get current connection status
     * @returns ConnectionStatus hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public ConnectionStatus getCurrentStatus() {
        return currentStatus;
    }

    /* ---------------------------------------------------
     * Check xem đang trong recovery process không
     * @returns true nếu đang recovery
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public boolean isRecovering() {
        return isRecovering;
    }

    /* ---------------------------------------------------
     * Force retry recovery (manual trigger)
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public void forceRetry() {
        if (isRecovering) {
            logger.warn("Recovery already in progress");
            return;
        }
        
        logger.info("Manual recovery retry requested");
        startRecovery();
    }

    /* ---------------------------------------------------
     * ConnectionStatus enum - Trạng thái connection
     * @author: K24DTCN210-NVMANH (23/11/2025 17:41)
     * --------------------------------------------------- */
    public enum ConnectionStatus {
        CONNECTED("Đã kết nối", "green"),
        DISCONNECTED("Mất kết nối", "red"),
        RECONNECTING("Đang kết nối lại...", "orange"),
        PARTIAL_RECOVERY("Kết nối lại một phần", "orange"),
        RECOVERY_FAILED("Kết nối lại thất bại", "red");
        
        private final String displayText;
        private final String colorHint;
        
        ConnectionStatus(String displayText, String colorHint) {
            this.displayText = displayText;
            this.colorHint = colorHint;
        }
        
        public String getDisplayText() {
            return displayText;
        }
        
        public String getColorHint() {
            return colorHint;
        }
    }
}
