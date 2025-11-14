package com.mstrust.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/* ---------------------------------------------------
 * Entity mapping cho bảng subject_classes trong database
 * Quản lý thông tin lớp môn học (một môn học có thể có nhiều lớp)
 * @author: K24DTCN210-NVMANH (14/11/2025 13:59)
 * --------------------------------------------------- */
@Entity
@Table(name = "subject_classes")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    @Size(max = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "semester", nullable = false, length = 20)
    @Size(max = 20)
    private String semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "schedule", length = 500)
    @Size(max = 500)
    private String schedule;

    @Column(name = "max_students")
    @Min(1)
    @Builder.Default
    private Integer maxStudents = 50;

    @OneToMany(mappedBy = "subjectClass", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubjectClassStudent> enrolledStudents = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    /* ---------------------------------------------------
     * Kiểm tra lớp môn học có bị xóa mềm không
     * @returns true nếu lớp môn học đã bị xóa
     * @author: K24DTCN210-NVMANH (14/11/2025 13:59)
     * --------------------------------------------------- */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /* ---------------------------------------------------
     * Đánh dấu lớp môn học là đã xóa (soft delete)
     * @author: K24DTCN210-NVMANH (14/11/2025 13:59)
     * --------------------------------------------------- */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /* ---------------------------------------------------
     * Kiểm tra lớp môn học có còn chỗ trống không
     * @returns true nếu còn chỗ trống
     * @author: K24DTCN210-NVMANH (14/11/2025 13:59)
     * --------------------------------------------------- */
    public boolean hasAvailableSlots() {
        long enrolledCount = enrolledStudents.stream()
                .filter(e -> e.getStatus() == SubjectClassStudent.EnrollmentStatus.ENROLLED)
                .count();
        return enrolledCount < maxStudents;
    }

    /* ---------------------------------------------------
     * Lấy số lượng sinh viên đã đăng ký
     * @returns Số lượng sinh viên đang học
     * @author: K24DTCN210-NVMANH (14/11/2025 13:59)
     * --------------------------------------------------- */
    public long getEnrolledCount() {
        return enrolledStudents.stream()
                .filter(e -> e.getStatus() == SubjectClassStudent.EnrollmentStatus.ENROLLED)
                .count();
    }
}
