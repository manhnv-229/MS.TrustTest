package com.mstrust.client.teacher.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để hoàn tất việc chấm điểm
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeGradingRequest {
    private String generalFeedback;
}

