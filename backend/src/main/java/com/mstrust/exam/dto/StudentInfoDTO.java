package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO chứa thông tin cơ bản của sinh viên
 * @author: K24DTCN210-NVMANH (20/11/2025 11:16)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfoDTO {
    
    /* ---------------------------------------------------
     * ID của sinh viên
     * --------------------------------------------------- */
    private Long id;
    
    /* ---------------------------------------------------
     * Mã sinh viên
     * --------------------------------------------------- */
    private String studentCode;
    
    /* ---------------------------------------------------
     * Tên đầy đủ của sinh viên
     * --------------------------------------------------- */
    private String fullName;
    
    /* ---------------------------------------------------
     * Email của sinh viên
     * --------------------------------------------------- */
    private String email;
}
