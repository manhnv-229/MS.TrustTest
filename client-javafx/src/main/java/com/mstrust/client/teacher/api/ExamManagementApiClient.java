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
import com.mstrust.client.exam.dto.LoginResponse;
import com.mstrust.client.teacher.dto.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * API Client cho Exam Management
 * Wrapper cho các endpoints: /api/exams/*, /api/classes
 * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
 * --------------------------------------------------- */
public class ExamManagementApiClient {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private String jwtToken;
    
    /* ---------------------------------------------------
     * Constructor - load config từ AppConfig
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 16:11) - Load baseUrl từ config thay vì hardcode
     * EditBy: K24DTCN210-NVMANH (28/11/2025 16:35) - Add LocalDateTime TypeAdapter
     * --------------------------------------------------- */
    public ExamManagementApiClient() {
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
     * @param token JWT token từ login response
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public void setToken(String token) {
        this.jwtToken = token;
    }
    
    /* ---------------------------------------------------
     * Set token từ LoginResponse
     * @param loginResponse Login response chứa accessToken
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public void setToken(LoginResponse loginResponse) {
        this.jwtToken = loginResponse.getToken();
    }
    
    /* ---------------------------------------------------
     * Get JWT token hiện tại
     * @return JWT token
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public String getAuthToken() {
        return this.jwtToken;
    }
    
    /* ---------------------------------------------------
     * Tạo exam mới
     * POST /api/exams
     * @param request ExamCreateRequest
     * @return ExamDTO (exam vừa tạo với examId)
     * @throws IOException Network error
     * @throws ApiException API error (4xx, 5xx)
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public ExamDTO createExam(ExamCreateRequest request) throws IOException, ApiException {
        String json = gson.toJson(request);
        
        RequestBody body = RequestBody.create(json, JSON);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams")
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, ExamDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Publish exam - cho phép students làm bài
     * POST /api/exams/{examId}/publish
     * @param examId ID của exam cần publish
     * @return ExamDTO (exam đã publish)
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public ExamDTO publishExam(Long examId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId + "/publish")
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", JSON))
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, ExamDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Unpublish exam - ẩn exam khỏi students
     * POST /api/exams/{examId}/unpublish
     * @param examId ID của exam cần unpublish
     * @return ExamDTO (exam đã unpublish)
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public ExamDTO unpublishExam(Long examId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId + "/unpublish")
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", JSON))
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, ExamDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Thêm câu hỏi vào exam
     * POST /api/exams/{examId}/questions
     * @param examId ID của exam
     * @param questionId ID của câu hỏi
     * @param questionOrder Thứ tự câu hỏi
     * @param points Điểm của câu hỏi trong exam này
     * @return ExamQuestionDTO
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public ExamQuestionDTO addQuestionToExam(Long examId, Long questionId, 
                                            Integer questionOrder, Double points) 
                                            throws IOException, ApiException {
        // Create request object
        AddQuestionRequest request = new AddQuestionRequest();
        request.setQuestionId(questionId);
        request.setQuestionOrder(questionOrder);
        request.setPoints(points);
        
        String json = gson.toJson(request);
        
        RequestBody body = RequestBody.create(json, JSON);
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId + "/questions")
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, ExamQuestionDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Thêm nhiều câu hỏi vào exam (batch)
     * Gọi addQuestionToExam nhiều lần
     * @param examId ID của exam
     * @param mappings Danh sách ExamQuestionMapping
     * @return List<ExamQuestionDTO>
     * @throws IOException Network error
     * @throws ApiException API error (nếu có bất kỳ request nào fail)
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public List<ExamQuestionDTO> addMultipleQuestions(Long examId, 
                                                     List<ExamQuestionMapping> mappings) 
                                                     throws IOException, ApiException {
        java.util.List<ExamQuestionDTO> results = new java.util.ArrayList<>();
        
        for (ExamQuestionMapping mapping : mappings) {
            ExamQuestionDTO result = addQuestionToExam(
                examId,
                mapping.getQuestionId(),
                mapping.getQuestionOrder(),
                mapping.getPoints().doubleValue()
            );
            results.add(result);
        }
        
        return results;
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách tất cả exams với pagination
     * GET /api/exams?page=0&size=100
     * @param page Số trang (0-based)
     * @param size Số items per page
     * @return List<ExamDTO> danh sách exams
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public List<ExamDTO> getAllExams(int page, int size) throws IOException, ApiException {
        String url = baseUrl + "/exams?page=" + page + "&size=" + size;
        
        Request httpRequest = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            // Parse Page response từ Spring
            // Backend trả về Page<ExamSummaryDTO> với structure:
            // { "content": [...], "totalElements": ..., "totalPages": ..., ... }
            com.google.gson.JsonObject pageJson = gson.fromJson(responseBody, com.google.gson.JsonObject.class);
            
            // Extract content array từ Page response
            com.google.gson.JsonArray contentArray = pageJson.getAsJsonArray("content");
            if (contentArray == null) {
                return new java.util.ArrayList<>();
            }
            
            // Parse content array thành List<ExamDTO>
            // ExamSummaryDTO có thể map sang ExamDTO vì có các field tương tự
            Type listType = new TypeToken<List<ExamDTO>>(){}.getType();
            return gson.fromJson(contentArray, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách tất cả exams (không pagination, lấy tất cả)
     * GET /api/exams?page=0&size=1000
     * @return List<ExamDTO> danh sách exams
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public List<ExamDTO> getAllExams() throws IOException, ApiException {
        return getAllExams(0, 1000); // Lấy tối đa 1000 exams
    }

    /* ---------------------------------------------------
     * Lấy chi tiết exam theo ID
     * GET /api/exams/{examId}
     * @param examId ID của exam
     * @return ExamDTO
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public ExamDTO getExamById(Long examId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId)
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            return gson.fromJson(responseBody, ExamDTO.class);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi trong exam
     * GET /api/exams/{examId}/questions
     * @param examId ID của exam
     * @return List<ExamQuestionDTO>
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public List<ExamQuestionDTO> getExamQuestions(Long examId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId + "/questions")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type listType = new TypeToken<List<ExamQuestionDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Xóa exam (soft delete)
     * DELETE /api/exams/{examId}
     * @param examId ID của exam
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    public void deleteExam(Long examId) throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/exams/" + examId)
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
     * Lấy danh sách tất cả các lớp
     * GET /api/classes
     * @return List<ClassDTO>
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (28/11/2025 15:59)
     * --------------------------------------------------- */
    public List<ClassDTO> getAllClasses() throws IOException, ApiException {
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
            
            Type listType = new TypeToken<List<ClassDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách tất cả các lớp học phần
     * GET /api/subject-classes
     * @return List<SubjectClassDTO>
     * @throws IOException Network error
     * @throws ApiException API error
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public List<SubjectClassDTO> getAllSubjectClasses() throws IOException, ApiException {
        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/subject-classes")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), responseBody);
            }
            
            Type listType = new TypeToken<List<SubjectClassDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }
    
    /* ---------------------------------------------------
     * Inner class: Request để add question to exam
     * Mapping với backend AddQuestionToExamRequest
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
     * --------------------------------------------------- */
    private static class AddQuestionRequest {
        private Long questionId;
        private Integer questionOrder;
        private Double points;
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public void setQuestionOrder(Integer questionOrder) {
            this.questionOrder = questionOrder;
        }
        
        public void setPoints(Double points) {
            this.points = points;
        }
    }
    
    /* ---------------------------------------------------
     * LocalDateTime TypeAdapter cho Gson serialization/deserialization
     * Handle LocalDateTime fields trong ClassDTO và ExamDTO
     * @author: K24DTCN210-NVMANH (28/11/2025 16:36)
     * --------------------------------------------------- */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(localDateTime.format(FORMATTER));
        }
        
        @Override
        public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            String dateTimeString = jsonElement.getAsString();
            try {
                return LocalDateTime.parse(dateTimeString, FORMATTER);
            } catch (Exception e) {
                // Fallback: Try other common formats
                try {
                    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e2) {
                    throw new JsonParseException("Unable to parse LocalDateTime: " + dateTimeString, e2);
                }
            }
        }
    }
    
    /* ---------------------------------------------------
     * Exception cho API errors (4xx, 5xx)
     * @author: K24DTCN210-NVMANH (28/11/2025 07:58)
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
