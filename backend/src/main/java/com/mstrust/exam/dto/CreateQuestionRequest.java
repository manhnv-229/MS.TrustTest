package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho việc tạo mới câu hỏi (Phase 3)
 * @author NVMANH with Cline
 * @created 18/11/2025 18:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {
    
    private Long subjectId; // Optional
    
    @NotNull(message = "Question type không được để trống")
    private QuestionType questionType;
    
    private Difficulty difficulty;
    private String tags;
    
    @NotBlank(message = "Question text không được để trống")
    private String questionText;
    
    // Multiple Choice / Multiple Select / True-False Fields
    private String options;
    private String correctAnswer;
    
    // Essay Fields
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    
    // Coding Fields
    private String programmingLanguage;
    private String starterCode;
    private String testCases;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // Fill in Blank Fields
    private String blankPositions;
    
    // Matching Fields
    private String leftItems;
    private String rightItems;
    private String correctMatches;
    
    // Attachments
    private String attachments;
}
