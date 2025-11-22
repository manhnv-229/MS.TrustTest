package com.mstrust.exam.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO đơn giản cho User entity (dùng trong GradingDetailDTO)
 * @author: K24DTCN210-NVMANH (21/11/2025 14:56)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String studentCode;
}
