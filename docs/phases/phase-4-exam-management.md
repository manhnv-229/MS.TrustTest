# Phase 4: Exam Management - Chi Tiết Triển Khai

**Status**: 🔄 IN PROGRESS  
**Started**: 19/11/2025 00:45  
**Estimated Duration**: 2 tuần  
**Dependencies**: Phase 3 ✅ COMPLETED  
**Author**: K24DTCN210-NVMANH with Cline

---

## Tổng Quan

Phase 4 tập trung vào việc xây dựng hệ thống quản lý bài thi và ngân hàng câu hỏi. Đây là phase quan trọng, tạo nền tảng cho việc tổ chức thi trực tuyến.

### Mục Tiêu Chính

1. **Exam Management**: CRUD cho bài thi với nhiều loại và cấu hình
2. **Question Bank**: Ngân hàng câu hỏi có thể tái sử dụng
3. **Question Types**: Hỗ trợ 8 loại câu hỏi khác nhau
4. **Exam Configuration**: Cấu hình monitoring, timing, randomization
5. **Exam Publishing**: Phát hành bài thi cho subject_classes

### Kiến Trúc Database (Đã refactor với V12)

```
questions (Question Bank - Independent)
    ↓ N:M relationship
exam_questions (Join Table)
    ↓
exams → subject_classes → subjects
```

**Ưu điểm**:
- Questions có thể tái sử dụng cho nhiều exams
- Dễ dàng quản lý Question Bank theo subject
- Hỗ trợ tagging, difficulty level
- Optimistic locking với version field

---

## Các Bước Triển Khai

### Step 1: Question Bank Module ⏳
**Duration**: 2-3 ngày  
**Priority**: 🔴 Critical (Foundation)

#### 1.1. Entity Layer

**QuestionBank.java** (Entity cho questions table)
```java
@Entity
@Table(name = "questions")
public class QuestionBank {
    @Id @GeneratedValue
    private Long id;
    
    // Basic info
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private DifficultyLevel difficulty; // EASY, MEDIUM, HARD
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    // JSON fields
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags; // ["Java", "OOP", "Inheritance"]
    
    @Column(name = "options", columnDefinition = "JSON")
    private String options; // For multiple choice
    
    @Column(name = "correct_answer")
    private String correctAnswer;
    
    // Essay specific
    @Column(name = "max_words")
    private Integer maxWords;
    
    @Column(name = "min_words")
    private Integer minWords;
    
    @Column(name = "grading_criteria", columnDefinition = "TEXT")
    private String gradingCriteria;
    
    // Coding specific
    @Column(name = "programming_language")
    private String programmingLanguage;
    
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;
    
    @Column(name = "test_cases", columnDefinition = "JSON")
    private String testCases;
    
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;
    
    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb;
    
    // Fill in blank specific
    @Column(name = "blank_positions", columnDefinition = "JSON")
    private String blankPositions;
    
    // Matching specific
    @Column(name = "left_items", columnDefinition = "JSON")
    private String leftItems;
    
    @Column(name = "right_items", columnDefinition = "JSON")
    private String rightItems;
    
    @Column(name = "correct_matches", columnDefinition = "JSON")
    private String correctMatches;
    
    // Attachments
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;
    
    // Audit fields
    @Version
    private Integer version;
    
    @Column(name = "created_at")
    private Timestamp createdAt;
    
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    
    @Column(name = "deleted_at")
    private Timestamp deletedAt;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "question")
    private List<ExamQuestion> examQuestions;
}
```

**QuestionType.java** (Enum)
```java
public enum QuestionType {
    MULTIPLE_CHOICE,    // Trắc nghiệm 1 đáp án
    MULTIPLE_SELECT,    // Chọn nhiều đáp án
    TRUE_FALSE,         // Đúng/Sai
    ESSAY,              // Tự luận
    SHORT_ANSWER,       // Trả lời ngắn
    CODING,             // Lập trình
    FILL_IN_BLANK,      // Điền chỗ trống
    MATCHING            // Nối câu
}
```

**DifficultyLevel.java** (Enum)
```java
public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}
```

#### 1.2. Repository Layer

**QuestionBankRepository.java**
```java
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    
    // Find by subject
    List<QuestionBank> findBySubjectIdAndDeletedAtIsNull(Long subjectId);
    
    // Find by difficulty
    List<QuestionBank> findByDifficultyAndDeletedAtIsNull(DifficultyLevel difficulty);
    
    // Find by type
    List<QuestionBank> findByQuestionTypeAndDeletedAtIsNull(QuestionType type);
    
    // Search by text
    @Query("SELECT q FROM QuestionBank q WHERE q.questionText LIKE %:keyword% AND q.deletedAt IS NULL")
    List<QuestionBank> searchByKeyword(@Param("keyword") String keyword);
    
    // Filter by subject + difficulty + type
    @Query("SELECT q FROM QuestionBank q WHERE " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:type IS NULL OR q.questionType = :type) AND " +
           "q.deletedAt IS NULL")
    Page<QuestionBank> filterQuestions(
        @Param("subjectId") Long subjectId,
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("type") QuestionType type,
        Pageable pageable
    );
    
    // Count by subject
    @Query("SELECT COUNT(q) FROM QuestionBank q WHERE q.subject.id = :subjectId AND q.deletedAt IS NULL")
    long countBySubject(@Param("subjectId") Long subjectId);
    
    // Statistics by difficulty
    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionBank q " +
           "WHERE q.subject.id = :subjectId AND q.deletedAt IS NULL " +
           "GROUP BY q.difficulty")
    List<Object[]> getStatisticsByDifficulty(@Param("subjectId") Long subjectId);
}
```

#### 1.3. DTO Layer

**QuestionBankDTO.java**
```java
@Data
@Builder
public class QuestionBankDTO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private QuestionType questionType;
    private DifficultyLevel difficulty;
    private String questionText;
    private List<String> tags;
    
    // Options for multiple choice
    private List<OptionDTO> options;
    private String correctAnswer;
    
    // Essay fields
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    
    // Coding fields
    private String programmingLanguage;
    private String starterCode;
    private List<TestCaseDTO> testCases;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // Fill in blank
    private List<Integer> blankPositions;
    
    // Matching
    private List<MatchingItemDTO> leftItems;
    private List<MatchingItemDTO> rightItems;
    private Map<String, String> correctMatches;
    
    // Attachments
    private List<AttachmentDTO> attachments;
    
    // Metadata
    private Integer version;
    private Timestamp createdAt;
    private String createdByName;
}

@Data
@Builder
class OptionDTO {
    private String key;      // A, B, C, D
    private String text;     // Option text
}

@Data
@Builder
class TestCaseDTO {
    private String input;
    private String expectedOutput;
    private boolean isHidden;
}

@Data
@Builder
class MatchingItemDTO {
    private String id;
    private String text;
}

@Data
@Builder
class AttachmentDTO {
    private String filename;
    private String url;
    private String type; // IMAGE, PDF, CODE
}
```

**CreateQuestionRequest.java**
```java
@Data
@Builder
public class CreateQuestionRequest {
    @NotNull
    private Long subjectId;
    
    @NotNull
    private QuestionType questionType;
    
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;
    
    @NotBlank
    private String questionText;
    
    private List<String> tags;
    
    // Type-specific fields (validate based on questionType)
    private List<OptionDTO> options;
    private String correctAnswer;
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    private String programmingLanguage;
    private String starterCode;
    private List<TestCaseDTO> testCases;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    private List<Integer> blankPositions;
    private List<MatchingItemDTO> leftItems;
    private List<MatchingItemDTO> rightItems;
    private Map<String, String> correctMatches;
    private List<AttachmentDTO> attachments;
}
```

#### 1.4. Service Layer

**QuestionBankService.java**
```java
@Service
@Transactional
public class QuestionBankService {
    
    private final QuestionBankRepository questionRepository;
    private final SubjectRepository subjectRepository;
    
    /* ---------------------------------------------------
     * Tạo câu hỏi mới trong Question Bank
     * @param request Thông tin câu hỏi
     * @param currentUser User đang tạo
     * @returns QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    public QuestionBankDTO createQuestion(CreateQuestionRequest request, User currentUser) {
        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        
        // Validate question type specific fields
        validateQuestionFields(request);
        
        // Create entity
        QuestionBank question = new QuestionBank();
        question.setSubject(subject);
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setQuestionText(request.getQuestionText());
        question.setCreatedBy(currentUser);
        question.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Set type-specific fields
        setTypeSpecificFields(question, request);
        
        // Save
        QuestionBank saved = questionRepository.save(question);
        
        return mapToDTO(saved);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi theo subject với filter
     * @param subjectId ID môn học
     * @param difficulty Độ khó (optional)
     * @param type Loại câu hỏi (optional)
     * @param keyword Từ khóa tìm kiếm (optional)
     * @param pageable Phân trang
     * @returns Page<QuestionBankDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    public Page<QuestionBankDTO> getQuestions(Long subjectId, DifficultyLevel difficulty, 
                                              QuestionType type, String keyword, Pageable pageable) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Cập nhật câu hỏi
     * @param id ID câu hỏi
     * @param request Thông tin mới
     * @param currentUser User đang cập nhật
     * @returns QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    public QuestionBankDTO updateQuestion(Long id, CreateQuestionRequest request, User currentUser) {
        // Implementation với optimistic locking
    }
    
    /* ---------------------------------------------------
     * Soft delete câu hỏi
     * @param id ID câu hỏi
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    public void deleteQuestion(Long id) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Lấy thống kê câu hỏi theo subject
     * @param subjectId ID môn học
     * @returns QuestionStatisticsDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    public QuestionStatisticsDTO getStatistics(Long subjectId) {
        // Implementation
    }
    
    // Private helper methods
    private void validateQuestionFields(CreateQuestionRequest request) {
        // Validate based on question type
    }
    
    private void setTypeSpecificFields(QuestionBank question, CreateQuestionRequest request) {
        // Set fields based on question type
    }
    
    private QuestionBankDTO mapToDTO(QuestionBank question) {
        // Map entity to DTO
    }
}
```

#### 1.5. Controller Layer

**QuestionBankController.java**
```java
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionBankController {
    
    private final QuestionBankService questionService;
    
    /* ---------------------------------------------------
     * Tạo câu hỏi mới
     * POST /api/questions
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionBankDTO> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi với filter
     * GET /api/questions?subjectId=1&difficulty=MEDIUM&type=MULTIPLE_CHOICE&keyword=java
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<QuestionBankDTO>> getQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết câu hỏi
     * GET /api/questions/{id}
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionBankDTO> getQuestion(@PathVariable Long id) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Cập nhật câu hỏi
     * PUT /api/questions/{id}
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionBankDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Xóa câu hỏi (soft delete)
     * DELETE /api/questions/{id}
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        // Implementation
    }
    
    /* ---------------------------------------------------
     * Lấy thống kê câu hỏi theo subject
     * GET /api/questions/statistics?subjectId=1
     * @author: K24DTCN210-NVMANH (19/11/2025 00:50)
     * --------------------------------------------------- */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionStatisticsDTO> getStatistics(
            @RequestParam Long subjectId) {
        // Implementation
    }
}
```

**APIs Summary (Step 1)**:
- POST `/api/questions` - Create question
- GET `/api/questions` - List with filters
- GET `/api/questions/{id}` - Get detail
- PUT `/api/questions/{id}` - Update question
- DELETE `/api/questions/{id}` - Soft delete
- GET `/api/questions/statistics?subjectId=X` - Statistics

**Total**: 6 endpoints

---

### Step 2: Exam Module ⏳
**Duration**: 3-4 ngày  
**Priority**: 🔴 Critical

#### 2.1. Entity Layer

**Exam.java**
```java
@Entity
@Table(name = "exams")
public class Exam {
    @Id @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "subject_class_id", nullable = false)
    private SubjectClass subjectClass;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_purpose", nullable = false)
    private ExamPurpose examPurpose;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_format", nullable = false)
    private ExamFormat examFormat;
    
    @Column(name = "start_time", nullable = false)
    private Timestamp startTime;
    
    @Column(name = "end_time", nullable = false)
    private Timestamp endTime;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "passing_score")
    private BigDecimal passingScore;
    
    @Column(name = "total_score")
    private BigDecimal totalScore;
    
    // Monitoring settings
    @Enumerated(EnumType.STRING)
    @Column(name = "monitoring_level")
    private MonitoringLevel monitoringLevel;
    
    @Column(name = "screenshot_interval_seconds")
    private Integer screenshotIntervalSeconds;
    
    @Column(name = "allow_tab_switch")
    private Boolean allowTabSwitch;
    
    // Question settings
    @Column(name = "randomize_questions")
    private Boolean randomizeQuestions;
    
    @Column(name = "randomize_options")
    private Boolean randomizeOptions;
    
    // Review settings
    @Column(name = "allow_review_after_submit")
    private Boolean allowReviewAfterSubmit;
    
    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers;
    
    // Coding settings
    @Column(name = "allow_code_execution")
    private Boolean allowCodeExecution;
    
    @Column(name = "programming_language")
    private String programmingLanguage;
    
    // Publishing
    @Column(name = "is_published")
    private Boolean isPublished;
    
    // Audit
    @Version
    private Integer version;
    
    @Column(name = "created_at")
    private Timestamp createdAt;
    
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    
    @Column(name = "deleted_at")
    private Timestamp deletedAt;
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "exam")
    private List<ExamQuestion> examQuestions;
    
    @OneToMany(mappedBy = "exam")
    private List<ExamSubmission> submissions;
}
```

**ExamQuestion.java** (Join table entity)
```java
@Entity
@Table(name = "exam_questions")
public class ExamQuestion {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Qu
