package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho Question response (trả về client)
 * 
 * Chứa thông tin câu hỏi để hiển thị (không bao gồm sensitive data)
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    
    private Long id;
    private Long subjectId;
    private String subjectName;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String tags; // JSON string
    
    // Question content
    private String questionText;
    private String options; // JSON string - for multiple choice
    private String correctAnswer; // Hidden in some contexts
    
    // Essay fields
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    
    // Coding fields
    private String programmingLanguage;
    private String starterCode;
    private String testCases; // JSON string
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // Fill in blank
    private String blankPositions; // JSON string
    
    // Matching
    private String leftItems; // JSON string
    private String rightItems; // JSON string
    private String correctMatches; // JSON string
    
    // Attachments
    private String attachments; // JSON string
    
    // Audit info
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // Usage statistics
    private Long usageCount; // Số lần được sử dụng trong exams
    
    // Version for optimistic locking
    private Integer version;
}
