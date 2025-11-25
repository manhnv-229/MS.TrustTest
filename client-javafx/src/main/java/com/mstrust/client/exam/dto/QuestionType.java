package com.mstrust.client.exam.dto;

/* ---------------------------------------------------
 * Enum định nghĩa các loại câu hỏi trong hệ thống
 * - Phải khớp với backend QuestionType enum
 * @author: K24DTCN210-NVMANH (23/11/2025 11:50)
 * EditBy: K24DTCN210-NVMANH (25/11/2025 23:13) - Thêm getDisplayName() method
 * --------------------------------------------------- */
public enum QuestionType {
    MULTIPLE_CHOICE("Trắc nghiệm (1 đáp án)"),
    MULTIPLE_SELECT("Trắc nghiệm (nhiều đáp án)"),
    TRUE_FALSE("Đúng/Sai"),
    ESSAY("Tự luận"),
    SHORT_ANSWER("Câu trả lời ngắn"),
    CODING("Lập trình"),
    FILL_IN_BLANK("Điền vào chỗ trống"),
    MATCHING("Nối cặp");
    
    private final String displayName;
    
    QuestionType(String displayName) {
        this.displayName = displayName;
    }
    
    /* ---------------------------------------------------
     * Lấy tên hiển thị tiếng Việt của loại câu hỏi
     * @returns Tên hiển thị tiếng Việt
     * @author: K24DTCN210-NVMANH (25/11/2025 23:13)
     * --------------------------------------------------- */
    public String getDisplayName() {
        return displayName;
    }
}
