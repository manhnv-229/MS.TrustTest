# System Patterns: MS.TrustTest

## Kiến Trúc Tổng Thể

### 1. Architecture Pattern: 3-Tier

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION TIER                         │
│                  (JavaFX Desktop Client)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Login UI   │  │   Exam UI    │  │  Admin UI    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────────────────────────────────────────┐       │
│  │         Anti-Cheat Monitoring Services           │       │
│  │  (Screen, Window, Process, Clipboard Monitor)    │       │
│  └──────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                           ↕ REST API / WebSocket
┌─────────────────────────────────────────────────────────────┐
│                     BUSINESS TIER                            │
│                  (Spring Boot Backend)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Auth Service │  │ Exam Service │  │Monitor Service│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ User Service │  │Grade Service │  │ Alert Service│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────────────────────────────────────────┐       │
│  │         Spring Security + JWT + WebSocket        │       │
│  └──────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                           ↕ JPA / JDBC
┌─────────────────────────────────────────────────────────────┐
│                      DATA TIER                               │
│                    (MySQL Database)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Users     │  │    Exams     │  │  Activities  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Questions   │  │  Submissions │  │ Screenshots  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## Design Patterns Áp Dụng

### 1. Repository Pattern (Data Access Layer)

**Mục đích**: Tách biệt logic truy cập dữ liệu khỏi business logic

**Implementation**:
```java
// Interface
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentCode(String studentCode);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
}

// Usage in Service
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User findByCredential(String credential) {
        // Try student code first
        Optional<User> user = userRepository.findByStudentCode(credential);
        if (user.isPresent()) return user.get();
        
        // Try email
        user = userRepository.findByEmail(credential);
        if (user.isPresent()) return user.get();
        
        // Try phone number
        return userRepository.findByPhoneNumber(credential)
            .orElseThrow(() -> new UserNotFoundException(credential));
    }
}
```

### 2. Service Layer Pattern

**Mục đích**: Tập trung business logic, dễ test và maintain

**Cấu trúc**:
```
Controller → Service → Repository → Database
    ↓          ↓
   DTO    Entity/Domain
```

**Example**:
```java
@RestController
@RequestMapping("/api/exams")
public class ExamController {
    @Autowired
    private ExamService examService;
    
    @PostMapping
    public ResponseEntity<ExamDTO> createExam(@RequestBody CreateExamRequest request) {
        ExamDTO exam = examService.createExam(request);
        return ResponseEntity.ok(exam);
    }
}

@Service
public class ExamService {
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Transactional
    public ExamDTO createExam(CreateExamRequest request) {
        // Business logic here
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setStartTime(request.getStartTime());
        // ... more logic
        
        Exam saved = examRepository.save(exam);
        return ExamMapper.toDTO(saved);
    }
}
```

### 3. DTO Pattern (Data Transfer Object)

**Mục đích**: 
- Tách biệt entity database khỏi API response
- Kiểm soát data expose ra ngoài
- Tránh lazy loading issues

**Implementation**:
```java
// Entity (Internal)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentCode;
    private String email;
    private String passwordHash;  // Không expose ra ngoài
    
    @OneToMany(mappedBy = "user")
    private List<ExamSubmission> submissions;
    // ... getters/setters
}

// DTO (External API)
public class UserDTO {
    private Long id;
    private String studentCode;
    private String email;
    private String fullName;
    // Không có passwordHash
    // Không có relationships để tránh N+1
}

// Mapper
public class UserMapper {
    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setStudentCode(user.getStudentCode());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}
```

### 4. Strategy Pattern (Monitoring Strategies)

**Mục đích**: Cho phép switch giữa các monitoring strategies

**Implementation**:
```java
// Strategy Interface
public interface MonitoringStrategy {
    void startMonitoring(ExamSession session);
    void stopMonitoring();
    List<Alert> checkViolations();
}

// Concrete Strategies
public class LowLevelMonitoring implements MonitoringStrategy {
    // Chỉ chụp màn hình mỗi 5 phút
}

public class HighLevelMonitoring implements MonitoringStrategy {
    // Chụp màn hình 30s, monitor tất cả
}

// Context
public class MonitoringService {
    private MonitoringStrategy strategy;
    
    public void setStrategy(MonitoringLevel level) {
        switch(level) {
            case LOW:
                this.strategy = new LowLevelMonitoring();
                break;
            case HIGH:
                this.strategy = new HighLevelMonitoring();
                break;
        }
    }
}
```

### 5. Observer Pattern (Real-time Alerts)

**Mục đích**: Notify admin khi có suspicious activity

**Implementation với WebSocket**:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

@Service
public class AlertService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public void notifyAlert(Alert alert) {
        // Broadcast to all admins
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        
        // Send to specific admin
        messagingTemplate.convertAndSendToUser(
            alert.getAdminId().toString(),
            "/queue/alerts",
            alert
        );
    }
}
```

### 6. Builder Pattern (Complex Object Creation)

**Mục đích**: Xây dựng object phức tạp một cách clear

**Implementation**:
```java
public class Exam {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private MonitoringLevel monitoringLevel;
    private boolean allowReview;
    private List<Question> questions;
    
    // Private constructor
    private Exam() {}
    
    public static class Builder {
        private Exam exam = new Exam();
        
        public Builder title(String title) {
            exam.title = title;
            return this;
        }
        
        public Builder startTime(LocalDateTime startTime) {
            exam.startTime = startTime;
            return this;
        }
        
        // ... more setters
        
        public Exam build() {
            // Validation
            if (exam.title == null) {
                throw new IllegalStateException("Title required");
            }
            return exam;
        }
    }
}

// Usage
Exam exam = new Exam.Builder()
    .title("Midterm Exam")
    .startTime(LocalDateTime.now())
    .duration(90)
    .monitoringLevel(MonitoringLevel.HIGH)
    .build();
```

### 7. Factory Pattern (Question Type Creation)

**Mục đích**: Create different types of questions

**Implementation**:
```java
public interface Question {
    QuestionType getType();
    boolean validate();
    double calculateScore(Answer answer);
}

public class MultipleChoiceQuestion implements Question {
    private List<String> options;
    private String correctAnswer;
    // ...
}

public class EssayQuestion implements Question {
    private String prompt;
    private double maxScore;
    // Manual grading required
}

public class QuestionFactory {
    public static Question createQuestion(QuestionType type) {
        switch(type) {
            case MULTIPLE_CHOICE:
                return new MultipleChoiceQuestion();
            case ESSAY:
                return new EssayQuestion();
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }
}
```

## Component Relationships

### 1. Authentication Flow

```
Client                Backend              Database
  |                      |                    |
  |-- POST /auth/login --|                    |
  |                      |-- Query user ------|
  |                      |<-- User data ------|
  |                      |                    |
  |                      |-- BCrypt verify ---|
  |                      |                    |
  |                      |-- Generate JWT ----|
  |<-- JWT token --------|                    |
  |                      |                    |
  |-- API calls ---------|                    |
  |  (with JWT header)   |                    |
  |                      |-- Verify JWT ------|
  |                      |                    |
```

### 2. Exam Taking Flow

```
Student Client          Backend              WebSocket           Admin Dashboard
     |                     |                     |                      |
     |-- Start exam -------|                     |                      |
     |                     |-- Create session ---|                      |
     |                     |                     |-- Notify ----------->|
     |<-- Exam data -------|                     |                      |
     |                     |                     |                      |
  [Monitoring starts]      |                     |                      |
     |                     |                     |                      |
     |-- Screenshot -------|                     |                      |
     |                     |-- Save & analyze ---|                      |
     |                     |                     |                      |
  [Suspicious detected]    |                     |                      |
     |                     |-- Create alert -----|                      |
     |                     |                     |-- Push alert ------->|
     |<-- Warning ---------|                     |                      |
     |                     |                     |                      |
```

### 3. Monitoring Architecture

```
┌─────────────────────────────────────────────────────┐
│              CLIENT MONITORS                         │
│  ┌──────────────┐  ┌──────────────┐                 │
│  │Screen Monitor│  │Window Monitor│                 │
│  └──────┬───────┘  └──────┬───────┘                 │
│         │                  │                         │
│         └──────────┬───────┘                         │
│                    ↓                                 │
│  ┌──────────────────────────────────┐               │
│  │    Monitoring Coordinator        │               │
│  │  - Collect data from monitors    │               │
│  │  - Detect violations              │               │
│  │  - Send to server                │               │
│  └──────────────────────────────────┘               │
└─────────────────────────────────────────────────────┘
                     ↓ WebSocket
┌─────────────────────────────────────────────────────┐
│              BACKEND SERVICES                        │
│  ┌──────────────────────────────────┐               │
│  │    Monitoring Service            │               │
│  │  - Receive monitoring data       │               │
│  │  - Analyze patterns              │               │
│  │  - Store screenshots             │               │
│  └──────────────┬───────────────────┘               │
│                 │                                    │
│                 ↓                                    │
│  ┌──────────────────────────────────┐               │
│  │    Alert Service                 │               │
│  │  - Evaluate severity             │               │
│  │  - Create alerts                 │               │
│  │  - Notify admins                 │               │
│  └──────────────────────────────────┘               │
└─────────────────────────────────────────────────────┘
```

## Security Patterns

### 1. JWT Authentication

```
┌─────────────────────────────────────────┐
│  JWT Structure                          │
├─────────────────────────────────────────┤
│  Header:                                │
│    - alg: "HS256"                       │
│    - typ: "JWT"                         │
├─────────────────────────────────────────┤
│  Payload:                               │
│    - sub: userId                        │
│    - roles: ["STUDENT", "USER"]         │
│    - exp: timestamp                     │
│    - iat: timestamp                     │
├─────────────────────────────────────────┤
│  Signature:                             │
│    HMACSHA256(base64(header) + "." +    │
│               base64(payload), secret)  │
└─────────────────────────────────────────┘
```

**Implementation**:
```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    public String generateToken(Authentication auth) {
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2. Role-Based Access Control (RBAC)

```
Permission Hierarchy:
ADMIN
  └── DEPT_MANAGER
      └── CLASS_MANAGER
          └── TEACHER
              └── STUDENT

Access Matrix:
┌─────────────────┬─────────┬──────────┬───────────────┬──────────────┬───────┐
│ Resource        │ STUDENT │ TEACHER  │ CLASS_MANAGER │ DEPT_MANAGER │ ADMIN │
├─────────────────┼─────────┼──────────┼───────────────┼──────────────┼───────┤
│ Take Exam       │   ✓     │    ✗     │      ✗        │      ✗       │   ✓   │
│ Create Exam     │   ✗     │    ✓     │      ✗        │      ✗       │   ✓   │
│ Grade Exam      │   ✗     │    ✓     │      ✗        │      ✗       │   ✓   │
│ Manage Students │   ✗     │    ✗     │      ✓        │      ✓       │   ✓   │
│ View Alerts     │   ✗     │    ✓     │      ✓        │      ✓       │   ✓   │
│ Config System   │   ✗     │    ✗     │      ✗        │      ✗       │   ✓   │
└─────────────────┴─────────┴──────────┴───────────────┴──────────────┴───────┘
```

**Implementation**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/exams/take/**").hasRole("STUDENT")
                .antMatchers("/api/exams/create/**").hasAnyRole("TEACHER", "ADMIN")
                .antMatchers("/api/students/**").hasAnyRole("CLASS_MANAGER", "DEPT_MANAGER", "ADMIN")
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```

## Database Patterns

### 1. Soft Delete Pattern

**Mục đích**: Không xóa thật data, chỉ đánh dấu deleted

```java
@Entity
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User {
    @Id
    private Long id;
    
    private String studentCode;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

### 2. Audit Pattern

**Mục đích**: Track who created/updated records

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

// Usage
@Entity
public class Exam extends AuditableEntity {
    // Automatically tracks who created/updated
}
```

### 3. Optimistic Locking

**Mục đích**: Prevent concurrent update conflicts

```java
@Entity
public class ExamSubmission {
    @Id
    private Long id;
    
    @Version
    private Long version;  // Auto-incremented on each update
    
    private String answers;
    
    // JPA will throw OptimisticLockException if version mismatch
}
```

## Error Handling Patterns

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ExamNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleExamNotActive(ExamNotActiveException ex) {
        ErrorResponse error = new ErrorResponse(
            "EXAM_NOT_ACTIVE",
            ex.getMessage(),
            HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Caching Strategy (Future)

```
┌─────────────────────────────────────────┐
│  Redis Cache Layers                     │
├─────────────────────────────────────────┤
│  L1: User sessions (JWT payload)        │
│      TTL: 24h                           │
├─────────────────────────────────────────┤
│  L2: Active exams list                  │
│      TTL: 5m, invalidate on update      │
├─────────────────────────────────────────┤
│  L3: Question bank                      │
│      TTL: 1h                            │
└─────────────────────────────────────────┘
```

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 13:59  
**Last Updated**: 13/11/2025 13:59
