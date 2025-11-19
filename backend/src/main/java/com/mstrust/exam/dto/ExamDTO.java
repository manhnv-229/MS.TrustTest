package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import com.mstrust.exam.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho response của Exam entity (full details)
 * Sử dụng khi: Get detail, Create/Update response
 * @author: K24DTCN210-NVMANH (19/11/2025 08:35)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    
    private Long id;
    
    // Basic information
    private String title;
    private String description;
    
    // Subject class info
    private Long subjectClassId;
    private String subjectClassName;
    private Long subjectId;
    private String subjectName;
    private Long classId;
    private String className;
    
    // Exam classification
    private ExamPurpose examPurpose;
    private ExamFormat examFormat;
    
    // Time configuration
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    
    // Scoring configuration
    private BigDecimal passingScore;
    private BigDecimal totalScore;
    
    // Exam behavior settings
    private Boolean randomizeQuestions;
    private Boolean randomizeOptions;
    private Boolean allowReviewAfterSubmit;
    private Boolean showCorrectAnswers;
    
    // Coding exam specific
    private Boolean allowCodeExecution;
    private String programmingLanguage;
    
    // Publication status
    private Boolean isPublished;
    private ExamStatus currentStatus;
    
    // Statistics
    private Integer questionCount;
    private Integer submissionCount;
    
    // Metadata
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String updatedByName;
}
