package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ClassEntity;
import com.mstrust.exam.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request tạo mới Class (Lớp hành chính)
 * @author NVMANH with Cline
 * @created 15/11/2025 13:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassRequest {

    @NotBlank(message = "Mã lớp không được để trống")
    @Size(max = 20, message = "Mã lớp không được vượt quá 20 ký tự")
    private String classCode;

    @NotBlank(message = "Tên lớp không được để trống")
    @Size(max = 100, message = "Tên lớp không được vượt quá 100 ký tự")
    private String className;

    @NotNull(message = "ID khoa không được để trống")
    private Long departmentId;

    @NotBlank(message = "Năm học không được để trống")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Năm học phải có định dạng YYYY-YYYY (ví dụ: 2023-2024)")
    @Size(max = 20, message = "Năm học không được vượt quá 20 ký tự")
    private String academicYear;

    @Size(max = 100, message = "Tên giáo viên chủ nhiệm không được vượt quá 100 ký tự")
    private String homeroomTeacher;

    private Long classManagerId;

    @Builder.Default
    private Boolean isActive = true;

    /** ------------------------------------------
     * Mục đích: Chuyển đổi từ CreateClassRequest sang ClassEntity
     * @param department Department entity của lớp
     * @return ClassEntity
     * @author NVMANH with Cline
     * @created 15/11/2025 13:56
     */
    public ClassEntity toEntity(Department department) {
        return ClassEntity.builder()
                .classCode(this.classCode)
                .className(this.className)
                .department(department)
                .academicYear(this.academicYear)
                .homeroomTeacher(this.homeroomTeacher)
                .isActive(this.isActive != null ? this.isActive : true)
                .build();
    }
}
