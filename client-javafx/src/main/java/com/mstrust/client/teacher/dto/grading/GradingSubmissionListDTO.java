package com.mstrust.client.teacher.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO hiển thị danh sách bài nộp cần chấm cho giáo viên
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingSubmissionListDTO {
    private Long id;
    private Long examId;
    private String examTitle;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private LocalDateTime submitTime;
    private String status; // SUBMITTED, GRADED, etc.
    private Integer pendingManualQuestions;
    private Double autoGradedScore;
    private Double maxScore;
    private Double gradingProgress;
}

