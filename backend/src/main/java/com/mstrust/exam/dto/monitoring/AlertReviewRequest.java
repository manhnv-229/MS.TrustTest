package com.mstrust.exam.dto.monitoring;

import lombok.*;

/* ---------------------------------------------------
 * DTO cho request review alert (giáo viên xem xét cảnh báo)
 * @author: K24DTCN210-NVMANH (21/11/2025 10:11)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertReviewRequest {
    
    private String reviewNote;
}
