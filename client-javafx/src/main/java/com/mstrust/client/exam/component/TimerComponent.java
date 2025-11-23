package com.mstrust.client.exam.component;

import com.mstrust.client.exam.util.TimeFormatter;
import com.mstrust.client.exam.util.TimerPhase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/* ---------------------------------------------------
 * Component hiển thị đồng hồ đếm ngược thời gian làm bài
 * - Countdown display format HH:MM:SS
 * - Color coding: Green (>50%), Yellow (20-50%), Red (<20%)
 * - Visual warnings tại 10min, 5min, 1min
 * - Auto-submit callback khi hết giờ
 * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
 * --------------------------------------------------- */
public class TimerComponent extends VBox {
    
    private Label titleLabel;
    private Label timerLabel;
    private Label warningLabel;
    
    private long remainingSeconds;
    private long totalSeconds;
    private Timeline timeline;
    
    private Runnable onTimeExpiredCallback;
    private Runnable onWarningCallback;
    
    private boolean hasWarned10Min = false;
    private boolean hasWarned5Min = false;
    private boolean hasWarned1Min = false;
    
    /* ---------------------------------------------------
     * Constructor khởi tạo timer component
     * @param totalSeconds Tổng số giây của bài thi
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public TimerComponent(long totalSeconds) {
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = totalSeconds;
        
        initializeUI();
        setupTimeline();
        updateDisplay();
    }
    
    /* ---------------------------------------------------
     * Khởi tạo giao diện component
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void initializeUI() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.getStyleClass().add("timer-component");
        
        // Title label
        titleLabel = new Label("Thời gian còn lại");
        titleLabel.getStyleClass().add("timer-title");
        
        // Timer display label
        timerLabel = new Label("00:00:00");
        timerLabel.getStyleClass().addAll("timer-display", "timer-green");
        
        // Warning label (ẩn mặc định)
        warningLabel = new Label();
        warningLabel.getStyleClass().add("timer-warning");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
        
        this.getChildren().addAll(titleLabel, timerLabel, warningLabel);
    }
    
    /* ---------------------------------------------------
     * Thiết lập Timeline để đếm ngược mỗi giây
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void setupTimeline() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    updateDisplay();
                    checkWarnings();
                } else {
                    handleTimeExpired();
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    /* ---------------------------------------------------
     * Cập nhật hiển thị đồng hồ và màu sắc
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void updateDisplay() {
        Platform.runLater(() -> {
            // Update time text
            String timeText = TimeFormatter.formatSeconds(remainingSeconds);
            timerLabel.setText(timeText);
            
            // Update color phase
            updateColorPhase();
        });
    }
    
    /* ---------------------------------------------------
     * Cập nhật màu sắc dựa trên % thời gian còn lại
     * Green: >50%, Yellow: 20-50%, Red: <20%
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void updateColorPhase() {
        TimerPhase phase = TimeFormatter.getTimerPhase(remainingSeconds, totalSeconds);
        
        // Remove all color classes
        timerLabel.getStyleClass().removeAll("timer-green", "timer-yellow", "timer-red");
        
        // Add appropriate color class
        switch (phase) {
            case GREEN:
                timerLabel.getStyleClass().add("timer-green");
                break;
            case YELLOW:
                timerLabel.getStyleClass().add("timer-yellow");
                break;
            case RED:
                timerLabel.getStyleClass().add("timer-red");
                break;
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra và hiển thị cảnh báo tại các mốc thời gian
     * 10 phút, 5 phút, 1 phút
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void checkWarnings() {
        long minutes = remainingSeconds / 60;
        
        if (minutes == 10 && !hasWarned10Min) {
            hasWarned10Min = true;
            showWarning("⚠️ Còn 10 phút!");
            triggerWarningCallback();
        } else if (minutes == 5 && !hasWarned5Min) {
            hasWarned5Min = true;
            showWarning("⚠️ Còn 5 phút!");
            triggerWarningCallback();
        } else if (minutes == 1 && !hasWarned1Min) {
            hasWarned1Min = true;
            showWarning("🚨 Còn 1 phút!");
            triggerWarningCallback();
        }
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo cảnh báo
     * @param message Nội dung cảnh báo
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void showWarning(String message) {
        Platform.runLater(() -> {
            warningLabel.setText(message);
            warningLabel.setVisible(true);
            warningLabel.setManaged(true);
            
            // Auto hide after 5 seconds
            Timeline hideTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> {
                    warningLabel.setVisible(false);
                    warningLabel.setManaged(false);
                })
            );
            hideTimeline.play();
        });
    }
    
    /* ---------------------------------------------------
     * Xử lý khi hết giờ làm bài
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void handleTimeExpired() {
        stop();
        Platform.runLater(() -> {
            timerLabel.setText("00:00:00");
            timerLabel.getStyleClass().removeAll("timer-green", "timer-yellow");
            timerLabel.getStyleClass().add("timer-red");
            showWarning("⏰ HẾT GIỜ! Đang tự động nộp bài...");
            
            // Trigger auto-submit callback
            if (onTimeExpiredCallback != null) {
                onTimeExpiredCallback.run();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Gọi callback khi có cảnh báo
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    private void triggerWarningCallback() {
        if (onWarningCallback != null) {
            Platform.runLater(() -> onWarningCallback.run());
        }
    }
    
    /* ---------------------------------------------------
     * Bắt đầu đếm ngược
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void start() {
        if (timeline != null) {
            timeline.play();
        }
    }
    
    /* ---------------------------------------------------
     * Tạm dừng đếm ngược
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }
    
    /* ---------------------------------------------------
     * Tiếp tục đếm ngược
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }
    
    /* ---------------------------------------------------
     * Dừng hẳn đếm ngược
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
    
    /* ---------------------------------------------------
     * Set callback khi hết giờ
     * @param callback Runnable sẽ được gọi khi timer = 0
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void setOnTimeExpired(Runnable callback) {
        this.onTimeExpiredCallback = callback;
    }
    
    /* ---------------------------------------------------
     * Set callback khi có cảnh báo
     * @param callback Runnable sẽ được gọi tại các mốc cảnh báo
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void setOnWarning(Runnable callback) {
        this.onWarningCallback = callback;
    }
    
    /* ---------------------------------------------------
     * Lấy số giây còn lại
     * @returns Số giây còn lại
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public long getRemainingSeconds() {
        return remainingSeconds;
    }
    
    /* ---------------------------------------------------
     * Set thời gian còn lại (dùng khi sync với server)
     * @param seconds Số giây mới
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void setRemainingSeconds(long seconds) {
        this.remainingSeconds = seconds;
        updateDisplay();
    }
    
    /* ---------------------------------------------------
     * Kiểm tra timer có đang chạy không
     * @returns true nếu đang chạy
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return timeline != null && 
               timeline.getStatus() == Timeline.Status.RUNNING;
    }
    
    /* ---------------------------------------------------
     * Cleanup resources khi component bị destroy
     * @author: K24DTCN210-NVMANH (23/11/2025 12:14)
     * --------------------------------------------------- */
    public void cleanup() {
        stop();
        if (timeline != null) {
            timeline = null;
        }
    }
}
