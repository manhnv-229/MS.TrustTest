package com.mstrust.exam.controller;

import com.mstrust.exam.dto.grading.*;
import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.entity.StudentAnswer;
import com.mstrust.exam.entity.SubmissionStatus;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.UserRepository;
import com.mstrust.exam.service.GradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * Controller xử lý các API chấm điểm bài thi
 * Endpoints: /api/grading/**
 * @author: K24DTCN210-NVMANH (21/11/2025 14:28)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/grading")
@RequiredArgsConstructor
@Slf4j
public class GradingController {
    
    private final GradingService gradingService;
    private final UserRepository userRepository;
    
    /* ---------------------------------------------------
     * Lấy danh sách bài nộp cần chấm cho giáo viên
     * GET /api/grading/submissions?status=SUBMITTED&examId=1
     * @param status Trạng thái bài nộp (tùy chọn)
     * @param examId ID đề thi (tùy chọn)
     * @returns Danh sách bài nộp cần chấm
     * @author: K24DTCN210-NVMANH (21/11/2025 14:30)
     * --------------------------------------------------- */
    @GetMapping("/submissions")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<GradingSubmissionListDTO>> getSubmissionsForGrading(
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) Long examId,
            Authentication auth) {
        
        Long teacherId = getCurrentUserId(auth);
        log.info("GET /api/grading/submissions - teacherId: {}, status: {}, examId: {}", 
                teacherId, status, examId);
        
        List<GradingSubmissionListDTO> submissions = gradingService.getSubmissionsForGrading(
                teacherId, status, examId);
        
        log.info("Found {} submissions for grading", submissions.size());
        return ResponseEntity.ok(submissions);
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết bài nộp để chấm điểm
     * GET /api/grading/submissions/{id}
     * @param id ID của bài nộp
     * @returns Chi tiết bài nộp với tất cả câu trả lời
     * @author: K24DTCN210-NVMANH (21/11/2025 14:32)
     * --------------------------------------------------- */
    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<GradingDetailDTO> getSubmissionDetail(
            @PathVariable Long id,
            Authentication auth) {
        Long teacherId = getCurrentUserId(auth);
        log.info("GET /api/grading/submissions/{} - teacherId: {}", id, teacherId);
        
        GradingDetailDTO detail = gradingService.getSubmissionDetail(id, teacherId);
        
        log.info("Retrieved submission detail - submissionId: {}, totalQuestions: {}", 
                id, detail.getTotalQuestions());
        return ResponseEntity.ok(detail);
    }
    
    /* ---------------------------------------------------
     * Chấm điểm một câu trả lời
     * POST /api/grading/answers/{answerId}/grade
     * @param answerId ID của câu trả lời
     * @param request Request chứa điểm và feedback
     * @returns GradeAnswerResponse chứa thông tin đã chấm
     * @author: K24DTCN210-NVMANH (21/11/2025 14:34)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 15:37) - Return DTO thay vì entity
     * --------------------------------------------------- */
    @PostMapping("/answers/{answerId}/grade")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<GradeAnswerResponse> gradeAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody GradeAnswerRequest request,
            Authentication auth) {
        
        Long teacherId = getCurrentUserId(auth);
        log.info("POST /api/grading/answers/{}/grade - teacherId: {}, score: {}", 
                answerId, teacherId, request.getScore());
        
        GradeAnswerResponse response = gradingService.gradeAnswer(answerId, request, teacherId);
        
        log.info("Answer graded successfully - answerId: {}, score: {}/{}", 
                answerId, response.getCurrentScore(), response.getMaxScore());
        return ResponseEntity.ok(response);
    }
    
    /* ---------------------------------------------------
     * Hoàn tất việc chấm điểm - tính tổng điểm và chuyển status sang GRADED
     * POST /api/grading/submissions/{id}/finalize
     * @param id ID của bài nộp
     * @param request Request chứa nhận xét chung
     * @returns FinalizeGradingResponse chứa thông tin hoàn tất
     * @author: K24DTCN210-NVMANH (21/11/2025 14:36)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 16:22) - Return DTO thay vì entity
     * --------------------------------------------------- */
    @PostMapping("/submissions/{id}/finalize")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<FinalizeGradingResponse> finalizeGrading(
            @PathVariable Long id,
            @Valid @RequestBody FinalizeGradingRequest request,
            Authentication auth) {
        
        Long teacherId = getCurrentUserId(auth);
        log.info("POST /api/grading/submissions/{}/finalize - teacherId: {}", id, teacherId);
        
        FinalizeGradingResponse response = gradingService.finalizeGrading(id, request, teacherId);
        
        log.info("Grading finalized successfully - submissionId: {}, totalScore: {}/{}, status: {}", 
                id, response.getTotalScore(), response.getMaxScore(), response.getStatus());
        return ResponseEntity.ok(response);
    }
    
    /* ---------------------------------------------------
     * Lấy thống kê chấm điểm cho một đề thi
     * GET /api/grading/stats/{examId}
     * @param examId ID của đề thi
     * @returns Map chứa các thống kê
     * @author: K24DTCN210-NVMANH (21/11/2025 14:38)
     * --------------------------------------------------- */
    @GetMapping("/stats/{examId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getGradingStats(
            @PathVariable Long examId,
            Authentication auth) {
        Long teacherId = getCurrentUserId(auth);
        log.info("GET /api/grading/stats/{} - teacherId: {}", examId, teacherId);
        
        Map<String, Object> stats = gradingService.getGradingStats(examId, teacherId);
        
        log.info("Retrieved grading stats - examId: {}, totalSubmissions: {}, graded: {}", 
                examId, stats.get("totalSubmissions"), stats.get("graded"));
        return ResponseEntity.ok(stats);
    }
    
    /* ---------------------------------------------------
     * Lấy ID của user hiện tại từ Authentication
     * JWT token có sub field chứa email, auth.getName() trả về email
     * @param auth Authentication object
     * @returns userId
     * @author: K24DTCN210-NVMANH (21/11/2025 14:42)
     * EditBy: K24DTCN210-NVMANH (21/11/2025 15:56) - Fix lỗi: auth.getName() trả về userId chứ không phải email
     * EditBy: K24DTCN210-NVMANH (01/12/2025) - Fix: JWT sub field là email, cần query user từ email
     * --------------------------------------------------- */
    private Long getCurrentUserId(Authentication auth) {
        String subject = auth.getName();
        
        // Try parse as Long first (if JWT has userId in "sub")
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            // If not a number, it's email - fallback to query by email
            User user = userRepository.findByEmail(subject)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + subject));
            return user.getId();
        }
    }
}
