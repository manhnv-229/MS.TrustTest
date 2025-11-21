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
     * --------------------------------------------------- */
    public static ActivityData windowFocus(String windowTitle) {
        return ActivityData.builder()
                .activityType(ActivityType.WINDOW_FOCUS)
                .details("Window switched to: " + windowTitle)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho process detected event
     * @param processName Tên process
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * --------------------------------------------------- */
    public static ActivityData processDetected(String processName) {
        return ActivityData.builder()
                .activityType(ActivityType.PROCESS_DETECTED)
                .details("Suspicious process detected: " + processName)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho clipboard event
     * @param operation Thao tác (COPY/PASTE)
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * --------------------------------------------------- */
    public static ActivityData clipboard(String operation) {
        return ActivityData.builder()
                .activityType(ActivityType.CLIPBOARD)
                .details("Clipboard operation: " + operation)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /* ---------------------------------------------------
     * Tạo ActivityData cho keystroke event
     * @param pattern Mô tả pattern
     * @returns ActivityData instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:39)
     * --------------------------------------------------- */
    public static ActivityData keystroke(String pattern) {
        return ActivityData.builder()
                .activityType(ActivityType.KEYSTROKE)
                .details("Keystroke pattern: " + pattern)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
