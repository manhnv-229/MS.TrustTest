package com.mstrust.exam.controller;

import com.mstrust.exam.dto.AssignRoleRequest;
import com.mstrust.exam.dto.ChangePasswordRequest;
import com.mstrust.exam.dto.CreateUserRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.dto.UserSearchCriteria;
import com.mstrust.exam.dto.UserStatisticsDTO;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * User Controller
 * REST APIs cho user management (CRUD operations)
 * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /* ---------------------------------------------------
     * Tạo user mới (Admin only)
     * POST /api/users
     * @param request CreateUserRequest chứa thông tin user mới
     * @returns ResponseEntity chứa UserDTO với status 201 Created
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /users - Creating new user");
        log.debug("Request data - Email: {}, FullName: {}, Roles: {}, DepartmentId: {}, ClassId: {}", 
            request.getEmail(), request.getFullName(), request.getRoles(), 
            request.getDepartmentId(), request.getClassId());
        
        try {
            UserDTO createdUser = userService.createUser(request);
            log.info("User created successfully with ID: {}, email: {}", 
                createdUser.getId(), createdUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (DuplicateResourceException e) {
            log.warn("Duplicate resource error: {}", e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    /* ---------------------------------------------------
     * Get all users endpoint (Admin only)
     * @returns ResponseEntity chứa List of UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Get users with pagination endpoint (Admin only)
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @param sortBy Sort field (default "id")
     * @param direction Sort direction (default "ASC")
     * @returns ResponseEntity chứa Page of UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<UserDTO> users = userService.getUsersPage(pageable);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Get user by ID endpoint
     * @param id User ID
     * @returns ResponseEntity chứa UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'CLASS_MANAGER', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /* ---------------------------------------------------
     * Get user by student code endpoint
     * @param studentCode Student code
     * @returns ResponseEntity chứa UserDTO
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @GetMapping("/student-code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'CLASS_MANAGER', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> getUserByStudentCode(@PathVariable String studentCode) {
        UserDTO user = userService.getUserByStudentCode(studentCode);
        return ResponseEntity.ok(user);
    }

    /* ---------------------------------------------------
     * Update user endpoint
     * @param id User ID
     * @param userDTO UserDTO với thông tin mới
     * @returns ResponseEntity chứa UserDTO đã update
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLASS_MANAGER', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Delete user endpoint (soft delete)
     * @param id User ID
     * @returns ResponseEntity với success message
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------
     * Change password endpoint
     * @param id User ID
     * @param request ChangePasswordRequest
     * @returns ResponseEntity với success message
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(id, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------
     * Toggle user active status endpoint
     * @param id User ID
     * @returns ResponseEntity chứa UserDTO đã update
     * @author: NVMANH with Cline (15/11/2025 17:18)
     * Updated: Toggle without requiring request body
     * --------------------------------------------------- */
    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable Long id) {
        UserDTO updatedUser = userService.toggleUserActive(id);
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Assign role cho user endpoint
     * @param id User ID
     * @param request AssignRoleRequest chứa roleId
     * @returns ResponseEntity chứa UserDTO đã được assign role
     * @author NVMANH with Cline (15/11/2025 15:07)
     * --------------------------------------------------- */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> assignRole(@PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        UserDTO updatedUser = userService.assignRole(id, request.getRoleId());
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Remove role từ user endpoint
     * @param id User ID
     * @param roleId Role ID
     * @returns ResponseEntity chứa UserDTO đã remove role
     * @author NVMANH with Cline (15/11/2025 15:07)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> removeRole(@PathVariable Long id, @PathVariable Long roleId) {
        UserDTO updatedUser = userService.removeRole(id, roleId);
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Get users by role endpoint
     * @param roleName Tên role (STUDENT, TEACHER, ADMIN, etc.)
     * @returns ResponseEntity chứa List UserDTO
     * @author NVMANH with Cline (15/11/2025 15:08)
     * --------------------------------------------------- */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName) {
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Assign user to department endpoint
     * @param id User ID
     * @param departmentId Department ID
     * @returns ResponseEntity chứa UserDTO đã được assign
     * @author NVMANH with Cline (15/11/2025 15:08)
     * --------------------------------------------------- */
    @PutMapping("/{id}/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<UserDTO> assignToDepartment(@PathVariable Long id, @PathVariable Long departmentId) {
        UserDTO updatedUser = userService.assignToDepartment(id, departmentId);
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Assign user to class endpoint
     * @param id User ID
     * @param classId Class ID
     * @returns ResponseEntity chứa UserDTO đã được assign
     * @author NVMANH with Cline (15/11/2025 15:08)
     * --------------------------------------------------- */
    @PutMapping("/{id}/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLASS_MANAGER')")
    public ResponseEntity<UserDTO> assignToClass(@PathVariable Long id, @PathVariable Long classId) {
        UserDTO updatedUser = userService.assignToClass(id, classId);
        return ResponseEntity.ok(updatedUser);
    }

    /* ---------------------------------------------------
     * Get users by department endpoint
     * @param departmentId Department ID
     * @returns ResponseEntity chứa List UserDTO
     * @author NVMANH with Cline (15/11/2025 15:09)
     * --------------------------------------------------- */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getUsersByDepartment(@PathVariable Long departmentId) {
        List<UserDTO> users = userService.getUsersByDepartment(departmentId);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Get users by class endpoint
     * @param classId Class ID
     * @returns ResponseEntity chứa List UserDTO
     * @author NVMANH with Cline (15/11/2025 15:09)
     * --------------------------------------------------- */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getUsersByClass(@PathVariable Long classId) {
        List<UserDTO> users = userService.getUsersByClass(classId);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Advanced search users endpoint
     * @param criteria UserSearchCriteria với các filter
     * @returns ResponseEntity chứa List UserDTO
     * @author NVMANH with Cline (15/11/2025 15:09)
     * --------------------------------------------------- */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER')")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestBody UserSearchCriteria criteria) {
        List<UserDTO> users = userService.searchUsers(criteria);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Filter users by status endpoint
     * @param status Active status (ACTIVE hoặc INACTIVE)
     * @returns ResponseEntity chứa List UserDTO
     * @author NVMANH with Cline (15/11/2025 15:10)
     * --------------------------------------------------- */
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<List<UserDTO>> filterUsersByStatus(@RequestParam(name = "status") String status) {
        Boolean isActive = status.equalsIgnoreCase("ACTIVE");
        List<UserDTO> users = userService.filterUsersByStatus(isActive);
        return ResponseEntity.ok(users);
    }

    /* ---------------------------------------------------
     * Get user statistics endpoint
     * @returns ResponseEntity chứa UserStatisticsDTO
     * @author NVMANH with Cline (15/11/2025 15:10)
     * --------------------------------------------------- */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {
        UserStatisticsDTO statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    /* ---------------------------------------------------
     * Count users by role endpoint
     * @returns ResponseEntity chứa Map với count by role
     * @author NVMANH with Cline (15/11/2025 15:10)
     * --------------------------------------------------- */
    @GetMapping("/count-by-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<Map<String, Long>> countUsersByRole() {
        UserStatisticsDTO statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics.getUsersByRole());
    }

    /* ---------------------------------------------------
     * Count users by department endpoint
     * @returns ResponseEntity chứa Map với count by department
     * @author NVMANH with Cline (15/11/2025 15:11)
     * --------------------------------------------------- */
    @GetMapping("/count-by-department")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<Map<String, Long>> countUsersByDepartment() {
        UserStatisticsDTO statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics.getUsersByDepartment());
    }
}
