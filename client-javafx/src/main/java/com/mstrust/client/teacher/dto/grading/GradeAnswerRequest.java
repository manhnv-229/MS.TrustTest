package com.mstrust.client.teacher.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để giáo viên chấm điểm một câu trả lời
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAnswerRequest {
    private Double score;
    private String feedback;
}

