package com.mstrust.client.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để tạo mới môn học
 * @author: K24DTCN210-NVMANH (26/11/2025 01:46)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {
    private String subjectCode;     // Mã môn học (required, max 20, unique)
    private String subjectName;     // Tên môn học (required, max 255)
    private String description;     // Mô tả (optional)
    private Integer credits;        // Số tín chỉ (required, >= 0)
    private Long departmentId;      // ID khoa (required)
}
