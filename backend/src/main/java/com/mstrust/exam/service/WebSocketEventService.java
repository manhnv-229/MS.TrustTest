package com.mstrust.exam.service;

import com.mstrust.exam.dto.websocket.ConnectionStatusMessage;
import com.mstrust.exam.dto.websocket.ExamTimerSyncMessage;
import com.mstrust.exam.dto.websocket.StudentProgressMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Service xử lý gửi WebSocket messages tới clients
 * - Gửi timer sync, progress updates, connection status
 * - Sử dụng SimpMessagingTemplate để broadcast messages
 * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /* ---------------------------------------------------
     * Gửi exam timer sync message tới tất cả clients đang làm bài thi
     * @param message ExamTimerSyncMessage chứa thông tin timer
     * Topic: /topic/exam/{examId}/timer
     * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
     * --------------------------------------------------- */
    public void sendTimerSync(ExamTimerSyncMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            String destination = String.format("/topic/exam/%d/timer", message.getExamId());
            messagingTemplate.convertAndSend(destination, message);
            // log.debug("Sent timer sync for exam {} - remaining: {}s", 
            //     message.getExamId(), message.getRemainingSeconds());
        } catch (Exception e) {
            log.error("Error sending timer sync for exam {}: {}", 
                message.getExamId(), e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Gửi student progress update tới teachers
     * @param message StudentProgressMessage chứa tiến độ sinh viên
     * Topic: /topic/exam/{examId}/progress
     * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
     * --------------------------------------------------- */
    public void sendProgressUpdate(StudentProgressMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            String destination = String.format("/topic/exam/%d/progress", message.getExamId());
            messagingTemplate.convertAndSend(destination, message);
            // log.debug("Sent progress update for student {} in exam {} - {}%", 
            //     message.getStudentId(), message.getExamId(), 
            //     message.getCompletionPercentage());
        } catch (Exception e) {
            log.error("Error sending progress update for exam {}: {}", 
                message.getExamId(), e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Gửi connection status update khi student connect/disconnect
     * @param message ConnectionStatusMessage chứa trạng thái kết nối
     * Topic: /topic/exam/{examId}/connections
     * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
     * --------------------------------------------------- */
    public void sendConnectionStatus(ConnectionStatusMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            String destination = String.format("/topic/exam/%d/connections", 
                message.getExamId());
            messagingTemplate.convertAndSend(destination, message);
            // log.debug("Sent connection status for student {} in exam {} - {}", 
            //     message.getStudentId(), message.getExamId(), message.getStatus());
        } catch (Exception e) {
            log.error("Error sending connection status for exam {}: {}", 
                message.getExamId(), e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Gửi alert message tới specific user
     * @param userId ID của user cần nhận alert
     * @param message Nội dung alert message
     * Topic: /user/{userId}/queue/alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
     * --------------------------------------------------- */
    public void sendUserAlert(Long userId, String message) {
        try {
            String destination = String.format("/user/%d/queue/alerts", userId);
            messagingTemplate.convertAndSend(destination, message);
            // log.debug("Sent alert to user {}: {}", userId, message);
        } catch (Exception e) {
            log.error("Error sending alert to user {}: {}", userId, e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Broadcast system message tới tất cả connected clients
     * @param message System message content
     * Topic: /topic/system
     * @author: K24DTCN210-NVMANH (21/11/2025 01:48)
     * --------------------------------------------------- */
    public void broadcastSystemMessage(String message) {
        try {
            messagingTemplate.convertAndSend("/topic/system", message);
            // log.info("Broadcasted system message: {}", message);
        } catch (Exception e) {
            log.error("Error broadcasting system message: {}", e.getMessage());
        }
    }
}
