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

/** ------------------------------------------
 * Mục đích: Entity mapping cho bảng subjects trong database
 * Quản lý thông tin môn học/học phần
 * @author NVMANH with Cline
 * @created 15/11/2025 14:14
 * ------------------------------------------ */
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

    @Column(name = "subject_code", nullable = false, unique = true, length = 20)
    @Size(max = 20)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false)
    @Size(max = 255)
    private String subjectName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "credits")
    @Min(0)
    @Builder.Default
    private Integer credits = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Version
    private Long version;

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

    /** ------------------------------------------
     * Mục đích: Kiểm tra môn học có bị xóa mềm không
     * @return true nếu môn học đã bị xóa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     * ------------------------------------------ */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /** ------------------------------------------
     * Mục đích: Đánh dấu môn học là đã xóa (soft delete)
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     * ------------------------------------------ */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
