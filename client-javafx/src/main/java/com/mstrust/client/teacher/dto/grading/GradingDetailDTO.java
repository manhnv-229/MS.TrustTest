package com.mstrust.client.teacher.dto.grading;

import com.mstrust.client.teacher.dto.ExamDTO;
import com.mstrust.client.teacher.dto.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * DTO chi tiết bài nộp để giáo viên chấm điểm
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * EditBy: K24DTCN210-NVMANH (01/12/2025 15:42) - Thêm field status
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingDetailDTO {
    private Long submissionId;
    private SubmissionStatus status;
    private ExamDTO exam;
    private Map<String, Object> student; // UserDTO as Map
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private List<AnswerForGradingDTO> answers;
    private Double currentScore;
    private Double maxScore;
    private Integer gradedQuestions;
    private Integer totalQuestions;
    private String generalFeedback;
}

