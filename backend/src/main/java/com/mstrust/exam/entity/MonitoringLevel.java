package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa mức độ giám sát chống gian lận
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:20
 */
public enum MonitoringLevel {
    LOW("Thấp"),
    MEDIUM("Trung bình"),
    HIGH("Cao");
    
    private final String displayName;
    
    MonitoringLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
