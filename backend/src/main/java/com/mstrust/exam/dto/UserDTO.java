package com.mstrust.exam.dto;

import com.mstrust.exam.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * DTO cho User entity
 * Không bao gồm password_hash vì lý do bảo mật
 * @author: K24DTCN210-NVMANH (13/11/2025 14:58)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String studentCode;
    private String email;
    private String phoneNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String avatarUrl;
    private Long departmentId;
    private String departmentName;
    private Long classId;
    private String className;
    private Set<String> roles;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    /* ---------------------------------------------------
     * Convert từ User entity sang UserDTO
     * @param user User entity
     * @returns UserDTO object
     * @author: K24DTCN210-NVMANH (13/11/2025 14:58)
     * --------------------------------------------------- */
    public static UserDTO from(User user) {
        UserDTOBuilder builder = UserDTO.builder()
                .id(user.getId())
                .studentCode(user.getStudentCode())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt());

        if (user.getDepartment() != null) {
            builder.departmentId(user.getDepartment().getId())
                   .departmentName(user.getDepartment().getDepartmentName());
        }

        if (user.getClassEntity() != null) {
            builder.classId(user.getClassEntity().getId())
                   .className(user.getClassEntity().getClassName());
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            builder.roles(user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toSet()));
        }

        return builder.build();
    }
}
