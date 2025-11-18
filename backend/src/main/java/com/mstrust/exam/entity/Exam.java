package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** ------------------------------------------
 * Mục đích: Entity đại diện cho bài thi/exam
 * 
 * Đặc điểm:
 * - Gắn với SubjectClass cụ thể
 * - Chứa cấu hình thi (thời gian, điểm số, giám sát)
 * - Quản lý questions qua ExamQuestion (N:M)
 * - Hỗ trợ nhiều mục đích thi (midterm, final, practice, etc)
 * - Soft delete với deleted_at
 * - Optimistic locking với version
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:22
 */
@Entity
@Table(name = "exams")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE exams SET deleted_at = NOW() WHERE id = ? AND version = ?")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic information
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Subject class relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_class_id", nullable = false)
    private SubjectClass subjectClass;
    
    // Exam classification
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_purpose", nullable = false)
    private ExamPurpose examPurpose;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_format", nullable = false)
    private ExamFormat examFormat;
    
    // Time configuration
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    // Scoring configuration
    @Column(name = "passing_score", precision = 5, scale = 2)
    private BigDecimal passingScore = BigDecimal.valueOf(50.00);
    
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.valueOf(100.00);
    
    // Monitoring & Anti-cheat settings
    @Enumerated(EnumType.STRING)
    @Column(name = "monitoring_level")
    private MonitoringLevel monitoringLevel = MonitoringLevel.MEDIUM;
    
    @Column(name = "screenshot_interval_seconds")
    private Integer screenshotIntervalSeconds = 60;
    
    @Column(name = "allow_tab_switch")
    private Boolean allowTabSwitch = false;
    
    // Exam behavior settings
    @Column(name = "randomize_questions")
    private Boolean randomizeQuestions = false;
    
    @Column(name = "randomize_options")
    private Boolean randomizeOptions = false;
    
    @Column(name = "allow_review_after_submit")
    private Boolean allowReviewAfterSubmit = true;
    
    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers = false;
    
    // Coding exam specific
    @Column(name = "allow_code_execution")
    private Boolean allowCodeExecution = false;
    
    @Column(name = "programming_language", length = 50)
    private String programmingLanguage;
    
    // Publication status
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    // Version for optimistic locking
    @Version
    @Column(name = "version")
    private Integer version;
    
    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
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
    
    /** ------------------------------------------
     * Mục đích: Tính toán trạng thái hiện tại của exam dựa trên time và publication status
     * @return ExamStatus - trạng thái hiện tại
     * @author NVMANH with Cline
     * @created 18/11/2025 18:22
     */
    public ExamStatus getCurrentStatus() {
        if (!isPublished) {
            return ExamStatus.DRAFT;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(startTime)) {
            return ExamStatus.PUBLISHED;
        } else if (now.isAfter(endTime)) {
            return ExamStatus.COMPLETED;
        } else {
            return ExamStatus.ONGOING;
        }
    }
}
