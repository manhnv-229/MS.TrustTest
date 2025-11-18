package com.mstrust.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ----------------------------------------------------------------
 * Mục đích: Request DTO để assign/remove role từ user
 * @author NVMANH with Cline
 * @created 15/11/2025 14:56
 * ---------------------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequest {

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
