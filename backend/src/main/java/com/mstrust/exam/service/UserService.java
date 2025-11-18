package com.mstrust.exam.service;

import com.mstrust.exam.dto.ChangePasswordRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.dto.UserSearchCriteria;
import com.mstrust.exam.dto.UserStatisticsDTO;
import com.mstrust.exam.entity.ClassEntity;
import com.mstrust.exam.entity.Department;
import com.mstrust.exam.entity.Role;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ClassRepository;
import com.mstrust.exam.repository.DepartmentRepository;
import com.mstrust.exam.repository.RoleRepository;
import com.mstrust.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
