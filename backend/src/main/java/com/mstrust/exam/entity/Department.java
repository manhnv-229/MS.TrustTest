package com.mstrust.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
 * Entity mapping cho bảng departments trong database
 * Quản lý thông tin khoa/phòng ban trong trường
 * @author: K24DTCN210-NVMANH (13/11/2025 14:49)
 * --------------------------------------------------- */
@Entity
@Table(name = "departments")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_code", nullable = false, unique = true, length = 20)
    @Size(max = 20)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 100)
    @Size(max = 100)
    private String departmentName;

    @Column(length = 500)
    private String description;

    @Column(name = "head_of_department", length = 100)
    private String headOfDepartment;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    @Email
    private String email;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<ClassEntity> classes = new ArrayList<>();

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

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }
}
