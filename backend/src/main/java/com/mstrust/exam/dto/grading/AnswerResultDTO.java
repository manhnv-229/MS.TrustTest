package com.mstrust.exam.dto.grading;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO hiển thị kết quả từng câu trả lời cho học sinh
 * Chứa câu hỏi, câu trả lời của học sinh, điểm và nhận xét
 * @author: K24DTCN210-NVMANH (21/11/2025 13:58)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultDTO {
    /* Số thứ tự câu hỏi */
    private Integer questionNumber;
    
    /* Nội dung câu hỏi */
    private String questionText;
    
    /* Loại câu hỏi */
    private QuestionType questionType;
    
    /* Câu trả lời của học sinh */
    private String studentAnswer;
    
    /* Đáp án đúng (chỉ hiển thị nếu exam cho phép) */
    private String correctAnswer;
    
    /* Điểm đạt được */
    private Double score;
    
    /* Điểm tối đa */
    private Double maxScore;
    
    /* Đúng hay sai */
    private Boolean isCorrect;
    
    /* Nhận xét của giáo viên */
    private String feedback;
}
