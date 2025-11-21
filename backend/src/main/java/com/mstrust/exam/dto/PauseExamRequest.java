package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho request pause exam
 * Teacher có thể pause exam session của student
 * @author: K24DTCN210-NVMANH (21/11/2025 02:01)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PauseExamRequest {
    
    /* ---------------------------------------------------
     * ID của submission cần pause
     * --------------------------------------------------- */
    private Long submissionId;
    
    /* ---------------------------------------------------
     * Lý do pause (optional)
     * --------------------------------------------------- */
    private String reason;
    
    /* ---------------------------------------------------
     * Thời gian pause (phút) - nếu null thì pause indefinitely
     * --------------------------------------------------- */
    private Integer pauseDurationMinutes;
}
