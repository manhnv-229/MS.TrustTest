package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho request tạo Exam mới
 * Validation:
 * - title: required, min 3, max 200
 * - subjectClassId: required
 * - examPurpose, examFormat: required
 * - startTime < endTime
 * - durationMinutes > 0
 * - passingScore <= totalScore
 * @author: K24DTCN210-NVMANH (19/11/2025 08:36)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Subject class ID is required")
    private Long subjectClassId;
    
    @NotNull(message = "Exam purpose is required")
    private ExamPurpose examPurpose;
    
    @NotNull(message = "Exam format is required")
    private ExamFormat examFormat;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    @Max(value = 480, message = "Duration must not exceed 480 minutes (8 hours)")
    private Integer durationMinutes;
    
    @DecimalMin(value = "0.00", message = "Passing score must be >= 0")
    private BigDecimal passingScore = BigDecimal.valueOf(50.00);
    
    @DecimalMin(value = "0.00", message = "Total score must be >= 0")
    @DecimalMax(value = "100.00", message = "Total score must be <= 100")
    private BigDecimal totalScore = BigDecimal.valueOf(100.00);
    
    // Exam behavior settings (optional, có defaults)
    private Boolean randomizeQuestions = false;
    private Boolean randomizeOptions = false;
    private Boolean allowReviewAfterSubmit = true;
    private Boolean showCorrectAnswers = false;
    
    // Coding exam specific (optional)
    private Boolean allowCodeExecution = false;
    
    @Size(max = 50, message = "Programming language name must not exceed 50 characters")
    private String programmingLanguage;
}
