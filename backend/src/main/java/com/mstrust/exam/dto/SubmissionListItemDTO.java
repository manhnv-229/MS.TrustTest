package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO hiển thị thông tin tóm tắt của một bài nộp trong danh sách
 * @author: K24DTCN210-NVMANH (20/11/2025 11:17)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionListItemDTO {
    
    /* ---------------------------------------------------
     * ID của bài nộp
     * --------------------------------------------------- */
    private Long id;
    
    /* ---------------------------------------------------
     * ID của đề thi
     * --------------------------------------------------- */
    private Long examId;
    
    /* ---------------------------------------------------
     * Tiêu đề đề thi
     * --------------------------------------------------- */
    private String examTitle;
    
    /* ---------------------------------------------------
     * ID của sinh viên
     * --------------------------------------------------- */
    private Long studentId;
    
    /* ---------------------------------------------------
     * Tên sinh viên
     * --------------------------------------------------- */
    private String studentName;
    
    /* ---------------------------------------------------
     * Mã sinh viên
     * --------------------------------------------------- */
    private String studentCode;
    
    /* ---------------------------------------------------
     * Thời gian nộp bài
     * --------------------------------------------------- */
    private LocalDateTime submittedAt;
    
    /* ---------------------------------------------------
     * Tổng điểm đạt được
     * --------------------------------------------------- */
    private BigDecimal totalScore;
    
    /* ---------------------------------------------------
     * Điểm tối đa của đề thi
     * --------------------------------------------------- */
    private BigDecimal maxScore;
    
    /* ---------------------------------------------------
     * Phần trăm điểm đạt được (%)
     * --------------------------------------------------- */
    private BigDecimal percentage;
    
    /* ---------------------------------------------------
     * Đạt hay không đạt
     * --------------------------------------------------- */
    private Boolean passed;
    
    /* ---------------------------------------------------
     * Trạng thái bài nộp (SUBMITTED, GRADED, etc.)
     * --------------------------------------------------- */
    private SubmissionStatus status;
    
    /* ---------------------------------------------------
     * Số câu trả lời chưa được chấm điểm
     * --------------------------------------------------- */
    private Integer ungradedAnswers;
    
    /* ---------------------------------------------------
     * Thời gian làm bài (giây)
     * --------------------------------------------------- */
    private Integer timeSpentSeconds;
}
