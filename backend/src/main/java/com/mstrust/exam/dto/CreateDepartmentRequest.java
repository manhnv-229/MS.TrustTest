package com.mstrust.exam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho request tạo mới Department
 * Chứa validation rules cho các field bắt buộc
 * @author: K24DTCN210-NVMANH (14/11/2025 14:09)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepartmentRequest {

    @NotBlank(message = "Mã khoa không được để trống")
    @Size(max = 20, message = "Mã khoa không được vượt quá 20 ký tự")
    private String departmentCode;

    @NotBlank(message = "Tên khoa không được để trống")
    @Size(max = 100, message = "Tên khoa không được vượt quá 100 ký tự")
    private String departmentName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Size(max = 100, message = "Tên trưởng khoa không được vượt quá 100 ký tự")
    private String headOfDepartment;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Builder.Default
    private Boolean isActive = true;

    /* ---------------------------------------------------
     * Chuyển đổi request sang Department entity
     * @returns Department entity (chưa có ID)
     * @author: K24DTCN210-NVMANH (14/11/2025 14:09)
     * --------------------------------------------------- */
    public com.mstrust.exam.entity.Department toEntity() {
        return com.mstrust.exam.entity.Department.builder()
                .departmentCode(this.departmentCode)
                .departmentName(this.departmentName)
                .description(this.description)
                .headOfDepartment(this.headOfDepartment)
                .phone(this.phone)
                .email(this.email)
                .isActive(this.isActive != null ? this.isActive : true)
                .build();
    }
}
