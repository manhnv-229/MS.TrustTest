package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: Entity cho bảng trung gian subject_class_students (N:M relationship)
 * Quản lý việc đăng ký sinh viên vào lớp học phần
 * @author NVMANH with Cline
 * @created 15/11/2025 14:24
 */
@Entity
@Table(name = "subject_class_students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassStudent {
    
    /** ------------------------------------------
     * Composite primary key (subject_class_id + student_id)
     */
    @EmbeddedId
    private SubjectClassStudentId id;
    
    /** ------------------------------------------
     * Reference đến SubjectClass entity
     * Sử dụng @MapsId để map với subjectClassId trong composite key
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectClassId")
    @JoinColumn(name = "subject_class_id", nullable = false)
    private SubjectClass subjectClass;
    
    /** ------------------------------------------
     * Reference đến User entity (Student)
     * Sử dụng @MapsId để map với studentId trong composite key
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    /** ------------------------------------------
     * Thời điểm sinh viên đăng ký vào lớp
     */
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;
    
    /** ------------------------------------------
     * Trạng thái đăng ký: ENROLLED (đang học), DROPPED (đã rút), COMPLETED (đã hoàn thành)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status;
    
    /** ------------------------------------------
     * Enum định nghĩa các trạng thái đăng ký
     */
    public enum EnrollmentStatus {
        ENROLLED,   // Đang học
        DROPPED,    // Đã rút môn
        COMPLETED   // Đã hoàn thành
    }
    
    /** ------------------------------------------
     * Mục đích: Tự động set enrolledAt trước khi persist
     * @author NVMANH with Cline
     * @created 15/11/2025 14:24
     */
    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EnrollmentStatus.ENROLLED;
        }
    }
}
