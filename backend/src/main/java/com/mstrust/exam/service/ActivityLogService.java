package com.mstrust.exam.service;

import com.mstrust.exam.dto.monitoring.ActivityLogDTO;
import com.mstrust.exam.dto.monitoring.ActivityLogRequest;
import com.mstrust.exam.entity.ActivityLog;
import com.mstrust.exam.entity.ActivityType;
import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ActivityLogRepository;
import com.mstrust.exam.repository.ExamSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý business logic cho activity logs
 * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ActivityLogService {
    
    private final ActivityLogRepository activityLogRepository;
    private final ExamSubmissionRepository submissionRepository;
    
    /* ---------------------------------------------------
     * Ghi log activities (hỗ trợ batch)
     * @param request ActivityLogRequest chứa danh sách activities
     * @returns Danh sách ActivityLogDTO đã được lưu
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<ActivityLogDTO> logActivities(ActivityLogRequest request) {
        ExamSubmission submission = submissionRepository.findById(request.getSubmissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        List<ActivityLog> activityLogs = request.getActivities().stream()
            .map(entry -> ActivityLog.builder()
                .submission(submission)
                .activityType(entry.getActivityType())
                .details(entry.getDetails())
                .timestamp(entry.getTimestamp())
                .build())
            .collect(Collectors.toList());
        
        activityLogs = activityLogRepository.saveAll(activityLogs);
        
        log.info("Logged {} activities for submission {}", activityLogs.size(), request.getSubmissionId());
        
        return activityLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy tất cả activity logs của submission
     * @param submissionId ID của submission
     * @returns Danh sách ActivityLogDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<ActivityLogDTO> getActivitiesBySubmission(Long submissionId) {
        return activityLogRepository.findBySubmissionId(submissionId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy activities theo loại
     * @param submissionId ID của submission
     * @param activityType Loại activity
     * @returns Danh sách ActivityLogDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public List<ActivityLogDTO> getActivitiesByType(Long submissionId, ActivityType activityType) {
        return activityLogRepository.findBySubmissionIdAndActivityType(submissionId, activityType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Đếm window switches trong khoảng thời gian
     * @param submissionId ID của submission
     * @param minutes Số phút để check
     * @returns Số lần switch
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    public long countWindowSwitchesInLastMinutes(Long submissionId, int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        LocalDateTime endTime = LocalDateTime.now();
        return activityLogRepository.countWindowSwitchesInTimeRange(submissionId, startTime, endTime);
    }
    
    /* ---------------------------------------------------
     * Convert ActivityLog entity to DTO
     * @param log ActivityLog entity
     * @returns ActivityLogDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:14)
     * --------------------------------------------------- */
    private ActivityLogDTO convertToDTO(ActivityLog log) {
        return ActivityLogDTO.builder()
            .id(log.getId())
            .submissionId(log.getSubmission().getId())
            .activityType(log.getActivityType())
            .details(log.getDetails())
            .timestamp(log.getTimestamp())
            .createdAt(log.getCreatedAt())
            .build();
    }
}
