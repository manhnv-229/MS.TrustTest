package com.mstrust.client.exam.service;

import com.google.gson.Gson;
import com.mstrust.client.exam.api.ExamApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* ---------------------------------------------------
 * Client Log Service - Gửi log lỗi từ client về backend
 * 
 * @author: K24DTCN210-NVMANH (04/12/2025 22:15)
 * --------------------------------------------------- */
public class ClientLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientLogService.class);
    private static final String LOG_ENDPOINT = "/api/system-logs";
    
    private final ExamApiClient apiClient;
    private final Gson gson;
    private final ExecutorService executorService;
    private final HttpClient httpClient;
    
    public ClientLogService(ExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ClientLog-Sender");
            t.setDaemon(true);
            return t;
        });
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /* ---------------------------------------------------
     * Log error to backend (asynchronous)
     * @param source Nguồn lỗi (VD: AutoSaveService)
     * @param message Thông báo lỗi
     * @param throwable Exception (nếu có)
     * @param submissionId ID bài thi (nếu có)
     * @param additionalData Dữ liệu bổ sung (JSON string hoặc object)
     * --------------------------------------------------- */
    public void logError(String source, String message, Throwable throwable, Long submissionId, Object additionalData) {
        executorService.submit(() -> {
            try {
                String stackTrace = null;
                if (throwable != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    stackTrace = sw.toString();
                }
                
                String additionalDataStr = null;
                if (additionalData != null) {
                    if (additionalData instanceof String) {
                        additionalDataStr = (String) additionalData;
                    } else {
                        additionalDataStr = gson.toJson(additionalData);
                    }
                }
                
                Map<String, Object> logRequest = new HashMap<>();
                logRequest.put("level", "ERROR");
                logRequest.put("source", source);
                logRequest.put("message", message);
                logRequest.put("stackTrace", stackTrace);
                logRequest.put("submissionId", submissionId);
                logRequest.put("additionalData", additionalDataStr);
                
                sendToBackend(logRequest);
                
            } catch (Exception e) {
                logger.error("Failed to send error log to backend: {}", e.getMessage());
            }
        });
    }
    
    private void sendToBackend(Map<String, Object> logRequest) throws IOException, InterruptedException {
        String jsonBody = gson.toJson(logRequest);
        String baseUrl = apiClient.getBaseUrl(); // Cần đảm bảo ExamApiClient có phương thức này hoặc tự construct
        if (baseUrl == null || baseUrl.isEmpty()) {
             // Fallback nếu không lấy được base URL từ apiClient (vì field thường private)
             // Giả định localhost hoặc config
             baseUrl = "http://localhost:8080"; 
        }
        
        // Nếu apiClient expose baseUrl thì tốt, nếu không ta dùng reflection hoặc hardcode tạm thời cho dev env
        // Tốt nhất là thêm getBaseUrl vào ExamApiClient, nhưng để tránh sửa nhiều file, ta thử dùng baseUrl từ config.properties nếu có.
        // Ở đây tôi sẽ dùng URI resolve từ apiClient nếu có thể, nhưng ExamApiClient dùng okhttp/apache bên trong.
        // Cách đơn giản nhất: assume apiClient đã init đúng, ta dùng lại token của nó.
        
        // Tuy nhiên, ExamApiClient.java chưa chắc expose getBaseUrl().
        // Tôi sẽ dùng hardcode URL + context path "/api" vì project này đang chạy localhost:8080
        // Cần cẩn thận với môi trường PROD.
        
        String url = baseUrl + LOG_ENDPOINT;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiClient.getAuthToken())
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();
            
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            logger.error("Backend rejected log: {} - {}", response.statusCode(), response.body());
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
