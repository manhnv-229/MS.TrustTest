package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Department;
import com.mstrust.exam.entity.Subject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request tạo mới Subject (Môn học)
 * @author NVMANH with Cline
 * @created 15/11/2025 14:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {

    @NotBlank(message = "Mã môn học không được để trống")
    @Size(max = 20, message = "Mã môn học không được vượt quá 20 ký tự")
    private String subjectCode;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 255, message = "Tên môn học không được vượt quá 255 ký tự")
    private String subjectName;

    private String description;

    @Min(value = 0, message = "Số tín chỉ phải >= 0")
    @Builder.Default
    private Integer credits = 0;

    private Long departmentId;

    /** ------------------------------------------
     * Mục đích: Chuyển đổi từ CreateSubjectRequest sang Subject entity
     * @param department Department entity của môn học (có thể null)
     * @return Subject entity
     * @author NVMANH with Cline
     * @created 15/11/2025 14:15
     */
    public Subject toEntity(Department department) {
        return Subject.builder()
                .subjectCode(this.subjectCode)
                .subjectName(this.subjectName)
                .description(this.description)
                .credits(this.credits != null ? this.credits : 0)
                .department(department)
                .build();
    }
}
