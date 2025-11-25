package com.mstrust.client.teacher.dto;

import com.mstrust.client.exam.dto.QuestionType;

/* ---------------------------------------------------
 * DTO cho QuestionBank - Nhận từ backend
 * @author: K24DTCN210-NVMANH (25/11/2025 22:36)
 * --------------------------------------------------- */
public class QuestionBankDTO {
    
    private Long id;
    private Long subjectId;
    private String subjectName;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String tags;
    private Integer version;
    private String questionText;
    
    // Multiple Choice / Multiple Select / True-False Fields
    private String options;
    private String correctAnswer;
    
    // Essay Fields
    private Integer maxWords;
    private Integer minWords;
    private String gradingCriteria;
    
    // Coding Fields
    private String programmingLanguage;
    private String starterCode;
    private String testCases;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // Fill in Blank Fields
    private String blankPositions;
    
    // Matching Fields
    private String leftItems;
    private String rightItems;
    private String correctMatches;
    
    // Attachments
    private String attachments;
    
    // Audit Fields
    private String createdAt;
    private String updatedAt;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
    
    // Statistics
    private Long usageCount;
    
    // Constructors
    public QuestionBankDTO() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public QuestionType getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getOptions() {
        return options;
    }
    
    public void setOptions(String options) {
        this.options = options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public Integer getMaxWords() {
        return maxWords;
    }
    
    public void setMaxWords(Integer maxWords) {
        this.maxWords = maxWords;
    }
    
    public Integer getMinWords() {
        return minWords;
    }
    
    public void setMinWords(Integer minWords) {
        this.minWords = minWords;
    }
    
    public String getGradingCriteria() {
        return gradingCriteria;
    }
    
    public void setGradingCriteria(String gradingCriteria) {
        this.gradingCriteria = gradingCriteria;
    }
    
    public String getProgrammingLanguage() {
        return programmingLanguage;
    }
    
    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
    
    public String getStarterCode() {
        return starterCode;
    }
    
    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
    
    public String getTestCases() {
        return testCases;
    }
    
    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }
    
    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }
    
    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }
    
    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }
    
    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }
    
    public String getBlankPositions() {
        return blankPositions;
    }
    
    public void setBlankPositions(String blankPositions) {
        this.blankPositions = blankPositions;
    }
    
    public String getLeftItems() {
        return leftItems;
    }
    
    public void setLeftItems(String leftItems) {
        this.leftItems = leftItems;
    }
    
    public String getRightItems() {
        return rightItems;
    }
    
    public void setRightItems(String rightItems) {
        this.rightItems = rightItems;
    }
    
    public String getCorrectMatches() {
        return correctMatches;
    }
    
    public void setCorrectMatches(String correctMatches) {
        this.correctMatches = correctMatches;
    }
    
    public String getAttachments() {
        return attachments;
    }
    
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getCreatedById() {
        return createdById;
    }
    
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public Long getUpdatedById() {
        return updatedById;
    }
    
    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }
    
    public String getUpdatedByName() {
        return updatedByName;
    }
    
    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
    
    public Long getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }
    
    /* ---------------------------------------------------
     * Lấy preview của question text (50 ký tự đầu)
     * @return String preview
     * @author: K24DTCN210-NVMANH (25/11/2025 22:36)
     * --------------------------------------------------- */
    public String getQuestionPreview() {
        if (questionText == null || questionText.isEmpty()) {
            return "";
        }
        // Strip HTML tags cho preview
        String plainText = questionText.replaceAll("<[^>]*>", "");
        if (plainText.length() <= 50) {
            return plainText;
        }
        return plainText.substring(0, 50) + "...";
    }
    
    /* ---------------------------------------------------
     * Alias cho getQuestionText() - để tương thích với backend field name
     * @return String content
     * @author: K24DTCN210-NVMANH (25/11/2025 22:49)
     * --------------------------------------------------- */
    public String getContent() {
        return questionText;
    }
    
    /* ---------------------------------------------------
     * Alias cho setQuestionText() - để tương thích với backend field name
     * @param content String content
     * @author: K24DTCN210-NVMANH (25/11/2025 22:49)
     * --------------------------------------------------- */
    public void setContent(String content) {
        this.questionText = content;
    }
    
    /* ---------------------------------------------------
     * Alias cho getQuestionType() - để tương thích với controller
     * @return QuestionType type
     * @author: K24DTCN210-NVMANH (25/11/2025 22:49)
     * --------------------------------------------------- */
    public QuestionType getType() {
        return questionType;
    }
    
    /* ---------------------------------------------------
     * Alias cho setQuestionType() - để tương thích với controller
     * @param type QuestionType
     * @author: K24DTCN210-NVMANH (25/11/2025 22:49)
     * --------------------------------------------------- */
    public void setType(QuestionType type) {
        this.questionType = type;
    }
}
