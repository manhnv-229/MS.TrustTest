package com.mstrust.client.exam.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.exam.dto.ExamInfoDTO;
import com.mstrust.client.exam.dto.QuestionDTO;
import com.mstrust.client.exam.dto.SaveAnswerRequest;
import com.mstrust.client.exam.dto.StartExamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * API Client cho Exam Taking APIs
 * - Quản lý HTTP requests tới backend exam endpoints
 * - Sử dụng Java 11+ HttpClient
 * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
 * --------------------------------------------------- */
public class ExamApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ExamApiClient.class);
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;
    private String authToken;

    /* ---------------------------------------------------
     * Constructor - khởi tạo HTTP client và Gson
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public ExamApiClient() {
        AppConfig config = AppConfig.getInstance();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getApiTimeoutSeconds()))
                .build();
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        
        this.baseUrl = config.getApiBaseUrl();
        
        logger.info("ExamApiClient initialized with baseUrl: {}", baseUrl);
    }

    /* ---------------------------------------------------
     * Constructor với authToken
     * @param authToken JWT access token
     * @author: K24DTCN210-NVMANH (23/11/2025 13:51)
     * --------------------------------------------------- */
    public ExamApiClient(String authToken) {
        this();
        this.authToken = authToken;
        logger.info("ExamApiClient initialized with auth token");
    }

    /* ---------------------------------------------------
     * Set JWT token cho authentication
     * @param token JWT access token
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public void setAuthToken(String token) {
        this.authToken = token;
        logger.info("Auth token set for ExamApiClient");
    }

    /* ---------------------------------------------------
     * Get JWT token hiện tại
     * @returns String JWT access token
     * @author: K24DTCN210-NVMANH (23/11/2025 14:46)
     * --------------------------------------------------- */
    public String getAuthToken() {
        return this.authToken;
    }

    /* ---------------------------------------------------
     * Login authentication
     * POST /api/auth/login
     * @param email Email của user
     * @param password Password
     * @returns JWT token
     * @author: K24DTCN210-NVMANH (24/11/2025 08:03)
     * --------------------------------------------------- */
    public String login(String email, String password) throws IOException, InterruptedException {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", email); // Backend yêu cầu field "username" thay vì "email"
        requestBody.put("password", password);
        
        String jsonBody = gson.toJson(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Map<String, Object> responseMap = gson.fromJson(response.body(), 
                    new TypeToken<Map<String, Object>>(){}.getType());
            String token = (String) responseMap.get("token");
            this.authToken = token;
            logger.info("Login successful for email: {}", email);
            return token;
        } else {
            logger.error("Login failed. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            throw new IOException("Login failed: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Lấy danh sách đề thi available cho student
     * GET /api/exam-taking/available
     * @returns List<ExamInfoDTO> danh sách đề thi
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public List<ExamInfoDTO> getAvailableExams() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/available"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<ExamInfoDTO>>(){}.getType();
            List<ExamInfoDTO> exams = gson.fromJson(response.body(), listType);
            logger.info("Retrieved {} available exams", exams.size());
            return exams;
        } else {
            logger.error("Failed to get available exams. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            throw new IOException("Failed to get available exams: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Bắt đầu làm bài thi
     * POST /api/exam-taking/start/{examId}
     * @param examId ID của đề thi
     * @returns Map chứa submissionId và questions
     * @throws ExamStartException nếu có lỗi từ backend (chứa error message)
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 11:50) - Improved error handling
     * --------------------------------------------------- */
    public StartExamResponse startExam(Long examId) throws IOException, InterruptedException, ExamStartException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/start/" + examId))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            StartExamResponse result = gson.fromJson(response.body(), StartExamResponse.class);
            logger.info("Started exam {}. SubmissionId: {}", examId, result.getSubmissionId());
            return result;
        } else {
            // Parse error message from response body
            String errorMessage = "Không thể bắt đầu bài thi";
            try {
                Map<String, Object> errorBody = gson.fromJson(response.body(), 
                        new TypeToken<Map<String, Object>>(){}.getType());
                if (errorBody != null && errorBody.containsKey("message")) {
                    errorMessage = (String) errorBody.get("message");
                }
            } catch (Exception e) {
                // If can't parse, use status code
                errorMessage = "Lỗi HTTP " + response.statusCode();
            }
            
            logger.error("Failed to start exam. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            
            throw new ExamStartException(errorMessage, response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Lưu câu trả lời
     * POST /api/exam-taking/save-answer/{submissionId}
     * @param submissionId ID của submission
     * @param questionId ID của câu hỏi
     * @param answerText Câu trả lời
     * @param isAutoSave true nếu là auto-save
     * @returns true nếu thành công
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public boolean saveAnswer(Long submissionId, Long questionId, 
                             String answerText, boolean isAutoSave) 
                             throws IOException, InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("questionId", questionId);
        requestBody.put("answerText", answerText);
        requestBody.put("isAutoSave", isAutoSave);
        
        String jsonBody = gson.toJson(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/save-answer/" + submissionId))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            logger.debug("Saved answer for question {} (autoSave: {})", 
                    questionId, isAutoSave);
            return true;
        } else {
            logger.error("Failed to save answer. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            return false;
        }
    }

    /* ---------------------------------------------------
     * Lưu câu trả lời (overload với SaveAnswerRequest)
     * POST /api/exam-taking/save-answer/{submissionId}
     * @param submissionId ID của submission
     * @param request SaveAnswerRequest object
     * @author: K24DTCN210-NVMANH (23/11/2025 13:51)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 15:26) - Added detailed logging
     * --------------------------------------------------- */
    public void saveAnswer(Long submissionId, SaveAnswerRequest request) 
                          throws IOException, InterruptedException {
        String jsonBody = gson.toJson(request);
        
        logger.info("[API] Saving answer - SubmissionId: {}, QuestionId: {}, AutoSave: {}", 
            submissionId, request.getQuestionId(), request.getIsAutoSave());
        logger.debug("[API] Request body: {}", jsonBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/save-answer/" + submissionId))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            logger.info("[API] Save answer SUCCESS - Status: 200, QuestionId: {}", 
                request.getQuestionId());
            logger.debug("[API] Response body: {}", response.body());
        } else {
            logger.error("[API] Save answer FAILED - Status: {}, QuestionId: {}, Body: {}", 
                    response.statusCode(), request.getQuestionId(), response.body());
            throw new IOException("Failed to save answer: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Lấy danh sách questions cho submission
     * GET /api/exam-taking/questions/{submissionId}
     * @param submissionId ID của submission
     * @returns List<QuestionDTO> danh sách câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 13:51)
     * --------------------------------------------------- */
    public List<QuestionDTO> getQuestionsForSubmission(Long submissionId) 
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/questions/" + submissionId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<QuestionDTO>>(){}.getType();
            List<QuestionDTO> questions = gson.fromJson(response.body(), listType);
            logger.info("Retrieved {} questions for submission {}", 
                    questions.size(), submissionId);
            return questions;
        } else {
            logger.error("Failed to get questions. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            throw new IOException("Failed to get questions: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Nộp bài thi
     * POST /api/exam-taking/submit/{submissionId}
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (23/11/2025 13:51)
     * --------------------------------------------------- */
    public void submitExam(Long submissionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/submit/" + submissionId))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            logger.info("Submitted exam. SubmissionId: {}", submissionId);
        } else {
            logger.error("Failed to submit exam. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            throw new IOException("Failed to submit exam: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Lấy kết quả bài thi
     * GET /api/exam-taking/results/{submissionId}
     * @param submissionId ID của submission
     * @returns ExamResultResponse chứa điểm và answers
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public ExamResultResponse getExamResult(Long submissionId) 
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/exam-taking/result/" + submissionId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            ExamResultResponse result = gson.fromJson(response.body(), 
                    ExamResultResponse.class);
            logger.info("Retrieved exam result. Score: {}", result.getTotalScore());
            return result;
        } else {
            logger.error("Failed to get exam result. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
            throw new IOException("Failed to get exam result: " + response.statusCode());
        }
    }

    /* ---------------------------------------------------
     * Test health check endpoint
     * @returns true nếu backend alive
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            boolean success = response.statusCode() == 200;
            logger.info("Connection test: {}", success ? "SUCCESS" : "FAILED");
            return success;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }

    /* ---------------------------------------------------
     * Response class cho Exam Result API
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public static class ExamResultResponse {
        private Long submissionId;
        private String examTitle;
        private Double totalScore;
        private Double maxScore;
        private String status;
        private LocalDateTime submittedAt;
        private List<AnswerResult> answers;

        public Long getSubmissionId() { return submissionId; }
        public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

        public Double getTotalScore() { return totalScore; }
        public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

        public Double getMaxScore() { return maxScore; }
        public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

        public List<AnswerResult> getAnswers() { return answers; }
        public void setAnswers(List<AnswerResult> answers) { this.answers = answers; }
    }

    /* ---------------------------------------------------
     * Answer result inner class
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    public static class AnswerResult {
        private Long questionId;
        private String questionContent;
        private String studentAnswer;
        private String correctAnswer;
        private Double score;
        private Double maxScore;
        private String feedback;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

        public String getStudentAnswer() { return studentAnswer; }
        public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public Double getMaxScore() { return maxScore; }
        public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    /* ---------------------------------------------------
     * Custom exception cho startExam errors
     * @author: K24DTCN210-NVMANH (24/11/2025 11:50)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 12:16) - Added isMaxAttemptsError()
     * --------------------------------------------------- */
    public static class ExamStartException extends Exception {
        private final int statusCode;
        
        public ExamStartException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public boolean isActiveSubmissionError() {
            return statusCode == 400 && getMessage().toLowerCase().contains("active submission");
        }
        
        public boolean isMaxAttemptsError() {
            return statusCode == 400 && getMessage().toLowerCase().contains("maximum attempts");
        }
    }

    /* ---------------------------------------------------
     * Gson TypeAdapter cho LocalDateTime
     * @author: K24DTCN210-NVMANH (23/11/2025 11:59)
     * --------------------------------------------------- */
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
