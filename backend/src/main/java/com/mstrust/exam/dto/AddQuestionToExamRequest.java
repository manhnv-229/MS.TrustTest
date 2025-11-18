package com.mstrust.exam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request thêm question vào exam
 * 
 * Validation:
 * - questionId: required
 * - points: required, > 0
 * - questionOrder: optional (auto-calculate if not provided)
 * 
 * Business Rules:
 * - Question phải tồn tại
 * - Question chưa được add vào exam này
 * - Exam phải ở trạng thái DRAFT
 * - Teacher phải có quyền edit exam
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToExamRequest {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotNull(message = "Points is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points must be greater than 0")
    private Double points;
    
    // Optional - nếu không có sẽ tự động add vào cuối
    @Min(value = 1, message = "Question order must be at least 1")
    private Integer questionOrder;
}
