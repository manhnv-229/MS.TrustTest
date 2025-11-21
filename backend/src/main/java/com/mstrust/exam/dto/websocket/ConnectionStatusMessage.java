package com.mstrust.exam.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho WebSocket message theo dõi trạng thái kết nối
 * - Gửi khi có student connect/disconnect
 * - Giúp teacher theo dõi ai đang online/offline
 * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatusMessage {
    
    /* ID của bài thi */
    private Long examId;
    
    /* ID của sinh viên */
    private Long studentId;
    
    /* Tên sinh viên */
    private String studentName;
    
    /* Email sinh viên */
    private String studentEmail;
    
    /* Trạng thái: CONNECTED, DISCONNECTED, RECONNECTED */
    private String status;
    
    /* Session ID của WebSocket */
    private String sessionId;
    
    /* IP address của client (optional) */
    private String ipAddress;
    
    /* Timestamp khi thay đổi trạng thái */
    private LocalDateTime timestamp;
}
