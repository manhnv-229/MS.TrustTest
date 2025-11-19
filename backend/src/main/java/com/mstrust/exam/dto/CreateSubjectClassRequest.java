package com.mstrust.exam.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request tạo mới SubjectClass
 * @author NVMANH with Cline
 * @created 15/11/2025 14:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubjectClassRequest {
    
    /** ------------------------------------------
     * Mã lớp học phần (unique, required)
     * Có thể nhập tự do, miễn là không trùng
     */
    @NotBlank(message = "Mã lớp học phần không được để trống")
    @Size(max = 20, message = "Mã lớp học phần không được vượt quá 20 ký tự")
    private String code;
    
    /** ------------------------------------------
     * ID của môn học (required)
     */
    @NotNull(message = "ID môn học không được để trống")
    @Positive(message = "ID môn học phải là số dương")
    private Long subjectId;
    
    /** ------------------------------------------
     * Học kỳ (required)
     * Format: "YYYY-YYYY-N" (N = 1, 2, hoặc 3)
     */
    @NotBlank(message = "Học kỳ không được để trống")
    @Size(max = 20, message = "Học kỳ không được vượt quá 20 ký tự")
    @Pattern(
        regexp = "^\\d{4}-\\d{4}-[123]$",
        message = "Học kỳ phải theo format: YYYY-YYYY-N (ví dụ: 2023-2024-1)"
    )
    private String semester;
    
    /** ------------------------------------------
     * ID của giáo viên phụ trách (required)
     */
    @NotNull(message = "ID giáo viên không được để trống")
    @Positive(message = "ID giáo viên phải là số dương")
    private Long teacherId;
    
    /** ------------------------------------------
     * Lịch học (optional)
     * Ví dụ: "Thứ 2: 7h-9h, Phòng A101; Thứ 4: 13h-15h, Phòng B202"
     */
    @Size(max = 500, message = "Lịch học không được vượt quá 500 ký tự")
    private String schedule;
    
    /** ------------------------------------------
     * Số lượng sinh viên tối đa (optional, default = 50)
     */
    @Positive(message = "Số lượng sinh viên tối đa phải là số dương")
    @Max(value = 200, message = "Số lượng sinh viên tối đa không được vượt quá 200")
    private Integer maxStudents;
}
