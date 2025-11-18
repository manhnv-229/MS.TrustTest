package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** ----------------------------------------------------------------
 * Mục đích: DTO chứa thống kê về users trong hệ thống
 * Bao gồm: total users, count by role, count by department, active/inactive
 * @author NVMANH with Cline
 * @created 15/11/2025 14:58
 * ---------------------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsDTO {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long deletedUsers;
    
    // Map<RoleName, Count>
    private Map<String, Long> usersByRole;
    
    // Map<DepartmentName, Count>
    private Map<String, Long> usersByDepartment;
    
    // Map<ClassName, Count>
    private Map<String, Long> usersByClass;
    
    // Map<Gender, Count>
    private Map<String, Long> usersByGender;
}
