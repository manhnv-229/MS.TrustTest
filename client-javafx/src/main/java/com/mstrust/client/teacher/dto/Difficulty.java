package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * Enum Difficulty - Độ khó của câu hỏi
 * @author: K24DTCN210-NVMANH (25/11/2025 22:35)
 * --------------------------------------------------- */
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
    
    @Override
    public String toString() {
        return displayName;
    }
}
