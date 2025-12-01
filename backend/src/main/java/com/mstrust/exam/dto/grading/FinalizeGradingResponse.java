package com.mstrust.exam.dto.grading;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Response DTO sau khi hoàn tất chấm điểm
 * Chứa thông tin tổng điểm và trạng thái cuối cùng
 * @author: K24DTCN210-NVMANH (01/12/2025 16:20)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeGradingResponse {
    /* ID của bài nộp */
    private Long submissionId;
    
    /* Trạng thái mới (GRADED) */
    private SubmissionStatus status;
    
    /* Tổng điểm đạt được */
    private Double totalScore;
    
    /* Tổng điểm tối đa */
    private Double maxScore;
    
    /* Phần trăm điểm */
    private Double percentage;
    
    /* Đã đạt hay chưa */
    private Boolean passed;
    
    /* Điểm yêu cầu để đạt */
    private Double passingScore;
    
    /* Thời điểm hoàn tất */
    private LocalDateTime finalizedAt;
    
    /* Thông báo */
    private String message;
}

