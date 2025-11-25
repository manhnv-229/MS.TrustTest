package com.mstrust.client.exam.dto;

import lombok.Data;
import java.util.List;

/* ---------------------------------------------------
 * DTO chứa thông tin câu hỏi trong bài thi
 * - Map từ backend ExamQuestion  
 * - Field names PHẢI match backend QuestionForStudentDTO
 * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
 * EditBy: K24DTCN210-NVMANH (24/11/2025 14:10) - Fixed field names to match backend
 * --------------------------------------------------- */
@Data
public class QuestionDTO {
    // Core fields - MUST match backend QuestionForStudentDTO exactly
    private Long id;
    private Long questionBankId;
    private String questionText;      // Backend field name
    private QuestionType questionType; // Backend field name
    private Double maxScore;          // Backend field name
    private Integer displayOrder;     // Backend field name
    
    // Options (cho MULTIPLE_CHOICE, MULTIPLE_SELECT, TRUE_FALSE, MATCHING)
    private List<String> options;
    
    // Helper methods for backward compatibility
    public QuestionType getType() {
        return questionType;
    }
    
    public void setType(QuestionType type) {
        this.questionType = type;
    }
    
    public String getContent() {
        return questionText;
    }
    
    public void setContent(String content) {
        this.questionText = content;
    }
    
    public Double getPoints() {
        return maxScore;
    }
    
    public void setPoints(Double points) {
        this.maxScore = points;
    }
    
    public Integer getOrderNumber() {
        return displayOrder;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.displayOrder = orderNumber;
    }
    
    // Student's answer (nếu đã trả lời)
    private String studentAnswer;
    private Long studentAnswerId; // ID trong student_answers table
    
    // UI state
    private boolean answered;
    private boolean markedForReview;
    private boolean isCurrentQuestion;
}
