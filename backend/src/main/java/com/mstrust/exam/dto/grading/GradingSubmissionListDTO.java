package com.mstrust.exam.dto.grading;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO hiển thị danh sách bài nộp cần chấm cho giáo viên
 * Chứa thông tin tổng quan về bài thi, học sinh và trạng thái chấm điểm
 * @author: K24DTCN210-NVMANH (21/11/2025 13:50)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingSubmissionListDTO {
    /* ID của bài nộp */
    private Long id;
    
    /* ID của đề thi */
    private Long examId;
    
    /* Tiêu đề đề thi */
    private String examTitle;
    
    /* ID của học sinh */
    private Long studentId;
    
    /* Họ tên học sinh */
    private String studentName;
    
    /* Mã số học sinh */
    private String studentCode;
    
    /* Thời điểm nộp bài */
    private LocalDateTime submitTime;
    
    /* Trạng thái bài nộp (IN_PROGRESS, SUBMITTED, GRADED) */
    private SubmissionStatus status;
    
    /* Số lượng câu hỏi cần chấm thủ công */
    private Integer pendingManualQuestions;
    
    /* Điểm đã được chấm tự động */
    private Double autoGradedScore;
    
    /* Tổng điểm tối đa */
    private Double maxScore;
    
    /* Phần trăm hoàn thành chấm điểm */
    private Double gradingProgress;
}
