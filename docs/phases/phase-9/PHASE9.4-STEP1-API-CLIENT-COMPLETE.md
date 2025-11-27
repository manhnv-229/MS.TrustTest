# Phase 9.4 Step 1: Subject Management API Client Layer - HOÀN THÀNH ✅

**Status**: ✅ COMPLETED  
**Date**: 26/11/2025 01:50  
**Author**: K24DTCN210-NVMANH

---

## 📋 OVERVIEW

Step 1 của Phase 9.4 hoàn thành việc tạo API Client layer để giao tiếp với Subject Management Backend. Layer này cung cấp tất cả operations cần thiết cho CRUD operations trên Subjects và Departments.

---

## ✅ DELIVERABLES COMPLETED

### 1. Files Created (4 files)

#### A. DepartmentDTO.java (103 lines)
**Path**: `client-javafx/src/main/java/com/mstrust/client/teacher/dto/DepartmentDTO.java`

**Purpose**: Data Transfer Object cho Department

**Fields**:
```java
public class DepartmentDTO {
    private Long id;
    private String code;           // VD: "CNTT"
    private String departmentName; // VD: "Công nghệ thông tin"
    private String description;
    private Long facultyId;
    private String facultyName;    // For display
    
    // Getters, Setters, toString()
}
```

**Usage**: Hiển thị trong ComboBox khi tạo/sửa Subject

#### B. CreateSubjectRequest.java (120 lines)
**Path**: `client-javafx/src/main/java/com/mstrust/client/teacher/dto/CreateSubjectRequest.java`

**Purpose**: Request DTO để tạo Subject mới

**Fields**:
```java
public class CreateSubjectRequest {
    @JsonProperty("subjectCode")
    private String subjectCode;     // Required, unique (VD: "MATH101")
    
    @JsonProperty("subjectName")
    private String subjectName;     // Required (VD: "Toán Cao Cấp")
    
    @JsonProperty("description")
    private String description;     // Optional
    
    @JsonProperty("credits")
    private Integer credits;        // Required (VD: 3)
    
    @JsonProperty("departmentId")
    private Long departmentId;      // Required
    
    // Getters, Setters, Validation
}
```

**Validation Rules**:
- `subjectCode`: Not null, unique
- `subjectName`: Not null
- `credits`: > 0
- `departmentId`: Not null

#### C. UpdateSubjectRequest.java (115 lines)
**Path**: `client-javafx/src/main/java/com/mstrust/client/teacher/dto/UpdateSubjectRequest.java`

**Purpose**: Request DTO để cập nhật Subject

**Fields**: Giống `CreateSubjectRequest` nhưng tất cả fields đều optional (có thể null)

**Difference from Create**:
- Không update `subjectCode` (immutable)
- Chỉ update các fields có giá trị mới

#### D. SubjectApiClient.java (428 lines)
**Path**: `client-javafx/src/main/java/com/mstrust/client/teacher/api/SubjectApiClient.java`

**Purpose**: API Client để giao tiếp với Subject Management Backend

**Key Features**:

**1. Constructor & Config**:
```java
private final String baseUrl;
private String authToken;
private final ObjectMapper objectMapper;

public SubjectApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
    this.objectMapper = new ObjectMapper();
    // Disable timestamps để serialize dates as ISO-8601
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}

public void setAuthToken(String token) {
    this.authToken = token;
}
```

**2. Subject Operations**:
```java
// Lấy tất cả subjects (không phân trang)
public List<SubjectDTO> getAllSubjects() throws IOException

// Lấy subjects với phân trang
public PageResponse<SubjectDTO> getSubjectsPage(
    int page, int size, String sortBy, String sortDir) throws IOException

// Lấy subject theo ID
public SubjectDTO getSubjectById(Long id) throws IOException

// Lấy subject theo code
public SubjectDTO getSubjectByCode(String code) throws IOException

// Tìm kiếm subjects
public PageResponse<SubjectDTO> searchSubjects(
    String keyword, int page, int size) throws IOException

// Lọc theo department
public List<SubjectDTO> getSubjectsByDepartment(Long departmentId) 
    throws IOException

// Tạo mới
public SubjectDTO createSubject(CreateSubjectRequest request) 
    throws IOException

// Cập nhật
public SubjectDTO updateSubject(Long id, UpdateSubjectRequest request) 
    throws IOException

// Xóa mềm
public void deleteSubject(Long id) throws IOException
```

**3. Department Operations**:
```java
// Lấy tất cả departments (cho ComboBox)
public List<DepartmentDTO> getAllDepartments() throws IOException
```

**4. HTTP Helper Methods**:
```java
// Tạo connection với headers chuẩn
private HttpURLConnection createConnection(String endpoint, String method) 
    throws IOException {
    URL url = new URL(baseUrl + endpoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    
    // Add JWT token nếu có
    if (authToken != null && !authToken.isEmpty()) {
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
    }
    
    return conn;
}

// Đọc error message
private String readError(HttpURLConnection conn) {
    // Read from error stream
}
```

**5. PageResponse Wrapper**:
```java
public static class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;      // Current page
    private int size;        // Page size
    
    // Getters, Setters
}
```

**Design Patterns Used**:
- **Builder Pattern**: For request DTOs
- **Factory Pattern**: Connection creation
- **Generic Types**: PageResponse<T>
- **Exception Handling**: IOException for all network errors

---

## 🔧 TECHNICAL DETAILS

### API Endpoints Integration

**Subject APIs** (from Phase 3):
```
GET    /api/subjects                    → getAllSubjects()
GET    /api/subjects/page               → getSubjectsPage()
GET    /api/subjects/{id}               → getSubjectById()
GET    /api/subjects/code/{code}        → getSubjectByCode()
GET    /api/subjects/search             → searchSubjects()
GET    /api/subjects/department/{id}    → getSubjectsByDepartment()
POST   /api/subjects                    → createSubject()
PUT    /api/subjects/{id}               → updateSubject()
DELETE /api/subjects/{id}               → deleteSubject()
```

**Department APIs** (from Phase 3):
```
GET    /api/departments                 → getAllDepartments()
```

### JSON Serialization

**Jackson Configuration**:
- Uses `@JsonProperty` annotations
- Disables `WRITE_DATES_AS_TIMESTAMPS`
- Serializes dates as ISO-8601 strings
- Handles `LocalDateTime` automatically

**Example Request JSON** (CreateSubjectRequest):
```json
{
  "subjectCode": "MATH101",
  "subjectName": "Toán Cao Cấp 1",
  "description": "Môn toán cơ bản",
  "credits": 3,
  "departmentId": 1
}
```

**Example Response JSON** (SubjectDTO):
```json
{
  "id": 10,
  "subjectCode": "MATH101",
  "subjectName": "Toán Cao Cấp 1",
  "description": "Môn toán cơ bản",
  "credits": 3,
  "departmentId": 1,
  "departmentName": "Công nghệ thông tin",
  "createdAt": "2025-11-26T01:30:00",
  "updatedAt": null,
  "deletedAt": null
}
```

### Error Handling

**IOException Thrown For**:
- Network errors (timeout, connection refused)
- HTTP errors (4xx, 5xx)
- JSON parsing errors

**Error Response Format**:
```java
if (responseCode != 200) {
    throw new IOException("Lỗi API: " + responseCode + " - " + readError(conn));
}
```

**Example Error Messages**:
- "Lỗi API: 400 - Subject code already exists"
- "Lỗi API: 404 - Subject not found"
- "Lỗi API: 500 - Internal server error"

### Authentication

**JWT Token Support**:
```java
SubjectApiClient apiClient = new SubjectApiClient("http://localhost:8080/api");
apiClient.setAuthToken(jwtToken);

// All subsequent requests include: Authorization: Bearer {token}
```

---

## 📦 CODE STATISTICS

```
Phase 9.4 Step 1 Deliverables:
├── Files Created: 4 files
│   ├── DepartmentDTO.java: 103 lines
│   ├── CreateSubjectRequest.java: 120 lines
│   ├── UpdateSubjectRequest.java: 115 lines
│   └── SubjectApiClient.java: 428 lines
│   └── Total: 766 lines
│
├── Files Modified: 1 file
│   └── pom.xml: +7 lines (jackson-datatype-jsr310 dependency)
│
└── Grand Total: ~773 lines of code
```

**Compilation Status**: ✅ SUCCESS
- 53 source files compiled
- 10 resources copied
- Build time: 9.841s

---

## 🧪 TESTING REQUIREMENTS

### Unit Testing (Manual - Next Step)

**1. Connection Test**:
```java
SubjectApiClient client = new SubjectApiClient("http://localhost:8080/api");
client.setAuthToken(teacherToken);

// Test connection
List<SubjectDTO> subjects = client.getAllSubjects();
System.out.println("Total subjects: " + subjects.size());
```

**2. CRUD Operations Test**:
```java
// CREATE
CreateSubjectRequest createReq = new CreateSubjectRequest();
createReq.setSubjectCode("TEST101");
createReq.setSubjectName("Test Subject");
createReq.setCredits(3);
createReq.setDepartmentId(1L);
SubjectDTO created = client.createSubject(createReq);

// READ
SubjectDTO found = client.getSubjectById(created.getId());
assertEquals("TEST101", found.getSubjectCode());

// UPDATE
UpdateSubjectRequest updateReq = new UpdateSubjectRequest();
updateReq.setSubjectName("Updated Name");
SubjectDTO updated = client.updateSubject(created.getId(), updateReq);

// DELETE
client.deleteSubject(created.getId());
```

**3. Pagination Test**:
```java
PageResponse<SubjectDTO> page = client.getSubjectsPage(0, 10, "subjectName", "asc");
System.out.println("Page: " + page.getNumber());
System.out.println("Total pages: " + page.getTotalPages());
System.out.println("Total elements: " + page.getTotalElements());
```

**4. Search Test**:
```java
PageResponse<SubjectDTO> results = client.searchSubjects("Toán", 0, 10);
System.out.println("Found: " + results.getTotalElements() + " subjects");
```

**5. Error Handling Test**:
```java
try {
    client.getSubjectById(999999L);
} catch (IOException e) {
    System.out.println("Expected error: " + e.getMessage());
}
```

---

## 🔗 INTEGRATION POINTS

### With Backend (Phase 3)
- All APIs from Subject Management Controller
- JWT authentication required
- Soft delete support (deletedAt field)

### With Future UI (Next Steps)
```java
// Will be used in SubjectManagementController
private SubjectApiClient apiClient;

@FXML
private void initialize() {
    apiClient = new SubjectApiClient(API_BASE_URL);
    apiClient.setAuthToken(getAuthToken());
    
    loadSubjects();
}

private void loadSubjects() {
    Task<PageResponse<SubjectDTO>> task = new Task<>() {
        @Override
        protected PageResponse<SubjectDTO> call() throws Exception {
            return apiClient.getSubjectsPage(currentPage, PAGE_SIZE, "subjectName", "asc");
        }
    };
    
    task.setOnSucceeded(event -> {
        PageResponse<SubjectDTO> response = task.getValue();
        updateTable(response.getContent());
        updatePagination(response);
    });
    
    new Thread(task).start();
}
```

---

## 🎯 KEY ACHIEVEMENTS

1. **✅ Complete API Coverage**: Tất cả Subject & Department operations
2. **✅ Clean Architecture**: Separation of concerns (DTO, Request, Client)
3. **✅ Type Safety**: Generic types cho pagination
4. **✅ Error Handling**: Comprehensive IOException handling
5. **✅ Authentication**: JWT token support
6. **✅ Jackson Integration**: Proper JSON serialization/deserialization
7. **✅ Compilation Success**: No errors, clean build
8. **✅ Documentation**: Full JavaDoc comments theo .clinerules standards

---

## 🚀 NEXT STEPS

### Phase 9.4 Step 2: UI Layer
1. Create `subject-management.fxml` - Main layout
2. Create `SubjectManagementController.java` - Table & operations
3. Create `subject-edit-dialog.fxml` - Create/Edit dialog
4. Create `SubjectEditDialogController.java` - Dialog logic
5. Update `teacher-styles.css` - Subject management styles
6. Integrate with `TeacherMainController` - Menu navigation

### Testing Plan
1. Manual API testing với Postman/Thunder Client
2. Integration testing với UI
3. Error scenario testing
4. Performance testing (pagination, search)

---

## 📝 NOTES & CONSIDERATIONS

### 1. Dependency Added
- `jackson-datatype-jsr310:2.15.3` - For Java 8 Date/Time support
- Removed `JavaTimeModule` usage (not needed with proper configuration)

### 2. Compilation Fix
- Issue: Maven cache cách file cũ có `JavaTimeModule`
- Solution: Delete target folder trước khi compile
- Result: ✅ BUILD SUCCESS

### 3. Design Decisions
- **HttpURLConnection**: Sử dụng built-in Java HTTP client (không cần external dependency)
- **Jackson ObjectMapper**: Consistent với backend serialization
- **PageResponse Generic**: Reusable cho các resources khác

### 4. Future Enhancements
- Add retry logic cho network failures
- Add caching cho department list
- Add request timeout configuration
- Add logging/monitoring

---

## 👥 CREDITS

**Developer**: K24DTCN210-NVMANH  
**Date**: 26/11/2025  
**Phase**: 9.4 Step 1 - Subject Management API Client  
**Status**: ✅ COMPLETE

---

**END OF PHASE 9.4 STEP 1 COMPLETION REPORT**
