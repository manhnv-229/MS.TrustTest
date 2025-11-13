package com.mstrust.exam.security;

import com.mstrust.exam.entity.User;
import com.mstrust.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Custom UserDetailsService implementation
 * Load user từ database và convert sang Spring Security UserDetails
 * Hỗ trợ multi-login: student_code, email, phone_number
 * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
 * --------------------------------------------------- */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /* ---------------------------------------------------
     * Load user by username (student_code, email hoặc phone)
     * @param username Username để login
     * @returns UserDetails object cho Spring Security
     * @throws UsernameNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Kiểm tra account có bị khóa không
        if (user.isAccountLocked()) {
            throw new RuntimeException("Account is locked due to multiple failed login attempts");
        }

        // Kiểm tra account có active không
        if (!user.getIsActive() || user.isDeleted()) {
            throw new RuntimeException("Account is inactive or deleted");
        }

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getIsActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isAccountLocked(), // accountNonLocked
                authorities
        );
    }

    /* ---------------------------------------------------
     * Load user by ID
     * @param id User ID
     * @returns UserDetails object
     * @throws UsernameNotFoundException nếu không tìm thấy user
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getIsActive(),
                true,
                true,
                !user.isAccountLocked(),
                authorities
        );
    }
}
