package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho request resume exam
 * Teacher có thể resume exam session đã bị pause
 * @author: K24DTCN210-NVMANH (21/11/2025 02:01)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeExamRequest {
    
    /* ---------------------------------------------------
     * ID của submission cần resume
     * --------------------------------------------------- */
    private Long submissionId;
    
    /* ---------------------------------------------------
     * Thời gian bù thêm (phút) - nếu cần extend time
     * --------------------------------------------------- */
    private Integer additionalTimeMinutes;
}
