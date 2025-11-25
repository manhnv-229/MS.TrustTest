package com.mstrust.client.exam.dto;

/* ---------------------------------------------------
 * DTO cho Login Response
 * Chứa thông tin user sau khi login thành công
 * @author: K24DTCN210-NVMANH (25/11/2025 21:44)
 * --------------------------------------------------- */
public class LoginResponse {
    private String token;
    private String userName;
    private String email;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userName, String email, String role) {
        this.token = token;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
