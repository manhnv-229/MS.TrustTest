package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO cho kết quả bài thi sau khi submit
 * Tuân theo exam settings (showResultsAfterSubmit, showScoreOnly)
 * @author: K24DTCN210-NVMANH (19/11/2025 15:19)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultDTO {
    
    private Long submissionId;
    private Long examId;
    private String examTitle;
    
    // Attempt info
    private Integer attemptNumber;
    private SubmissionStatus status;
    
    // Time info
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer timeSpentSeconds;
    
    // Scoring
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private BigDecimal passingScore;
    private Boolean passed;
    private BigDecimal percentage;  // (totalScore / maxScore) * 100
    
    // Statistics
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;  // Chỉ hiển thị nếu showCorrectAnswers = true
    private Integer incorrectAnswers;
    private Integer ungradedQuestions;  // Essay/Coding chưa chấm
    
    // Detailed answers (chỉ có nếu teacher cho phép)
    private List<AnswerReviewDTO> answers;  // null nếu showScoreOnly = true
    
    // Settings control
    private Boolean canViewDetailedAnswers;  // Có thể xem chi tiết không
    private Boolean showCorrectAnswers;  // Có hiển thị đáp án đúng không
    
    // Message
    private String message;  // "Congratulations! You passed." / "You did not pass."
    private String feedback;  // Optional teacher feedback
}
