package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho việc cập nhật câu hỏi trong Question Bank (Phase 4)
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 01:12)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionBankRequest {
    
    private Difficulty difficulty;
    private String tags;
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
