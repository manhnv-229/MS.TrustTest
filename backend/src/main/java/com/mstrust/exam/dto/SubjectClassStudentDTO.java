package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubjectClassStudent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho thông tin sinh viên đã enroll vào lớp học phần
 * @author NVMANH with Cline
 * @created 15/11/2025 14:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassStudentDTO {
    
    /** ------------------------------------------
     * ID của lớp học phần
     */
    private Long subjectClassId;
    
    /** ------------------------------------------
     * ID của sinh viên
     */
    private Long studentId;
    
    /** ------------------------------------------
     * Thông tin chi tiết sinh viên
     */
    private UserDTO student;
    
    /** ------------------------------------------
     * Thời điểm đăng ký
     */
    private LocalDateTime enrolledAt;
    
    /** ------------------------------------------
     * Trạng thái: ENROLLED, DROPPED, COMPLETED
     */
    private String status;
    
    /** ------------------------------------------
     * Mục đích: Convert từ SubjectClassStudent entity sang DTO
     * @param entity - SubjectClassStudent entity
     * @return SubjectClassStudentDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:28
     */
    public static SubjectClassStudentDTO fromEntity(SubjectClassStudent entity) {
        if (entity == null) {
            return null;
        }
        
        return SubjectClassStudentDTO.builder()
                .subjectClassId(entity.getId().getSubjectClassId())
                .studentId(entity.getId().getStudentId())
                .student(entity.getStudent() != null ? UserDTO.from(entity.getStudent()) : null)
                .enrolledAt(entity.getEnrolledAt())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .build();
    }
}
