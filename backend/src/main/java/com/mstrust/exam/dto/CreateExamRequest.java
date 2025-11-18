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
 * Mục đích: DTO cho request tạo mới Exam
 * 
 * Validation:
 * - subjectClassId: required
 * - title: required, 5-200 chars
 * - startTime: required, must be future
 * - endTime: required, must be after startTime
 * - durationMinutes: required, 15-240 minutes
 * - totalScore: required, > 0
 * - passingScore: required, >= 0, <= totalScore
 * 
 * Business Rules:
 * - start_time phải là thời điểm tương lai
 * - end_time > start_time
 * - duration_minutes: min 15, max 240
 * - passing_score không được lớn hơn total_score
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {
    
    @NotNull(message = "Subject class ID is required")
    private Long subjectClassId;
    
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
    @Future(message = "Start time must be in the future")
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
    
    private Boolean showResultsImmediately = false;
    private Boolean showCorrectAnswers = false;
    
    // Attempt settings
    @Min(value = 1, message = "Max attempts must be at least 1")
    @Max(value = 10, message = "Max attempts must not exceed 10")
    private Integer maxAttempts = 1;
    
    @Min(value = 0, message = "Attempt delay must be at least 0")
    private Integer attemptDelay = 0; // minutes
    
    // Question settings
    private Boolean shuffleQuestions = false;
    private Boolean shuffleAnswers = false;
    private Boolean allowReview = true;
    private Boolean allowSkip = true;
    
    // Monitoring settings
    @NotNull(message = "Monitoring level is required")
    private MonitoringLevel monitoringLevel = MonitoringLevel.MEDIUM;
    
    private Boolean requireWebcam = true;
    private Boolean requireScreenShare = false;
    private Boolean detectTabSwitch = true;
    private Boolean detectCopyPaste = true;
    
    @Min(value = 0, message = "Max tab switches must be at least 0")
    @Max(value = 50, message = "Max tab switches must not exceed 50")
    private Integer maxTabSwitches = 3;
}
