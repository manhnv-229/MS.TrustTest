package com.mstrust.exam.dto;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO chứa thông tin chi tiết câu trả lời của sinh viên để giáo viên chấm điểm
 * @author: K24DTCN210-NVMANH (20/11/2025 11:16)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerForGradingDTO {
    
    /* ---------------------------------------------------
     * ID của câu trả lời
     * --------------------------------------------------- */
    private Long id;
    
    /* ---------------------------------------------------
     * ID của câu hỏi
     * --------------------------------------------------- */
    private Long questionId;
    
    /* ---------------------------------------------------
     * Nội dung câu hỏi
     * --------------------------------------------------- */
    private String questionText;
    
    /* ---------------------------------------------------
     * Loại câu hỏi (MULTIPLE_CHOICE, ESSAY, CODING, FILE_UPLOAD)
     * --------------------------------------------------- */
    private QuestionType questionType;
    
    /* ---------------------------------------------------
     * Câu trả lời của sinh viên (dạng Object để linh hoạt)
     * --------------------------------------------------- */
    private Object studentAnswer;
    
    /* ---------------------------------------------------
     * Câu trả lời dạng text (cho ESSAY, CODING)
     * --------------------------------------------------- */
    private String studentAnswerText;
    
    /* ---------------------------------------------------
     * URL file đã upload (cho FILE_UPLOAD)
     * --------------------------------------------------- */
    private String uploadedFileUrl;
    
    /* ---------------------------------------------------
     * Đáp án đúng (dạng Object để linh hoạt)
     * --------------------------------------------------- */
    private Object correctAnswer;
    
    /* ---------------------------------------------------
     * Điểm tối đa của câu hỏi
     * --------------------------------------------------- */
    private BigDecimal maxPoints;
    
    /* ---------------------------------------------------
     * Điểm đạt được (null nếu chưa chấm)
     * --------------------------------------------------- */
    private BigDecimal pointsEarned;
    
    /* ---------------------------------------------------
     * Đúng hay sai (null nếu chưa chấm)
     * --------------------------------------------------- */
    private Boolean isCorrect;
    
    /* ---------------------------------------------------
     * Nhận xét của giáo viên
     * --------------------------------------------------- */
    private String teacherFeedback;
    
    /* ---------------------------------------------------
     * Đã được chấm điểm chưa
     * --------------------------------------------------- */
    private Boolean isGraded;
    
    /* ---------------------------------------------------
     * Thời gian chấm điểm
     * --------------------------------------------------- */
    private LocalDateTime gradedAt;
    
    /* ---------------------------------------------------
     * Tên giáo viên chấm điểm
     * --------------------------------------------------- */
    private String gradedByName;
}
