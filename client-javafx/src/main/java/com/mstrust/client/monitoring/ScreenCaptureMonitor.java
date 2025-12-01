package com.mstrust.client.monitoring;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.util.PlatformUtil;
import com.mstrust.client.util.WindowDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * Monitor chụp màn hình tự động với random interval
 * Random interval: 30-120s (configurable)
 * JPEG compression 70%, max resolution 1920x1080
 * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
 * --------------------------------------------------- */
public class ScreenCaptureMonitor implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(ScreenCaptureMonitor.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private ScheduledExecutorService scheduler;
    private final Path tempDir;
    private final Random random;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    private LocalDateTime lastCaptureTime;
    private int captureCount = 0;
    private int nextIntervalSeconds;

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client để upload
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    public ScreenCaptureMonitor(MonitoringApiClient apiClient) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.scheduler = null;
        this.random = new Random();
        
        // Tạo temp directory
        this.tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "mstrust-screenshots");
        try {
            java.nio.file.Files.createDirectories(tempDir);
            logger.info("Temp directory created: {}", tempDir);
        } catch (Exception e) {
            logger.error("Failed to create temp directory", e);
        }
    }

    @Override
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("ScreenCaptureMonitor already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.captureCount = 0;
        
        // Create new scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ScreenCapture-Monitor-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Calculate random interval (30-120s)
        int minInterval = config.getScreenshotIntervalSeconds();
        int maxInterval = minInterval * 4; // 4x min interval
        this.nextIntervalSeconds = minInterval + random.nextInt(maxInterval - minInterval + 1);
        
        // Schedule first capture
        scheduleNextCapture();
        
        logger.info("ScreenCaptureMonitor started. SubmissionId: {}, Next capture in: {}s", 
                submissionId, nextIntervalSeconds);
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
        
        logger.info("ScreenCaptureMonitor stopped. Total captures: {}", captureCount);
    }

    @Override
    public void shutdown() {
        stop();
        cleanupTempFiles();
        logger.info("ScreenCaptureMonitor shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return "ScreenCaptureMonitor";
    }

    /* ---------------------------------------------------
     * Schedule next capture với random interval
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    private void scheduleNextCapture() {
        if (!isRunning) {
            return;
        }
        
        // Show countdown warning (10s before capture)
        int warningSeconds = 10;
        if (nextIntervalSeconds > warningSeconds) {
            scheduler.schedule(() -> {
                if (isRunning) {
                    logger.info("Screenshot will be taken in {} seconds", warningSeconds);
                    // TODO: Show UI notification to student (if needed)
                }
            }, nextIntervalSeconds - warningSeconds, TimeUnit.SECONDS);
        }
        
        // Schedule actual capture
        scheduler.schedule(() -> {
            try {
                if (!isRunning) {
                    return;
                }
                captureAndUpload();
                
                // Schedule next capture với random interval
                int minInterval = config.getScreenshotIntervalSeconds();
                int maxInterval = minInterval * 4;
                nextIntervalSeconds = minInterval + random.nextInt(maxInterval - minInterval + 1);
                scheduleNextCapture();
            } catch (Exception e) {
                logger.error("Error in scheduled capture", e);
            }
        }, nextIntervalSeconds, TimeUnit.SECONDS);
    }

    /* ---------------------------------------------------
     * Chụp màn hình và upload
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    private void captureAndUpload() {
        try {
            // Capture screenshot
            BufferedImage screenshot = PlatformUtil.captureScreen();
            if (screenshot == null) {
                logger.error("Failed to capture screenshot");
                return;
            }
            
            // Resize nếu cần (max 1920x1080)
            screenshot = resizeIfNeeded(screenshot);
            
            // Get screen info
            String screenResolution = PlatformUtil.getScreenResolution();
            String windowTitle = WindowDetector.getActiveWindowTitle();
            
            // Save to temp file với compression
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
     * Resize image nếu vượt quá max resolution
     * @param image BufferedImage gốc
     * @returns BufferedImage đã resize
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    private BufferedImage resizeIfNeeded(BufferedImage image) {
        int maxWidth = config.getScreenshotMaxWidth();
        int maxHeight = config.getScreenshotMaxHeight();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return image; // Không cần resize
        }
        
        // Calculate new dimensions (maintain aspect ratio)
        double scale = Math.min(
            (double) maxWidth / width,
            (double) maxHeight / height
        );
        
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        
        // Resize
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                         java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        logger.debug("Screenshot resized: {}x{} -> {}x{}", width, height, newWidth, newHeight);
        return resized;
    }

    /* ---------------------------------------------------
     * Save image to temp file với JPEG compression
     * @param image BufferedImage to save
     * @returns Path to saved file
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    private Path saveTempImage(BufferedImage image) throws Exception {
        String filename = String.format("screenshot_%s_%d.jpg",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
            System.currentTimeMillis()
        );
        
        Path imagePath = tempDir.resolve(filename);
        File outputFile = imagePath.toFile();
        
        // JPEG compression với quality từ config
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) config.getScreenshotJpegQuality());
        }
        
        javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);
        writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        writer.dispose();
        ios.close();
        
        return imagePath;
    }

    /* ---------------------------------------------------
     * Cleanup temp files
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
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
     * Get số lần đã chụp
     * @returns int capture count
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    public int getCaptureCount() {
        return captureCount;
    }

    /* ---------------------------------------------------
     * Get thời gian chụp gần nhất
     * @returns LocalDateTime
     * @author: K24DTCN210-NVMANH (01/12/2025 10:10)
     * --------------------------------------------------- */
    public LocalDateTime getLastCaptureTime() {
        return lastCaptureTime;
    }
}

