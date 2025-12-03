package com.mstrust.client.exam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho available exams từ backend
 * Map từ backend AvailableExamDTO
 * @author: K24DTCN210-NVMANH (02/12/2025 18:15)
 * --------------------------------------------------- */
@Data
public class AvailableExamDTO {
    private Long id;
    private String title;
    private String description;
    
    // Subject class info
    private Long subjectClassId;
    private String subjectClassName;
    private String subjectCode;
    private String subjectName;  // Tên môn học
    
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
    private String status;  // DRAFT, PUBLISHED, ONGOING, COMPLETED
    private Boolean isEligible;  // Student có thể làm không
    private String ineligibleReason;  // Lý do không thể làm (nếu có)
    
    // Question count
    private Integer totalQuestions;
}
