package com.mstrust.exam.dto.monitoring;

import com.mstrust.exam.entity.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/* ---------------------------------------------------
 * DTO cho request tạo alert
 * @author: K24DTCN210-NVMANH (21/11/2025 10:11)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertCreateRequest {
    
    @NotNull(message = "Submission ID is required")
    private Long submissionId;
    
    @NotBlank(message = "Alert type is required")
    private String alertType;
    
    @NotNull(message = "Severity is required")
    private AlertSeverity severity;
    
    private String description;
}
