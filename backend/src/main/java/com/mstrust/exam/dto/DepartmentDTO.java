package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho Department entity
 * Dùng để trả về thông tin khoa cho client
 * @author: K24DTCN210-NVMANH (14/11/2025 14:08)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;
    private String headOfDepartment;
    private String phone;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    /* ---------------------------------------------------
     * Chuyển đổi từ Department entity sang DepartmentDTO
     * @param department Department entity
     * @returns DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:08)
     * --------------------------------------------------- */
    public static DepartmentDTO fromEntity(com.mstrust.exam.entity.Department department) {
        if (department == null) {
            return null;
        }
        
        return DepartmentDTO.builder()
                .id(department.getId())
                .departmentCode(department.getDepartmentCode())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .headOfDepartment(department.getHeadOfDepartment())
                .phone(department.getPhone())
                .email(department.getEmail())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .createdBy(department.getCreatedBy())
                .updatedBy(department.getUpdatedBy())
                .build();
    }
}
