package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import com.mstrust.exam.entity.ExamStatus;
import com.mstrust.exam.entity.MonitoringLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho response Exam với đầy đủ thông tin
 * 
 * Bao gồm:
 * - Thông tin cơ bản của exam
 * - Thông tin subject class
 * - Thông tin người tạo
 * - Tính toán current status động
 * - Statistics (số lượng câu hỏi, tổng điểm)
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    
    private Long id;
    
    // Subject Class info
    private Long subjectClassId;
    private String subjectClassName;
    private String subjectName;
    
    // Basic info
    private String title;
    private String description;
    private ExamPurpose purpose;
    private ExamFormat format;
    
    // Time settings
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    
    // Scoring settings
    private Double totalScore;
    private Double passingScore;
    private Boolean showResultsImmediately;
    private Boolean showCorrectAnswers;
    
    // Attempt settings
    private Integer maxAttempts;
    private Integer attemptDelay; // minutes
    
    // Question settings
    private Boolean shuffleQuestions;
    private Boolean shuffleAnswers;
    private Boolean allowReview;
    private Boolean allowSkip;
    
    // Monitoring settings
    private MonitoringLevel monitoringLevel;
    private Boolean requireWebcam;
    private Boolean requireScreenShare;
    private Boolean detectTabSwitch;
    private Boolean detectCopyPaste;
    private Integer maxTabSwitches;
    
    // Status info
    private ExamStatus publicationStatus;
    private String currentStatus; // DRAFT, UPCOMING, ONGOING, COMPLETED, CANCELLED
    private LocalDateTime publishedAt;
    
    // Statistics
    private Integer totalQuestions;
    private Integer totalStudents;
    private Integer completedSubmissions;
    
    // Audit info
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    private Integer version;
}
