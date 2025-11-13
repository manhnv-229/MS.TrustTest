package com.mstrust.exam.entity;

import jakarta.persistence.*;
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

/* ---------------------------------------------------
 * Entity mapping cho bảng roles trong database
 * Quản lý các vai trò trong hệ thống (STUDENT, TEACHER, CLASS_MANAGER, DEPT_MANAGER, ADMIN)
 * @author: K24DTCN210-NVMANH (13/11/2025 14:47)
 * --------------------------------------------------- */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Audit fields
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
     * Kiểm tra role có bị xóa mềm không
     * @returns true nếu role đã bị xóa, false nếu còn active
     * @author: K24DTCN210-NVMANH (13/11/2025 14:47)
     * --------------------------------------------------- */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /* ---------------------------------------------------
     * Đánh dấu role là đã xóa (soft delete)
     * @author: K24DTCN210-NVMANH (13/11/2025 14:47)
     * --------------------------------------------------- */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }
}
