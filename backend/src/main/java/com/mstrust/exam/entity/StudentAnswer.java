package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

/* ---------------------------------------------------
 * Entity lưu câu trả lời của student cho từng câu hỏi
 * Hỗ trợ nhiều loại câu trả lời: text, JSON, file upload
 * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
 * --------------------------------------------------- */
@Entity
@Table(name = "student_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------------------------------------------------
     * Relationship với ExamSubmission
     * --------------------------------------------------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission submission;

    /* ---------------------------------------------------
     * Relationship với QuestionBank
     * --------------------------------------------------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, insertable = false, updatable = false)
    private QuestionBank question;
    
    /* ---------------------------------------------------
     * ExamQuestion ID (để tracking câu hỏi trong exam)
     * --------------------------------------------------- */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /* ---------------------------------------------------
     * Answer Data (flexible cho nhiều loại câu hỏi)
     * --------------------------------------------------- */
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;  // Cho essay, short answer

    @Column(name = "answer_json", columnDefinition = "JSON")
    private String answerJson;  // Cho multiple choice, matching, coding test results, etc.
    
    /* ---------------------------------------------------
     * File Upload Support
     * --------------------------------------------------- */
    @Column(name = "uploaded_file_url", length = 500)
    private String uploadedFileUrl;

    @Column(name = "uploaded_file_name")
    private String uploadedFileName;

    /* ---------------------------------------------------
     * Grading Information
     * --------------------------------------------------- */
    @Column(name = "is_correct")
    private Boolean isCorrect;  // NULL nếu chưa chấm

    @Column(name = "points_earned", precision = 5, scale = 2)
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    @Column(name = "max_points", precision = 5, scale = 2)
    private BigDecimal maxPoints;

    /* ---------------------------------------------------
     * Teacher Feedback (cho manual grading)
     * --------------------------------------------------- */
    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @Column(name = "graded_at")
    private Timestamp gradedAt;

    /* ---------------------------------------------------
     * Auto-save Tracking
     * --------------------------------------------------- */
    @Column(name = "saved_count")
    private Integer savedCount = 0;

    @Column(name = "first_saved_at")
    private Timestamp firstSavedAt;

    @Column(name = "last_saved_at")
    private Timestamp lastSavedAt;

    /* ---------------------------------------------------
     * Audit Fields
     * --------------------------------------------------- */
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    /* ---------------------------------------------------
     * Lifecycle Callbacks
     * --------------------------------------------------- */
    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        updatedAt = now;
        if (firstSavedAt == null) {
            firstSavedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
        lastSavedAt = updatedAt;
        savedCount++;
    }

    /* ---------------------------------------------------
     * Business Logic Methods
     * --------------------------------------------------- */
    
    /* ---------------------------------------------------
     * Kiểm tra câu trả lời đã được chấm chưa
     * @returns true nếu đã chấm
     * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
     * --------------------------------------------------- */
    public boolean isGraded() {
        return isCorrect != null && gradedAt != null;
    }

    /* ---------------------------------------------------
     * Kiểm tra câu trả lời có rỗng không
     * @returns true nếu chưa trả lời
     * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
     * --------------------------------------------------- */
    public boolean isEmpty() {
        return (answerText == null || answerText.trim().isEmpty()) 
            && (answerJson == null || answerJson.trim().isEmpty())
            && (uploadedFileUrl == null || uploadedFileUrl.trim().isEmpty());
    }

    /* ---------------------------------------------------
     * Kiểm tra có file upload không
     * @returns true nếu có file upload
     * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
     * --------------------------------------------------- */
    public boolean hasFileUpload() {
        return uploadedFileUrl != null && !uploadedFileUrl.trim().isEmpty();
    }

    /* ---------------------------------------------------
     * Reset điểm về 0 (khi re-grade)
     * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
     * --------------------------------------------------- */
    public void resetGrading() {
        this.isCorrect = null;
        this.pointsEarned = BigDecimal.ZERO;
        this.teacherFeedback = null;
        this.gradedBy = null;
        this.gradedAt = null;
    }

    /* ---------------------------------------------------
     * Set điểm (auto-grading hoặc manual grading)
     * @param correct Đúng/Sai
     * @param points Điểm đạt được
     * @param grader User chấm bài (null nếu auto-grade)
     * @author: K24DTCN210-NVMANH (19/11/2025 15:09)
     * --------------------------------------------------- */
    public void setGrade(Boolean correct, BigDecimal points, User grader) {
        this.isCorrect = correct;
        this.pointsEarned = points;
        this.gradedBy = grader;
        this.gradedAt = new Timestamp(System.currentTimeMillis());
    }
}
