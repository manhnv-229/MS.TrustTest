package com.mstrust.client.exam.dto;

import lombok.Data;
import java.util.List;

/* ---------------------------------------------------
 * DTO chứa thông tin câu hỏi trong bài thi
 * - Map từ backend ExamQuestion
 * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
 * --------------------------------------------------- */
@Data
public class QuestionDTO {
    private Long id;
    private Long examQuestionId; // ID trong bảng exam_questions
    private String content;
    private QuestionType type;
    private Double points;
    private Integer orderNumber; // Thứ tự hiển thị (1, 2, 3...)
    
    // Options (cho MULTIPLE_CHOICE, MULTIPLE_SELECT, TRUE_FALSE, MATCHING)
    private List<String> options;
    
    // Correct answer (không show cho student)
    // private String correctAnswer;
    
    // Student's answer (nếu đã trả lời)
    private String studentAnswer;
    private Long studentAnswerId; // ID trong student_answers table
    
    // UI state
    private boolean answered;
    private boolean markedForReview;
    private boolean isCurrentQuestion;
}
