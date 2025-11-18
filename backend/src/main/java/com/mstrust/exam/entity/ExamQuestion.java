package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: Entity đại diện cho join table giữa Exam và Question (N:M relationship)
 * 
 * Đặc điểm:
 * - Quản lý mối quan hệ nhiều-nhiều giữa Exam và Question
 * - Lưu thông tin cụ thể cho từng câu hỏi trong exam:
 *   + Thứ tự câu hỏi (question_order)
 *   + Điểm số cho câu hỏi này (points)
 * - Một question có thể xuất hiện trong nhiều exams
 * - Một exam có thể chứa nhiều questions
 * - Không có soft delete vì là bảng join
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:22
 */
@Entity
@Table(name = "exam_questions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_exam_question", columnNames = {"exam_id", "question_id"}),
    @UniqueConstraint(name = "uk_exam_order", columnNames = {"exam_id", "question_order"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Exam relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    // Question relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    // Order of question in exam (1, 2, 3, ...)
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
    
    // Points assigned to this question in this exam
    @Column(name = "points", nullable = false, precision = 5, scale = 2)
    private BigDecimal points = BigDecimal.valueOf(1.00);
    
    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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
