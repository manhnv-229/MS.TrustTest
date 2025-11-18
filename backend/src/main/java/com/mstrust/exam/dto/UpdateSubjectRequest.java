package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Subject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ------------------------------------------
 * Mục đích: DTO cho request cập nhật Subject
 * Tất cả các field đều optional (cho phép update từng phần)
 * @author NVMANH with Cline
 * @created 15/11/2025 14:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubjectRequest {

    @Size(max = 20, message = "Mã môn học không được vượt quá 20 ký tự")
    private String subjectCode;

    @Size(max = 255, message = "Tên môn học không được vượt quá 255 ký tự")
    private String subjectName;

    private String description;

    @Min(value = 0, message = "Số tín chỉ phải >= 0")
    private Integer credits;

    private Long departmentId;

    /** ------------------------------------------
     * Mục đích: Cập nhật các field của Subject entity từ request
     * Chỉ update các field không null
     * @param subject Subject entity cần update
     * @author NVMANH with Cline
     * @created 15/11/2025 14:15
     */
    public void updateEntity(Subject subject) {
        if (this.subjectCode != null) {
            subject.setSubjectCode(this.subjectCode);
        }
        if (this.subjectName != null) {
            subject.setSubjectName(this.subjectName);
        }
        if (this.description != null) {
            subject.setDescription(this.description);
        }
        if (this.credits != null) {
            subject.setCredits(this.credits);
        }
        // Note: departmentId sẽ được xử lý trong Service layer
    }
}
