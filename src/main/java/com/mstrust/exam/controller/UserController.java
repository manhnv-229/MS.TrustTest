package com.mstrust.exam.controller;

import com.mstrust.exam.dto.ChangePasswordRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

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
     * Activate/Deactivate user endpoint
     * @param id User ID
     * @param request Map chứa isActive flag
     * @returns ResponseEntity chứa UserDTO đã update
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> setUserActive(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");
        UserDTO updatedUser = userService.setUserActive(id, isActive);
        return ResponseEntity.ok(updatedUser);
    }
}
