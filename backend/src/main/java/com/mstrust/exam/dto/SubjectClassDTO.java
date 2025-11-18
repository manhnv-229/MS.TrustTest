package com.mstrust.exam.dto;

import com.mstrust.exam.entity.SubjectClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho SubjectClass entity (Lớp học phần)
 * @author NVMANH with Cline
 * @created 15/11/2025 14:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassDTO {
    
    /** ------------------------------------------
     * ID của lớp học phần
     */
    private Long id;
    
    /** ------------------------------------------
     * Mã lớp học phần (unique)
     */
    private String code;
    
    /** ------------------------------------------
     * ID của môn học
     */
    private Long subjectId;
    
    /** ------------------------------------------
     * Thông tin chi tiết môn học
     */
    private SubjectDTO subject;
    
    /** ------------------------------------------
     * Học kỳ (format: "YYYY-YYYY-N")
     */
    private String semester;
    
    /** ------------------------------------------
     * ID của giáo viên phụ trách
     */
    private Long teacherId;
    
    /** ------------------------------------------
     * Thông tin chi tiết giáo viên
     */
    private UserDTO teacher;
    
    /** ------------------------------------------
     * Lịch học
     */
    private String schedule;
    
    /** ------------------------------------------
     * Số lượng sinh viên tối đa
     */
    private Integer maxStudents;
    
    /** ------------------------------------------
     * Số lượng sinh viên đã đăng ký (status = ENROLLED)
     */
    private Integer enrolledCount;
    
    /** ------------------------------------------
     * Số chỗ còn trống
     */
    private Integer availableSlots;
    
    /** ------------------------------------------
     * Version cho optimistic locking
     */
    private Long version;
    
    /** ------------------------------------------
     * Thời điểm tạo
     */
    private LocalDateTime createdAt;
    
    /** ------------------------------------------
     * Thời điểm cập nhật
     */
    private LocalDateTime updatedAt;
    
    /** ------------------------------------------
     * ID người tạo
     */
    private Long createdBy;
    
    /** ------------------------------------------
     * ID người cập nhật
     */
    private Long updatedBy;
    
    /** ------------------------------------------
     * Mục đích: Convert từ SubjectClass entity sang DTO
     * @param entity - SubjectClass entity
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:29
     */
    public static SubjectClassDTO fromEntity(SubjectClass entity) {
        if (entity == null) {
            return null;
        }
        
        return SubjectClassDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subject(entity.getSubject() != null ? SubjectDTO.fromEntity(entity.getSubject()) : null)
                .semester(entity.getSemester())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacher(entity.getTeacher() != null ? UserDTO.from(entity.getTeacher()) : null)
                .schedule(entity.getSchedule())
                .maxStudents(entity.getMaxStudents())
                .enrolledCount(entity.getEnrolledCount())
                .availableSlots(entity.getAvailableSlots())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
    
    /** ------------------------------------------
     * Mục đích: Convert sang DTO với minimal info (không load related entities)
     * Dùng cho performance khi không cần thông tin chi tiết subject/teacher
     * @param entity - SubjectClass entity
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:29
     */
    public static SubjectClassDTO fromEntityMinimal(SubjectClass entity) {
        if (entity == null) {
            return null;
        }
        
        return SubjectClassDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .semester(entity.getSemester())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .schedule(entity.getSchedule())
                .maxStudents(entity.getMaxStudents())
                .enrolledCount(entity.getEnrolledCount())
                .availableSlots(entity.getAvailableSlots())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
