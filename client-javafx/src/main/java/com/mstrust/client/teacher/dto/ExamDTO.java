package com.mstrust.client.teacher.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho response của Exam entity (full details)
 * Sử dụng khi: Get detail, Create/Update response
 * Mapping từ backend ExamDTO
 * @author: K24DTCN210-NVMANH (28/11/2025 08:03)
 * --------------------------------------------------- */
public class ExamDTO {
    
    @SerializedName("id")
    private Long id;
    
    // Basic information
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    // Subject class info
    @SerializedName("subjectClassId")
    private Long subjectClassId;
    
    @SerializedName("subjectClassName")
    private String subjectClassName;
    
    @SerializedName("subjectId")
    private Long subjectId;
    
    @SerializedName("subjectName")
    private String subjectName;
    
    @SerializedName("classId")
    private Long classId;
    
    @SerializedName("className")
    private String className;
    
    // Exam classification
    @SerializedName("examPurpose")
    private ExamPurpose examPurpose;
    
    @SerializedName("examFormat")
    private ExamFormat examFormat;
    
    // Time configuration
    @SerializedName("startTime")
    private LocalDateTime startTime;
    
    @SerializedName("endTime")
    private LocalDateTime endTime;
    
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    
    // Scoring configuration
    @SerializedName("passingScore")
    private BigDecimal passingScore;
    
    @SerializedName("totalScore")
    private BigDecimal totalScore;
    
    // Exam behavior settings
    @SerializedName("randomizeQuestions")
    private Boolean randomizeQuestions;
    
    @SerializedName("randomizeOptions")
    private Boolean randomizeOptions;
    
    @SerializedName("allowReviewAfterSubmit")
    private Boolean allowReviewAfterSubmit;
    
    @SerializedName("showCorrectAnswers")
    private Boolean showCorrectAnswers;
    
    // Coding exam specific
    @SerializedName("allowCodeExecution")
    private Boolean allowCodeExecution;
    
    @SerializedName("programmingLanguage")
    private String programmingLanguage;
    
    // Publication status
    @SerializedName("isPublished")
    private Boolean isPublished;
    
    @SerializedName("currentStatus")
    private String currentStatus; // ExamStatus as String
    
    // Statistics
    @SerializedName("questionCount")
    private Integer questionCount;
    
    @SerializedName("submissionCount")
    private Integer submissionCount;
    
    // Metadata
    @SerializedName("version")
    private Integer version;
    
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    @SerializedName("updatedAt")
    private LocalDateTime updatedAt;
    
    @SerializedName("createdByName")
    private String createdByName;
    
    @SerializedName("updatedByName")
    private String updatedByName;
    
    // Constructor
    public ExamDTO() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getSubjectClassId() {
        return subjectClassId;
    }
    
    public void setSubjectClassId(Long subjectClassId) {
        this.subjectClassId = subjectClassId;
    }
    
    public String getSubjectClassName() {
        return subjectClassName;
    }
    
    public void setSubjectClassName(String subjectClassName) {
        this.subjectClassName = subjectClassName;
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
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public ExamPurpose getExamPurpose() {
        return examPurpose;
    }
    
    public void setExamPurpose(ExamPurpose examPurpose) {
        this.examPurpose = examPurpose;
    }
    
    public ExamFormat getExamFormat() {
        return examFormat;
    }
    
    public void setExamFormat(ExamFormat examFormat) {
        this.examFormat = examFormat;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public BigDecimal getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(BigDecimal passingScore) {
        this.passingScore = passingScore;
    }
    
    public BigDecimal getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }
    
    public Boolean getRandomizeQuestions() {
        return randomizeQuestions;
    }
    
    public void setRandomizeQuestions(Boolean randomizeQuestions) {
        this.randomizeQuestions = randomizeQuestions;
    }
    
    public Boolean getRandomizeOptions() {
        return randomizeOptions;
    }
    
    public void setRandomizeOptions(Boolean randomizeOptions) {
        this.randomizeOptions = randomizeOptions;
    }
    
    public Boolean getAllowReviewAfterSubmit() {
        return allowReviewAfterSubmit;
    }
    
    public void setAllowReviewAfterSubmit(Boolean allowReviewAfterSubmit) {
        this.allowReviewAfterSubmit = allowReviewAfterSubmit;
    }
    
    public Boolean getShowCorrectAnswers() {
        return showCorrectAnswers;
    }
    
    public void setShowCorrectAnswers(Boolean showCorrectAnswers) {
        this.showCorrectAnswers = showCorrectAnswers;
    }
    
    public Boolean getAllowCodeExecution() {
        return allowCodeExecution;
    }
    
    public void setAllowCodeExecution(Boolean allowCodeExecution) {
        this.allowCodeExecution = allowCodeExecution;
    }
    
    public String getProgrammingLanguage() {
        return programmingLanguage;
    }
    
    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
    
    public Boolean getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public Integer getSubmissionCount() {
        return submissionCount;
    }
    
    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
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
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public String getUpdatedByName() {
        return updatedByName;
    }
    
    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}
