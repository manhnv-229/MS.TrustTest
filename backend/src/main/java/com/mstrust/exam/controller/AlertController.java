package com.mstrust.exam.controller;

import com.mstrust.exam.dto.monitoring.AlertDTO;
import com.mstrust.exam.dto.monitoring.AlertReviewRequest;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.UserRepository;
import com.mstrust.exam.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* ---------------------------------------------------
 * Controller xử lý alert management APIs cho teacher
 * (Xem alerts, review alerts)
 * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {
    
    private final AlertService alertService;
    private final UserRepository userRepository;
    
    /* ---------------------------------------------------
     * Lấy tất cả alerts của một submission
     * @param submissionId ID của submission
     * @returns Danh sách AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
     * --------------------------------------------------- */
    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<AlertDTO>> getAlertsBySubmission(
        @PathVariable Long submissionId
    ) {
        List<AlertDTO> alerts = alertService.getAlertsBySubmission(submissionId);
        return ResponseEntity.ok(alerts);
    }
    
    /* ---------------------------------------------------
     * Lấy alerts chưa review của một submission
     * @param submissionId ID của submission
     * @returns Danh sách AlertDTO chưa review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
     * --------------------------------------------------- */
    @GetMapping("/submission/{submissionId}/unreviewed")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<AlertDTO>> getUnreviewedAlertsBySubmission(
        @PathVariable Long submissionId
    ) {
        List<AlertDTO> alerts = alertService.getUnreviewedAlerts(submissionId);
        return ResponseEntity.ok(alerts);
    }
    
    /* ---------------------------------------------------
     * Lấy tất cả alerts chưa review của một exam
     * @param examId ID của exam
     * @returns Danh sách AlertDTO chưa review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
     * --------------------------------------------------- */
    @GetMapping("/exam/{examId}/unreviewed")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<AlertDTO>> getUnreviewedAlertsByExam(
        @PathVariable Long examId
    ) {
        List<AlertDTO> alerts = alertService.getUnreviewedAlertsByExam(examId);
        return ResponseEntity.ok(alerts);
    }
    
    /* ---------------------------------------------------
     * Review alert (giáo viên xem xét và đánh dấu đã review)
     * @param alertId ID của alert
     * @param request AlertReviewRequest
     * @returns AlertDTO đã review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
     * --------------------------------------------------- */
    @PostMapping("/{alertId}/review")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<AlertDTO> reviewAlert(
        @PathVariable Long alertId,
        @Valid @RequestBody AlertReviewRequest request
    ) {
        Long teacherId = getCurrentUserId();
        
        log.info("Teacher {} reviewing alert {}", teacherId, alertId);
        
        AlertDTO result = alertService.reviewAlert(alertId, teacherId, request.getReviewNote());
        
        return ResponseEntity.ok(result);
    }
    
    /* ---------------------------------------------------
     * Lấy current user ID từ SecurityContext
     * @returns User ID
     * @author: K24DTCN210-NVMANH (21/11/2025 10:16)
     * --------------------------------------------------- */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
