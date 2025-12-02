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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * API Client cho Reports APIs
 * Wrapper cho các endpoints: /api/admin/reports/**
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class ReportsApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public ReportsApiClient() {
        AppConfig config = AppConfig.getInstance();
        
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.getApiTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // Reports có thể mất thời gian
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
     * Generate exam statistics report
     * POST /api/admin/reports/exam-statistics
     * @param examId Exam ID (optional, null for all exams)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param format Export format (PDF, EXCEL, CSV)
     * @return InputStream của report file
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public InputStream generateExamStatisticsReport(Long examId, String startDate, String endDate, String format) 
            throws IOException, ApiException {
        
        Map<String, Object> requestData = new java.util.HashMap<>();
        if (examId != null) requestData.put("examId", examId);
        if (startDate != null) requestData.put("startDate", startDate);
        if (endDate != null) requestData.put("endDate", endDate);
        requestData.put("format", format);
        
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/reports/exam-statistics")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
            
            return response.body().byteStream();
        }
    }
    
    /* ---------------------------------------------------
     * Generate student performance report
     * POST /api/admin/reports/student-performance
     * @param studentId Student ID (optional)
     * @param classId Class ID (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param format Export format
     * @return InputStream của report file
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public InputStream generateStudentPerformanceReport(Long studentId, Long classId, 
            String startDate, String endDate, String format) 
            throws IOException, ApiException {
        
        Map<String, Object> requestData = new java.util.HashMap<>();
        if (studentId != null) requestData.put("studentId", studentId);
        if (classId != null) requestData.put("classId", classId);
        if (startDate != null) requestData.put("startDate", startDate);
        if (endDate != null) requestData.put("endDate", endDate);
        requestData.put("format", format);
        
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/reports/student-performance")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
            
            return response.body().byteStream();
        }
    }
    
    /* ---------------------------------------------------
     * Generate teacher activity report
     * POST /api/admin/reports/teacher-activity
     * @param teacherId Teacher ID (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param format Export format
     * @return InputStream của report file
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public InputStream generateTeacherActivityReport(Long teacherId, String startDate, String endDate, String format) 
            throws IOException, ApiException {
        
        Map<String, Object> requestData = new java.util.HashMap<>();
        if (teacherId != null) requestData.put("teacherId", teacherId);
        if (startDate != null) requestData.put("startDate", startDate);
        if (endDate != null) requestData.put("endDate", endDate);
        requestData.put("format", format);
        
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/reports/teacher-activity")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
            
            return response.body().byteStream();
        }
    }
    
    /* ---------------------------------------------------
     * Generate monitoring summary report
     * POST /api/admin/reports/monitoring-summary
     * @param examId Exam ID (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param format Export format
     * @return InputStream của report file
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public InputStream generateMonitoringSummaryReport(Long examId, String startDate, String endDate, String format) 
            throws IOException, ApiException {
        
        Map<String, Object> requestData = new java.util.HashMap<>();
        if (examId != null) requestData.put("examId", examId);
        if (startDate != null) requestData.put("startDate", startDate);
        if (endDate != null) requestData.put("endDate", endDate);
        requestData.put("format", format);
        
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/reports/monitoring-summary")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
            
            return response.body().byteStream();
        }
    }
    
    /* ---------------------------------------------------
     * Generate system usage report
     * POST /api/admin/reports/system-usage
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param format Export format
     * @return InputStream của report file
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public InputStream generateSystemUsageReport(String startDate, String endDate, String format) 
            throws IOException, ApiException {
        
        Map<String, Object> requestData = new java.util.HashMap<>();
        if (startDate != null) requestData.put("startDate", startDate);
        if (endDate != null) requestData.put("endDate", endDate);
        requestData.put("format", format);
        
        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/admin/reports/system-usage")
            .header("Authorization", "Bearer " + jwtToken)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new ApiException(response.code(), responseBody);
            }
            
            return response.body().byteStream();
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

