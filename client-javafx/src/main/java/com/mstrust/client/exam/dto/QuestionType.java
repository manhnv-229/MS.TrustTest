package com.mstrust.client.exam.dto;

/* ---------------------------------------------------
 * Enum định nghĩa các loại câu hỏi trong hệ thống
 * - Phải khớp với backend QuestionType enum
 * @author: K24DTCN210-NVMANH (23/11/2025 11:50)
 * --------------------------------------------------- */
public enum QuestionType {
    MULTIPLE_CHOICE,    // Trắc nghiệm 1 đáp án
    MULTIPLE_SELECT,    // Trắc nghiệm nhiều đáp án
    TRUE_FALSE,         // Đúng/Sai
    ESSAY,             // Tự luận
    SHORT_ANSWER,      // Câu trả lời ngắn
    CODING,            // Lập trình
    FILL_IN_BLANK,     // Điền vào chỗ trống
    MATCHING           // Nối câu
}
