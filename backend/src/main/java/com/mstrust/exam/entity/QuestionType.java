package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa các loại câu hỏi trong hệ thống
 * 
 * Các loại câu hỏi:
 * - MULTIPLE_CHOICE: Trắc nghiệm 1 đáp án đúng
 * - MULTIPLE_SELECT: Trắc nghiệm nhiều đáp án đúng
 * - TRUE_FALSE: Câu hỏi Đúng/Sai
 * - ESSAY: Câu hỏi tự luận
 * - SHORT_ANSWER: Câu trả lời ngắn
 * - CODING: Câu hỏi lập trình
 * - FILL_IN_BLANK: Điền vào chỗ trống
 * - MATCHING: Nối câu hỏi với đáp án
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:18
 */
public enum QuestionType {
    MULTIPLE_CHOICE("Trắc nghiệm 1 đáp án"),
    MULTIPLE_SELECT("Trắc nghiệm nhiều đáp án"),
    TRUE_FALSE("Đúng/Sai"),
    ESSAY("Tự luận"),
    SHORT_ANSWER("Trả lời ngắn"),
    CODING("Lập trình"),
    FILL_IN_BLANK("Điền vào chỗ trống"),
    MATCHING("Nối đáp án");
    
    private final String displayName;
    
    QuestionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
