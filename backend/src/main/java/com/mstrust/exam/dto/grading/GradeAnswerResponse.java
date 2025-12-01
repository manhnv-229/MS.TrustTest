package com.mstrust.exam.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Response DTO sau khi chấm điểm một câu trả lời thành công
 * Chứa thông tin về điểm số, feedback và thời điểm chấm
 * @author: K24DTCN210-NVMANH (01/12/2025 15:35)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAnswerResponse {
    /* ID của câu trả lời */
    private Long answerId;
    
    /* Điểm đã chấm */
    private Double currentScore;
    
    /* Điểm tối đa */
    private Double maxScore;
    
    /* Đúng hay sai */
    private Boolean isCorrect;
    
    /* Nhận xét của giáo viên */
    private String feedback;
    
    /* Tên giáo viên chấm bài */
    private String gradedByName;
    
    /* Thời điểm chấm */
    private LocalDateTime gradedAt;
    
    /* ID của submission để frontend có thể refresh */
    private Long submissionId;
}

