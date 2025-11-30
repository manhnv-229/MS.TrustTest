package com.mstrust.client.teacher.dto;

import java.math.BigDecimal;

/* ---------------------------------------------------
 * DTO đại diện cho mapping giữa Question và Exam
 * Chứa: questionId, order, points
 * Dùng để add questions vào exam trong wizard
 * @author: K24DTCN210-NVMANH (27/11/2025 22:28)
 * --------------------------------------------------- */
public class ExamQuestionMapping {
    
    private Long questionId;
    private String questionContent; // For display only
    private Integer questionOrder;
    private BigDecimal points;
    
    public ExamQuestionMapping() {
    }
    
    public ExamQuestionMapping(Long questionId, String questionContent, Integer questionOrder, BigDecimal points) {
        this.questionId = questionId;
        this.questionContent = questionContent;
        this.questionOrder = questionOrder;
        this.points = points;
    }
    
    /* ---------------------------------------------------
     * Validation: Kiểm tra mapping hợp lệ
     * @return true nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:28)
     * --------------------------------------------------- */
    public boolean isValid() {
        return questionId != null && questionId > 0
                && questionOrder != null && questionOrder > 0
                && points != null && points.compareTo(BigDecimal.ZERO) > 0
                && points.compareTo(BigDecimal.valueOf(100)) <= 0;
    }
    
    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestionContent() {
        return questionContent;
    }
    
    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
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
    
    @Override
    public String toString() {
        return "ExamQuestionMapping{" +
                "questionId=" + questionId +
                ", questionOrder=" + questionOrder +
                ", points=" + points +
                '}';
    }
}
