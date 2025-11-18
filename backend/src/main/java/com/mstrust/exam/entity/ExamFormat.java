package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa định dạng/hình thức bài thi
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:20
 */
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
}
