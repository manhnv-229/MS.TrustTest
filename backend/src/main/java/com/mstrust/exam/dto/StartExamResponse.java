package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Response DTO khi student start exam
 * Chứa submission ID và timer info
 * @author: K24DTCN210-NVMANH (19/11/2025 15:18)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartExamResponse {
    
    private Long submissionId;
    private Long examId;
    private String examTitle;
    
    // Attempt info
    private Integer attemptNumber;
    private Integer maxAttempts;
    
    // Timer info
    private LocalDateTime startedAt;
    private Integer durationMinutes;
    private LocalDateTime mustSubmitBefore;  // startedAt + duration
    private Integer remainingSeconds;  // Thời gian còn lại (tính từ bây giờ)
    
    // Question info
    private Integer totalQuestions;
    private Boolean randomizeQuestions;
    private Boolean randomizeOptions;
    
    // Auto-save info
    private Integer autoSaveIntervalSeconds;  // Suggest auto-save every X seconds
    
    // Message
    private String message;  // "Exam started successfully. Good luck!"
}
