package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa mục đích/loại của bài thi
 * 
 * Các loại bài thi:
 * - QUICK_TEST: Kiểm tra nhanh
 * - PROGRESS_TEST: Kiểm tra tiến độ
 * - MIDTERM: Thi giữa kỳ
 * - FINAL: Thi cuối kỳ
 * - MODULE_COMPLETION: Kiểm tra kết thúc module
 * - MAKEUP: Thi bù
 * - ASSIGNMENT: Bài tập đánh giá
 * - PRACTICE: Luyện tập
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:20
 */
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
}
