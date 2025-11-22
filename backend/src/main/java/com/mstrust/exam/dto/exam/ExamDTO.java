package com.mstrust.exam.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO đơn giản cho Exam entity (dùng trong GradingDetailDTO)
 * @author: K24DTCN210-NVMANH (21/11/2025 14:55)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Double passingScore;
}
