package com.mstrust.exam.config;

import com.mstrust.exam.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/* ---------------------------------------------------
 * Configuration cho JPA Auditing
 * Tự động điền created_by, updated_by khi insert/update entities
 * @author: K24DTCN210-NVMANH (14/11/2025 10:27)
 * EditBy: K24DTCN210-NVMANH (14/11/2025 14:48) - Changed return type from String to Long (user ID)
 * --------------------------------------------------- */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    /* ---------------------------------------------------
     * Bean cung cấp thông tin user hiện tại cho JPA Auditing
     * Trả về user ID (Long) thay vì username (String) để match database schema
     * @returns AuditorAware với user ID hoặc null nếu không có user
     * @author: K24DTCN210-NVMANH (14/11/2025 10:27)
     * EditBy: K24DTCN210-NVMANH (14/11/2025 14:48) - Return Long instead of String
     * --------------------------------------------------- */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // Return empty instead of "system" for Long type
            }
            
            // Get User object from authentication principal
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return Optional.of(user.getId());
            }
            
            return Optional.empty();
        };
    }
}
