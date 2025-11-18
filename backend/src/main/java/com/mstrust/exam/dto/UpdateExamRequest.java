package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import com.mstrust.exam.entity.MonitoringLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho request cập nhật Exam
 * 
 * Validation: Tương tự CreateExamRequest
 * 
 * Business Rules:
 * - Không thể update exam đã published (trừ extend time)
 * - Teacher chỉ có thể update exam của mình (trừ ADMIN)
 * - Nếu có students đã submit, một số fields không thể update
 * 
 * Note: 
 * - UpdateExamRequest có thể cho phép update một số fields
 *   mà CreateExamRequest không cho phép (ví dụ: extend endTime)
 * - Service layer sẽ validate business rules cụ thể
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Purpose is required")
    private ExamPurpose purpose;
    
    @NotNull(message = "Format is required")
    private ExamFormat format;
    
    // Time settings
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    private Integer durationMinutes;
    
    // Scoring settings
    @NotNull(message = "Total score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total score must be greater than 0")
    private Double totalScore;
    
    @NotNull(message = "Passing score is required")
    @DecimalMin(value = "0.0", message = "Passing score must be at least 0")
    private Double passingScore;
    
    private Boolean showResultsImmediately;
    private Boolean showCorrectAnswers;
    
    // Attempt settings
    @Min(value = 1, message = "Max attempts must be at least 1")
    @Max(value = 10, message = "Max attempts must not exceed 10")
    private Integer maxAttempts;
    
    @Min(value = 0, message = "Attempt delay must be at least 0")
    private Integer attemptDelay; // minutes
    
    // Question settings
    private Boolean shuffleQuestions;
    private Boolean shuffleAnswers;
    private Boolean allowReview;
    private Boolean allowSkip;
    
    // Monitoring settings
    @NotNull(message = "Monitoring level is required")
    private MonitoringLevel monitoringLevel;
    
    private Boolean requireWebcam;
    private Boolean requireScreenShare;
    private Boolean detectTabSwitch;
    private Boolean detectCopyPaste;
    
    @Min(value = 0, message = "Max tab switches must be at least 0")
    @Max(value = 50, message = "Max tab switches must not exceed 50")
    private Integer maxTabSwitches;
}
