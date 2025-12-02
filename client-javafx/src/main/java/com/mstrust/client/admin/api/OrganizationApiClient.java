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
 * API Client cho Organization Management APIs
 * Wrapper cho các endpoints: /api/departments, /api/classes, /api/subjects
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class OrganizationApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public OrganizationApiClient() {
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
    
    // ==================== DEPARTMENT APIs ====================
    
    /* ---------------------------------------------------
     * Lấy danh sách departments với pagination
     * GET /api/departments/page?page=0&size=10&sortBy=id&sortDir=asc
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getDepartmentsPage(int page, int size, String sortBy, String sortDir) 
            throws IOException, ApiException {
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/departments/page").newBuilder()
            .addQueryParameter("page", String.valueOf(page))
            .addQueryParameter("size", String.valueOf(size))
            .addQueryParameter("sortBy", sortBy != null ? sortBy : "id")
            .addQueryParameter("sortDir", sortDir != null ? sortDir : "asc");
        
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
     * Lấy tất cả departments (không phân trang)
     * GET /api/departments
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public List<Map<String, Object>> getAllDepartments() throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/departments")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Tạo department mới
     * POST /api/departments
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> createDepartment(Map<String, Object> departmentData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(departmentData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/departments")
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
     * Cập nhật department
     * PUT /api/departments/{id}
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> updateDepartment(Long departmentId, Map<String, Object> departmentData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(departmentData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/departments/" + departmentId)
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
     * Xóa department
     * DELETE /api/departments/{id}
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void deleteDepartment(Long departmentId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/departments/" + departmentId)
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
    
    // ==================== CLASS APIs ====================
    
    /* ---------------------------------------------------
     * Lấy danh sách classes với pagination
     * GET /api/classes/page?page=0&size=10&sortBy=id&sortDir=asc
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getClassesPage(int page, int size, String sortBy, String sortDir) 
            throws IOException, ApiException {
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/classes/page").newBuilder()
            .addQueryParameter("page", String.valueOf(page))
            .addQueryParameter("size", String.valueOf(size))
            .addQueryParameter("sortBy", sortBy != null ? sortBy : "id")
            .addQueryParameter("sortDir", sortDir != null ? sortDir : "asc");
        
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
     * Lấy tất cả classes (không phân trang)
     * GET /api/classes
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public List<Map<String, Object>> getAllClasses() throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/classes")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách classes theo department
     * GET /api/classes/department/{departmentId}
     * @param departmentId ID của department
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public List<Map<String, Object>> getClassesByDepartment(Long departmentId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/classes/department/" + departmentId)
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Tạo class mới
     * POST /api/classes
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> createClass(Map<String, Object> classData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(classData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/classes")
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
     * Cập nhật class
     * PUT /api/classes/{id}
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> updateClass(Long classId, Map<String, Object> classData) 
            throws IOException, ApiException {
        
        String json = gson.toJson(classData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/classes/" + classId)
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
     * Xóa class
     * DELETE /api/classes/{id}
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void deleteClass(Long classId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/classes/" + classId)
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
    
    // ==================== SUBJECT APIs ====================
    // Subject APIs đã có trong SubjectApiClient, có thể tái sử dụng
    
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

