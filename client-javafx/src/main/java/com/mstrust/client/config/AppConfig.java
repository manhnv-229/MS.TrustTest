package com.mstrust.client.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/* ---------------------------------------------------
 * Quản lý cấu hình ứng dụng từ config.properties
 * Singleton pattern để truy cập global
 * @author: K24DTCN210-NVMANH (21/11/2025 10:36)
 * --------------------------------------------------- */
@Getter
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance;
    
    // API Configuration
    private final String apiBaseUrl;
    private final int apiTimeoutSeconds;
    
    // Monitoring Configuration
    private final int screenshotIntervalSeconds;
    private final int activityBatchIntervalSeconds;
    private final int screenshotMaxWidth;
    private final int screenshotMaxHeight;
    private final double screenshotJpegQuality;
    
    // Alert Thresholds
    private final int alertWindowSwitchThreshold;
    private final int alertWindowSwitchTimeframeMinutes;
    private final int alertClipboardThreshold;
    private final int alertClipboardTimeframeMinutes;
    
    // Blacklisted Processes
    private final List<String> blacklistProcesses;
    
    // Network Queue Configuration
    private final int queueMaxSize;
    private final int queueRetryMaxAttempts;
    private final int queueRetryDelaySeconds;
    
    // Logging
    private final String loggingLevel;
    private final boolean loggingFileEnabled;
    private final String loggingFilePath;

    /* ---------------------------------------------------
     * Constructor private - đọc config từ file
     * @author: K24DTCN210-NVMANH (21/11/2025 10:36)
     * --------------------------------------------------- */
    private AppConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Không tìm thấy config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            logger.error("Lỗi khi đọc config.properties", e);
            throw new RuntimeException("Không thể load configuration", e);
        }
        
        // Load API Configuration
        this.apiBaseUrl = props.getProperty("api.base.url", "http://localhost:8080");
        this.apiTimeoutSeconds = Integer.parseInt(
            props.getProperty("api.timeout.seconds", "30"));
        
        // Load Monitoring Configuration
        this.screenshotIntervalSeconds = Integer.parseInt(
            props.getProperty("monitoring.screenshot.interval.seconds", "30"));
        this.activityBatchIntervalSeconds = Integer.parseInt(
            props.getProperty("monitoring.activity.batch.interval.seconds", "60"));
        this.screenshotMaxWidth = Integer.parseInt(
            props.getProperty("monitoring.screenshot.max.width", "1920"));
        this.screenshotMaxHeight = Integer.parseInt(
            props.getProperty("monitoring.screenshot.max.height", "1080"));
        this.screenshotJpegQuality = Double.parseDouble(
            props.getProperty("monitoring.screenshot.jpeg.quality", "0.7"));
        
        // Load Alert Thresholds
        this.alertWindowSwitchThreshold = Integer.parseInt(
            props.getProperty("alert.window.switch.threshold", "10"));
        this.alertWindowSwitchTimeframeMinutes = Integer.parseInt(
            props.getProperty("alert.window.switch.timeframe.minutes", "5"));
        this.alertClipboardThreshold = Integer.parseInt(
            props.getProperty("alert.clipboard.threshold", "20"));
        this.alertClipboardTimeframeMinutes = Integer.parseInt(
            props.getProperty("alert.clipboard.timeframe.minutes", "10"));
        
        // Load Blacklisted Processes
        String blacklistStr = props.getProperty("blacklist.processes", "");
        this.blacklistProcesses = Arrays.asList(blacklistStr.split(","));
        
        // Load Network Queue Configuration
        this.queueMaxSize = Integer.parseInt(
            props.getProperty("queue.max.size", "1000"));
        this.queueRetryMaxAttempts = Integer.parseInt(
            props.getProperty("queue.retry.max.attempts", "3"));
        this.queueRetryDelaySeconds = Integer.parseInt(
            props.getProperty("queue.retry.delay.seconds", "5"));
        
        // Load Logging Configuration
        this.loggingLevel = props.getProperty("logging.level", "INFO");
        this.loggingFileEnabled = Boolean.parseBoolean(
            props.getProperty("logging.file.enabled", "true"));
        this.loggingFilePath = props.getProperty("logging.file.path", "./logs/client.log");
        
        logger.info("Configuration loaded successfully");
    }

    /* ---------------------------------------------------
     * Lấy instance singleton
     * @returns Instance duy nhất của AppConfig
     * @author: K24DTCN210-NVMANH (21/11/2025 10:36)
     * --------------------------------------------------- */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /* ---------------------------------------------------
     * Lấy threshold cho window switches (alerts)
     * @returns int threshold (mặc định: 10)
     * @author: K24DTCN210-NVMANH (21/11/2025 11:44)
     * --------------------------------------------------- */
    public int getWindowSwitchThreshold() {
        return alertWindowSwitchThreshold;
    }

    /* ---------------------------------------------------
     * Lấy threshold cho clipboard operations (alerts)
     * @returns int threshold (mặc định: 20)
     * @author: K24DTCN210-NVMANH (21/11/2025 11:44)
     * --------------------------------------------------- */
    public int getClipboardThreshold() {
        return alertClipboardThreshold;
    }

    /* ---------------------------------------------------
     * Kiểm tra process có nằm trong blacklist không
     * @param processName Tên process cần kiểm tra
     * @returns true nếu nằm trong blacklist
     * @author: K24DTCN210-NVMANH (21/11/2025 11:46)
     * --------------------------------------------------- */
    public boolean isProcessBlacklisted(String processName) {
        if (processName == null || processName.trim().isEmpty()) {
            return false;
        }
        
        String lowerProcessName = processName.toLowerCase();
        return blacklistProcesses.stream()
            .filter(p -> !p.trim().isEmpty())
            .anyMatch(blacklisted -> lowerProcessName.contains(blacklisted.toLowerCase().trim()));
    }
}
