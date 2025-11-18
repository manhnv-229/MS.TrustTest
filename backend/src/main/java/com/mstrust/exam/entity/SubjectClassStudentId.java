package com.mstrust.exam.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/** ------------------------------------------
 * Mục đích: Composite key cho relationship Many-to-Many giữa SubjectClass và Student
 * Sử dụng @Embeddable để nhúng vào entity SubjectClassStudent
 * @author NVMANH with Cline
 * @created 15/11/2025 14:23
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassStudentId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** ------------------------------------------
     * ID của lớp học phần (SubjectClass)
     */
    private Long subjectClassId;
    
    /** ------------------------------------------
     * ID của sinh viên (User)
     */
    private Long studentId;
    
    /** ------------------------------------------
     * Mục đích: Override equals để so sánh composite key
     * @param o - Object cần so sánh
     * @return boolean - true nếu bằng nhau
     * @author NVMANH with Cline
     * @created 15/11/2025 14:23
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectClassStudentId that = (SubjectClassStudentId) o;
        return Objects.equals(subjectClassId, that.subjectClassId) &&
               Objects.equals(studentId, that.studentId);
    }
    
    /** ------------------------------------------
     * Mục đích: Override hashCode để sử dụng trong collections
     * @return int - hash code của composite key
     * @author NVMANH with Cline
     * @created 15/11/2025 14:23
     */
    @Override
    public int hashCode() {
        return Objects.hash(subjectClassId, studentId);
    }
}
