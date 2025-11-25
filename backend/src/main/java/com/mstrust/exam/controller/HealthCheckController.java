package com.mstrust.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/* ---------------------------------------------------
 * Health Check Controller - Public endpoint cho monitoring
 * 
 * Endpoint này KHÔNG yêu cầu authentication
 * Dùng để check server availability
 * 
 * @author: K24DTCN210-NVMANH (24/11/2025 14:17)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    /* ---------------------------------------------------
     * Health check endpoint - trả về status của server
     * PUBLIC endpoint - không cần authentication
     * 
     * @returns Map chứa status, timestamp, service name
     * @author: K24DTCN210-NVMANH (24/11/2025 14:17)
     * --------------------------------------------------- */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "MS.TrustTest Exam API");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /* ---------------------------------------------------
     * Ping endpoint - minimal response cho network check
     * PUBLIC endpoint - không cần authentication
     * 
     * @returns "pong" string
     * @author: K24DTCN210-NVMANH (24/11/2025 14:17)
     * --------------------------------------------------- */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
