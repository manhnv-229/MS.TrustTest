package com.mstrust.client.teacher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho Department - dùng để hiển thị trong ComboBox
 * @author: K24DTCN210-NVMANH (26/11/2025 01:45)
 * EditBy: K24DTCN210-NVMANH (26/11/2025 15:40) - Thêm @JsonIgnoreProperties để bỏ qua field headOfDepartment từ backend
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentDTO {
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;

    /* ---------------------------------------------------
     * Override toString để hiển thị tên khoa trong ComboBox
     * @return Tên khoa
     * @author: K24DTCN210-NVMANH (26/11/2025 01:45)
     * --------------------------------------------------- */
    @Override
    public String toString() {
        return departmentName != null ? departmentName : "";
    }

    /* ---------------------------------------------------
     * Tạo DepartmentDTO với tên khoa (dùng cho "Tất cả" option)
     * @param name Tên khoa
     * @return DepartmentDTO
     * @author: K24DTCN210-NVMANH (26/11/2025 01:45)
     * --------------------------------------------------- */
    public static DepartmentDTO allDepartments() {
        return DepartmentDTO.builder()
                .id(-1L)
                .departmentName("Tất cả khoa")
                .build();
    }
}
