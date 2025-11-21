package com.mstrust.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để tạo alert mới
 * Mirror từ backend: com.mstrust.exam.dto.monitoring.AlertCreateRequest
 * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertCreateRequest {
    private Long submissionId;
    private AlertSeverity severity;
    private String alertType;
    private String description;

    /* ---------------------------------------------------
     * Tạo alert cho window switch quá nhiều
     * @param submissionId ID bài làm
     * @param switchCount Số lần switch
     * @returns AlertCreateRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
     * --------------------------------------------------- */
    public static AlertCreateRequest windowSwitchAlert(Long submissionId, int switchCount) {
        return AlertCreateRequest.builder()
                .submissionId(submissionId)
                .severity(switchCount >= 20 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH)
                .alertType("MULTIPLE_WINDOW_SWITCHES")
                .description(String.format("Detected %d window switches in short time", switchCount))
                .build();
    }

    /* ---------------------------------------------------
     * Tạo alert cho suspicious process
     * @param submissionId ID bài làm
     * @param processName Tên process
     * @returns AlertCreateRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
     * --------------------------------------------------- */
    public static AlertCreateRequest suspiciousProcessAlert(Long submissionId, String processName) {
        return AlertCreateRequest.builder()
                .submissionId(submissionId)
                .severity(AlertSeverity.CRITICAL)
                .alertType("SUSPICIOUS_PROCESS")
                .description(String.format("Detected blacklisted process: %s", processName))
                .build();
    }

    /* ---------------------------------------------------
     * Tạo alert cho clipboard activity quá nhiều
     * @param submissionId ID bài làm
     * @param count Số lần clipboard operations
     * @returns AlertCreateRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
     * --------------------------------------------------- */
    public static AlertCreateRequest clipboardAlert(Long submissionId, int count) {
        return AlertCreateRequest.builder()
                .submissionId(submissionId)
                .severity(AlertSeverity.MEDIUM)
                .alertType("EXCESSIVE_CLIPBOARD")
                .description(String.format("Detected %d clipboard operations", count))
                .build();
    }
}
