package com.mstrust.exam.dto.monitoring;

import com.mstrust.exam.entity.AlertSeverity;
import lombok.*;
import java.util.Map;

/* ---------------------------------------------------
 * DTO trả về tổng hợp monitoring data của một submission
 * @author: K24DTCN210-NVMANH (21/11/2025 10:11)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringSummaryDTO {
    
    private Long submissionId;
    private String studentName;
    private String studentCode;
    
    // Screenshot statistics
    private long totalScreenshots;
    
    // Activity statistics
    private long totalActivities;
    private long windowSwitchCount;
    private long clipboardActivityCount;
    private long keystrokeAnomalies;
    
    // Alert statistics
    private long totalAlerts;
    private long unreviewedAlerts;
    private Map<AlertSeverity, Long> alertsBySeverity;
    
    // Latest screenshot
    private ScreenshotDTO latestScreenshot;
    
    // Risk assessment
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskDescription;
}
