package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

/** ------------------------------------------
 * Mục đích: Entity đại diện cho câu hỏi trong Question Bank
 * 
 * Đặc điểm:
 * - Câu hỏi độc lập, không gắn với exam cụ thể
 * - Có thể tái sử dụng cho nhiều exams (N:M qua exam_questions)
 * - Phân loại theo subject, difficulty, tags
 * - Hỗ trợ nhiều loại câu hỏi (trắc nghiệm, tự luận, coding, etc)
 * - Soft delete với deleted_at
 * - Optimistic locking với version
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:19
 */
@Entity
@Table(name = "questions")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE questions SET deleted_at = NOW() WHERE id = ? AND version = ?")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Subject relationship (optional - question có thể không thuộc subject)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
    // Question type (MULTIPLE_CHOICE, ESSAY, etc)
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
    
    // Difficulty level
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;
    
    // Tags for categorization (JSON array)
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;
    
    // Version for optimistic locking
    @Version
    @Column(name = "version")
    private Integer version;
    
    // Question content
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    // ===== MULTIPLE_CHOICE / MULTIPLE_SELECT fields =====
    @Column(name = "options", columnDefinition = "JSON")
    private String options; // JSON array: ["Option A", "Option B", "Option C", "Option D"]
    
    @Column(name = "correct_answer")
    private String correctAnswer; // For MULTIPLE_CHOICE: "A", for MULTIPLE_SELECT: "A,C"
    
    // ===== ESSAY / SHORT_ANSWER fields =====
    @Column(name = "max_words")
    private Integer maxWords;
    
    @Column(name = "min_words")
    private Integer minWords;
    
    @Column(name = "grading_criteria", columnDefinition = "TEXT")
    private String gradingCriteria;
    
    // ===== CODING fields =====
    @Column(name = "programming_language", length = 50)
    private String programmingLanguage;
    
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;
    
    @Column(name = "test_cases", columnDefinition = "JSON")
    private String testCases; // JSON array of test cases
    
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;
    
    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb;
    
    // ===== FILL_IN_BLANK fields =====
    @Column(name = "blank_positions", columnDefinition = "JSON")
    private String blankPositions; // JSON array indicating blank positions
    
    // ===== MATCHING fields =====
    @Column(name = "left_items", columnDefinition = "JSON")
    private String leftItems; // JSON array of left side items
    
    @Column(name = "right_items", columnDefinition = "JSON")
    private String rightItems; // JSON array of right side items
    
    @Column(name = "correct_matches", columnDefinition = "JSON")
    private String correctMatches; // JSON object mapping left to right
    
    // ===== Common fields =====
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // JSON array of attachment URLs
    
    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamQuestion> examQuestions;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
