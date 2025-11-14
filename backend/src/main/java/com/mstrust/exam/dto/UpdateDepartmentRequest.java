package com.mstrust.exam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho request cập nhật Department
 * Tất cả các field đều optional (cho phép update từng phần)
 * @author: K24DTCN210-NVMANH (14/11/2025 14:09)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDepartmentRequest {

    @Size(max = 20, message = "Mã khoa không được vượt quá 20 ký tự")
    private String departmentCode;

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

    private Boolean isActive;

    /* ---------------------------------------------------
     * Cập nhật các field của Department entity từ request
     * Chỉ update các field không null
     * @param department Department entity cần update
     * @author: K24DTCN210-NVMANH (14/11/2025 14:09)
     * --------------------------------------------------- */
    public void updateEntity(com.mstrust.exam.entity.Department department) {
        if (this.departmentCode != null) {
            department.setDepartmentCode(this.departmentCode);
        }
        if (this.departmentName != null) {
            department.setDepartmentName(this.departmentName);
        }
        if (this.description != null) {
            department.setDescription(this.description);
        }
        if (this.headOfDepartment != null) {
            department.setHeadOfDepartment(this.headOfDepartment);
        }
        if (this.phone != null) {
            department.setPhone(this.phone);
        }
        if (this.email != null) {
            department.setEmail(this.email);
        }
        if (this.isActive != null) {
            department.setIsActive(this.isActive);
        }
    }
}
