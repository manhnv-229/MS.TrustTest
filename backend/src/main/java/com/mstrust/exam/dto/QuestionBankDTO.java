package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * DTO cho QuestionBank - Trả về client
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 01:10)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankDTO {
    
    private Long id;
    private Long subjectId;
    private String subjectName;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String tags;
    private Integer version;
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
    
    // Audit Fields
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
    
    // Statistics
    private Long usageCount; // Số lần câu hỏi được sử dụng trong bài thi
}
