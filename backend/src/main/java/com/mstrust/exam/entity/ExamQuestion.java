package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entity cho bảng exam_questions - Join table giữa Exam và QuestionBank
 * Quan hệ N:M cho phép câu hỏi tái sử dụng cho nhiều bài thi
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 00:59)
 */
@Entity
@Table(name = "exam_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionBank question;
    
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
    
    @Column(name = "points", nullable = false, precision = 5, scale = 2)
    private BigDecimal points;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    
    // ===== Lifecycle Callbacks =====
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
        if (points == null) {
            points = BigDecimal.ONE; // Default 1.0 điểm
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
