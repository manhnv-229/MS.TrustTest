package com.mstrust.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/* ---------------------------------------------------
 * Entity mapping cho bảng users trong database
 * Quản lý thông tin người dùng hệ thống với multi-login support (student_code/email/phone)
 * Hỗ trợ soft delete, account locking sau 5 lần login failed
 * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
 * --------------------------------------------------- */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", unique = true, length = 20)
    @Size(max = 20)
    private String studentCode;

    @Column(nullable = false, unique = true, length = 100)
    @Email
    @Size(max = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 20)
    @Pattern(regexp = "^[0-9+\\-\\s()]*$")
    @Size(max = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    @Size(max = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 255)
    private String address;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

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

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    /* ---------------------------------------------------
     * Kiểm tra user có bị xóa mềm không
     * @returns true nếu user đã bị xóa, false nếu còn active
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /* ---------------------------------------------------
     * Đánh dấu user là đã xóa (soft delete)
     * Set deletedAt = now và isActive = false
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /* ---------------------------------------------------
     * Kiểm tra tài khoản có đang bị khóa không
     * @returns true nếu tài khoản đang bị khóa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /* ---------------------------------------------------
     * Tăng số lần đăng nhập thất bại
     * Nếu >= 5 lần thì khóa tài khoản 30 phút
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /* ---------------------------------------------------
     * Reset số lần đăng nhập thất bại về 0
     * Mở khóa tài khoản nếu đang bị khóa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /* ---------------------------------------------------
     * Cập nhật thời gian đăng nhập gần nhất
     * @author: K24DTCN210-NVMANH (13/11/2025 14:48)
     * --------------------------------------------------- */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
