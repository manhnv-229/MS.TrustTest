package com.mstrust.exam.dto.grading;

import com.mstrust.exam.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO hiển thị kết quả bài thi cho học sinh
 * Chứa thông tin điểm số, đánh giá và chi tiết từng câu trả lời
 * @author: K24DTCN210-NVMANH (21/11/2025 14:00)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultDTO {
    /* ID của bài nộp */
    private Long submissionId;
    
    /* Tiêu đề đề thi */
    private String examTitle;
    
    /* Thời điểm bắt đầu làm bài */
    private LocalDateTime startTime;
    
    /* Thời điểm nộp bài */
    private LocalDateTime submitTime;
    
    /* Trạng thái bài nộp */
    private SubmissionStatus status;
    
    /* Tổng điểm đạt được */
    private Double totalScore;
    
    /* Tổng điểm tối đa */
    private Double maxScore;
    
    /* Phần trăm điểm (%) */
    private Double percentage;
    
    /* Đã đạt yêu cầu hay chưa */
    private Boolean passed;
    
    /* Điểm yêu cầu để đạt */
    private Double passingScore;
    
    /* Nhận xét chung của giáo viên */
    private String generalFeedback;
    
    /* Chi tiết từng câu trả lời (chỉ hiển thị nếu exam cho phép xem đáp án) */
    private List<AnswerResultDTO> answers;
    
    /* Có được phép xem đáp án hay không */
    private Boolean canViewAnswers;
}
