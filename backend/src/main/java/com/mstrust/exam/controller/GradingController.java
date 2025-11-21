package com.mstrust.exam.controller;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.SubmissionStatus;
import com.mstrust.exam.service.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/* ---------------------------------------------------
 * Controller xử lý các API endpoints cho grading (chấm điểm)
 * Base path: /grading (KHÔNG có /api vì đã config trong application.yml)
 * @author: K24DTCN210-NVMANH (20/11/2025 11:21)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/grading")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    /* ---------------------------------------------------
     * Lấy danh sách submissions với filter và pagination
     * GET /api/grading/submissions
     * @param examId ID của exam cần lọc (optional)
     * @param status Trạng thái cần lọc (optional)
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 20)
     * @param sortBy Trường cần sort (default: submittedAt)
     * @param sortOrder Thứ tự sort (default: desc)
     * @param auth Authentication object
     * @returns Page danh sách submissions
     * @author: K24DTCN210-NVMANH (20/11/2025 11:21)
     * --------------------------------------------------- */
    @GetMapping("/submissions")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<SubmissionListItemDTO>> getSubmissions(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Authentication auth) {

        // Get teacher ID from authentication
        Long teacherId = Long.parseLong(auth.getName());

        // Get submissions
        Page<SubmissionListItemDTO> submissions = gradingService.getSubmissions(
                examId, status, page, size, sortBy, sortOrder, teacherId
        );

        return ResponseEntity.ok(submissions);
    }

    /* ---------------------------------------------------
     * Lấy chi tiết submission để chấm điểm
     * GET /api/grading/submissions/{submissionId}/detail
     * @param submissionId ID của submission
     * @param auth Authentication object
     * @returns Chi tiết submission với tất cả answers
     * @author: K24DTCN210-NVMANH (20/11/2025 11:21)
     * --------------------------------------------------- */
    @GetMapping("/submissions/{submissionId}/detail")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<SubmissionGradingDetailDTO> getSubmissionDetail(
            @PathVariable Long submissionId,
            Authentication auth) {

        // Get teacher ID from authentication
        Long teacherId = Long.parseLong(auth.getName());

        // Get submission detail
        SubmissionGradingDetailDTO detail = gradingService.getSubmissionDetail(
                submissionId, teacherId
        );

        return ResponseEntity.ok(detail);
    }

    /* ---------------------------------------------------
     * Chấm điểm cho một câu trả lời
     * POST /api/grading/grade-answer
     * @param request Thông tin chấm điểm
     * @param auth Authentication object
     * @returns Kết quả chấm điểm
     * @author: K24DTCN210-NVMANH (20/11/2025 11:21)
     * --------------------------------------------------- */
    @PostMapping("/grade-answer")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> gradeAnswer(
            @Valid @RequestBody GradeAnswerRequest request,
            Authentication auth) {

        // Get teacher ID from authentication
        Long teacherId = Long.parseLong(auth.getName());

        // Grade answer
        Map<String, Object> result = gradingService.gradeAnswer(request, teacherId);

        return ResponseEntity.ok(result);
    }

    /* ---------------------------------------------------
     * Hoàn tất quá trình chấm điểm (finalize)
     * POST /api/grading/finalize/{submissionId}
     * @param submissionId ID của submission
     * @param auth Authentication object
     * @returns Kết quả cuối cùng của bài thi
     * @author: K24DTCN210-NVMANH (20/11/2025 11:21)
     * --------------------------------------------------- */
    @PostMapping("/finalize/{submissionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<ExamResultDTO> finalizeGrading(
            @PathVariable Long submissionId,
            Authentication auth) {

        // Get teacher ID from authentication
        Long teacherId = Long.parseLong(auth.getName());

        // Finalize grading
        ExamResultDTO result = gradingService.finalizeGrading(submissionId, teacherId);

        return ResponseEntity.ok(result);
    }
}
