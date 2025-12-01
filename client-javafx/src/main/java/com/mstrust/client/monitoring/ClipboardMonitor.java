package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Monitor clipboard changes (Copy/Paste operations)
 * Detect large paste (> 100 characters)
 * Alert on paste from external source
 * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
 * --------------------------------------------------- */
public class ClipboardMonitor implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(ClipboardMonitor.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private final Consumer<ActivityData> activityCallback;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private Clipboard clipboard;
    private FlavorListener flavorListener;
    private String lastClipboardContent = "";
    private final Queue<String> clipboardHistory = new ConcurrentLinkedQueue<>();
    
    private static final int LARGE_PASTE_THRESHOLD = 100; // characters

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @param activityCallback Callback để gửi activities
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    public ClipboardMonitor(MonitoringApiClient apiClient, Consumer<ActivityData> activityCallback) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.activityCallback = activityCallback;
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    @Override
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("ClipboardMonitor already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.lastClipboardContent = "";
        clipboardHistory.clear();
        
        // Register clipboard listener
        flavorListener = new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                if (isRunning) {
                    onClipboardChanged();
                }
            }
        };
        
        clipboard.addFlavorListener(flavorListener);
        
        logger.info("ClipboardMonitor started for submission: {}", submissionId);
    }

    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Remove listener
        if (flavorListener != null && clipboard != null) {
            try {
                clipboard.removeFlavorListener(flavorListener);
            } catch (Exception e) {
                logger.error("Error removing clipboard listener", e);
            }
        }
        
        logger.info("ClipboardMonitor stopped");
    }

    @Override
    public void shutdown() {
        stop();
        logger.info("ClipboardMonitor shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return "ClipboardMonitor";
    }

    /* ---------------------------------------------------
     * Handle clipboard change event
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    private void onClipboardChanged() {
        try {
            // Get clipboard content
            String content = getClipboardContent();
            
            if (content == null || content.equals(lastClipboardContent)) {
                return; // No change or same content
            }
            
            logger.debug("Clipboard changed. Length: {}", content.length());
            
            // Add to history
            clipboardHistory.add(content);
            while (clipboardHistory.size() > 50) {
                clipboardHistory.poll();
            }
            
            // Determine operation type
            String operation = determineOperation(content);
            
            // Create activity
            ActivityData activity = ActivityData.clipboard(operation);
            if (activityCallback != null) {
                activityCallback.accept(activity);
            }
            
            // Check for large paste
            if (operation.equals("PASTE") && content.length() > LARGE_PASTE_THRESHOLD) {
                logger.warn("Large paste detected: {} characters", content.length());
                // Alert will be created by AlertDetectionService
            }
            
            lastClipboardContent = content;
            
        } catch (Exception e) {
            logger.error("Error handling clipboard change", e);
        }
    }

    /* ---------------------------------------------------
     * Get clipboard content as String
     * @returns String content hoặc null
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    private String getClipboardContent() {
        try {
            Transferable transferable = clipboard.getContents(null);
            if (transferable == null) {
                return null;
            }
            
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
            
            return null;
        } catch (Exception e) {
            logger.debug("Error getting clipboard content", e);
            return null;
        }
    }

    /* ---------------------------------------------------
     * Determine operation type (COPY/PASTE)
     * Heuristic: Nếu content mới khác nhiều so với trước = PASTE
     * @param content Clipboard content
     * @returns "COPY" hoặc "PASTE"
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    private String determineOperation(String content) {
        // Simple heuristic: Large content change = likely paste
        if (lastClipboardContent.isEmpty()) {
            return "COPY"; // First clipboard content
        }
        
        // If content is significantly different, likely paste
        if (content.length() > lastClipboardContent.length() * 2) {
            return "PASTE";
        }
        
        // Default to COPY (user copied something)
        return "COPY";
    }

    /* ---------------------------------------------------
     * Get clipboard history
     * @returns List clipboard contents
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    public List<String> getClipboardHistory() {
        return new ArrayList<>(clipboardHistory);
    }

    /* ---------------------------------------------------
     * Get số lần clipboard operations
     * @returns int count
     * @author: K24DTCN210-NVMANH (01/12/2025 10:40)
     * --------------------------------------------------- */
    public int getOperationCount() {
        return clipboardHistory.size();
    }
}

