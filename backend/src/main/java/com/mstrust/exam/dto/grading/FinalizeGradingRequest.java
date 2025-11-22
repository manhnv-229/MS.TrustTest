package com.mstrust.exam.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để hoàn tất việc chấm điểm
 * Chuyển trạng thái bài thi từ SUBMITTED sang GRADED
 * @author: K24DTCN210-NVMANH (21/11/2025 13:57)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeGradingRequest {
    /* Nhận xét chung cho toàn bài thi (tùy chọn) */
    private String generalFeedback;
}
