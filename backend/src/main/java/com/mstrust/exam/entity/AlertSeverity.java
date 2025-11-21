package com.mstrust.exam.entity;

/* ---------------------------------------------------
 * Enum định nghĩa mức độ nghiêm trọng của cảnh báo
 * @author: K24DTCN210-NVMANH (21/11/2025 10:07)
 * --------------------------------------------------- */
public enum AlertSeverity {
    LOW,       // Cảnh báo thấp - theo dõi
    MEDIUM,    // Cảnh báo trung bình - cần xem xét
    HIGH,      // Cảnh báo cao - cần xử lý ngay
    CRITICAL   // Cảnh báo nghiêm trọng - nguy cơ gian lận cao
}
