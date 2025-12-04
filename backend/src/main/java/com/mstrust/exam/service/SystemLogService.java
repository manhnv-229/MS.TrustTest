package com.mstrust.exam.service;

import com.mstrust.exam.dto.SystemLogRequest;
import com.mstrust.exam.entity.SystemLog;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.SystemLogRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLogService {
    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createLog(SystemLogRequest request) {
        Long userId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                userId = userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Failed to get current user for system log: {}", e.getMessage());
        }

        SystemLog systemLog = SystemLog.builder()
                .level(request.getLevel())
                .source(request.getSource())
                .message(request.getMessage())
                .stackTrace(request.getStackTrace())
                .additionalData(request.getAdditionalData())
                .submissionId(request.getSubmissionId())
                .createdBy(userId)
                .build();

        systemLogRepository.save(systemLog);
    }
}
