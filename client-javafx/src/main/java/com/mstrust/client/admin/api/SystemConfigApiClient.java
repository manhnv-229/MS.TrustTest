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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * API Client cho System Configuration APIs
 * Wrapper cho các endpoints: /api/admin/config/**
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class SystemConfigApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public SystemConfigApiClient() {
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
     * Lấy system configuration
     * GET /api/admin/config
     * @return Map chứa config data
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getSystemConfig() 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/config")
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
     * Lưu system configuration
     * PUT /api/admin/config
     * @param configData Map chứa config data
     * @return Map chứa saved config
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> saveSystemConfig(Map<String, Object> configData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(configData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/config")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .put(body)
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
     * Test email configuration
     * POST /api/admin/config/test-email
     * @param emailAddress Email address để test
     * @return Map chứa test result
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> testEmail(String emailAddress) 
            throws IOException, ApiException {
        
        Map<String, String> requestData = Map.of("email", emailAddress);
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/config/test-email")
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
     * Clear system cache
     * POST /api/admin/config/clear-cache
     * @return Map chứa result
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> clearCache() 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/config/clear-cache")
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", null))
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

