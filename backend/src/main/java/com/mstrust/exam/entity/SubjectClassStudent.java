package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Entity mapping cho bảng subject_class_students trong database
 * Quản lý quan hệ many-to-many giữa SubjectClass và Student (User)
 * Lưu thông tin đăng ký lớp môn học của sinh viên
 * Sử dụng composite primary key (subject_class_id, student_id)
 * @author: K24DTCN210-NVMANH (14/11/2025 14:00)
 * EditBy: K24DTCN210-NVMANH (14/11/2025 14:21) - Fixed composite key to match database schema
 * --------------------------------------------------- */
@Entity
@Table(name = "subject_class_students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(SubjectClassStudent.SubjectClassStudentId.class)
public class SubjectClassStudent {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_class_id", nullable = false)
    private SubjectClass subjectClass;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    /* ---------------------------------------------------
     * Enum trạng thái đăng ký lớp môn học
     * ENROLLED: Đang học
     * DROPPED: Đã hủy đăng ký
     * COMPLETED: Đã hoàn thành
     * @author: K24DTCN210-NVMANH (14/11/2025 14:00)
     * --------------------------------------------------- */
    public enum EnrollmentStatus {
        ENROLLED,    // Đang học
        DROPPED,     // Đã hủy đăng ký
        COMPLETED    // Đã hoàn thành
    }

    /* ---------------------------------------------------
     * Kiểm tra sinh viên có đang học lớp này không
     * @returns true nếu đang học
     * @author: K24DTCN210-NVMANH (14/11/2025 14:00)
     * --------------------------------------------------- */
    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED;
    }

    /* ---------------------------------------------------
     * Đánh dấu đã hoàn thành lớp môn học
     * @author: K24DTCN210-NVMANH (14/11/2025 14:00)
     * --------------------------------------------------- */
    public void markAsCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    /* ---------------------------------------------------
     * Hủy đăng ký lớp môn học
     * @author: K24DTCN210-NVMANH (14/11/2025 14:00)
     * --------------------------------------------------- */
    public void markAsDropped() {
        this.status = EnrollmentStatus.DROPPED;
    }

    /* ---------------------------------------------------
     * Composite Primary Key class cho SubjectClassStudent
     * Sử dụng @IdClass để map với composite PK trong database
     * @author: K24DTCN210-NVMANH (14/11/2025 14:21)
     * --------------------------------------------------- */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectClassStudentId implements Serializable {
        private Long subjectClass;  // Tên phải match với field trong entity
        private Long student;       // Tên phải match với field trong entity

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubjectClassStudentId that = (SubjectClassStudentId) o;
            return subjectClass.equals(that.subjectClass) && student.equals(that.student);
        }

        @Override
        public int hashCode() {
            return subjectClass.hashCode() + student.hashCode();
        }
    }
}
