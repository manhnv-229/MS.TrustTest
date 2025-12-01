package com.mstrust.client.teacher.api;

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
import com.mstrust.client.teacher.dto.grading.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * API Client cho Grading APIs
 * Wrapper cho các endpoints: /api/grading/**
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
public class GradingApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public GradingApiClient() {
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
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public void setToken(String token) {
        this.jwtToken = token;
    }
    
    /* ---------------------------------------------------
     * Get JWT token hiện tại
     * @return JWT token
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public String getAuthToken() {
        return this.jwtToken;
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách bài nộp cần chấm
     * GET /api/grading/submissions?status=SUBMITTED&examId=1
     * @param status Trạng thái bài nộp (tùy chọn)
     * @param examId ID đề thi (tùy chọn)
     * @return Danh sách bài nộp
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public List<GradingSubmissionListDTO> getSubmissionsForGrading(String status, Long examId) 
            throws IOException, ApiException {
        
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/grading/submissions").newBuilder();
        if (status != null && !status.isEmpty()) {
            urlBuilder.addQueryParameter("status", status);
        }
        if (examId != null) {
            urlBuilder.addQueryParameter("examId", examId.toString());
        }
        
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
            
            Type listType = new TypeToken<List<GradingSubmissionListDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết bài nộp để chấm điểm
     * GET /api/grading/submissions/{id}
     * @param submissionId ID của bài nộp
     * @return Chi tiết bài nộp
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public GradingDetailDTO getSubmissionDetail(Long submissionId) 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/grading/submissions/" + submissionId)
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, GradingDetailDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Chấm điểm một câu trả lời
     * POST /api/grading/answers/{answerId}/grade
     * @param answerId ID của câu trả lời
     * @param request Request chứa điểm và feedback
     * @return StudentAnswer đã được cập nhật (JSON object)
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> gradeAnswer(Long answerId, GradeAnswerRequest request) 
            throws IOException, ApiException {
        
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/grading/answers/" + answerId + "/grade")
            .header("Authorization", "Bearer " + jwtToken)
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
     * Hoàn tất việc chấm điểm
     * POST /api/grading/submissions/{id}/finalize
     * @param submissionId ID của bài nộp
     * @param request Request chứa nhận xét chung
     * @return ExamSubmission đã hoàn tất (JSON object)
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> finalizeGrading(Long submissionId, FinalizeGradingRequest request) 
            throws IOException, ApiException {
        
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/grading/submissions/" + submissionId + "/finalize")
            .header("Authorization", "Bearer " + jwtToken)
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
     * Lấy thống kê chấm điểm cho một đề thi
     * GET /api/grading/stats/{examId}
     * @param examId ID của đề thi
     * @return Map chứa các thống kê
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public Map<String, Object> getGradingStats(Long examId) 
            throws IOException, ApiException {
        
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/grading/stats/" + examId)
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
     * LocalDateTime TypeAdapter cho Gson
     * @author: K24DTCN210-NVMANH (01/12/2025)
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
     * @author: K24DTCN210-NVMANH (01/12/2025)
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

