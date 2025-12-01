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
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:50) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static AlertCreateRequest windowSwitchAlert(Long submissionId, int switchCount) {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setSubmissionId(submissionId);
        request.setSeverity(switchCount >= 20 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH);
        request.setAlertType("MULTIPLE_WINDOW_SWITCHES");
        request.setDescription(String.format("Phát hiện %d lần chuyển cửa sổ trong thời gian ngắn", switchCount));
        return request;
    }

    /* ---------------------------------------------------
     * Tạo alert cho suspicious process
     * @param submissionId ID bài làm
     * @param processName Tên process
     * @returns AlertCreateRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:50) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static AlertCreateRequest suspiciousProcessAlert(Long submissionId, String processName) {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setSubmissionId(submissionId);
        request.setSeverity(AlertSeverity.CRITICAL);
        request.setAlertType("SUSPICIOUS_PROCESS");
        request.setDescription(String.format("Phát hiện ứng dụng bị cấm: %s", processName));
        return request;
    }

    /* ---------------------------------------------------
     * Tạo alert cho clipboard activity quá nhiều
     * @param submissionId ID bài làm
     * @param count Số lần clipboard operations
     * @returns AlertCreateRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:41)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:50) - Chuyển sang tiếng Việt
     * --------------------------------------------------- */
    public static AlertCreateRequest clipboardAlert(Long submissionId, int count) {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setSubmissionId(submissionId);
        request.setSeverity(AlertSeverity.MEDIUM);
        request.setAlertType("EXCESSIVE_CLIPBOARD");
        request.setDescription(String.format("Phát hiện %d thao tác sao chép/dán", count));
        return request;
    }
    
    // Manual getters/setters (backup for Lombok issues)
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
