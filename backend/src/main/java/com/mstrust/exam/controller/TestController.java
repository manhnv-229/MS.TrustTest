package com.mstrust.exam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/* ---------------------------------------------------
 * Test Controller - CHỈ DÙNG ĐỂ TEST, XÓA SAU KHI DEPLOY
 * @author: K24DTCN210-NVMANH (14/11/2025 12:30)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ---------------------------------------------------
     * Generate BCrypt password hash
     * @param password Password cần hash
     * @returns Map chứa password và hash
     * @author: K24DTCN210-NVMANH (14/11/2025 12:30)
     * --------------------------------------------------- */
    @GetMapping("/hash-password")
    public Map<String, String> hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("cost_factor", "12");
        
        return result;
    }

    /* ---------------------------------------------------
     * Verify password với hash
     * @param password Password cần verify
     * @param hash Hash để so sánh
     * @returns Map chứa kết quả matches
     * @author: K24DTCN210-NVMANH (14/11/2025 12:30)
     * --------------------------------------------------- */
    @GetMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", matches);
        
        return result;
    }
}
