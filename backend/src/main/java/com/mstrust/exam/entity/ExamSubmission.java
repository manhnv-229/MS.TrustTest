package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

/* ---------------------------------------------------
 * Entity lưu thông tin lần làm bài thi của student
 * Mỗi student có thể làm nhiều lần (theo max_attempts của exam)
 * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
 * --------------------------------------------------- */
@Entity
@Table(name = "exam_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------------------------------------------------
     * Relationship với Exam
     * --------------------------------------------------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /* ---------------------------------------------------
     * Relationship với User (student)
     * --------------------------------------------------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /* ---------------------------------------------------
     * Lần làm bài thứ mấy (1, 2, 3...)
     * --------------------------------------------------- */
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    /* ---------------------------------------------------
     * Timing Information
     * --------------------------------------------------- */
    @Column(name = "started_at")
    private Timestamp startedAt;

    @Column(name = "submitted_at")
    private Timestamp submittedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds = 0;

    /* ---------------------------------------------------
     * Status & Scoring
     * --------------------------------------------------- */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.NOT_STARTED;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "passed")
    private Boolean passed = false;

    /* ---------------------------------------------------
     * Randomization Seeds (để recreate câu hỏi đã random)
     * --------------------------------------------------- */
    @Column(name = "question_seed")
    private Long questionSeed;

    @Column(name = "option_seed")
    private Long optionSeed;

    /* ---------------------------------------------------
     * Auto-save Tracking
     * --------------------------------------------------- */
    @Column(name = "last_saved_at")
    private Timestamp lastSavedAt;

    @Column(name = "auto_save_count")
    private Integer autoSaveCount = 0;

    /* ---------------------------------------------------
     * Audit Fields
     * --------------------------------------------------- */
    @Version
    @Column(name = "version")
    private Integer version = 0;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    /* ---------------------------------------------------
     * Business Logic Methods
     * --------------------------------------------------- */
    
    /* ---------------------------------------------------
     * Kiểm tra submission có đang active không (IN_PROGRESS)
     * @returns true nếu đang làm bài
     * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
     * --------------------------------------------------- */
    public boolean isActive() {
        return status == SubmissionStatus.IN_PROGRESS;
    }

    /* ---------------------------------------------------
     * Kiểm tra submission đã submit chưa
     * @returns true nếu đã nộp bài
     * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
     * --------------------------------------------------- */
    public boolean isSubmitted() {
        return status == SubmissionStatus.SUBMITTED || status == SubmissionStatus.GRADED;
    }

    /* ---------------------------------------------------
     * Tính thời gian làm bài (giây) từ start đến hiện tại
     * @returns Số giây đã làm bài
     * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
     * --------------------------------------------------- */
    public int calculateTimeSpent() {
        if (startedAt == null) {
            return 0;
        }
        Timestamp endTime = submittedAt != null ? submittedAt : new Timestamp(System.currentTimeMillis());
        return (int) ((endTime.getTime() - startedAt.getTime()) / 1000);
    }

    /* ---------------------------------------------------
     * Tính thời gian còn lại (giây)
     * @returns Số giây còn lại, -1 nếu đã hết giờ
     * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
     * --------------------------------------------------- */
    public int calculateTimeRemaining() {
        if (startedAt == null || exam == null) {
            return 0;
        }
        int durationSeconds = exam.getDurationMinutes() * 60;
        int spent = calculateTimeSpent();
        return Math.max(0, durationSeconds - spent);
    }

    /* ---------------------------------------------------
     * Kiểm tra đã hết giờ làm bài chưa
     * @returns true nếu đã hết giờ
     * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
     * --------------------------------------------------- */
    public boolean isExpired() {
        return calculateTimeRemaining() <= 0;
    }
}
