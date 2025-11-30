package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * Enum định nghĩa mục đích/loại của bài thi (Client-side)
 * Mapping với backend ExamPurpose enum
 * @author: K24DTCN210-NVMANH (27/11/2025 22:26)
 * --------------------------------------------------- */
public enum ExamPurpose {
    QUICK_TEST("Kiểm tra nhanh"),
    PROGRESS_TEST("Kiểm tra tiến độ"),
    MIDTERM("Thi giữa kỳ"),
    FINAL("Thi cuối kỳ"),
    MODULE_COMPLETION("Kết thúc module"),
    MAKEUP("Thi bù"),
    ASSIGNMENT("Bài tập đánh giá"),
    PRACTICE("Luyện tập");
    
    private final String displayName;
    
    ExamPurpose(String displayName) {
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
