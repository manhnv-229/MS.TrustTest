package com.mstrust.exam.controller;

import com.mstrust.exam.dto.websocket.ConnectionStatusMessage;
import com.mstrust.exam.dto.websocket.StudentProgressMessage;
import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.ExamSubmissionRepository;
import com.mstrust.exam.repository.StudentAnswerRepository;
import com.mstrust.exam.repository.UserRepository;
import com.mstrust.exam.service.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Optional;

/* ---------------------------------------------------
 * WebSocket Controller xử lý exam session events
 * - Student join/leave exam
 * - Progress updates real-time
 * - Connection monitoring
 * @author: K24DTCN210-NVMANH (21/11/2025 01:49)
 * --------------------------------------------------- */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ExamSessionWebSocketController {
    
    private final WebSocketEventService webSocketEventService;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    
    /* ---------------------------------------------------
     * Xử lý khi student join exam session
     * Client gửi message tới: /app/exam/{examId}/join
     * Server broadcast tới: /topic/exam/{examId}/connections
     * @param examId ID của bài thi
     * @param submissionId ID của submission
     * @param headerAccessor SimpMessageHeaderAccessor để lấy session info
     * @param authentication Authentication để lấy user info
     * @author: K24DTCN210-NVMANH (21/11/2025 01:49)
     * --------------------------------------------------- */
    @MessageMapping("/exam/{examId}/join")
    public void handleStudentJoin(
            @DestinationVariable Long examId,
            @Payload Long submissionId,
            SimpMessageHeaderAccessor headerAccessor,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User student = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String sessionId = headerAccessor.getSessionId();
            
            log.info("Student {} joined exam {} with session {}", 
                student.getId(), examId, sessionId);
            
            // Gửi connection status message
            ConnectionStatusMessage message = ConnectionStatusMessage.builder()
                    .examId(examId)
                    .studentId(student.getId())
                    .studentName(student.getFullName())
                    .studentEmail(student.getEmail())
                    .status("CONNECTED")
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            webSocketEventService.sendConnectionStatus(message);
            
            // Gửi initial progress update
            sendProgressUpdate(examId, submissionId, student);
            
        } catch (Exception e) {
            log.error("Error handling student join for exam {}: {}", examId, e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Xử lý khi student update progress (answer question)
     * Client gửi message tới: /app/exam/{examId}/progress
     * Server broadcast tới: /topic/exam/{examId}/progress
     * @param examId ID của bài thi
     * @param submissionId ID của submission
     * @param authentication Authentication để lấy user info
     * @author: K24DTCN210-NVMANH (21/11/2025 01:49)
     * --------------------------------------------------- */
    @MessageMapping("/exam/{examId}/progress")
    public void handleProgressUpdate(
            @DestinationVariable Long examId,
            @Payload Long submissionId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User student = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            sendProgressUpdate(examId, submissionId, student);
            
        } catch (Exception e) {
            log.error("Error handling progress update for exam {}: {}", examId, e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Xử lý khi student leave exam session (disconnect)
     * Client gửi message tới: /app/exam/{examId}/leave
     * Server broadcast tới: /topic/exam/{examId}/connections
     * @param examId ID của bài thi
     * @param headerAccessor SimpMessageHeaderAccessor để lấy session info
     * @param authentication Authentication để lấy user info
     * @author: K24DTCN210-NVMANH (21/11/2025 01:49)
     * --------------------------------------------------- */
    @MessageMapping("/exam/{examId}/leave")
    public void handleStudentLeave(
            @DestinationVariable Long examId,
            SimpMessageHeaderAccessor headerAccessor,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User student = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String sessionId = headerAccessor.getSessionId();
            
            log.info("Student {} left exam {} with session {}", 
                student.getId(), examId, sessionId);
            
            ConnectionStatusMessage message = ConnectionStatusMessage.builder()
                    .examId(examId)
                    .studentId(student.getId())
                    .studentName(student.getFullName())
                    .studentEmail(student.getEmail())
                    .status("DISCONNECTED")
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            webSocketEventService.sendConnectionStatus(message);
            
        } catch (Exception e) {
            log.error("Error handling student leave for exam {}: {}", examId, e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Helper method để gửi progress update message
     * @param examId ID của bài thi
     * @param submissionId ID của submission
     * @param student User entity của student
     * @author: K24DTCN210-NVMANH (21/11/2025 01:49)
     * --------------------------------------------------- */
    private void sendProgressUpdate(Long examId, Long submissionId, User student) {
        Optional<ExamSubmission> submissionOpt = examSubmissionRepository.findById(submissionId);
        
        if (submissionOpt.isPresent()) {
            ExamSubmission submission = submissionOpt.get();
            
            // Đếm số câu đã trả lời
            int totalQuestions = submission.getExam().getExamQuestions().size();
            long answeredQuestions = studentAnswerRepository
                    .countBySubmissionIdAndAnswerTextIsNotNull(submissionId);
            
            double completionPercentage = totalQuestions > 0 
                    ? (answeredQuestions * 100.0 / totalQuestions) 
                    : 0.0;
            
            StudentProgressMessage message = StudentProgressMessage.builder()
                    .submissionId(submissionId)
                    .examId(examId)
                    .studentId(student.getId())
                    .studentName(student.getFullName())
                    .studentEmail(student.getEmail())
                    .totalQuestions(totalQuestions)
                    .answeredQuestions((int) answeredQuestions)
                    .completionPercentage(completionPercentage)
                    .status(submission.getStatus().name())
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            
            webSocketEventService.sendProgressUpdate(message);
        }
    }
}
