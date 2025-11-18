package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa mức độ khó của câu hỏi
 * 
 * Các mức độ:
 * - EASY: Dễ
 * - MEDIUM: Trung bình
 * - HARD: Khó
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:19
 */
public enum Difficulty {
    EASY("Dễ"),
    MEDIUM("Trung bình"),
    HARD("Khó");
    
    private final String displayName;
    
    Difficulty(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
