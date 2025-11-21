package com.mstrust.exam.controller;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.UserRepository;
import com.mstrust.exam.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/* ---------------------------------------------------
 * REST Controller cho Exam Management
 * Base path: /exams (context-path /api đã được cấu hình trong application.yml)
 * Full URL: /api/exams
 * Authentication: Required (JWT)
 * Authorization: TEACHER, DEPT_MANAGER, ADMIN roles
 * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    
    private final ExamService examService;
    private final UserRepository userRepository;
    
    /* ---------------------------------------------------
     * Tạo exam mới
     * POST /api/exams
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody CreateExamRequest request) {
        Long currentUserId = getCurrentUserId();
        ExamDTO exam = examService.createExam(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(exam);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách exam với filters và pagination
     * GET /api/exams?subjectClassId=1&examPurpose=MIDTERM&examFormat=ONLINE&isPublished=true&page=0&size=10&sort=startTime,desc
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ExamSummaryDTO>> getExams(
            @RequestParam(required = false) Long subjectClassId,
            @RequestParam(required = false) ExamPurpose examPurpose,
            @RequestParam(required = false) ExamFormat examFormat,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        // Parse sort parameters
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = sort[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ExamSummaryDTO> exams = examService.getExams(
            subjectClassId, examPurpose, examFormat, isPublished, pageable);
        
        return ResponseEntity.ok(exams);
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết exam theo ID
     * GET /api/exams/{id}
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDTO> getExamById(@PathVariable Long id) {
        ExamDTO exam = examService.getExamById(id);
        return ResponseEntity.ok(exam);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách exam theo subject class
     * GET /api/exams/subject-class/{subjectClassId}?page=0&size=10
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @GetMapping("/subject-class/{subjectClassId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ExamSummaryDTO>> getExamsBySubjectClass(
            @PathVariable Long subjectClassId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,desc") String[] sort) {
        
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = sort[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ExamSummaryDTO> exams = examService.getExamsBySubjectClass(subjectClassId, pageable);
        
        return ResponseEntity.ok(exams);
    }
    
    /* ---------------------------------------------------
     * Cập nhật exam
     * PUT /api/exams/{id}
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDTO> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest request) {
        
        Long currentUserId = getCurrentUserId();
        ExamDTO exam = examService.updateExam(id, request, currentUserId);
        return ResponseEntity.ok(exam);
    }
    
    /* ---------------------------------------------------
     * Xóa exam (soft delete)
     * DELETE /api/exams/{id}
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 08:42)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
    
    /* ---------------------------------------------------
     * Publish exam - cho phép students thấy và làm bài thi
     * POST /api/exams/{id}/publish
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * Business rules:
     * - Exam phải có ít nhất 1 câu hỏi
     * - startTime phải trong tương lai
     * - Chỉ publish được exam chưa publish
     * @author: K24DTCN210-NVMANH (19/11/2025 08:47)
     * --------------------------------------------------- */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDTO> publishExam(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ExamDTO exam = examService.publishExam(id, currentUserId);
        return ResponseEntity.ok(exam);
    }
    
    /* ---------------------------------------------------
     * Unpublish exam - ẩn exam khỏi students
     * POST /api/exams/{id}/unpublish
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * Business rules:
     * - Chỉ unpublish được exam đã publish
     * - Không unpublish được exam đang diễn ra
     * @author: K24DTCN210-NVMANH (19/11/2025 08:47)
     * --------------------------------------------------- */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDTO> unpublishExam(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ExamDTO exam = examService.unpublishExam(id, currentUserId);
        return ResponseEntity.ok(exam);
    }
    
    /* ---------------------------------------------------
     * Thêm câu hỏi vào bài thi
     * POST /api/exams/{examId}/questions
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 09:19)
     * --------------------------------------------------- */
    @PostMapping("/{examId}/questions")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamQuestionDTO> addQuestionToExam(
            @PathVariable Long examId,
            @Valid @RequestBody AddQuestionToExamRequest request) {
        
        Long currentUserId = getCurrentUserId();
        ExamQuestionDTO examQuestion = examService.addQuestionToExam(examId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(examQuestion);
    }
    
    /* ---------------------------------------------------
     * Xóa câu hỏi khỏi bài thi
     * DELETE /api/exams/{examId}/questions/{questionId}
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 09:19)
     * --------------------------------------------------- */
    @DeleteMapping("/{examId}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> removeQuestionFromExam(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        
        Long currentUserId = getCurrentUserId();
        examService.removeQuestionFromExam(examId, questionId, currentUserId);
        return ResponseEntity.noContent().build();
    }
    
    /* ---------------------------------------------------
     * Sắp xếp lại thứ tự câu hỏi trong bài thi
     * PUT /api/exams/{examId}/questions/reorder
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 09:19)
     * --------------------------------------------------- */
    @PutMapping("/{examId}/questions/reorder")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<java.util.List<ExamQuestionDTO>> reorderQuestions(
            @PathVariable Long examId,
            @Valid @RequestBody ReorderQuestionsRequest request) {
        
        Long currentUserId = getCurrentUserId();
        java.util.List<ExamQuestionDTO> reordered = examService.reorderQuestions(examId, request, currentUserId);
        return ResponseEntity.ok(reordered);
    }
    
    /* ---------------------------------------------------
     * Cập nhật điểm số của câu hỏi trong bài thi
     * PUT /api/exams/{examId}/questions/{questionId}
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 09:19)
     * --------------------------------------------------- */
    @PutMapping("/{examId}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamQuestionDTO> updateQuestionScore(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionScoreRequest request) {
        
        Long currentUserId = getCurrentUserId();
        ExamQuestionDTO updated = examService.updateQuestionScore(examId, questionId, request, currentUserId);
        return ResponseEntity.ok(updated);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi trong bài thi
     * GET /api/exams/{examId}/questions
     * Auth: TEACHER, DEPT_MANAGER, ADMIN
     * @author: K24DTCN210-NVMANH (19/11/2025 09:19)
     * --------------------------------------------------- */
    @GetMapping("/{examId}/questions")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<java.util.List<ExamQuestionDTO>> getExamQuestions(@PathVariable Long examId) {
        java.util.List<ExamQuestionDTO> questions = examService.getExamQuestions(examId);
        return ResponseEntity.ok(questions);
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy ID của user hiện tại từ Security Context
     * JWT token có thể lưu userId trong "sub" claim
     * @author: K24DTCN210-NVMANH (19/11/2025 08:39)
     * EditBy: K24DTCN210-NVMANH (20/11/2025 20:56) - Fix user not found error
     * --------------------------------------------------- */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String subject = authentication.getName();
        
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
