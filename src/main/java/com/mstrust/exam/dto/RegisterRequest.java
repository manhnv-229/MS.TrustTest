package com.mstrust.exam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/* ---------------------------------------------------
 * DTO cho register request
 * Validate input khi tạo user mới
 * @author: K24DTCN210-NVMANH (13/11/2025 14:59)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @Size(max = 20, message = "Student code must not exceed 20 characters")
    private String studentCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private LocalDate dateOfBirth;

    private String gender; // MALE, FEMALE, OTHER

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private Long departmentId;

    private Long classId;
}
