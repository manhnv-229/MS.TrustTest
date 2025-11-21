package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO chứa thông tin chi tiết đầy đủ của bài nộp để giáo viên chấm điểm
 * @author: K24DTCN210-NVMANH (20/11/2025 11:17)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionGradingDetailDTO {
    
    /* ---------------------------------------------------
     * ID của bài nộp
     * --------------------------------------------------- */
    private Long id;
    
    /* ---------------------------------------------------
     * Thông tin tóm tắt về đề thi
     * --------------------------------------------------- */
    private ExamSummaryDTO exam;
    
    /* ---------------------------------------------------
     * Thông tin sinh viên
     * --------------------------------------------------- */
    private StudentInfoDTO student;
    
    /* ---------------------------------------------------
     * Thời gian bắt đầu làm bài
     * --------------------------------------------------- */
    private LocalDateTime startedAt;
    
    /* ---------------------------------------------------
     * Thời gian nộp bài
     * --------------------------------------------------- */
    private LocalDateTime submittedAt;
    
    /* ---------------------------------------------------
     * Thời gian làm bài (giây)
     * --------------------------------------------------- */
    private Integer timeSpentSeconds;
    
    /* ---------------------------------------------------
     * Trạng thái bài nộp
     * --------------------------------------------------- */
    private SubmissionStatus status;
    
    /* ---------------------------------------------------
     * Danh sách các câu trả lời cần chấm điểm
     * --------------------------------------------------- */
    private List<AnswerForGradingDTO> answers;
    
    /* ---------------------------------------------------
     * Tổng điểm hiện tại
     * --------------------------------------------------- */
    private BigDecimal currentScore;
    
    /* ---------------------------------------------------
     * Điểm tối đa của đề thi
     * --------------------------------------------------- */
    private BigDecimal maxScore;
    
    /* ---------------------------------------------------
     * Đã đạt hay chưa
     * --------------------------------------------------- */
    private Boolean passed;
    
    /* ---------------------------------------------------
     * Số câu trả lời chưa được chấm
     * --------------------------------------------------- */
    private Integer ungradedAnswersCount;
}
