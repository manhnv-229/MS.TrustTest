package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho login response
 * Chứa JWT token, refresh token và thông tin user
 * @author: K24DTCN210-NVMANH (13/11/2025 14:59)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserDTO user;

    public LoginResponse(String token, String refreshToken, UserDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        this.tokenType = "Bearer";
    }
}
