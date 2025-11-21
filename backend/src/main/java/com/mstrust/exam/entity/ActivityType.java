package com.mstrust.exam.entity;

/* ---------------------------------------------------
 * Enum định nghĩa các loại hoạt động trong monitoring system
 * @author: K24DTCN210-NVMANH (21/11/2025 10:07)
 * --------------------------------------------------- */
public enum ActivityType {
    WINDOW_FOCUS,      // Chuyển cửa sổ (alt+tab)
    PROCESS_DETECTED,  // Phát hiện process đáng ngờ
    CLIPBOARD,         // Hoạt động clipboard (copy/paste)
    KEYSTROKE          // Phân tích keystroke patterns
}
