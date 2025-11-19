package com.mstrust.exam.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để cập nhật điểm số của câu hỏi trong bài thi
 * @author: K24DTCN210-NVMANH (19/11/2025 09:16)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionScoreRequest {
    
    @NotNull(message = "Points không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points phải lớn hơn 0")
    @DecimalMax(value = "100.0", message = "Points không được vượt quá 100")
    private BigDecimal points;
}
