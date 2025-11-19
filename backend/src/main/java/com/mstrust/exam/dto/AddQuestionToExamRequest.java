package com.mstrust.exam.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để thêm câu hỏi vào bài thi
 * @author: K24DTCN210-NVMANH (19/11/2025 09:15)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToExamRequest {
    
    @NotNull(message = "Question ID không được null")
    @Positive(message = "Question ID phải là số dương")
    private Long questionId;
    
    @NotNull(message = "Question order không được null")
    @Positive(message = "Question order phải là số dương")
    private Integer questionOrder;
    
    @NotNull(message = "Points không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points phải lớn hơn 0")
    @DecimalMax(value = "100.0", message = "Points không được vượt quá 100")
    private BigDecimal points;
}
