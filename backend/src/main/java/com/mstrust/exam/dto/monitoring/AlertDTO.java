package com.mstrust.exam.dto.monitoring;

import com.mstrust.exam.entity.AlertSeverity;
import lombok.*;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO trả về thông tin alert
 * @author: K24DTCN210-NVMANH (21/11/2025 10:11)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    
    private Long id;
    private Long submissionId;
    private String studentName;
    private String studentCode;
    private String alertType;
    private AlertSeverity severity;
    private String description;
    private Boolean reviewed;
    private Long reviewedBy;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewNote;
    private LocalDateTime createdAt;
}
