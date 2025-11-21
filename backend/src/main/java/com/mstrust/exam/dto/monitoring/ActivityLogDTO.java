package com.mstrust.exam.dto.monitoring;

import com.mstrust.exam.entity.ActivityType;
import lombok.*;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO trả về thông tin activity log
 * @author: K24DTCN210-NVMANH (21/11/2025 10:11)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    
    private Long id;
    private Long submissionId;
    private ActivityType activityType;
    private String details;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
}
