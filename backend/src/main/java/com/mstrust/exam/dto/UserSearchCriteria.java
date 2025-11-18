package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ----------------------------------------------------------------
 * Mục đích: Criteria DTO để search users với multiple filters
 * Hỗ trợ search theo name, email, role, department, class, status
 * @author NVMANH with Cline
 * @created 15/11/2025 14:57
 * ---------------------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchCriteria {

    private String keyword;        // Search trong fullName, email, studentCode
    private String roleName;       // Filter theo role name
    private Long departmentId;     // Filter theo department
    private Long classId;          // Filter theo class
    private Boolean isActive;      // Filter theo active status
    private String gender;         // Filter theo gender
}
