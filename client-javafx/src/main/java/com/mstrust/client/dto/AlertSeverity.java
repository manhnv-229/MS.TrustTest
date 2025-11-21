package com.mstrust.client.dto;

/* ---------------------------------------------------
 * Enum định nghĩa mức độ nghiêm trọng của cảnh báo
 * Mirror từ backend: com.mstrust.exam.entity.AlertSeverity
 * @author: K24DTCN210-NVMANH (21/11/2025 10:38)
 * --------------------------------------------------- */
public enum AlertSeverity {
    LOW,       // Mức thấp - Thông tin
    MEDIUM,    // Mức trung bình - Cần xem xét
    HIGH,      // Mức cao - Cần xem xét ngay
    CRITICAL   // Nghiêm trọng - Vi phạm nặng
}
