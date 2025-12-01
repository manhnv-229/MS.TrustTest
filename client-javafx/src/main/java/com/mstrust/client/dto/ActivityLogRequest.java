package com.mstrust.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Request DTO để gửi batch activities lên backend
 * Mirror từ backend: com.mstrust.exam.dto.monitoring.ActivityLogRequest
 * Backend expect ActivityEntry (nested class), không phải ActivityData
 * @author: K24DTCN210-NVMANH (21/11/2025 10:40)
 * EditBy: K24DTCN210-NVMANH (01/12/2025 23:00) - Fix format để match backend ActivityEntry
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogRequest {
    private Long submissionId;
    private List<ActivityEntry> activities;
    
    /* ---------------------------------------------------
     * Nested class ActivityEntry để match backend format
     * @author: K24DTCN210-NVMANH (01/12/2025 23:00)
     * --------------------------------------------------- */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEntry {
        private ActivityType activityType; // ActivityType enum (Gson sẽ serialize thành string)
        private String details;
        private LocalDateTime timestamp;
        
        // Manual getters/setters (backup for Lombok issues)
        public ActivityType getActivityType() { return activityType; }
        public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /* ---------------------------------------------------
     * Tạo request với submission ID và danh sách activities
     * Convert ActivityData sang ActivityEntry để match backend
     * @param submissionId ID của bài làm
     * @param activities Danh sách hoạt động cần log (ActivityData)
     * @returns ActivityLogRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:40)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:00) - Convert ActivityData to ActivityEntry
     * --------------------------------------------------- */
    public static ActivityLogRequest of(Long submissionId, List<ActivityData> activities) {
        List<ActivityEntry> entries = activities.stream()
            .map(activity -> {
                ActivityEntry entry = new ActivityEntry();
                entry.setActivityType(activity.getActivityType());
                entry.setDetails(activity.getDetails());
                entry.setTimestamp(activity.getTimestamp());
                return entry;
            })
            .collect(Collectors.toList());
        
        ActivityLogRequest request = new ActivityLogRequest();
        request.setSubmissionId(submissionId);
        request.setActivities(entries);
        return request;
    }
    
    // Manual getters/setters (backup for Lombok issues)
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    
    public List<ActivityEntry> getActivities() { return activities; }
    public void setActivities(List<ActivityEntry> activities) { this.activities = activities; }
}
