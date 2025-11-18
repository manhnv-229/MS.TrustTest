package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO để trả về thông tin Subject cho client
 * @author NVMANH with Cline
 * @created 15/11/2025 14:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO {

    private Long id;
    private String subjectCode;
    private String subjectName;
    private String description;
    private Integer credits;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version;

    /** ------------------------------------------
     * Mục đích: Chuyển đổi từ Subject entity sang SubjectDTO
     * @param entity Subject entity cần chuyển đổi
     * @return SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:15
     */
    public static SubjectDTO fromEntity(Subject entity) {
        if (entity == null) {
            return null;
        }

        return SubjectDTO.builder()
                .id(entity.getId())
                .subjectCode(entity.getSubjectCode())
                .subjectName(entity.getSubjectName())
                .description(entity.getDescription())
                .credits(entity.getCredits())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();
    }
}
