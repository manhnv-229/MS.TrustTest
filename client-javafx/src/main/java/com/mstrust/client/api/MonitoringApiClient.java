package com.mstrust.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.dto.ActivityLogRequest;
import com.mstrust.client.dto.AlertCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/* ---------------------------------------------------
 * Client API để giao tiếp với backend monitoring endpoints
 * Sử dụng Java 11+ HttpClient
 * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
 * --------------------------------------------------- */
public class MonitoringApiClient {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringApiClient.class);
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;
    private String authToken;

    /* ---------------------------------------------------
     * Constructor - khởi tạo HTTP client và Gson
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
     * --------------------------------------------------- */
    public MonitoringApiClient() {
        AppConfig config = AppConfig.getInstance();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getApiTimeoutSeconds()))
                .build();
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        
        this.baseUrl = config.getApiBaseUrl();
        
        logger.info("MonitoringApiClient initialized with baseUrl: {}", baseUrl);
    }

    /* ---------------------------------------------------
     * Set auth token cho các requests
     * @param token JWT token
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
     * --------------------------------------------------- */
    public void setAuthToken(String token) {
        this.authToken = token;
        logger.info("Auth token set");
    }

    /* ---------------------------------------------------
     * Upload screenshot lên backend
     * @param imagePath Path đến file ảnh
     * @param submissionId ID bài làm
     * @param screenResolution Độ phân giải màn hình
     * @param windowTitle Tiêu đề cửa sổ active
     * @returns true nếu thành công
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
     * --------------------------------------------------- */
    public boolean uploadScreenshot(Path imagePath, Long submissionId, 
                                   String screenResolution, String windowTitle) {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            
            String boundary = "----" + UUID.randomUUID().toString().replace("-", "");
            
            StringBuilder bodyBuilder = new StringBuilder();
            
            // Add file part
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(imagePath.getFileName()).append("\"\r\n");
            bodyBuilder.append("Content-Type: image/jpeg\r\n\r\n");
            
            String bodyStart = bodyBuilder.toString();
            
            // Add other form fields
            StringBuilder bodyEnd = new StringBuilder();
            bodyEnd.append("\r\n");
            
            bodyEnd.append("--").append(boundary).append("\r\n");
            bodyEnd.append("Content-Disposition: form-data; name=\"submissionId\"\r\n\r\n");
            bodyEnd.append(submissionId).append("\r\n");
            
            if (screenResolution != null) {
                bodyEnd.append("--").append(boundary).append("\r\n");
                bodyEnd.append("Content-Disposition: form-data; name=\"screenResolution\"\r\n\r\n");
                bodyEnd.append(screenResolution).append("\r\n");
            }
            
            if (windowTitle != null) {
                bodyEnd.append("--").append(boundary).append("\r\n");
                bodyEnd.append("Content-Disposition: form-data; name=\"windowTitle\"\r\n\r\n");
                bodyEnd.append(windowTitle).append("\r\n");
            }
            
            bodyEnd.append("--").append(boundary).append("--\r\n");
            
            // Combine parts
            byte[] bodyStartBytes = bodyStart.getBytes();
            byte[] bodyEndBytes = bodyEnd.toString().getBytes();
            byte[] body = new byte[bodyStartBytes.length + imageBytes.length + bodyEndBytes.length];
            
            System.arraycopy(bodyStartBytes, 0, body, 0, bodyStartBytes.length);
            System.arraycopy(imageBytes, 0, body, bodyStartBytes.length, imageBytes.length);
            System.arraycopy(bodyEndBytes, 0, body, bodyStartBytes.length + imageBytes.length, bodyEndBytes.length);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/monitoring/screenshots"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                logger.info("Screenshot uploaded successfully");
                return true;
            } else {
                logger.error("Failed to upload screenshot. Status: {}, Body: {}", 
                        response.statusCode(), response.body());
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error uploading screenshot", e);
            return false;
        }
    }

    /* ---------------------------------------------------
     * Gửi batch activities lên backend
     * @param request ActivityLogRequest chứa danh sách activities
     * @returns true nếu thành công
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
     * --------------------------------------------------- */
    public boolean logActivities(ActivityLogRequest request) {
        try {
            String jsonBody = gson.toJson(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/monitoring/activities"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            // Backend returns 201 (CREATED) for successful activity log
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                logger.info("Activities logged successfully. Count: {}", 
                        request.getActivities().size());
                return true;
            } else {
                logger.error("Failed to log activities. Status: {}, Body: {}", 
                        response.statusCode(), response.body());
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error logging activities", e);
            return false;
        }
    }

    /* ---------------------------------------------------
     * Tạo alert mới
     * @param request AlertCreateRequest
     * @returns true nếu thành công
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
     * --------------------------------------------------- */
    public boolean createAlert(AlertCreateRequest request) {
        try {
            String jsonBody = gson.toJson(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/monitoring/alerts"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                logger.warn("Alert created: {} - {}", request.getAlertType(), 
                        request.getDescription());
                return true;
            } else {
                logger.error("Failed to create alert. Status: {}, Body: {}", 
                        response.statusCode(), response.body());
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error creating alert", e);
            return false;
        }
    }

    /* ---------------------------------------------------
     * Test connection đến backend
     * @returns true nếu kết nối thành công
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
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
     * Gson TypeAdapter cho LocalDateTime
     * @author: K24DTCN210-NVMANH (21/11/2025 10:43)
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
