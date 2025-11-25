package com.mstrust.client.exam.service;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Service quản lý chế độ full-screen trong khi thi
 * Đảm bảo học sinh không thể thoát khỏi màn hình thi
 * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
 * --------------------------------------------------- */
public class FullScreenLockService {
    private static final Logger logger = LoggerFactory.getLogger(FullScreenLockService.class);
    
    private final Stage stage;
    private boolean isLocked = false;
    private KeyboardBlocker keyboardBlocker;
    
    /* ---------------------------------------------------
     * Constructor
     * @param stage JavaFX Stage cần lock
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    public FullScreenLockService(Stage stage) {
        this.stage = stage;
        this.keyboardBlocker = new KeyboardBlocker();
    }
    
    /* ---------------------------------------------------
     * Bật chế độ full-screen và lock keyboard
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    public void enableFullScreen() {
        if (isLocked) {
            logger.warn("Full-screen already locked");
            return;
        }
        
        try {
            // Set full-screen
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Hide exit hint
            
            // Prevent full-screen exit with Esc key
            stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
            
            // Try to block keyboard shortcuts (Windows only)
            try {
                if (isWindows()) {
                    keyboardBlocker.install();
                    logger.info("Keyboard blocker installed (Windows)");
                } else {
                    logger.info("Keyboard blocker skipped (not Windows)");
                }
            } catch (Exception e) {
                logger.warn("Could not install keyboard blocker (may need admin rights): {}", e.getMessage());
                // Continue anyway - full-screen still works without keyboard blocking
            }
            
            isLocked = true;
            logger.info("Full-screen mode enabled and locked");
            
        } catch (Exception e) {
            logger.error("Failed to enable full-screen lock", e);
            throw new RuntimeException("Không thể bật chế độ full-screen", e);
        }
    }
    
    /* ---------------------------------------------------
     * Tắt chế độ full-screen và unlock keyboard
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    public void disableFullScreen() {
        if (!isLocked) {
            logger.warn("Full-screen not locked");
            return;
        }
        
        try {
            // Unblock keyboard
            if (keyboardBlocker.isInstalled()) {
                keyboardBlocker.uninstall();
            }
            
            // Exit full-screen
            stage.setFullScreen(false);
            
            isLocked = false;
            logger.info("Full-screen mode disabled and unlocked");
            
        } catch (Exception e) {
            logger.error("Failed to disable full-screen lock", e);
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra trạng thái lock
     * @return true nếu đang lock
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    public boolean isLocked() {
        return isLocked;
    }
    
    /* ---------------------------------------------------
     * Cleanup resources
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    public void cleanup() {
        if (isLocked) {
            disableFullScreen();
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem có phải Windows không
     * @return true nếu là Windows
     * @author: K24DTCN210-NVMANH (24/11/2025 09:10)
     * --------------------------------------------------- */
    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
}
