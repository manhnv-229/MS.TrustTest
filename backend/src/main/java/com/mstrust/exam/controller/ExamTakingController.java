package com.mstrust.exam.controller;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.dto.grading.StudentResultDTO;
import com.mstrust.exam.service.ExamTakingService;
import com.mstrust.exam.service.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * REST Controller cho exam taking (student làm bài thi)
 * Base path: /exam-taking (URL cuối: /api/exam-taking)
 * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
 * EditBy: K24DTCN210-NVMANH (20/11/2025 09:18) - Fix context-path violation
 * EditBy: K24DTCN210-NVMANH (21/11/2025 14:50) - Add graded result endpoint
 * --------------------------------------------------- */
@RestController
@RequestMapping("/exam-taking")
@RequiredArgsConstructor
public class ExamTakingController {
    
    private final ExamTakingService examTakingService;
    private final GradingService gradingService;
    
    /* ---------------------------------------------------
     * GET /exam-taking/available
     * Lấy danh sách exams mà student có thể làm
     * @returns List<AvailableExamDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AvailableExamDTO>> getAvailableExams(Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        List<AvailableExamDTO> exams = examTakingService.getAvailableExams(studentId);
        return ResponseEntity.ok(exams);
    }
    
    /* ---------------------------------------------------
     * GET /exam-taking/check-eligibility/{examId}
     * Check xem student có thể làm exam này không
     * @param examId ID của exam
     * @returns Map với isEligible và reason
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @GetMapping("/check-eligibility/{examId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @PathVariable Long examId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        Map<String, Object> result = examTakingService.checkEligibility(examId, studentId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * POST /exam-taking/start/{examId}
     * Bắt đầu làm bài thi - tạo submission mới
     * @param examId ID của exam
     * @returns StartExamResponse
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @PostMapping("/start/{examId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StartExamResponse> startExam(
            @PathVariable Long examId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        StartExamResponse response = examTakingService.startExam(examId, studentId);
        return ResponseEntity.ok(response);
    }
    
    /* ---------------------------------------------------
     * GET /exam-taking/questions/{submissionId}
     * Lấy danh sách câu hỏi trong exam (with randomization)
     * @param submissionId ID của submission
     * @returns List<QuestionForStudentDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @GetMapping("/questions/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuestionForStudentDTO>> getExamQuestions(
            @PathVariable Long submissionId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        List<QuestionForStudentDTO> questions = examTakingService.getExamQuestions(submissionId, studentId);
        return ResponseEntity.ok(questions);
    }
    
    /* ---------------------------------------------------
     * POST /exam-taking/save-answer/{submissionId}
     * Lưu/submit câu trả lời cho một câu hỏi
     * @param submissionId ID của submission
     * @param request SubmitAnswerRequest
     * @returns Map với success message
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @PostMapping("/save-answer/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> saveAnswer(
            @PathVariable Long submissionId,
            @RequestBody SubmitAnswerRequest request,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        Map<String, Object> result = examTakingService.saveAnswer(submissionId, request, studentId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * POST /exam-taking/submit/{submissionId}
     * Submit bài thi (final submission)
     * @param submissionId ID của submission
     * @returns ExamResultDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @PostMapping("/submit/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamResultDTO> submitExam(
            @PathVariable Long submissionId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        ExamResultDTO result = examTakingService.submitExam(submissionId, studentId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * GET /exam-taking/result/{submissionId}
     * Xem kết quả bài thi (theo exam settings)
     * @param submissionId ID của submission
     * @returns ExamResultDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    @GetMapping("/result/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamResultDTO> getResult(
            @PathVariable Long submissionId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        ExamResultDTO result = examTakingService.getResult(submissionId, studentId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * GET /exam-taking/graded-result/{submissionId}
     * Xem kết quả bài thi đã được chấm điểm
     * @param submissionId ID của submission
     * @returns StudentResultDTO với điểm số và feedback chi tiết
     * @author: K24DTCN210-NVMANH (21/11/2025 14:50)
     * --------------------------------------------------- */
    @GetMapping("/graded-result/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResultDTO> getGradedResult(
            @PathVariable Long submissionId,
            Authentication auth) {
        Long studentId = getCurrentUserId(auth);
        StudentResultDTO result = gradingService.getStudentResult(submissionId, studentId);
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy userId từ Authentication
     * @param auth Authentication object
     * @returns userId
     * @author: K24DTCN210-NVMANH (19/11/2025 15:32)
     * --------------------------------------------------- */
    private Long getCurrentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
