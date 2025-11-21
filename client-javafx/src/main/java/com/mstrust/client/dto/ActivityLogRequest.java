package com.mstrust.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* ---------------------------------------------------
 * Request DTO để gửi batch activities lên backend
 * Mirror từ backend: com.mstrust.exam.dto.monitoring.ActivityLogRequest
 * @author: K24DTCN210-NVMANH (21/11/2025 10:40)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogRequest {
    private Long submissionId;
    private List<ActivityData> activities;

    /* ---------------------------------------------------
     * Tạo request với submission ID và danh sách activities
     * @param submissionId ID của bài làm
     * @param activities Danh sách hoạt động cần log
     * @returns ActivityLogRequest instance
     * @author: K24DTCN210-NVMANH (21/11/2025 10:40)
     * --------------------------------------------------- */
    public static ActivityLogRequest of(Long submissionId, List<ActivityData> activities) {
        return ActivityLogRequest.builder()
                .submissionId(submissionId)
                .activities(activities)
                .build();
    }
}
