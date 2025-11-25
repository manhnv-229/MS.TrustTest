package com.mstrust.exam.dto;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/* ---------------------------------------------------
 * DTO cho câu hỏi khi student làm bài
 * KHÔNG chứa correctAnswer để tránh leak đáp án
 * @author: K24DTCN210-NVMANH (19/11/2025 15:18)
 * EditBy: K24DTCN210-NVMANH (24/11/2025 10:27) - Changed options from Map to List<String>
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionForStudentDTO {
    
    private Long id;
    private Long questionBankId;
    
    // Question content
    private QuestionType questionType;
    private String questionText;
    private String questionCode;
    
    // Options (for SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE)
    // Format: ["A. Option A text", "B. Option B text", "C. Option C text", ...]
    // ⚠️ KHÔNG chứa correctAnswer
    private List<String> options;
    
    // Scoring
    private BigDecimal maxScore;  // Điểm tối đa của câu này trong exam
    
    // Display order
    private Integer displayOrder;
    
    // Media (optional)
    private String imageUrl;
    private String audioUrl;
    private String videoUrl;
    
    // Code execution (for CODING type)
    private Boolean allowCodeExecution;
    private String programmingLanguage;
    
    // Hints (optional)
    private String hint;
    
    // Student's saved answer (if exists)
    private Object savedAnswer;  // JSON object chứa answer đã save
    private Boolean isAnswered;  // Đã trả lời chưa
}
