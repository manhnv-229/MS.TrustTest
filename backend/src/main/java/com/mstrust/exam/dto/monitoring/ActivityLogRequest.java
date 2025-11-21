package com.mstrust.exam.dto.monitoring;

import com.mstrust.exam.entity.ActivityType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO cho request ghi log activity (hỗ trợ batch upload)
 * @author: K24DTCN210-NVMANH (21/11/2025 10:10)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogRequest {
    
    @NotNull(message = "Submission ID is required")
    private Long submissionId;
    
    // Hỗ trợ batch upload nhiều activities cùng lúc
    private List<ActivityEntry> activities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEntry {
        @NotNull(message = "Activity type is required")
        private ActivityType activityType;
        
        private String details;
        
        @NotNull(message = "Timestamp is required")
        private LocalDateTime timestamp;
    }
}
