package com.mstrust.exam.dto.grading;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để giáo viên chấm điểm một câu trả lời
 * Chứa điểm số và nhận xét cho câu trả lời
 * @author: K24DTCN210-NVMANH (21/11/2025 13:56)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAnswerRequest {
    /* Điểm số cho câu trả lời (phải >= 0) */
    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm số phải lớn hơn hoặc bằng 0")
    private Double score;
    
    /* Nhận xét của giáo viên (tùy chọn) */
    private String feedback;
}
