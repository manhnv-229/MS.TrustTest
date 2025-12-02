package com.mstrust.exam.service;

import com.mstrust.exam.dto.ChangePasswordRequest;
import com.mstrust.exam.dto.CreateUserRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.dto.UserSearchCriteria;
import com.mstrust.exam.dto.UserStatisticsDTO;
import com.mstrust.exam.entity.ClassEntity;
import com.mstrust.exam.entity.Department;
import com.mstrust.exam.entity.Role;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ClassRepository;
import com.mstrust.exam.repository.DepartmentRepository;
import com.mstrust.exam.repository.RoleRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * User Service
 * Xử lý CRUD operations cho User
 * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
 * --------------------------------------------------- */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ClassRepository classRepository;

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
     * Tạo user mới (Admin only)
     * @param request CreateUserRequest chứa thông tin user mới
     * @returns UserDTO của user vừa tạo
     * @throws DuplicateResourceException nếu email/student_code/phone đã tồn tại
     * @throws ResourceNotFoundException nếu department/class/role không tồn tại
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Validate duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email detected: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Validate duplicate student code
        if (request.getStudentCode() != null && !request.getStudentCode().trim().isEmpty()) {
            if (userRepository.existsByStudentCode(request.getStudentCode())) {
                log.warn("Duplicate student code detected: {}", request.getStudentCode());
                throw new DuplicateResourceException("User", "studentCode", request.getStudentCode());
            }
        }

        // Validate duplicate phone
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                log.warn("Duplicate phone number detected: {}", request.getPhoneNumber());
                throw new DuplicateResourceException("User", "phoneNumber", request.getPhoneNumber());
            }
        }

        // Create new user
        User user = new User();
        user.setStudentCode(request.getStudentCode() != null && !request.getStudentCode().trim().isEmpty() 
            ? request.getStudentCode().trim() : null);
        user.setEmail(request.getEmail().trim());
        user.setPhoneNumber(request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty() 
            ? request.getPhoneNumber().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid gender value: {}, ignoring", request.getGender());
            }
        }
        
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Assign department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found with ID: {}", request.getDepartmentId());
                        return new ResourceNotFoundException("Department", "id", request.getDepartmentId());
                    });
            user.setDepartment(department);
            log.debug("Assigned department: {} to user", department.getDepartmentName());
        }

        // Assign class
        if (request.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> {
                        log.error("Class not found with ID: {}", request.getClassId());
                        return new ResourceNotFoundException("Class", "id", request.getClassId());
                    });
            user.setClassEntity(classEntity);
            log.debug("Assigned class: {} to user", classEntity.getClassName());
        }

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                // Normalize role name (remove ROLE_ prefix if present, then add it)
                String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
                Role role = roleRepository.findByRoleName(normalizedRoleName)
                        .orElseThrow(() -> {
                            log.error("Role not found: {}", normalizedRoleName);
                            return new ResourceNotFoundException("Role", "roleName", normalizedRoleName);
                        });
                roles.add(role);
                log.debug("Assigned role: {} to user", normalizedRoleName);
            }
        } else {
            // Default to STUDENT role if no roles specified
            Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                    .orElseThrow(() -> {
                        log.error("Default STUDENT role not found");
                        return new ResourceNotFoundException("Role", "roleName", "ROLE_STUDENT");
                    });
            roles.add(studentRole);
            log.debug("Assigned default STUDENT role to user");
        }
        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        return UserDTO.from(savedUser);
    }

    /* ---------------------------------------------------
     * Toggle user active status (active <-> inactive)
     * @param id User ID
     * @returns UserDTO đã update
     * @author NVMANH with Cline (15/11/2025 17:21)
     * --------------------------------------------------- */
    public UserDTO toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);

        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Update user
     * @param id User ID
     * @param userDTO UserDTO với thông tin mới
     * @returns UserDTO đã update
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        log.debug("Update data - Email: {}, FullName: {}, Gender: {}, StudentCode: {}, PhoneNumber: {}, IsActive: {}, Roles: {}", 
            userDTO.getEmail(), userDTO.getFullName(), userDTO.getGender(), 
            userDTO.getStudentCode(), userDTO.getPhoneNumber(), userDTO.getIsActive(), userDTO.getRoles());
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        if (user.isDeleted()) {
            log.error("User with ID {} is deleted", id);
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Update fields (không update password ở đây, có method riêng)
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
            log.debug("Updated fullName: {}", userDTO.getFullName());
        }
        
        // Update email (cần validate duplicate, loại trừ user hiện tại)
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            // Tìm user khác có email này (loại trừ user hiện tại)
            User existingUser = userRepository.findByEmail(userDTO.getEmail()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(id)) {
                log.warn("Duplicate email detected: {} (belongs to user ID: {})", userDTO.getEmail(), existingUser.getId());
                throw new DuplicateResourceException("User", "email", userDTO.getEmail());
            }
            user.setEmail(userDTO.getEmail());
            log.debug("Updated email: {}", userDTO.getEmail());
        }
        
        // Update student code (cần validate duplicate, loại trừ user hiện tại)
        if (userDTO.getStudentCode() != null) {
            if (!userDTO.getStudentCode().equals(user.getStudentCode())) {
                // Tìm user khác có student code này (loại trừ user hiện tại)
                User existingUser = userRepository.findByStudentCode(userDTO.getStudentCode()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(id)) {
                    log.warn("Duplicate student code detected: {} (belongs to user ID: {})", userDTO.getStudentCode(), existingUser.getId());
                    throw new DuplicateResourceException("User", "studentCode", userDTO.getStudentCode());
                }
            }
            user.setStudentCode(userDTO.getStudentCode());
            log.debug("Updated studentCode: {}", userDTO.getStudentCode());
        } else {
            // Nếu gửi null thì xóa student code
            user.setStudentCode(null);
            log.debug("Cleared studentCode");
        }
        
        // Update date of birth - có thể cập nhật hoặc xóa
        if (userDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userDTO.getDateOfBirth());
            log.debug("Updated dateOfBirth: {}", userDTO.getDateOfBirth());
        } else {
            // Nếu gửi null thì xóa date of birth
            user.setDateOfBirth(null);
            log.debug("Cleared dateOfBirth");
        }
        
        if (userDTO.getGender() != null) {
            try {
                user.setGender(User.Gender.valueOf(userDTO.getGender().toUpperCase()));
                log.debug("Updated gender: {}", userDTO.getGender());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid gender value: {}, ignoring", userDTO.getGender());
            }
        } else {
            // Nếu gửi null thì xóa gender
            user.setGender(null);
            log.debug("Cleared gender");
        }
        
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
            log.debug("Updated address");
        } else {
            // Nếu gửi null thì xóa address
            user.setAddress(null);
            log.debug("Cleared address");
        }
        
        if (userDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
            log.debug("Updated avatarUrl");
        }
        
        if (userDTO.getPhoneNumber() != null) {
            // Validate duplicate phone number (loại trừ user hiện tại)
            if (!userDTO.getPhoneNumber().equals(user.getPhoneNumber())) {
                // Tìm user khác có phone number này (loại trừ user hiện tại)
                User existingUser = userRepository.findByPhoneNumber(userDTO.getPhoneNumber()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(id)) {
                    log.warn("Duplicate phone number detected: {} (belongs to user ID: {})", userDTO.getPhoneNumber(), existingUser.getId());
                    throw new DuplicateResourceException("User", "phoneNumber", userDTO.getPhoneNumber());
                }
            }
            user.setPhoneNumber(userDTO.getPhoneNumber());
            log.debug("Updated phoneNumber: {}", userDTO.getPhoneNumber());
        } else {
            // Nếu gửi null thì xóa phone number
            user.setPhoneNumber(null);
            log.debug("Cleared phoneNumber");
        }
        
        // Update isActive
        if (userDTO.getIsActive() != null) {
            user.setIsActive(userDTO.getIsActive());
            log.debug("Updated isActive: {}", userDTO.getIsActive());
        }
        
        // Update department - có thể cập nhật hoặc xóa
        if (userDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedAtIsNull(userDTO.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found with ID: {}", userDTO.getDepartmentId());
                        return new ResourceNotFoundException("Department", "id", userDTO.getDepartmentId());
                    });
            user.setDepartment(department);
            log.debug("Updated department: {} ({})", department.getDepartmentName(), department.getId());
        } else {
            // Nếu gửi null thì xóa department
            user.setDepartment(null);
            log.debug("Cleared department");
        }
        
        // Update class - có thể cập nhật hoặc xóa
        if (userDTO.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(userDTO.getClassId())
                    .orElseThrow(() -> {
                        log.error("Class not found with ID: {}", userDTO.getClassId());
                        return new ResourceNotFoundException("Class", "id", userDTO.getClassId());
                    });
            if (classEntity.getDeletedAt() != null) {
                log.error("Class with ID {} is deleted", userDTO.getClassId());
                throw new ResourceNotFoundException("Class", "id", userDTO.getClassId());
            }
            user.setClassEntity(classEntity);
            log.debug("Updated class: {} ({})", classEntity.getClassName(), classEntity.getId());
        } else {
            // Nếu gửi null thì xóa class
            user.setClassEntity(null);
            log.debug("Cleared class");
        }
        
        // Update roles - luôn cập nhật (kể cả khi empty list)
        if (userDTO.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            if (!userDTO.getRoles().isEmpty()) {
                for (String roleName : userDTO.getRoles()) {
                    // Normalize role name (remove ROLE_ prefix if present, then add it)
                    String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
                    Role role = roleRepository.findByRoleName(normalizedRoleName)
                            .orElseThrow(() -> {
                                log.error("Role not found: {}", normalizedRoleName);
                                return new ResourceNotFoundException("Role", "roleName", normalizedRoleName);
                            });
                    roles.add(role);
                    log.debug("Assigned role: {} to user", normalizedRoleName);
                }
            }
            // Nếu roles rỗng, set empty set (xóa tất cả roles)
            user.setRoles(roles);
            log.debug("Updated roles: {} (total: {})", userDTO.getRoles(), roles.size());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}, email: {}", updatedUser.getId(), updatedUser.getEmail());
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

    /* ---------------------------------------------------
     * Assign role cho user
     * @param userId User ID
     * @param roleId Role ID
     * @returns UserDTO đã được assign role
     * @throws ResourceNotFoundException nếu không tìm thấy user hoặc role
     * @throws BadRequestException nếu user đã có role này rồi
     * @author NVMANH with Cline (15/11/2025 15:02)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Check if user already has this role
        if (user.getRoles().contains(role)) {
            throw new BadRequestException("User already has this role");
        }

        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Remove role từ user
     * @param userId User ID
     * @param roleId Role ID
     * @returns UserDTO đã remove role
     * @throws ResourceNotFoundException nếu không tìm thấy user hoặc role
     * @throws BadRequestException nếu đây là role duy nhất của user
     * @author NVMANH with Cline (15/11/2025 15:03)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Check if this is the only role
        if (user.getRoles().size() == 1 && user.getRoles().contains(role)) {
            throw new BadRequestException("Cannot remove the only role from user");
        }

        // Check if user has this role
        if (!user.getRoles().contains(role)) {
            throw new BadRequestException("User does not have this role");
        }

        user.getRoles().remove(role);
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Lấy danh sách users theo role name
     * @param roleName Tên role
     * @returns List các UserDTO có role đó
     * @author NVMANH with Cline (15/11/2025 15:03)
     * --------------------------------------------------- */
    public List<UserDTO> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Assign user to department
     * @param userId User ID
     * @param departmentId Department ID
     * @returns UserDTO đã được assign department
     * @throws ResourceNotFoundException nếu không tìm thấy user hoặc department
     * @author NVMANH with Cline (15/11/2025 15:04)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO assignToDepartment(Long userId, Long departmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Department department = departmentRepository.findByIdAndDeletedAtIsNull(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        user.setDepartment(department);
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Assign user to class
     * @param userId User ID
     * @param classId Class ID
     * @returns UserDTO đã được assign class
     * @throws ResourceNotFoundException nếu không tìm thấy user hoặc class
     * @author NVMANH with Cline (15/11/2025 15:04)
     * --------------------------------------------------- */
    @Transactional
    public UserDTO assignToClass(Long userId, Long classId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

        if (classEntity.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Class", "id", classId);
        }

        user.setClassEntity(classEntity);
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    /* ---------------------------------------------------
     * Lấy danh sách users theo department
     * @param departmentId Department ID
     * @returns List các UserDTO trong department
     * @author NVMANH with Cline (15/11/2025 15:05)
     * --------------------------------------------------- */
    public List<UserDTO> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepository.findByDepartmentId(departmentId);
        return users.stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Lấy danh sách users theo class
     * @param classId Class ID
     * @returns List các UserDTO trong class
     * @author NVMANH with Cline (15/11/2025 15:05)
     * --------------------------------------------------- */
    public List<UserDTO> getUsersByClass(Long classId) {
        List<User> users = userRepository.findByClassId(classId);
        return users.stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Advanced search users với multiple criteria
     * @param criteria UserSearchCriteria với các filter
     * @returns List các UserDTO match criteria
     * @author NVMANH with Cline (15/11/2025 15:05)
     * --------------------------------------------------- */
    public List<UserDTO> searchUsers(UserSearchCriteria criteria) {
        List<User> users = userRepository.searchUsers(
                criteria.getKeyword(),
                criteria.getRoleName(),
                criteria.getDepartmentId(),
                criteria.getClassId(),
                criteria.getIsActive(),
                criteria.getGender()
        );
        return users.stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Lấy thống kê về users trong hệ thống
     * @returns UserStatisticsDTO chứa các thống kê
     * @author NVMANH with Cline (15/11/2025 15:06)
     * --------------------------------------------------- */
    public UserStatisticsDTO getUserStatistics() {
        Long totalUsers = userRepository.countActiveUsers();
        Long activeUsers = userRepository.countByIsActive(true);
        Long inactiveUsers = userRepository.countByIsActive(false);
        Long deletedUsers = userRepository.countDeletedUsers();

        // Count by role
        Map<String, Long> usersByRole = new HashMap<>();
        List<Role> allRoles = roleRepository.findAll();
        for (Role role : allRoles) {
            Long count = (long) userRepository.findByRoleName(role.getRoleName()).size();
            if (count > 0) {
                usersByRole.put(role.getRoleName(), count);
            }
        }

        // Count by department
        Map<String, Long> usersByDepartment = new HashMap<>();
        List<Department> allDepartments = departmentRepository.findByDeletedAtIsNull();
        for (Department dept : allDepartments) {
            Long count = (long) userRepository.findByDepartmentId(dept.getId()).size();
            if (count > 0) {
                usersByDepartment.put(dept.getDepartmentName(), count);
            }
        }

        // Count by class
        Map<String, Long> usersByClass = new HashMap<>();
        List<ClassEntity> allClasses = classRepository.findAll().stream()
                .filter(c -> c.getDeletedAt() == null)
                .collect(Collectors.toList());
        for (ClassEntity classEntity : allClasses) {
            Long count = (long) userRepository.findByClassId(classEntity.getId()).size();
            if (count > 0) {
                usersByClass.put(classEntity.getClassName(), count);
            }
        }

        // Count by gender
        Map<String, Long> usersByGender = new HashMap<>();
        List<User> allUsers = userRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null)
                .collect(Collectors.toList());
        for (User user : allUsers) {
            if (user.getGender() != null) {
                String gender = user.getGender().name();
                usersByGender.put(gender, usersByGender.getOrDefault(gender, 0L) + 1);
            }
        }

        return UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .deletedUsers(deletedUsers)
                .usersByRole(usersByRole)
                .usersByDepartment(usersByDepartment)
                .usersByClass(usersByClass)
                .usersByGender(usersByGender)
                .build();
    }

    /* ---------------------------------------------------
     * Filter users theo active status
     * @param isActive Active status cần filter
     * @returns List các UserDTO có status tương ứng
     * @author NVMANH with Cline (15/11/2025 15:06)
     * --------------------------------------------------- */
    public List<UserDTO> filterUsersByStatus(Boolean isActive) {
        List<User> users = userRepository.findByIsActive(isActive);
        return users.stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }
}
