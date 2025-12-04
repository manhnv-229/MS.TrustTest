package com.mstrust.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogRequest {
    @NotBlank(message = "Level is required")
    private String level;

    @NotBlank(message = "Source is required")
    private String source;

    private String message;
    private String stackTrace;
    private String additionalData;
    private Long submissionId;
}
