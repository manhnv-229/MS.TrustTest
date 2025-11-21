package com.mstrust.exam.controller;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.service.ExamTakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * REST Controller cho exam session management (teacher control)
 * Base path: /exam-sessions (URL cuối: /api/exam-sessions)
 * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/exam-sessions")
@RequiredArgsConstructor
public class ExamSessionController {
    
    private final ExamTakingService examTakingService;
    
    /* ---------------------------------------------------
     * POST /exam-sessions/{id}/pause
     * Tạm dừng bài thi của học sinh (giáo viên thực hiện)
     * @param id ID của submission
     * @param request PauseExamRequest
     * @returns Map với success message
     * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
     * --------------------------------------------------- */
    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> pauseExam(
            @PathVariable Long id,
            @RequestBody PauseExamRequest request,
            Authentication auth) {
        Long teacherId = getCurrentUserId(auth);
        request.setSubmissionId(id);
        Map<String, Object> result = examTakingService.pauseExam(request, teacherId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * POST /exam-sessions/{id}/resume
     * Tiếp tục bài thi đã tạm dừng (giáo viên thực hiện)
     * @param id ID của submission
     * @param request ResumeExamRequest
     * @returns Map với success message
     * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
     * --------------------------------------------------- */
    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> resumeExam(
            @PathVariable Long id,
            @RequestBody ResumeExamRequest request,
            Authentication auth) {
        Long teacherId = getCurrentUserId(auth);
        request.setSubmissionId(id);
        Map<String, Object> result = examTakingService.resumeExam(request, teacherId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * GET /exam-sessions/active
     * Lấy danh sách các phiên thi đang active (giáo viên xem)
     * @returns List<ActiveSessionDTO>
     * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
     * --------------------------------------------------- */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ActiveSessionDTO>> getActiveSessions() {
        List<ActiveSessionDTO> sessions = examTakingService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }
    
    /* ---------------------------------------------------
     * GET /exam-sessions/live/{examId}
     * Lấy live view của một bài thi cho giáo viên
     * @param examId ID của exam
     * @returns TeacherLiveViewDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
     * --------------------------------------------------- */
    @GetMapping("/live/{examId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<TeacherLiveViewDTO> getLiveView(@PathVariable Long examId) {
        TeacherLiveViewDTO liveView = examTakingService.getTeacherLiveView(examId);
        return ResponseEntity.ok(liveView);
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy userId từ Authentication
     * @param auth Authentication object
     * @returns userId
     * @author: K24DTCN210-NVMANH (21/11/2025 02:23)
     * --------------------------------------------------- */
    private Long getCurrentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
