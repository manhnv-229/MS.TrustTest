package com.mstrust.exam.controller;

import com.mstrust.exam.dto.LoginRequest;
import com.mstrust.exam.dto.LoginResponse;
import com.mstrust.exam.dto.RegisterRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.security.JwtTokenProvider;
import com.mstrust.exam.service.AuthService;
import com.mstrust.exam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/* ---------------------------------------------------
 * Authentication Controller
 * REST APIs cho authentication (login, register, logout, etc.)
 * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /* ---------------------------------------------------
     * Login endpoint
     * @param loginRequest LoginRequest với username và password
     * @returns ResponseEntity chứa LoginResponse với JWT token
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------
     * Register endpoint
     * @param registerRequest RegisterRequest với thông tin user mới
     * @returns ResponseEntity chứa UserDTO của user vừa tạo
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDTO user = authService.register(registerRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* ---------------------------------------------------
     * Get current logged in user endpoint
     * @returns ResponseEntity chứa UserDTO của current user
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /* ---------------------------------------------------
     * Refresh token endpoint
     * @param request Map chứa refreshToken
     * @returns ResponseEntity chứa LoginResponse với token mới
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------
     * Validate token endpoint
     * @param request Map chứa token cần validate
     * @returns ResponseEntity với kết quả validation
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        boolean isValid = authService.validateToken(token);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------
     * Logout endpoint (client-side: xóa token)
     * Server-side stateless nên chỉ return success message
     * @returns ResponseEntity với success message
     * @author: K24DTCN210-NVMANH (13/11/2025 15:03)
     * --------------------------------------------------- */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}
