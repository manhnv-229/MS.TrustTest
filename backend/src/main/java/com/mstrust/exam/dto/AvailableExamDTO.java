package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho danh sách bài thi có thể làm
 * Lightweight version, không chứa questions
 * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableExamDTO {
    
    private Long id;
    private String title;
    private String description;
    
    // Subject class info
    private Long subjectClassId;
    private String subjectClassName;
    private String subjectCode;
    
    // Time info
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    
    // Scoring info
    private BigDecimal totalScore;
    private BigDecimal passingScore;
    
    // Exam settings
    private String examPurpose;  // MIDTERM, FINAL, QUIZ, etc.
    private String examFormat;   // ONLINE, PAPER, etc.
    private Boolean randomizeQuestions;
    private Boolean randomizeOptions;
    
    // Retake policy
    private Integer maxAttempts;  // 0 = unlimited
    
    // Student's attempt info
    private Integer attemptsMade;  // Số lần đã làm
    private Integer remainingAttempts;  // Số lần còn lại (null nếu unlimited)
    private Boolean hasActiveSubmission;  // Đang có bài làm dở
    private Boolean hasPassed;  // Đã pass chưa
    private BigDecimal highestScore;  // Điểm cao nhất (null nếu chưa submit lần nào)
    
    // Status
    private ExamStatus status;  // DRAFT, PUBLISHED, ONGOING, COMPLETED
    private Boolean isEligible;  // Student có thể làm không
    private String ineligibleReason;  // Lý do không thể làm (nếu có)
    
    // Question count
    private Integer totalQuestions;
}
