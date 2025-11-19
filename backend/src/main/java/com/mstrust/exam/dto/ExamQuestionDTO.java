package com.mstrust.exam.dto;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Response DTO cho exam-question association
 * Chứa thông tin câu hỏi kèm theo điểm số và thứ tự trong bài thi
 * @author: K24DTCN210-NVMANH (19/11/2025 09:16)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    
    // Thông tin từ ExamQuestion
    private Long examQuestionId;
    private Integer questionOrder;
    private BigDecimal points;
    
    // Thông tin từ QuestionBank
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private String difficulty;
    private Long subjectId;
    private String subjectName;
    
    // Metadata
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
