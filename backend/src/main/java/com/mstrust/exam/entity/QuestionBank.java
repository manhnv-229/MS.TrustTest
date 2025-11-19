package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * Entity cho bảng questions - Question Bank
 * Câu hỏi độc lập, có thể tái sử dụng cho nhiều bài thi
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 00:57)
 */
@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ===== Basic Information =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private Difficulty difficulty;
    
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated: "Java,OOP,Inheritance"
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    // ===== Multiple Choice / Multiple Select / True-False Fields =====
    
    @Column(name = "options", columnDefinition = "JSON")
    private String options; // JSON: [{"key":"A","text":"Option A"},{"key":"B","text":"Option B"}]
    
    @Column(name = "correct_answer", length = 255)
    private String correctAnswer; // For MULTIPLE_CHOICE: "A", MULTIPLE_SELECT: "A,C,D", TRUE_FALSE: "TRUE"
    
    // ===== Essay Fields =====
    
    @Column(name = "max_words")
    private Integer maxWords;
    
    @Column(name = "min_words")
    private Integer minWords;
    
    @Column(name = "grading_criteria", columnDefinition = "TEXT")
    private String gradingCriteria;
    
    // ===== Coding Fields =====
    
    @Column(name = "programming_language", length = 50)
    private String programmingLanguage; // "Java", "Python", "C++", etc.
    
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;
    
    @Column(name = "test_cases", columnDefinition = "JSON")
    private String testCases; // JSON: [{"input":"1 2","expectedOutput":"3","isHidden":false}]
    
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;
    
    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb;
    
    // ===== Fill in Blank Fields =====
    
    @Column(name = "blank_positions", columnDefinition = "JSON")
    private String blankPositions; // JSON: [0, 5, 12] - vị trí các chỗ trống
    
    // ===== Matching Fields =====
    
    @Column(name = "left_items", columnDefinition = "JSON")
    private String leftItems; // JSON: [{"id":"L1","text":"Java"},{"id":"L2","text":"Python"}]
    
    @Column(name = "right_items", columnDefinition = "JSON")
    private String rightItems; // JSON: [{"id":"R1","text":"OOP Language"},{"id":"R2","text":"Scripting"}]
    
    @Column(name = "correct_matches", columnDefinition = "JSON")
    private String correctMatches; // JSON: {"L1":"R1","L2":"R2"}
    
    // ===== Attachments =====
    
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // JSON: [{"filename":"image.png","url":"/files/123.png","type":"IMAGE"}]
    
    // ===== Audit Fields =====
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    
    @Column(name = "deleted_at")
    private Timestamp deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    // ===== Relationships =====
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamQuestion> examQuestions;
    
    // ===== Lifecycle Callbacks =====
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
        if (version == null) {
            version = 0;
        }
        if (difficulty == null) {
            difficulty = Difficulty.MEDIUM;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
