package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * Enum định nghĩa định dạng/hình thức bài thi (Client-side)
 * Mapping với backend ExamFormat enum
 * @author: K24DTCN210-NVMANH (27/11/2025 22:27)
 * --------------------------------------------------- */
public enum ExamFormat {
    MULTIPLE_CHOICE_ONLY("Chỉ trắc nghiệm"),
    ESSAY_ONLY("Chỉ tự luận"),
    CODING_ONLY("Chỉ coding"),
    MIXED("Hỗn hợp");
    
    private final String displayName;
    
    ExamFormat(String displayName) {
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
