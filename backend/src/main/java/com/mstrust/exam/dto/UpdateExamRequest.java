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
 * DTO cho request cập nhật Exam
 * Similar to CreateExamRequest nhưng:
 * - Không yêu cầu @Future cho startTime (có thể update exam đã bắt đầu)
 * - Tất cả fields đều optional (partial update)
 * - Cần check version để tránh conflict
 * @author: K24DTCN210-NVMANH (19/11/2025 08:37)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {
    
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private Long subjectClassId;
    
    private ExamPurpose examPurpose;
    
    private ExamFormat examFormat;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Positive(message = "Duration must be positive")
    @Max(value = 480, message = "Duration must not exceed 480 minutes (8 hours)")
    private Integer durationMinutes;
    
    @DecimalMin(value = "0.00", message = "Passing score must be >= 0")
    @DecimalMax(value = "100.00", message = "Passing score must be <= 100")
    private BigDecimal passingScore;
    
    @DecimalMin(value = "0.00", message = "Total score must be >= 0")
    @DecimalMax(value = "100.00", message = "Total score must be <= 100")
    private BigDecimal totalScore;
    
    // Exam behavior settings
    private Boolean randomizeQuestions;
    private Boolean randomizeOptions;
    private Boolean allowReviewAfterSubmit;
    private Boolean showCorrectAnswers;
    
    // Coding exam specific
    private Boolean allowCodeExecution;
    
    @Size(max = 50, message = "Programming language name must not exceed 50 characters")
    private String programmingLanguage;
    
    // Version for optimistic locking
    @NotNull(message = "Version is required for update")
    private Integer version;
}
