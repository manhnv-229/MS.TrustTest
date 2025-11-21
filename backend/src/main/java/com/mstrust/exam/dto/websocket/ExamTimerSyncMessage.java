package com.mstrust.exam.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho WebSocket message đồng bộ timer bài thi
 * - Gửi từ server tới clients qua topic /topic/exam/{examId}/timer
 * - Cập nhật thời gian còn lại của bài thi real-time
 * @author: K24DTCN210-NVMANH (21/11/2025 01:47)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTimerSyncMessage {
    
    /* ID của bài thi */
    private Long examId;
    
    /* Thời gian bắt đầu bài thi */
    private LocalDateTime startTime;
    
    /* Thời gian kết thúc bài thi */
    private LocalDateTime endTime;
    
    /* Thời gian còn lại (seconds) */
    private Long remainingSeconds;
    
    /* Trạng thái: ACTIVE, PAUSED, ENDED */
    private String status;
    
    /* Timestamp khi gửi message */
    private LocalDateTime timestamp;
}
