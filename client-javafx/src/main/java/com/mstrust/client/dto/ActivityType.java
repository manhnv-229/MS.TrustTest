package com.mstrust.client.dto;

/* ---------------------------------------------------
 * Enum định nghĩa các loại hoạt động giám sát
 * Mirror từ backend: com.mstrust.exam.entity.ActivityType
 * @author: K24DTCN210-NVMANH (21/11/2025 10:37)
 * --------------------------------------------------- */
public enum ActivityType {
    WINDOW_FOCUS,      // Chuyển cửa sổ (Alt+Tab)
    PROCESS_DETECTED,  // Phát hiện process đáng ngờ
    CLIPBOARD,         // Thao tác clipboard (Copy/Paste)
    KEYSTROKE          // Phân tích keystroke pattern
}
