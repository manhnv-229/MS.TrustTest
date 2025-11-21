package com.mstrust.exam.dto.monitoring;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/* ---------------------------------------------------
 * DTO cho request upload screenshot từ client
 * @author: K24DTCN210-NVMANH (21/11/2025 10:10)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotUploadRequest {
    
    @NotNull(message = "Submission ID is required")
    private Long submissionId;
    
    private String screenResolution;
    
    private String windowTitle;
    
    private String metadata;
    
    // File sẽ được upload qua multipart/form-data
    // Xử lý trong controller với @RequestParam("file") MultipartFile
}
