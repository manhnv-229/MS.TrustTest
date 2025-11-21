package com.mstrust.exam.service;

import com.mstrust.exam.dto.monitoring.AlertCreateRequest;
import com.mstrust.exam.dto.monitoring.AlertDTO;
import com.mstrust.exam.dto.monitoring.MonitoringSummaryDTO;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý business logic cho monitoring alerts
 * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertService {
    
    private final MonitoringAlertRepository alertRepository;
    private final ExamSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ScreenshotRepository screenshotRepository;
    private final ActivityLogRepository activityLogRepository;
    
    /* ---------------------------------------------------
     * Tạo alert mới
     * @param request AlertCreateRequest
     * @returns AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public AlertDTO createAlert(AlertCreateRequest request) {
        ExamSubmission submission = submissionRepository.findById(request.getSubmissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        MonitoringAlert alert = MonitoringAlert.builder()
            .submission(submission)
            .alertType(request.getAlertType())
            .severity(request.getSeverity())
            .description(request.getDescription())
            .reviewed(false)
            .build();
        
        alert = alertRepository.save(alert);
        
        log.info("Created {} alert for submission {}: {}", 
            request.getSeverity(), request.getSubmissionId(), request.getAlertType());
        
        return convertToDTO(alert);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách alerts của submission
     * @param submissionId ID của submission
     * @returns Danh sách AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<AlertDTO> getAlertsBySubmission(Long submissionId) {
        return alertRepository.findBySubmissionId(submissionId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy alerts chưa review của submission
     * @param submissionId ID của submission
     * @returns Danh sách AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<AlertDTO> getUnreviewedAlerts(Long submissionId) {
        return alertRepository.findUnreviewedBySubmissionId(submissionId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy tất cả alerts chưa review của một exam
     * @param examId ID của exam
     * @returns Danh sách AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<AlertDTO> getUnreviewedAlertsByExam(Long examId) {
        return alertRepository.findUnreviewedByExamId(examId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Review alert (giáo viên xem xét)
     * @param alertId ID của alert
     * @param teacherId ID của giáo viên
     * @param reviewNote Ghi chú review
     * @returns AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public AlertDTO reviewAlert(Long alertId, Long teacherId, String reviewNote) {
        MonitoringAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        
        alert.setReviewed(true);
        alert.setReviewedBy(teacher);
        alert.setReviewedAt(LocalDateTime.now());
        alert.setReviewNote(reviewNote);
        
        alert = alertRepository.save(alert);
        
        log.info("Alert {} reviewed by teacher {}", alertId, teacherId);
        
        return convertToDTO(alert);
    }
    
    /* ---------------------------------------------------
     * Lấy tổng hợp monitoring data của submission
     * @param submissionId ID của submission
     * @returns MonitoringSummaryDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public MonitoringSummaryDTO getMonitoringSummary(Long submissionId) {
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        User student = submission.getStudent();
        
        // Get statistics
        long totalScreenshots = screenshotRepository.countBySubmissionId(submissionId);
        long totalActivities = activityLogRepository.findBySubmissionId(submissionId).size();
        
        // Window switches
        LocalDateTime last30Min = LocalDateTime.now().minusMinutes(30);
        long windowSwitchCount = activityLogRepository.countWindowSwitchesInTimeRange(
            submissionId, last30Min, LocalDateTime.now()
        );
        
        // Clipboard activities
        long clipboardCount = activityLogRepository
            .findBySubmissionIdAndActivityType(submissionId, ActivityType.CLIPBOARD)
            .size();
        
        // Keystroke anomalies (giả sử có logic phát hiện)
        long keystrokeAnomalies = 0; // TODO: Implement detection logic
        
        // Alert statistics
        long totalAlerts = alertRepository.findBySubmissionId(submissionId).size();
        long unreviewedAlerts = alertRepository.countUnreviewedBySubmissionId(submissionId);
        
        Map<AlertSeverity, Long> alertsBySeverity = new HashMap<>();
        for (AlertSeverity severity : AlertSeverity.values()) {
            long count = alertRepository.countBySubmissionIdAndSeverity(submissionId, severity);
            if (count > 0) {
                alertsBySeverity.put(severity, count);
            }
        }
        
        // Latest screenshot
        List<Screenshot> screenshots = screenshotRepository.findBySubmissionId(submissionId);
        var latestScreenshot = screenshots.isEmpty() ? null : 
            convertScreenshotToDTO(screenshots.get(0));
        
        // Risk assessment
        String riskLevel = calculateRiskLevel(windowSwitchCount, unreviewedAlerts, alertsBySeverity);
        String riskDescription = generateRiskDescription(windowSwitchCount, unreviewedAlerts);
        
        return MonitoringSummaryDTO.builder()
            .submissionId(submissionId)
            .studentName(student.getFullName())
            .studentCode(student.getStudentCode())
            .totalScreenshots(totalScreenshots)
            .totalActivities(totalActivities)
            .windowSwitchCount(windowSwitchCount)
            .clipboardActivityCount(clipboardCount)
            .keystrokeAnomalies(keystrokeAnomalies)
            .totalAlerts(totalAlerts)
            .unreviewedAlerts(unreviewedAlerts)
            .alertsBySeverity(alertsBySeverity)
            .latestScreenshot(latestScreenshot)
            .riskLevel(riskLevel)
            .riskDescription(riskDescription)
            .build();
    }
    
    /* ---------------------------------------------------
     * Tính toán mức độ rủi ro
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    private String calculateRiskLevel(long windowSwitches, long unreviewedAlerts, 
                                     Map<AlertSeverity, Long> alertsBySeverity) {
        // CRITICAL: Có critical alerts hoặc quá nhiều window switches
        if (alertsBySeverity.getOrDefault(AlertSeverity.CRITICAL, 0L) > 0 || windowSwitches > 20) {
            return "CRITICAL";
        }
        
        // HIGH: Có high alerts hoặc nhiều unreviewed
        if (alertsBySeverity.getOrDefault(AlertSeverity.HIGH, 0L) > 0 || unreviewedAlerts > 5) {
            return "HIGH";
        }
        
        // MEDIUM: Có warning alerts hoặc vài window switches
        if (alertsBySeverity.getOrDefault(AlertSeverity.MEDIUM, 0L) > 0 || windowSwitches > 5) {
            return "MEDIUM";
        }
        
        return "LOW";
    }
    
    /* ---------------------------------------------------
     * Tạo mô tả rủi ro
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    private String generateRiskDescription(long windowSwitches, long unreviewedAlerts) {
        StringBuilder desc = new StringBuilder();
        
        if (windowSwitches > 10) {
            desc.append("Phát hiện ").append(windowSwitches)
                .append(" lần chuyển cửa sổ trong 30 phút qua. ");
        }
        
        if (unreviewedAlerts > 0) {
            desc.append("Có ").append(unreviewedAlerts)
                .append(" cảnh báo chưa được xem xét. ");
        }
        
        if (desc.length() == 0) {
            desc.append("Không phát hiện hành vi bất thường.");
        }
        
        return desc.toString().trim();
    }
    
    /* ---------------------------------------------------
     * Convert Screenshot to DTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    private com.mstrust.exam.dto.monitoring.ScreenshotDTO convertScreenshotToDTO(Screenshot screenshot) {
        return com.mstrust.exam.dto.monitoring.ScreenshotDTO.builder()
            .id(screenshot.getId())
            .submissionId(screenshot.getSubmission().getId())
            .filePath(screenshot.getFilePath())
            .fileSize(screenshot.getFileSize())
            .timestamp(screenshot.getTimestamp())
            .screenResolution(screenshot.getScreenResolution())
            .windowTitle(screenshot.getWindowTitle())
            .metadata(screenshot.getMetadata())
            .createdAt(screenshot.getCreatedAt())
            .build();
    }
    
    /* ---------------------------------------------------
     * Convert MonitoringAlert entity to DTO
     * @param alert MonitoringAlert entity
     * @returns AlertDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    private AlertDTO convertToDTO(MonitoringAlert alert) {
        User student = alert.getSubmission().getStudent();
        
        return AlertDTO.builder()
            .id(alert.getId())
            .submissionId(alert.getSubmission().getId())
            .studentName(student.getFullName())
            .studentCode(student.getStudentCode())
            .alertType(alert.getAlertType())
            .severity(alert.getSeverity())
            .description(alert.getDescription())
            .reviewed(alert.getReviewed())
            .reviewedBy(alert.getReviewedBy() != null ? alert.getReviewedBy().getId() : null)
            .reviewedByName(alert.getReviewedBy() != null ? alert.getReviewedBy().getFullName() : null)
            .reviewedAt(alert.getReviewedAt())
            .reviewNote(alert.getReviewNote())
            .createdAt(alert.getCreatedAt())
            .build();
    }
}
