package com.mstrust.exam.controller;

import com.mstrust.exam.dto.SystemLogRequest;
import com.mstrust.exam.service.SystemLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system-logs")
@RequiredArgsConstructor
public class SystemLogController {
    private final SystemLogService systemLogService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createLog(@Valid @RequestBody SystemLogRequest request) {
        systemLogService.createLog(request);
        return ResponseEntity.ok(Map.of("message", "Log created successfully"));
    }
}
