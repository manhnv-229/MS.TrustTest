package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/* ---------------------------------------------------
 * Request DTO để chấm điểm cho một câu trả lời của học sinh
 * @author: K24DTCN210-NVMANH (20/11/2025 11:15)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAnswerRequest {
    
    /* ---------------------------------------------------
     * ID của câu trả lời cần chấm điểm
     * --------------------------------------------------- */
    @NotNull(message = "Answer ID is required")
    private Long answerId;
    
    /* ---------------------------------------------------
     * Điểm đạt được (phải từ 0 đến maxPoints của câu hỏi)
     * --------------------------------------------------- */
    @NotNull(message = "Points earned is required")
    @DecimalMin(value = "0.0", message = "Points earned must be greater than or equal to 0")
    private BigDecimal pointsEarned;
    
    /* ---------------------------------------------------
     * Nhận xét của giáo viên cho câu trả lời này
     * --------------------------------------------------- */
    private String feedback;
    
    /* ---------------------------------------------------
     * Đúng hay sai (true = đúng, false = sai)
     * --------------------------------------------------- */
    @NotNull(message = "IsCorrect is required")
    private Boolean isCorrect;
}
