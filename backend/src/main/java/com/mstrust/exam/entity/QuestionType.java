package com.mstrust.exam.entity;

/**
 * Enum định nghĩa các loại câu hỏi trong hệ thống
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 00:55)
 */
public enum QuestionType {
    /**
     * Trắc nghiệm - Chọn 1 đáp án đúng
     */
    MULTIPLE_CHOICE,
    
    /**
     * Chọn nhiều đáp án đúng
     */
    MULTIPLE_SELECT,
    
    /**
     * Câu hỏi Đúng/Sai
     */
    TRUE_FALSE,
    
    /**
     * Tự luận - Trả lời dài
     */
    ESSAY,
    
    /**
     * Trả lời ngắn - Điền từ/cụm từ
     */
    SHORT_ANSWER,
    
    /**
     * Lập trình - Viết code
     */
    CODING,
    
    /**
     * Điền vào chỗ trống
     */
    FILL_IN_BLANK,
    
    /**
     * Nối câu - Ghép đáp án
     */
    MATCHING
}
