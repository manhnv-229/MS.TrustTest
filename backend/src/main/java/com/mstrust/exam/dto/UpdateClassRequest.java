package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ClassEntity;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request cập nhật Class
 * Tất cả các field đều optional (cho phép update từng phần)
 * @author NVMANH with Cline
 * @created 15/11/2025 13:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClassRequest {

    @Size(max = 20, message = "Mã lớp không được vượt quá 20 ký tự")
    private String classCode;

    @Size(max = 100, message = "Tên lớp không được vượt quá 100 ký tự")
    private String className;

    private Long departmentId;

    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Năm học phải có định dạng YYYY-YYYY (ví dụ: 2023-2024)")
    @Size(max = 20, message = "Năm học không được vượt quá 20 ký tự")
    private String academicYear;

    @Size(max = 100, message = "Tên giáo viên chủ nhiệm không được vượt quá 100 ký tự")
    private String homeroomTeacher;

    private Long classManagerId;

    private Boolean isActive;

    /** ------------------------------------------
     * Mục đích: Cập nhật các field của ClassEntity từ request
     * Chỉ update các field không null
     * @param classEntity ClassEntity cần update
     * @author NVMANH with Cline
     * @created 15/11/2025 13:57
     */
    public void updateEntity(ClassEntity classEntity) {
        if (this.classCode != null) {
            classEntity.setClassCode(this.classCode);
        }
        if (this.className != null) {
            classEntity.setClassName(this.className);
        }
        if (this.academicYear != null) {
            classEntity.setAcademicYear(this.academicYear);
        }
        if (this.homeroomTeacher != null) {
            classEntity.setHomeroomTeacher(this.homeroomTeacher);
        }
        if (this.isActive != null) {
            classEntity.setIsActive(this.isActive);
        }
        // Note: departmentId và classManagerId sẽ được xử lý trong Service layer
    }
}
