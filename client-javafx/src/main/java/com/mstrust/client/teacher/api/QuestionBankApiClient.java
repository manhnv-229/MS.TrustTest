package com.mstrust.client.teacher.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mstrust.client.teacher.dto.QuestionBankDTO;
import com.mstrust.client.teacher.dto.CreateQuestionRequest;
import com.mstrust.client.teacher.dto.Difficulty;
import com.mstrust.client.exam.dto.QuestionType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * API Client cho Question Bank Management
 * Giao tiếp với backend APIs để quản lý ngân hàng câu hỏi
 * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
 * --------------------------------------------------- */
public class QuestionBankApiClient {
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private String authToken;
    
    /* ---------------------------------------------------
     * Constructor
     * @param baseUrl URL của backend API
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public QuestionBankApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    /* ---------------------------------------------------
     * Set JWT token để authenticate requests
     * @param token JWT token
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi với filter và pagination
     * @param subjectId ID môn học (optional)
     * @param difficulty Độ khó (optional)
     * @param type Loại câu hỏi (optional)
     * @param keyword Từ khóa tìm kiếm (optional)
     * @param page Số trang (default 0)
     * @param size Kích thước trang (default 20)
     * @return QuestionBankResponse chứa danh sách câu hỏi và thông tin phân trang
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public QuestionBankResponse getQuestions(
            Long subjectId,
            Difficulty difficulty,
            QuestionType type,
            String keyword,
            int page,
            int size
    ) throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder(baseUrl + "/api/question-banks?page=" + page + "&size=" + size);
        
        if (subjectId != null) {
            url.append("&subjectId=").append(subjectId);
        }
        if (difficulty != null) {
            url.append("&difficulty=").append(difficulty.name());
        }
        if (type != null) {
            url.append("&type=").append(type.name());
        }
        if (keyword != null && !keyword.isEmpty()) {
            url.append("&keyword=").append(keyword);
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Type responseType = new TypeToken<QuestionBankResponse>(){}.getType();
            return gson.fromJson(response.body(), responseType);
        } else {
            throw new IOException("Failed to get questions. Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết một câu hỏi theo ID
     * @param id ID của câu hỏi
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public QuestionBankDTO getQuestionById(Long id) throws IOException, InterruptedException {
        String url = baseUrl + "/api/question-banks/" + id;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), QuestionBankDTO.class);
        } else {
            throw new IOException("Failed to get question. Status: " + response.statusCode());
        }
    }
    
    /* ---------------------------------------------------
     * Tạo mới câu hỏi
     * @param request Thông tin câu hỏi cần tạo
     * @return QuestionBankDTO của câu hỏi vừa tạo
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public QuestionBankDTO createQuestion(CreateQuestionRequest request) throws IOException, InterruptedException {
        String url = baseUrl + "/api/question-banks";
        String jsonBody = gson.toJson(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return gson.fromJson(response.body(), QuestionBankDTO.class);
        } else {
            throw new IOException("Failed to create question. Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
    
    /* ---------------------------------------------------
     * Cập nhật câu hỏi
     * @param id ID của câu hỏi
     * @param request Thông tin cập nhật
     * @return QuestionBankDTO đã được cập nhật
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public QuestionBankDTO updateQuestion(Long id, CreateQuestionRequest request) throws IOException, InterruptedException {
        String url = baseUrl + "/api/question-banks/" + id;
        String jsonBody = gson.toJson(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), QuestionBankDTO.class);
        } else {
            throw new IOException("Failed to update question. Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
    
    /* ---------------------------------------------------
     * Xóa câu hỏi (soft delete)
     * @param id ID của câu hỏi cần xóa
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public void deleteQuestion(Long id) throws IOException, InterruptedException {
        String url = baseUrl + "/api/question-banks/" + id;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to delete question. Status: " + response.statusCode());
        }
    }
    
    /* ---------------------------------------------------
     * Inner class cho paginated response
     * @author: K24DTCN210-NVMANH (25/11/2025 22:38)
     * --------------------------------------------------- */
    public static class QuestionBankResponse {
        private List<QuestionBankDTO> content;
        private int totalPages;
        private long totalElements;
        private int number;
        private int size;
        
        public List<QuestionBankDTO> getContent() {
            return content;
        }
        
        public void setContent(List<QuestionBankDTO> content) {
            this.content = content;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getNumber() {
            return number;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public int getSize() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }
    }
}
