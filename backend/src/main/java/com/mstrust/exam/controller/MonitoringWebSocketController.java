package com.mstrust.exam.controller;

import com.mstrust.exam.dto.websocket.ExamTimerSyncMessage;
import com.mstrust.exam.entity.Exam;
import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.repository.ExamRepository;
import com.mstrust.exam.repository.ExamSubmissionRepository;
import com.mstrust.exam.service.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * WebSocket Controller cho monitoring và real-time updates
 * - Timer sync cho exams
 * - Teacher monitoring dashboard
 * - System alerts
 * @author: K24DTCN210-NVMANH (21/11/2025 01:51)
 * --------------------------------------------------- */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MonitoringWebSocketController {
    
    private final WebSocketEventService webSocketEventService;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    
    /* ---------------------------------------------------
     * Scheduled task gửi timer sync mỗi 5 giây cho active exams
     * Tự động chạy background để đồng bộ timer
     * @author: K24DTCN210-NVMANH (21/11/2025 01:51)
     * --------------------------------------------------- */
    @Scheduled(fixedRate = 5000) // Chạy mỗi 5 giây
    public void syncExamTimers() {
        try {
            // Lấy tất cả active submissions với eager fetch exam
            List<ExamSubmission> activeSubmissions = examSubmissionRepository
                    .findByStatus(com.mstrust.exam.entity.SubmissionStatus.IN_PROGRESS);
            
            for (ExamSubmission submission : activeSubmissions) {
                // Safely get exam - skip if exam not loaded
                if (submission.getExam() == null) {
                    continue;
                }
                Exam exam = examRepository.findById(submission.getExam().getId())
                        .orElse(null);
                if (exam == null) {
                    continue;
                }
                LocalDateTime now = LocalDateTime.now();
                
                // Convert Timestamp to LocalDateTime
                LocalDateTime startTime = submission.getStartedAt() != null 
                    ? submission.getStartedAt().toLocalDateTime() 
                    : now;
                
                // Tính endTime dựa trên startTime + duration
                LocalDateTime endTime = null;
                long remainingSeconds = 0;
                String status = "ACTIVE";
                
                if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
                    endTime = startTime.plusMinutes(exam.getDurationMinutes());
                    
                    if (now.isAfter(endTime)) {
                        status = "ENDED";
                        remainingSeconds = 0;
                    } else {
                        Duration duration = Duration.between(now, endTime);
                        remainingSeconds = duration.getSeconds();
                    }
                }
                
                // Gửi timer sync message
                ExamTimerSyncMessage message = ExamTimerSyncMessage.builder()
                        .examId(exam.getId())
                        .startTime(startTime)
                        .endTime(endTime)
                        .remainingSeconds(remainingSeconds)
                        .status(status)
                        .timestamp(now)
                        .build();
                
                webSocketEventService.sendTimerSync(message);
            }
            
        } catch (Exception e) {
            log.error("Error syncing exam timers: {}", e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Teacher request để force sync timer của một exam
     * Client gửi message tới: /app/monitoring/exam/{examId}/sync-timer
     * @param examId ID của bài thi cần sync
     * @author: K24DTCN210-NVMANH (21/11/2025 01:51)
     * --------------------------------------------------- */
    @MessageMapping("/monitoring/exam/{examId}/sync-timer")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public void forceSyncTimer(@DestinationVariable Long examId) {
        try {
            // Lấy tất cả submissions của exam này
            List<ExamSubmission> submissions = examSubmissionRepository
                    .findByExamIdAndStatus(examId, 
                        com.mstrust.exam.entity.SubmissionStatus.IN_PROGRESS);
            
            if (!submissions.isEmpty()) {
                ExamSubmission submission = submissions.get(0);
                // Fetch exam explicitly to avoid lazy loading
                Exam exam = examRepository.findById(examId)
                        .orElseThrow(() -> new RuntimeException("Exam not found"));
                LocalDateTime now = LocalDateTime.now();
                
                // Convert Timestamp to LocalDateTime
                LocalDateTime startTime = submission.getStartedAt() != null 
                    ? submission.getStartedAt().toLocalDateTime() 
                    : now;
                
                // Tính endTime dựa trên startTime + duration
                LocalDateTime endTime = null;
                long remainingSeconds = 0;
                String status = "ACTIVE";
                
                if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
                    endTime = startTime.plusMinutes(exam.getDurationMinutes());
                    
                    if (now.isAfter(endTime)) {
                        status = "ENDED";
                        remainingSeconds = 0;
                    } else {
                        Duration duration = Duration.between(now, endTime);
                        remainingSeconds = duration.getSeconds();
                    }
                }
                
                ExamTimerSyncMessage message = ExamTimerSyncMessage.builder()
                        .examId(exam.getId())
                        .startTime(startTime)
                        .endTime(endTime)
                        .remainingSeconds(remainingSeconds)
                        .status(status)
                        .timestamp(now)
                        .build();
                
                webSocketEventService.sendTimerSync(message);
                log.info("Force synced timer for exam {}", examId);
            }
            
        } catch (Exception e) {
            log.error("Error force syncing timer for exam {}: {}", examId, e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Broadcast alert message tới tất cả teachers monitoring exam
     * Client gửi message tới: /app/monitoring/alert
     * @param alertMessage Nội dung alert
     * @author: K24DTCN210-NVMANH (21/11/2025 01:51)
     * --------------------------------------------------- */
    @MessageMapping("/monitoring/alert")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    public void broadcastAlert(String alertMessage) {
        try {
            webSocketEventService.broadcastSystemMessage(alertMessage);
            log.info("Broadcasted alert: {}", alertMessage);
        } catch (Exception e) {
            log.error("Error broadcasting alert: {}", e.getMessage());
        }
    }
}
