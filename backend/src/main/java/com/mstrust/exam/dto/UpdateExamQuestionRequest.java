package com.mstrust.exam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request cập nhật points hoặc order của question trong exam
 * 
 * Validation:
 * - points: required, > 0
 * - questionOrder: required, >= 1
 * 
 * Business Rules:
 * - Exam phải ở trạng thái DRAFT
 * - Teacher phải có quyền edit exam
 * - Question phải đã tồn tại trong exam
 * - Không được duplicate order (service sẽ validate)
 * 
 * Use cases:
 * - Update points của một question
 * - Update order của một question (reorder)
 * - Update cả points và order
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamQuestionRequest {
    
    @NotNull(message = "Points is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points must be greater than 0")
    private Double points;
    
    @NotNull(message = "Question order is required")
    @Min(value = 1, message = "Question order must be at least 1")
    private Integer questionOrder;
}
