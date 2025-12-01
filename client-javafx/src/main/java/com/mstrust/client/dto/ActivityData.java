package com.mstrust.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO chứa thông tin một hoạt động giám sát
 * Dùng để gửi batch activities lên backend
 * Mirror từ backend ActivityLogRequest.ActivityData
 * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityData {
    private ActivityType activityType;
    private String details;
    private LocalDateTime timestamp;

    /* ---------------------------------------------------
     * Tạo ActivityData cho window focus event
     * @param windowTitle Tiêu đề cửa sổ
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:55) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static ActivityData windowFocus(String windowTitle) {
        ActivityData data = new ActivityData();
        data.setActivityType(ActivityType.WINDOW_FOCUS);
        data.setDetails("Chuyển cửa sổ sang: " + windowTitle);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho process detected event
     * @param processName Tên process
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:55) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static ActivityData processDetected(String processName) {
        ActivityData data = new ActivityData();
        data.setActivityType(ActivityType.PROCESS_DETECTED);
        data.setDetails("Phát hiện ứng dụng đáng nghi: " + processName);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho clipboard event
     * @param operation Thao tác (COPY/PASTE)
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:55) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static ActivityData clipboard(String operation) {
        ActivityData data = new ActivityData();
        data.setActivityType(ActivityType.CLIPBOARD);
        data.setDetails("Thao tác clipboard: " + operation);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho keystroke event
     * @param pattern Mô tả pattern
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:55) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static ActivityData keystroke(String pattern) {
        ActivityData data = new ActivityData();
        data.setActivityType(ActivityType.KEYSTROKE);
        data.setDetails("Mẫu gõ phím: " + pattern);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }
    
    // Manual getters/setters (backup for Lombok issues)
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
