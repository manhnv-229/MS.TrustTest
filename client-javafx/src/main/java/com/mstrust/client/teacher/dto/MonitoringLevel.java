package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * Enum định nghĩa mức độ giám sát thi (Client-side only)
 * Backend không có enum này - sẽ lưu như String hoặc config
 * @author: K24DTCN210-NVMANH (27/11/2025 22:27)
 * --------------------------------------------------- */
public enum MonitoringLevel {
    LOW("Thấp - Chỉ screenshot định kỳ"),
    MEDIUM("Trung bình - Screenshot + window tracking"),
    HIGH("Cao - Full monitoring (screenshot, window, process, clipboard)");
    
    private final String displayName;
    
    MonitoringLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
