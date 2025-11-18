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
import java.util.List;

/** ------------------------------------------
 * Mục đích: DTO cho response Exam với full danh sách Questions
 * 
 * Bao gồm:
 * - Tất cả thông tin từ ExamDTO
 * - Danh sách đầy đủ questions (List<ExamQuestionDTO>)
 * - Sử dụng khi cần hiển thị exam details với questions
 * 
 * Use cases:
 * - Teacher xem exam để edit
 * - Teacher preview exam trước khi publish
 * - Admin/Manager review exam
 * - Student xem exam để làm bài (trong Phase 5)
 * 
 * Note:
 * - DTO này có thể nặng nếu exam có nhiều questions
 * - Cân nhắc pagination cho list questions nếu cần
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamWithQuestionsDTO {
    
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
    
    // Questions list
    private List<ExamQuestionDTO> questions;
    
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
