package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ClassEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO để trả về thông tin Class cho client
 * @author NVMANH with Cline
 * @created 15/11/2025 13:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDTO {

    private Long id;
    private String classCode;
    private String className;
    private Long departmentId;
    private String departmentName;
    private String academicYear;
    private String homeroomTeacher;
    private Long classManagerId;
    private Boolean isActive;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version;

    /** ------------------------------------------
     * Mục đích: Chuyển đổi từ ClassEntity sang ClassDTO
     * @param entity ClassEntity cần chuyển đổi
     * @return ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 13:55
     */
    public static ClassDTO fromEntity(ClassEntity entity) {
        if (entity == null) {
            return null;
        }

        return ClassDTO.builder()
                .id(entity.getId())
                .classCode(entity.getClassCode())
                .className(entity.getClassName())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null)
                .academicYear(entity.getAcademicYear())
                .homeroomTeacher(entity.getHomeroomTeacher())
                .isActive(entity.getIsActive())
                .studentCount(entity.getStudents() != null ? entity.getStudents().size() : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();
    }
}
