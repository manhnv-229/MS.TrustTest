package com.mstrust.exam.service;

import com.mstrust.exam.dto.LoginRequest;
import com.mstrust.exam.dto.LoginResponse;
import com.mstrust.exam.dto.RegisterRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.entity.Role;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.InvalidCredentialsException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.RoleRepository;
import com.mstrust.exam.repository.UserRepository;
import com.mstrust.exam.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/* ---------------------------------------------------
 * Authentication Service
 * Xử lý login, register, token validation
 * @author: K24DTCN210-NVMANH (13/11/2025 15:01)
 * --------------------------------------------------- */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    /* ---------------------------------------------------
     * Login user và generate JWT tokens
     * @param request LoginRequest chứa username và password
     * @returns LoginResponse với JWT token và user info
     * @throws InvalidCredentialsException nếu credentials sai
     * @author: K24DTCN210-NVMANH (13/11/2025 15:01)
     * --------------------------------------------------- */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Tìm user theo username (có thể là student_code, email hoặc phone)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Kiểm tra account active
        if (!user.getIsActive() || user.isDeleted()) {
            throw new InvalidCredentialsException("Account is inactive or deleted");
        }

        // Kiểm tra account locked
        if (user.isAccountLocked()) {
            throw new InvalidCredentialsException("Account is locked due to multiple failed login attempts");
        }

        // Authenticate với Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Update last login time và reset failed attempts (không dùng save() để tránh auditing conflict)
        userRepository.updateLastLogin(user.getId());

        // Generate tokens
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new LoginResponse(token, refreshToken, UserDTO.from(user));
    }

    /* ---------------------------------------------------
     * Register new user
     * @param request RegisterRequest chứa thông tin user mới
     * @returns UserDTO của user vừa tạo
     * @throws DuplicateResourceException nếu email/student_code/phone đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 15:01)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Validate duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Validate duplicate student code
        if (request.getStudentCode() != null && userRepository.existsByStudentCode(request.getStudentCode())) {
            throw new DuplicateResourceException("User", "studentCode", request.getStudentCode());
        }

        // Validate duplicate phone
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("User", "phoneNumber", request.getPhoneNumber());
        }

        // Create new user
        User user = new User();
        user.setStudentCode(request.getStudentCode());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        
        if (request.getGender() != null) {
            user.setGender(User.Gender.valueOf(request.getGender()));
        }
        
        user.setAddress(request.getAddress());
        user.setIsActive(true);

        // Assign default STUDENT role
        Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleName", "ROLE_STUDENT"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);

        return UserDTO.from(savedUser);
    }

    /* ---------------------------------------------------
     * Validate JWT token
     * @param token JWT token cần validate
     * @returns true nếu token hợp lệ
     * @author: K24DTCN210-NVMANH (13/11/2025 15:01)
     * --------------------------------------------------- */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /* ---------------------------------------------------
     * Refresh JWT token
     * @param refreshToken Refresh token
     * @returns LoginResponse với token mới
     * @throws InvalidCredentialsException nếu refresh token không hợp lệ
     * @author: K24DTCN210-NVMANH (13/11/2025 15:01)
     * --------------------------------------------------- */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String newToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new LoginResponse(newToken, newRefreshToken, UserDTO.from(user));
    }
}
