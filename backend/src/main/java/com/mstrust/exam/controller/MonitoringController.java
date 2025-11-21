package com.mstrust.exam.controller;

import com.mstrust.exam.dto.monitoring.*;
import com.mstrust.exam.service.ActivityLogService;
import com.mstrust.exam.service.AlertService;
import com.mstrust.exam.service.ScreenshotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/* ---------------------------------------------------
 * Controller xử lý monitoring APIs cho student client
 * (Upload screenshots, log activities, create alerts)
 * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {
    
    private final ScreenshotService screenshotService;
    private final ActivityLogService activityLogService;
    private final AlertService alertService;
    
    /* ---------------------------------------------------
     * Upload screenshot từ student client
     * @param file File ảnh screenshot
     * @param submissionId ID của submission
     * @param screenResolution Độ phân giải màn hình
     * @param windowTitle Tiêu đề cửa sổ
     * @param metadata Metadata khác (JSON)
     * @returns ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @PostMapping("/screenshots")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ScreenshotDTO> uploadScreenshot(
        @RequestParam("file") MultipartFile file,
        @RequestParam("submissionId") Long submissionId,
        @RequestParam(value = "screenResolution", required = false) String screenResolution,
        @RequestParam(value = "windowTitle", required = false) String windowTitle,
        @RequestParam(value = "metadata", required = false) String metadata
    ) {
        log.info("Upload screenshot request for submission: {}", submissionId);
        
        ScreenshotDTO result = screenshotService.uploadScreenshot(
            file, submissionId, screenResolution, windowTitle, metadata
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách screenshots của submission
     * @param submissionId ID của submission
     * @returns Danh sách ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @GetMapping("/screenshots/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScreenshotDTO>> getScreenshots(
        @PathVariable Long submissionId
    ) {
        List<ScreenshotDTO> screenshots = screenshotService.getScreenshotsBySubmission(submissionId);
        return ResponseEntity.ok(screenshots);
    }
    
    /* ---------------------------------------------------
     * Ghi log activities từ student client (batch)
     * @param request ActivityLogRequest
     * @returns Danh sách ActivityLogDTO đã lưu
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @PostMapping("/activities")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ActivityLogDTO>> logActivities(
        @Valid @RequestBody ActivityLogRequest request
    ) {
        log.info("Log activities request for submission: {} (count: {})", 
            request.getSubmissionId(), request.getActivities().size());
        
        List<ActivityLogDTO> result = activityLogService.logActivities(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /* ---------------------------------------------------
     * Lấy activity logs của submission
     * @param submissionId ID của submission
     * @returns Danh sách ActivityLogDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @GetMapping("/activities/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getActivities(
        @PathVariable Long submissionId
    ) {
        List<ActivityLogDTO> activities = activityLogService.getActivitiesBySubmission(submissionId);
        return ResponseEntity.ok(activities);
    }
    
    /* ---------------------------------------------------
     * Tạo alert từ client hoặc auto-detection
     * @param request AlertCreateRequest
     * @returns AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @PostMapping("/alerts")
    @PreAuthorize("hasAnyRole('STUDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<AlertDTO> createAlert(
        @Valid @RequestBody AlertCreateRequest request
    ) {
        log.info("Create alert for submission: {} - Type: {}, Severity: {}", 
            request.getSubmissionId(), request.getAlertType(), request.getSeverity());
        
        AlertDTO result = alertService.createAlert(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /* ---------------------------------------------------
     * Lấy monitoring summary của submission
     * @param submissionId ID của submission
     * @returns MonitoringSummaryDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:15)
     * --------------------------------------------------- */
    @GetMapping("/summary/{submissionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_MANAGER', 'ADMIN')")
    public ResponseEntity<MonitoringSummaryDTO> getMonitoringSummary(
        @PathVariable Long submissionId
    ) {
        MonitoringSummaryDTO summary = alertService.getMonitoringSummary(submissionId);
        return ResponseEntity.ok(summary);
    }
}
