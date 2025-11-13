package com.mstrust.exam.entity;

import jakarta.persistence.*;
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
 * Entity mapping cho bảng classes trong database
 * Quản lý thông tin lớp hành chính
 * Tên class là ClassEntity vì Class là từ khóa Java
 * @author: K24DTCN210-NVMANH (13/11/2025 14:51)
 * --------------------------------------------------- */
@Entity
@Table(name = "classes")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", nullable = false, unique = true, length = 20)
    @Size(max = 20)
    private String classCode;

    @Column(name = "class_name", nullable = false, length = 100)
    @Size(max = 100)
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "homeroom_teacher", length = 100)
    private String homeroomTeacher;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<User> students = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    /* ---------------------------------------------------
     * Kiểm tra lớp có bị xóa mềm không
     * @returns true nếu lớp đã bị xóa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
     * --------------------------------------------------- */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /* ---------------------------------------------------
     * Đánh dấu lớp là đã xóa (soft delete)
     * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
     * --------------------------------------------------- */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }
}
