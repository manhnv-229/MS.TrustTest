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
 * Entity mapping cho bảng subjects trong database
 * Quản lý thông tin môn học
 * @author: K24DTCN210-NVMANH (14/11/2025 13:58)
 * --------------------------------------------------- */
@Entity
@Table(name = "subjects")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    @Size(max = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    @Size(max = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "credits")
    @Min(0)
    @Builder.Default
    private Integer credits = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubjectClass> subjectClasses = new ArrayList<>();

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
     * Kiểm tra môn học có bị xóa mềm không
     * @returns true nếu môn học đã bị xóa
     * @author: K24DTCN210-NVMANH (14/11/2025 13:58)
     * --------------------------------------------------- */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /* ---------------------------------------------------
     * Đánh dấu môn học là đã xóa (soft delete)
     * @author: K24DTCN210-NVMANH (14/11/2025 13:58)
     * --------------------------------------------------- */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
