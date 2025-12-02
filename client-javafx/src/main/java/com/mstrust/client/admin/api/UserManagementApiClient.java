package com.mstrust.client.admin.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mstrust.client.config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * API Client cho User Management APIs
 * Wrapper cho các endpoints: /api/users/**
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class UserManagementApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public UserManagementApiClient() {
        AppConfig config = AppConfig.getInstance();
        
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.getApiTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(config.getApiTimeoutSeconds(), TimeUnit.SECONDS)
            .writeTimeout(config.getApiTimeoutSeconds(), TimeUnit.SECONDS)
            .build();
        
        // Create Gson với LocalDateTime TypeAdapter
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
        
        // Load baseUrl từ config và thêm /api suffix
        this.baseUrl = config.getApiBaseUrl() + "/api";
    }
    
    /* ---------------------------------------------------
     * Set JWT token cho authentication
     * @param token JWT token
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void setToken(String token) {
        this.jwtToken = token;
    }
    
    /* ---------------------------------------------------
     * Decode JWT token để lấy current user info (ID và roles)
     * @return Map chứa userId và roles, hoặc null nếu không decode được
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getCurrentUserInfo() {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }
        
        try {
            // JWT format: header.payload.signature
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            // Decode base64 payload
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse JSON to get claims
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> claims = gson.fromJson(payload, mapType);
            
            // Debug: Log JWT claims
            System.out.println("[DEBUG] JWT Claims: " + claims);
            
            Map<String, Object> userInfo = new java.util.HashMap<>();
            
            // Get user ID - thử nhiều cách
            Object userIdObj = null;
            if (claims.containsKey("sub")) {
                userIdObj = claims.get("sub");
            } else if (claims.containsKey("userId")) {
                userIdObj = claims.get("userId");
            } else if (claims.containsKey("id")) {
                userIdObj = claims.get("id");
            }
            
            if (userIdObj != null) {
                // Try to parse as Long
                try {
                    if (userIdObj instanceof Number) {
                        userInfo.put("userId", ((Number) userIdObj).longValue());
                    } else {
                        // Nếu là string, có thể là email hoặc ID dạng string
                        String userIdStr = userIdObj.toString();
                        try {
                            userInfo.put("userId", Long.parseLong(userIdStr));
                        } catch (NumberFormatException e) {
                            // Nếu không parse được, có thể là email
                            userInfo.put("email", userIdStr);
                        }
                    }
                } catch (Exception e) {
                    // If can't parse as Long, treat as email
                    userInfo.put("email", userIdObj.toString());
                }
            }
            
            // Get email if available (ưu tiên email từ claim "email")
            if (claims.containsKey("email")) {
                userInfo.put("email", claims.get("email"));
            } else if (claims.containsKey("sub") && !userInfo.containsKey("userId")) {
                // Nếu sub không phải là số, có thể là email
                Object subObj = claims.get("sub");
                if (subObj != null && subObj.toString().contains("@")) {
                    userInfo.put("email", subObj.toString());
                }
            }
            
            // Get roles
            List<String> roles = new java.util.ArrayList<>();
            if (claims.containsKey("authorities")) {
                Object authObj = claims.get("authorities");
                if (authObj instanceof List) {
                    List<?> authorities = (List<?>) authObj;
                    for (Object auth : authorities) {
                        String authStr = auth.toString();
                        // Remove "ROLE_" prefix if present
                        if (authStr.startsWith("ROLE_")) {
                            authStr = authStr.substring(5);
                        }
                        roles.add(authStr.toUpperCase());
                    }
                }
            } else if (claims.containsKey("roles")) {
                Object rolesObj = claims.get("roles");
                if (rolesObj instanceof List) {
                    List<?> rolesList = (List<?>) rolesObj;
                    for (Object role : rolesList) {
                        String roleStr = role.toString();
                        if (roleStr.startsWith("ROLE_")) {
                            roleStr = roleStr.substring(5);
                        }
                        roles.add(roleStr.toUpperCase());
                    }
                }
            }
            userInfo.put("roles", roles);
            
            return userInfo;
            
        } catch (Exception e) {
            // Log error but don't throw
            System.err.println("[WARN] Failed to decode JWT token: " + e.getMessage());
            return null;
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách users với pagination
     * GET /api/users/page?page=0&size=10&sortBy=id&direction=ASC
     * @param page Số trang (0-based)
     * @param size Kích thước trang
     * @param sortBy Trường sắp xếp
     * @param direction Hướng sắp xếp (ASC/DESC)
     * @return Map chứa page data
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getUsersPage(int page, int size, String sortBy, String direction) 
            throws IOException, ApiException {
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/users/page").newBuilder()
            .addQueryParameter("page", String.valueOf(page))
            .addQueryParameter("size", String.valueOf(size))
            .addQueryParameter("sortBy", sortBy != null ? sortBy : "id")
            .addQueryParameter("direction", direction != null ? direction : "ASC");
        
        Request httpRequest = new Request.Builder()
            .url(urlBuilder.build())
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(responseBody, mapType);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy user theo ID
     * GET /api/users/{id}
     * @param userId ID của user
     * @return Map chứa user data
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getUserById(Long userId) 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/users/" + userId)
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(responseBody, mapType);
        }
    }
    
    /* ---------------------------------------------------
     * Tạo user mới
     * POST /api/users
     * @param userData Map chứa user data
     * @return Map chứa created user
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> createUser(Map<String, Object> userData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(userData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/users")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(responseBody, mapType);
        }
    }
    
    /* ---------------------------------------------------
     * Cập nhật user
     * PUT /api/users/{id}
     * @param userId ID của user
     * @param userData Map chứa user data cần update
     * @return Map chứa updated user
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Thêm logging để debug
     * --------------------------------------------------- */
    public Map<String, Object> updateUser(Long userId, Map<String, Object> userData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(userData);
        RequestBody body = RequestBody.create(json, JSON);
        
        // Log request
        System.out.println("==========================================");
        System.out.println("[DEBUG UserManagementApiClient] PUT " + baseUrl + "/users/" + userId);
        System.out.println("[DEBUG] Request JSON Body:");
        System.out.println(json);
        System.out.println("==========================================");
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/users/" + userId)
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .put(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            // Log response
            System.out.println("[DEBUG UserManagementApiClient] Response Status: " + response.code());
            System.out.println("[DEBUG] Response Body: " + responseBody);
            
            if (!response.isSuccessful()) {
                System.out.println("[ERROR] Update user failed with status: " + response.code());
                System.out.println("[ERROR] Error response: " + responseBody);
                throw new ApiException(response.code(), responseBody);
            }
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> result = gson.fromJson(responseBody, mapType);
            System.out.println("[DEBUG] Update user successful");
            System.out.println("==========================================");
            return result;
        }
    }
    
    /* ---------------------------------------------------
     * Xóa user (soft delete)
     * DELETE /api/users/{id}
     * @param userId ID của user
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void deleteUser(Long userId) 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/users/" + userId)
            .header("Authorization", "Bearer " + jwtToken)
            .delete()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
        }
    }
    
    /* ---------------------------------------------------
     * Kích hoạt/Vô hiệu hóa user
     * PUT /api/users/{id}/active
     * @param userId ID của user
     * @param isActive true để kích hoạt, false để vô hiệu hóa
     * @return Map chứa updated user
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> setUserActive(Long userId, boolean isActive) 
            throws IOException, ApiException {
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/users/" + userId + "/active").newBuilder()
            .addQueryParameter("isActive", String.valueOf(isActive));
        
        Request httpRequest = new Request.Builder()
            .url(urlBuilder.build())
            .header("Authorization", "Bearer " + jwtToken)
            .put(RequestBody.create("", null))
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(responseBody, mapType);
        }
    }
    
    /* ---------------------------------------------------
     * LocalDateTime TypeAdapter cho Gson
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }
        
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
    
    /* ---------------------------------------------------
     * Exception class cho API errors
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public static class ApiException extends Exception {
        private final int statusCode;
        private final String responseBody;
        
        public ApiException(int statusCode, String responseBody) {
            super("API Error: " + statusCode + " - " + responseBody);
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getResponseBody() {
            return responseBody;
        }
    }
}

