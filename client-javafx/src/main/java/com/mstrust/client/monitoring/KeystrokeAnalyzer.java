package com.mstrust.client.monitoring;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Analyzer phân tích keystroke patterns
 * Track typing speed (WPM), detect paste vs typing
 * Alert on suspicious patterns (speed > 2x average, instant text)
 * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
 * --------------------------------------------------- */
public class KeystrokeAnalyzer implements Monitor, NativeKeyListener {
    private static final Logger logger = LoggerFactory.getLogger(KeystrokeAnalyzer.class);
    
    private final MonitoringApiClient apiClient;
    private final AppConfig config;
    private final Consumer<ActivityData> activityCallback;
    
    private Long currentSubmissionId;
    private boolean isRunning = false;
    
    // Keystroke tracking
    private final Queue<KeystrokeEvent> keystrokeEvents = new ConcurrentLinkedQueue<>();
    private LocalDateTime lastKeystrokeTime;
    private int totalKeystrokes = 0;
    
    // Statistics
    private double averageWPM = 0.0;
    private long totalTypingTime = 0; // milliseconds
    private final List<Double> wpmSamples = new ArrayList<>();
    
    // Paste detection
    private static final long PASTE_DETECTION_THRESHOLD_MS = 100; // < 100ms = likely paste
    private int suspiciousPasteCount = 0;

    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client
     * @param activityCallback Callback để gửi activities
     * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
     * --------------------------------------------------- */
    public KeystrokeAnalyzer(MonitoringApiClient apiClient, Consumer<ActivityData> activityCallback) {
        this.apiClient = apiClient;
        this.config = AppConfig.getInstance();
        this.activityCallback = activityCallback;
    }

    @Override
    public void start(Long submissionId) {
        if (isRunning) {
            logger.warn("KeystrokeAnalyzer already running");
            return;
        }
        
        this.currentSubmissionId = submissionId;
        this.isRunning = true;
        this.totalKeystrokes = 0;
        this.averageWPM = 0.0;
        this.suspiciousPasteCount = 0;
        keystrokeEvents.clear();
        wpmSamples.clear();
        
        // Register global keyboard hook
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            logger.info("KeystrokeAnalyzer started for submission: {}", submissionId);
        } catch (NativeHookException e) {
            logger.error("Failed to register native hook", e);
            isRunning = false;
        }
    }

    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Unregister keyboard hook
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            logger.error("Error unregistering native hook", e);
        }
        
        logger.info("KeystrokeAnalyzer stopped. Total keystrokes: {}, Avg WPM: {:.2f}", 
                totalKeystrokes, String.format("%.2f", averageWPM));
    }

    @Override
    public void shutdown() {
        stop();
        logger.info("KeystrokeAnalyzer shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return "KeystrokeAnalyzer";
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!isRunning) {
            return;
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            long timeSinceLastKeystroke = 0;
            
            if (lastKeystrokeTime != null) {
                timeSinceLastKeystroke = java.time.Duration.between(lastKeystrokeTime, now).toMillis();
            }
            
            // Record keystroke
            KeystrokeEvent event = new KeystrokeEvent(now, timeSinceLastKeystroke);
            keystrokeEvents.add(event);
            
            // Keep only last 1000 events
            while (keystrokeEvents.size() > 1000) {
                keystrokeEvents.poll();
            }
            
            totalKeystrokes++;
            lastKeystrokeTime = now;
            
            // Detect paste (very fast keystrokes)
            if (timeSinceLastKeystroke > 0 && timeSinceLastKeystroke < PASTE_DETECTION_THRESHOLD_MS) {
                suspiciousPasteCount++;
                if (suspiciousPasteCount >= 10) {
                    logger.warn("Suspicious paste pattern detected: {} fast keystrokes", suspiciousPasteCount);
                    // Create activity
                    ActivityData activity = ActivityData.keystroke("Phát hiện mẫu dán văn bản đáng nghi");
                    if (activityCallback != null) {
                        activityCallback.accept(activity);
                    }
                    suspiciousPasteCount = 0; // Reset
                }
            } else {
                suspiciousPasteCount = 0; // Reset on normal typing
            }
            
            // Calculate WPM periodically
            if (totalKeystrokes % 50 == 0) {
                calculateWPM();
            }
            
        } catch (Exception ex) {
            logger.error("Error processing keystroke", ex);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not used
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used
    }

    /* ---------------------------------------------------
     * Calculate Words Per Minute (WPM)
     * WPM = (total characters / 5) / (time in minutes)
     * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
     * --------------------------------------------------- */
    private void calculateWPM() {
        if (keystrokeEvents.size() < 10) {
            return; // Need at least 10 keystrokes
        }
        
        try {
            KeystrokeEvent first = keystrokeEvents.peek();
            KeystrokeEvent last = null;
            for (KeystrokeEvent event : keystrokeEvents) {
                last = event;
            }
            
            if (first == null || last == null) {
                return;
            }
            
            long totalTime = java.time.Duration.between(first.timestamp, last.timestamp).toMillis();
            if (totalTime == 0) {
                return;
            }
            
            // WPM = (characters / 5) / (minutes)
            // Assume each keystroke = 1 character (simplified)
            double minutes = totalTime / 60000.0;
            double wpm = (keystrokeEvents.size() / 5.0) / minutes;
            
            wpmSamples.add(wpm);
            
            // Keep only last 20 samples
            while (wpmSamples.size() > 20) {
                wpmSamples.remove(0);
            }
            
            // Calculate average WPM
            averageWPM = wpmSamples.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            
            // Check for abnormal speed (> 2x average)
            if (averageWPM > 0 && wpm > averageWPM * 2) {
                logger.warn("Abnormal typing speed detected: {} WPM (avg: {})", 
                        String.format("%.2f", wpm), String.format("%.2f", averageWPM));
                // Create activity
                ActivityData activity = ActivityData.keystroke(
                    String.format("Tốc độ gõ bất thường: %.2f WPM (trung bình: %.2f)", wpm, averageWPM)
                );
                if (activityCallback != null) {
                    activityCallback.accept(activity);
                }
            }
            
            logger.debug("WPM calculated: {} (avg: {})", 
                    String.format("%.2f", wpm), String.format("%.2f", averageWPM));
            
        } catch (Exception e) {
            logger.error("Error calculating WPM", e);
        }
    }

    /* ---------------------------------------------------
     * Get average WPM
     * @returns double average WPM
     * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
     * --------------------------------------------------- */
    public double getAverageWPM() {
        return averageWPM;
    }

    /* ---------------------------------------------------
     * Get total keystrokes
     * @returns int count
     * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
     * --------------------------------------------------- */
    public int getTotalKeystrokes() {
        return totalKeystrokes;
    }

    /* ---------------------------------------------------
     * Inner class để track keystroke events
     * @author: K24DTCN210-NVMANH (01/12/2025 10:50)
     * --------------------------------------------------- */
    private static class KeystrokeEvent {
        final LocalDateTime timestamp;
        final long timeSinceLastKeystroke; // milliseconds
        
        KeystrokeEvent(LocalDateTime timestamp, long timeSinceLastKeystroke) {
            this.timestamp = timestamp;
            this.timeSinceLastKeystroke = timeSinceLastKeystroke;
        }
    }
}

