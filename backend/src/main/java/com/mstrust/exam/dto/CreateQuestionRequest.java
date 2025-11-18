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
 * Mục đích: DTO cho request tạo mới Question
 * 
 * Validation:
 * - questionType: required
 * - questionText: required
 * - Các fields khác tùy thuộc vào questionType
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {
    
    private Long subjectId; // Optional - có thể tạo question không thuộc subject nào
    
    @NotNull(message = "Question type is required")
    private QuestionType questionType;
    
    private Difficulty difficulty; // Default: MEDIUM
    
    private String tags; // JSON string: ["tag1", "tag2"]
    
    @NotBlank(message = "Question text is required")
    private String questionText;
    
    // MULTIPLE_CHOICE / MULTIPLE_SELECT fields
    private String options; // JSON string: ["Option A", "Option B", "Option C", "Option D"]
    private String correctAnswer; // "A" or "A,C" for multiple select
    
    // ESSAY / SHORT_ANSWER fields
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    
    // CODING fields
    private String programmingLanguage;
    private String starterCode;
    private String testCases; // JSON string
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // FILL_IN_BLANK fields
    private String blankPositions; // JSON string
    
    // MATCHING fields
    private String leftItems; // JSON string
    private String rightItems; // JSON string
    private String correctMatches; // JSON string
    
    // Common
    private String attachments; // JSON string: [{"name": "file1.pdf", "url": "..."}]
}
