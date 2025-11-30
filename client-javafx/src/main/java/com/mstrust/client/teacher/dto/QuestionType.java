package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * Enum định nghĩa các loại câu hỏi trong hệ thống
 * @author: K24DTCN210-NVMANH (28/11/2025 14:20)
 * --------------------------------------------------- */
public enum QuestionType {
    MULTIPLE_CHOICE("Trắc nghiệm"),
    ESSAY("Tự luận"), 
    CODING("Lập trình"),
    TRUE_FALSE("Đúng/Sai"),
    FILL_IN_BLANK("Điền vào chỗ trống");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
