package com.mstrust.exam.dto.monitoring;

import lombok.*;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO trả về thông tin screenshot
 * @author: K24DTCN210-NVMANH (21/11/2025 10:10)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotDTO {
    
    private Long id;
    private Long submissionId;
    private String filePath;
    private Long fileSize;
    private LocalDateTime timestamp;
    private String screenResolution;
    private String windowTitle;
    private String metadata;
    private LocalDateTime createdAt;
}
