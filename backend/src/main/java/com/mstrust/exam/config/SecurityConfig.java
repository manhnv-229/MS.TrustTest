package com.mstrust.exam.config;

import com.mstrust.exam.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/* ---------------------------------------------------
 * Spring Security Configuration
 * Cấu hình authentication, authorization, JWT filter, CORS
 * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
 * --------------------------------------------------- */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /* ---------------------------------------------------
     * Password encoder với BCrypt algorithm (cost factor 12)
     * @returns BCryptPasswordEncoder instance
     * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
     * --------------------------------------------------- */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /* ---------------------------------------------------
     * Authentication provider với UserDetailsService và PasswordEncoder
     * @returns DaoAuthenticationProvider instance
     * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
     * --------------------------------------------------- */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /* ---------------------------------------------------
     * Authentication Manager bean
     * @param authConfig Authentication configuration
     * @returns AuthenticationManager instance
     * @throws Exception
     * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
     * --------------------------------------------------- */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /* ---------------------------------------------------
     * Security Filter Chain - Cấu hình HTTP security
     * @param http HttpSecurity object
     * @returns SecurityFilterChain
     * @throws Exception
     * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
     * --------------------------------------------------- */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - MUST be first to match correctly
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/health/**").permitAll() // Health check endpoint
                        // Swagger UI endpoints - Cho phép truy cập không cần authentication
                        .requestMatchers(
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/api/v3/api-docs/**",
                                "/api/v3/api-docs.yaml",
                                "/api/swagger-resources/**",
                                "/api/webjars/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Admin only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Manager endpoints
                        .requestMatchers("/manager/**").hasAnyRole("ADMIN", "DEPT_MANAGER", "CLASS_MANAGER")
                        // Teacher endpoints
                        .requestMatchers("/teacher/**").hasAnyRole("ADMIN", "TEACHER")
                        // All authenticated users
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ---------------------------------------------------
     * CORS Configuration - Cho phép frontend connect
     * @returns CorsConfigurationSource
     * @author: K24DTCN210-NVMANH (13/11/2025 14:56)
     * --------------------------------------------------- */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins (bao gồm null cho JavaFX client)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        // Allow credentials
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
