package com.mstrust.exam.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho WebSocket message cập nhật tiến độ sinh viên
 * - Gửi từ server tới teacher qua topic /topic/exam/{examId}/progress
 * - Theo dõi real-time số câu đã trả lời, trạng thái làm bài
 * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressMessage {
    
    /* ID của submission */
    private Long submissionId;
    
    /* ID của bài thi */
    private Long examId;
    
    /* ID của sinh viên */
    private Long studentId;
    
    /* Tên sinh viên */
    private String studentName;
    
    /* Email sinh viên */
    private String studentEmail;
    
    /* Tổng số câu hỏi */
    private Integer totalQuestions;
    
    /* Số câu đã trả lời */
    private Integer answeredQuestions;
    
    /* Phần trăm hoàn thành (0-100) */
    private Double completionPercentage;
    
    /* Trạng thái: IN_PROGRESS, PAUSED, SUBMITTED */
    private String status;
    
    /* Thời gian cập nhật cuối */
    private LocalDateTime lastUpdateTime;
    
    /* Timestamp khi gửi message */
    private LocalDateTime timestamp;
}
