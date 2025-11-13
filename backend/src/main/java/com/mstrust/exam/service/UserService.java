package com.mstrust.exam.service;

import com.mstrust.exam.dto.ChangePasswordRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * User Service
 * Xử lý CRUD operations cho User
 * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
 * --------------------------------------------------- */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ---------------------------------------------------
     * Lấy tất cả users (không bao gồm deleted)
     * @returns List of UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Lấy users với pagination
     * @param pageable Pageable object
     * @returns Page of UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    public Page<UserDTO> getUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::from);
    }

    /* ---------------------------------------------------
     * Tìm user theo ID
     * @param id User ID
     * @returns UserDTO
     * @throws ResourceNotFoundException nếu không tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        
        return UserDTO.from(user);
    }

    /* ---------------------------------------------------
     * Tìm user theo student code
     * @param studentCode Student code
     * @returns UserDTO
     * @throws ResourceNotFoundException nếu không tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    public UserDTO getUserByStudentCode(String studentCode) {
        User user = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("User", "studentCode", studentCode));
        
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "studentCode", studentCode);
        }
        
        return UserDTO.from(user);
    }

    /* ---------------------------------------------------
     * Tìm user theo email
     * @param email Email
     * @returns UserDTO
     * @throws ResourceNotFoundException nếu không tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "email", email);
        }
        
        return UserDTO.from(user);
    }

    /* ---------------------------------------------------
     * Update user information
     * @param id User ID
     * @param userDTO UserDTO với thông tin mới
     * @returns UserDTO đã update
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Update fields (không update password ở đây, có method riêng)
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userDTO.getDateOfBirth());
        }
        if (userDTO.getGender() != null) {
            user.setGender(User.Gender.valueOf(userDTO.getGender()));
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        if (userDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Soft delete user
     * @param id User ID
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);
    }

    /* ---------------------------------------------------
     * Change user password
     * @param id User ID
     * @param request ChangePasswordRequest
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @throws BadRequestException nếu old password sai hoặc new password không match
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Verify new password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /* ---------------------------------------------------
     * Activate/Deactivate user account
     * @param id User ID
     * @param isActive true để activate, false để deactivate
     * @returns UserDTO đã update
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO setUserActive(Long id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }
}
