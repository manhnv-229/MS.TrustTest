package com.mstrust.client.teacher.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Response DTO cho exam-question association
 * Chứa thông tin câu hỏi kèm theo điểm số và thứ tự trong bài thi
 * Mapping từ backend ExamQuestionDTO
 * @author: K24DTCN210-NVMANH (28/11/2025 08:04)
 * --------------------------------------------------- */
public class ExamQuestionDTO {
    
    // Thông tin từ ExamQuestion
    @SerializedName("examQuestionId")
    private Long examQuestionId;
    
    @SerializedName("questionOrder")
    private Integer questionOrder;
    
    @SerializedName("points")
    private BigDecimal points;
    
    // Thông tin từ QuestionBank
    @SerializedName("questionId")
    private Long questionId;
    
    @SerializedName("questionText")
    private String questionText;
    
    @SerializedName("questionType")
    private String questionType; // QuestionType as String
    
    @SerializedName("difficulty")
    private String difficulty;
    
    @SerializedName("subjectId")
    private Long subjectId;
    
    @SerializedName("subjectName")
    private String subjectName;
    
    // Metadata
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    @SerializedName("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public ExamQuestionDTO() {
    }
    
    // Getters and Setters
    public Long getExamQuestionId() {
        return examQuestionId;
    }
    
    public void setExamQuestionId(Long examQuestionId) {
        this.examQuestionId = examQuestionId;
    }
    
    public Integer getQuestionOrder() {
        return questionOrder;
    }
    
    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
    
    public BigDecimal getPoints() {
        return points;
    }
    
    public void setPoints(BigDecimal points) {
        this.points = points;
    }
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public Long getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
