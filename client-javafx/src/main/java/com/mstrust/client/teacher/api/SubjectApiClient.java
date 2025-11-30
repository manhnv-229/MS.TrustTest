package com.mstrust.client.teacher.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mstrust.client.teacher.dto.CreateSubjectRequest;
import com.mstrust.client.teacher.dto.DepartmentDTO;
import com.mstrust.client.teacher.dto.UpdateSubjectRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/* ---------------------------------------------------
 * API Client để giao tiếp với Subject Management Backend
 * Hỗ trợ tất cả operations: GET, POST, PUT, DELETE
 * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
 * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Rewrite without TypeReference để fix module access
 * --------------------------------------------------- */
public class SubjectApiClient {
    
    private final String baseUrl;
    private String authToken;
    private final ObjectMapper objectMapper;
    private final JavaType subjectListType;
    private final JavaType departmentListType;
    private final JavaType pageResponseType;

    /* ---------------------------------------------------
     * Constructor khởi tạo với base URL
     * @param baseUrl Base URL của backend API (VD: http://localhost:8080/api)
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Pre-build JavaType để tránh anonymous classes
     * --------------------------------------------------- */
    public SubjectApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule để hỗ trợ Java 8 Date/Time (LocalDateTime, LocalDate, ...)
        this.objectMapper.registerModule(new JavaTimeModule());
        // Disable WRITE_DATES_AS_TIMESTAMPS để serialize dates as ISO-8601 strings
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Pre-build JavaType objects (thay vì dùng anonymous TypeReference)
        this.subjectListType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, com.mstrust.client.teacher.dto.SubjectDTO.class);
        this.departmentListType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, DepartmentDTO.class);
        this.pageResponseType = objectMapper.getTypeFactory()
            .constructParametricType(PageResponse.class, com.mstrust.client.teacher.dto.SubjectDTO.class);
    }

    /* ---------------------------------------------------
     * Set JWT token để authenticate requests
     * @param token JWT token từ login
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /* ---------------------------------------------------
     * Lấy tất cả subjects (không phân trang)
     * @return Danh sách tất cả subjects
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Dùng JavaType thay vì TypeReference
     * --------------------------------------------------- */
    public List<com.mstrust.client.teacher.dto.SubjectDTO> getAllSubjects() throws IOException {
        HttpURLConnection conn = createConnection("/subjects", "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(conn.getInputStream(), subjectListType);
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Lấy subjects với phân trang
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số items mỗi trang
     * @param sortBy Trường để sort (VD: "subjectName")
     * @param sortDir Hướng sort ("asc" hoặc "desc")
     * @return PageResponse chứa subjects và thông tin phân trang
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Dùng JavaType thay vì TypeReference
     * --------------------------------------------------- */
    public PageResponse<com.mstrust.client.teacher.dto.SubjectDTO> getSubjectsPage(
            int page, int size, String sortBy, String sortDir) throws IOException {
        
        String endpoint = String.format(
            "/subjects/page?page=%d&size=%d&sortBy=%s&sortDir=%s",
            page, size, sortBy, sortDir
        );
        
        HttpURLConnection conn = createConnection(endpoint, "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(conn.getInputStream(), pageResponseType);
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Lấy subject theo ID
     * @param id ID của subject
     * @return SubjectDTO
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public com.mstrust.client.teacher.dto.SubjectDTO getSubjectById(Long id) throws IOException {
        HttpURLConnection conn = createConnection("/subjects/" + id, "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(
                conn.getInputStream(), 
                com.mstrust.client.teacher.dto.SubjectDTO.class
            );
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Lấy subject theo code
     * @param code Mã môn học (VD: "MATH101")
     * @return SubjectDTO
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public com.mstrust.client.teacher.dto.SubjectDTO getSubjectByCode(String code) throws IOException {
        HttpURLConnection conn = createConnection("/subjects/code/" + code, "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(
                conn.getInputStream(), 
                com.mstrust.client.teacher.dto.SubjectDTO.class
            );
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Tìm kiếm subjects theo keyword
     * @param keyword Từ khóa tìm kiếm (tên hoặc mã môn)
     * @param page Số trang
     * @param size Số items mỗi trang
     * @return PageResponse chứa kết quả tìm kiếm
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Dùng JavaType thay vì TypeReference
     * --------------------------------------------------- */
    public PageResponse<com.mstrust.client.teacher.dto.SubjectDTO> searchSubjects(
            String keyword, int page, int size) throws IOException {
        
        String endpoint = String.format(
            "/subjects/search?keyword=%s&page=%d&size=%d",
            keyword, page, size
        );
        
        HttpURLConnection conn = createConnection(endpoint, "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(conn.getInputStream(), pageResponseType);
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Lọc subjects theo department
     * @param departmentId ID của department
     * @return Danh sách subjects trong department đó
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Dùng JavaType thay vì TypeReference
     * --------------------------------------------------- */
    public List<com.mstrust.client.teacher.dto.SubjectDTO> getSubjectsByDepartment(Long departmentId) throws IOException {
        HttpURLConnection conn = createConnection("/subjects/department/" + departmentId, "GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(conn.getInputStream(), subjectListType);
        } else {
            throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Tạo mới subject
     * @param request CreateSubjectRequest chứa thông tin subject mới
     * @return SubjectDTO đã được tạo
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public com.mstrust.client.teacher.dto.SubjectDTO createSubject(CreateSubjectRequest request) throws IOException {
        HttpURLConnection conn = createConnection("/subjects", "POST");
        conn.setDoOutput(true);
        
        // Write request body
        objectMapper.writeValue(conn.getOutputStream(), request);
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 201 || responseCode == 200) {
            return objectMapper.readValue(
                conn.getInputStream(), 
                com.mstrust.client.teacher.dto.SubjectDTO.class
            );
        } else {
            throw new IOException("Lỗi tạo subject: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Cập nhật subject
     * @param id ID của subject cần update
     * @param request UpdateSubjectRequest chứa thông tin mới
     * @return SubjectDTO đã được cập nhật
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public com.mstrust.client.teacher.dto.SubjectDTO updateSubject(Long id, UpdateSubjectRequest request) throws IOException {
        HttpURLConnection conn = createConnection("/subjects/" + id, "PUT");
        conn.setDoOutput(true);
        
        // Write request body
        objectMapper.writeValue(conn.getOutputStream(), request);
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return objectMapper.readValue(
                conn.getInputStream(), 
                com.mstrust.client.teacher.dto.SubjectDTO.class
            );
        } else {
            throw new IOException("Lỗi cập nhật subject: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Xóa mềm subject
     * @param id ID của subject cần xóa
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    public void deleteSubject(Long id) throws IOException {
        HttpURLConnection conn = createConnection("/subjects/" + id, "DELETE");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 204) {
            throw new IOException("Lỗi xóa subject: " + responseCode + " - " + readError(conn));
        }
    }

    /* ---------------------------------------------------
     * Lấy tất cả departments (dùng để populate ComboBox)
     * @return Danh sách tất cả departments
     * @throws IOException Lỗi network hoặc server
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 02:22) - Dùng JavaType thay vì TypeReference
     * --------------------------------------------------- */
    public List<DepartmentDTO> getAllDepartments() throws IOException {
        String endpoint = "/departments";
        String fullUrl = baseUrl + endpoint;
        
        System.out.println("[DEBUG SubjectApiClient] ============================================");
        System.out.println("[DEBUG SubjectApiClient] getAllDepartments() called");
        System.out.println("[DEBUG SubjectApiClient] BaseURL: " + baseUrl);
        System.out.println("[DEBUG SubjectApiClient] Endpoint: " + endpoint);
        System.out.println("[DEBUG SubjectApiClient] Full URL: " + fullUrl);
        System.out.println("[DEBUG SubjectApiClient] Has Token: " + (authToken != null && !authToken.isEmpty()));
        System.out.println("[DEBUG SubjectApiClient] ============================================");
        
        HttpURLConnection conn = createConnection(endpoint, "GET");
        
        int responseCode = conn.getResponseCode();
        System.out.println("[DEBUG SubjectApiClient] Response Code: " + responseCode);
        
        if (responseCode == 200) {
            return objectMapper.readValue(conn.getInputStream(), departmentListType);
        } else {
            String errorBody = readError(conn);
            System.out.println("[DEBUG SubjectApiClient] Error Body: " + errorBody);
            throw new IOException("Lỗi API: " + responseCode + " - " + errorBody);
        }
    }

    /* ---------------------------------------------------
     * Tạo HTTP connection với headers chuẩn
     * @param endpoint API endpoint (VD: "/subjects")
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @return HttpURLConnection đã cấu hình
     * @throws IOException Lỗi khi tạo connection
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    private HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        
        return conn;
    }

    /* ---------------------------------------------------
     * Đọc error message từ response
     * @param conn HttpURLConnection
     * @return Error message
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * --------------------------------------------------- */
    private String readError(HttpURLConnection conn) {
        try {
            if (conn.getErrorStream() != null) {
                return new String(conn.getErrorStream().readAllBytes());
            }
        } catch (IOException e) {
            // Ignore
        }
        return "Unknown error";
    }

    /* ---------------------------------------------------
     * Inner class để wrap paginated response
     * @author: K24DTCN210-NVMANH (26/11/2025 01:47)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 16:12) - Add @JsonIgnoreProperties để ignore "pageable" field
     * --------------------------------------------------- */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageResponse<T> {
        private List<T> content;
        private int totalPages;
        private long totalElements;
        private int number;
        private int size;

        public PageResponse() {}

        public List<T> getContent() {
            return content;
        }

        public void setContent(List<T> content) {
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

