package com.mstrust.exam.dto.grading;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO hiển thị từng câu trả lời cần chấm điểm
 * Chứa thông tin câu hỏi, câu trả lời của học sinh và điểm hiện tại
 * @author: K24DTCN210-NVMANH (21/11/2025 13:52)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerForGradingDTO {
    /* ID của câu trả lời */
    private Long answerId;
    
    /* ID của câu hỏi */
    private Long questionId;
    
    /* Nội dung câu hỏi */
    private String questionText;
    
    /* Loại câu hỏi (MULTIPLE_CHOICE, ESSAY, SHORT_ANSWER, etc.) */
    private QuestionType questionType;
    
    /* Câu trả lời của học sinh */
    private String studentAnswer;
    
    /* Đáp án đúng (để giáo viên tham khảo) */
    private String correctAnswer;
    
    /* Điểm hiện tại của câu trả lời */
    private Double currentScore;
    
    /* Điểm tối đa của câu hỏi */
    private Double maxScore;
    
    /* Đã được chấm tự động hay chưa */
    private Boolean isAutoGraded;
    
    /* Nhận xét của giáo viên */
    private String feedback;
    
    /* Đánh giá đúng/sai (dùng cho câu hỏi tự động) */
    private Boolean isCorrect;
}
