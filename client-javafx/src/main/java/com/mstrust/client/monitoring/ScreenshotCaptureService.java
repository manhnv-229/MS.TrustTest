package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.util.WindowDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * Service chụp màn hình tự động và upload lên backend
 * Chụp mỗi 30 giây, compress và upload via API
 * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
 * --------------------------------------------------- */
public class ScreenshotCaptureService {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotCaptureService.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private ScheduledExecutorService scheduler;
    private final Path tempDir;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private LocalDateTime lastCaptureTime;
    private int captureCount = 0;

    /* ---------------------------------------------------
     * Constructor - khởi tạo service
     * @param apiClient API client để upload
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public ScreenshotCaptureService(MonitoringApiClient apiClient) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.scheduler = null; // Will be created on start
        
        // Tạo temp directory
        this.tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "mstrust-screenshots");
        try {
            java.nio.file.Files.createDirectories(tempDir);
            logger.info("Temp directory created: {}", tempDir);
        } catch (Exception e) {
            logger.error("Failed to create temp directory", e);
        }
    }

    /* ---------------------------------------------------
     * Bắt đầu chụp màn hình định kỳ
     * @param submissionId ID bài làm
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("Screenshot capture already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.captureCount = 0;
        
        // Create new scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Screenshot-Capture-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        int interval = config.getScreenshotIntervalSeconds();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning) {
                    return; // Check if stopped
                }
                captureAndUpload();
            } catch (Exception e) {
                logger.error("Error during screenshot capture", e);
            }
        }, 0, interval, TimeUnit.SECONDS);
        
        logger.info("Screenshot capture started. Interval: {}s, SubmissionId: {}", 
                interval, submissionId);
    }

    /* ---------------------------------------------------
     * Dừng chụp màn hình
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * EditBy: K24DTCN210-NVMANH (21/11/2025 12:53) - Shutdown scheduler khi stop
     * --------------------------------------------------- */
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
        
        logger.info("Screenshot capture stopped. Total captures: {}", captureCount);
    }

    /* ---------------------------------------------------
     * Shutdown service hoàn toàn
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public void shutdown() {
        stop(); // stop() already shuts down scheduler
        
        // Cleanup temp files
        cleanupTempFiles();
        
        logger.info("Screenshot capture service shutdown complete");
    }

    /* ---------------------------------------------------
     * Chụp màn hình và upload
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    private void captureAndUpload() {
        try {
            // Capture screenshot
            BufferedImage screenshot = captureScreen();
            if (screenshot == null) {
                logger.error("Failed to capture screenshot");
                return;
            }
            
            // Get screen info
            String screenResolution = getScreenResolution();
            String windowTitle = WindowDetector.getActiveWindowTitle();
            
            // Save to temp file
            Path imagePath = saveTempImage(screenshot);
            
            // Upload
            boolean success = apiClient.uploadScreenshot(
                imagePath, 
                currentSubmissionId, 
                screenResolution, 
                windowTitle
            );
            
            if (success) {
                captureCount++;
                lastCaptureTime = LocalDateTime.now();
                logger.info("Screenshot uploaded successfully. Count: {}", captureCount);
            } else {
                logger.error("Failed to upload screenshot");
            }
            
            // Delete temp file
            imagePath.toFile().delete();
            
        } catch (Exception e) {
            logger.error("Error in captureAndUpload", e);
        }
    }

    /* ---------------------------------------------------
     * Chụp màn hình
     * @returns BufferedImage của screenshot
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    private BufferedImage captureScreen() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(
                Toolkit.getDefaultToolkit().getScreenSize()
            );
            return robot.createScreenCapture(screenRect);
        } catch (Exception e) {
            logger.error("Failed to capture screen", e);
            return null;
        }
    }

    /* ---------------------------------------------------
     * Lấy screen resolution
     * @returns String format "WIDTHxHEIGHT"
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    private String getScreenResolution() {
        try {
            java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return screenSize.width + "x" + screenSize.height;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /* ---------------------------------------------------
     * Save image to temp file
     * @param image BufferedImage to save
     * @returns Path to saved file
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    private Path saveTempImage(BufferedImage image) throws Exception {
        String filename = String.format("screenshot_%s_%d.jpg",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
            System.currentTimeMillis()
        );
        
        Path imagePath = tempDir.resolve(filename);
        File outputFile = imagePath.toFile();
        
        ImageIO.write(image, "jpg", outputFile);
        
        return imagePath;
    }

    /* ---------------------------------------------------
     * Cleanup temp files
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    private void cleanupTempFiles() {
        try {
            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().startsWith("screenshot_")) {
                        file.delete();
                    }
                }
            }
            logger.info("Temp files cleaned up");
        } catch (Exception e) {
            logger.error("Error cleaning up temp files", e);
        }
    }

    /* ---------------------------------------------------
     * Get thống kê
     * @returns Số lần đã chụp
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public int getCaptureCount() {
        return captureCount;
    }

    /* ---------------------------------------------------
     * Get thời gian chụp gần nhất
     * @returns LocalDateTime
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public LocalDateTime getLastCaptureTime() {
        return lastCaptureTime;
    }

    /* ---------------------------------------------------
     * Check service đang chạy không
     * @returns true nếu đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:20)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return isRunning;
    }
}
