package com.mstrust.exam.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request cập nhật SubjectClass
 * @author NVMANH with Cline
 * @created 15/11/2025 14:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubjectClassRequest {
    
    /** ------------------------------------------
     * Mã lớp học phần (unique, optional khi update)
     * Format: MATH101-2023-2024-1
     */
    @Size(max = 20, message = "Mã lớp học phần không được vượt quá 20 ký tự")
    @Pattern(
        regexp = "^[A-Z0-9]+-\\d{4}-\\d{4}-[123]$",
        message = "Mã lớp học phần phải theo format: CODE-YYYY-YYYY-N (ví dụ: MATH101-2023-2024-1)"
    )
    private String code;
    
    /** ------------------------------------------
     * ID của môn học (optional khi update)
     */
    @Positive(message = "ID môn học phải là số dương")
    private Long subjectId;
    
    /** ------------------------------------------
     * Học kỳ (optional khi update)
     * Format: "YYYY-YYYY-N" (N = 1, 2, hoặc 3)
     */
    @Size(max = 20, message = "Học kỳ không được vượt quá 20 ký tự")
    @Pattern(
        regexp = "^\\d{4}-\\d{4}-[123]$",
        message = "Học kỳ phải theo format: YYYY-YYYY-N (ví dụ: 2023-2024-1)"
    )
    private String semester;
    
    /** ------------------------------------------
     * ID của giáo viên phụ trách (optional khi update)
     */
    @Positive(message = "ID giáo viên phải là số dương")
    private Long teacherId;
    
    /** ------------------------------------------
     * Lịch học (optional)
     * Ví dụ: "Thứ 2: 7h-9h, Phòng A101; Thứ 4: 13h-15h, Phòng B202"
     */
    @Size(max = 500, message = "Lịch học không được vượt quá 500 ký tự")
    private String schedule;
    
    /** ------------------------------------------
     * Số lượng sinh viên tối đa (optional)
     * Lưu ý: Không được nhỏ hơn số sinh viên đã enrolled
     */
    @Positive(message = "Số lượng sinh viên tối đa phải là số dương")
    @Max(value = 200, message = "Số lượng sinh viên tối đa không được vượt quá 200")
    private Integer maxStudents;
}
