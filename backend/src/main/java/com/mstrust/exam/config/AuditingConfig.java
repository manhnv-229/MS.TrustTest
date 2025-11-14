package com.mstrust.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/* ---------------------------------------------------
 * JPA Auditing Configuration
 * Tự động set created_by, updated_by khi insert/update entity
 * @author: K24DTCN210-NVMANH (14/11/2025 13:31)
 * --------------------------------------------------- */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    /* ---------------------------------------------------
     * Bean cung cấp thông tin auditor (người tạo/sửa)
     * Lấy từ SecurityContext, nếu không có thì dùng "system"
     * @returns AuditorAware instance
     * @author: K24DTCN210-NVMANH (14/11/2025 13:31)
     * --------------------------------------------------- */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            
            return Optional.of(authentication.getName());
        };
    }
}
