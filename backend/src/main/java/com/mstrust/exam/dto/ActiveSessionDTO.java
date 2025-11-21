package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho danh sách active exam sessions
 * Teacher xem các sessions đang diễn ra
 * @author: K24DTCN210-NVMANH (21/11/2025 02:01)
 * EditBy: K24DTCN210-NVMANH (21/11/2025 02:08) - Fixed structure to match service
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveSessionDTO {
    
    /* ---------------------------------------------------
     * Submission Information
     * --------------------------------------------------- */
    private Long submissionId;
    private SubmissionStatus status;
    
    /* ---------------------------------------------------
     * Student Information
     * --------------------------------------------------- */
    private Long studentId;
    private String studentName;
    private String studentEmail;
    
    /* ---------------------------------------------------
     * Exam Information
     * --------------------------------------------------- */
    private Long examId;
    private String examTitle;
    private Integer durationMinutes;
    
    /* ---------------------------------------------------
     * Time Information
     * --------------------------------------------------- */
    private LocalDateTime startedAt;
    private Integer remainingMinutes;
    private LocalDateTime lastActivity;
    private Integer autoSaveCount;
    
    /* ---------------------------------------------------
     * Progress Information
     * --------------------------------------------------- */
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private BigDecimal progressPercentage;
    
    /* ---------------------------------------------------
     * Status Flags
     * --------------------------------------------------- */
    private Boolean isInactive; // No activity for 10+ minutes
}
