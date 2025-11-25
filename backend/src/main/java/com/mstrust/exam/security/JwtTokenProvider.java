package com.mstrust.exam.security;

import com.mstrust.exam.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * JWT Token Provider - Generate và validate JWT tokens
 * Sử dụng HS512 algorithm với secret key từ application.yml
 * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
 * --------------------------------------------------- */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /* ---------------------------------------------------
     * Generate JWT token từ User entity
     * @param user User entity
     * @returns JWT token string
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(user.getEmail())  // ✅ Email as subject
                .claim("userId", user.getId())
                .claim("studentCode", user.getStudentCode())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /* ---------------------------------------------------
     * Generate refresh token với expiration dài hơn
     * @param user User entity
     * @returns Refresh token string
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs * 7); // 7 days

        return Jwts.builder()
                .setSubject(user.getEmail())  // ✅ Email as subject
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /* ---------------------------------------------------
     * Lấy email từ JWT token (subject)
     * @param token JWT token
     * @returns User email
     * @author: K24DTCN210-NVMANH (26/11/2025 01:07)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 01:07) - Changed to return email from subject
     * --------------------------------------------------- */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();  // Email is now the subject
    }

    /* ---------------------------------------------------
     * Lấy user ID từ JWT token (claim)
     * @param token JWT token
     * @returns User ID
     * @author: K24DTCN210-NVMANH (26/11/2025 01:07)
     * --------------------------------------------------- */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.get("userId").toString());
    }

    /* ---------------------------------------------------
     * Validate JWT token
     * @param token JWT token cần validate
     * @returns true nếu token hợp lệ
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // Invalid JWT signature
        } catch (ExpiredJwtException e) {
            // Expired JWT token
        } catch (UnsupportedJwtException e) {
            // Unsupported JWT token
        } catch (IllegalArgumentException e) {
            // JWT claims string is empty
        }
        return false;
    }

    /* ---------------------------------------------------
     * Lấy signing key từ secret string
     * @returns SecretKey cho JWT signing
     * @author: K24DTCN210-NVMANH (13/11/2025 14:54)
     * --------------------------------------------------- */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
