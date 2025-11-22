package com.mstrust.exam.dto.grading;

import com.mstrust.exam.dto.exam.ExamDTO;
import com.mstrust.exam.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO chi tiết bài nộp để giáo viên chấm điểm
 * Chứa thông tin đầy đủ về bài thi, học sinh và tất cả các câu trả lời
 * @author: K24DTCN210-NVMANH (21/11/2025 13:54)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingDetailDTO {
    /* ID của bài nộp */
    private Long submissionId;
    
    /* Thông tin đề thi */
    private ExamDTO exam;
    
    /* Thông tin học sinh */
    private UserDTO student;
    
    /* Thời điểm bắt đầu làm bài */
    private LocalDateTime startTime;
    
    /* Thời điểm nộp bài */
    private LocalDateTime submitTime;
    
    /* Danh sách các câu trả lời cần chấm */
    private List<AnswerForGradingDTO> answers;
    
    /* Tổng điểm hiện tại */
    private Double currentScore;
    
    /* Tổng điểm tối đa */
    private Double maxScore;
    
    /* Số câu đã chấm */
    private Integer gradedQuestions;
    
    /* Tổng số câu */
    private Integer totalQuestions;
    
    /* Nhận xét chung (nếu có) */
    private String generalFeedback;
}
