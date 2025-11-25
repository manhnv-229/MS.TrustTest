package com.mstrust.client.teacher.dto;

import com.mstrust.client.exam.dto.QuestionType;

/* ---------------------------------------------------
 * DTO cho việc tạo mới câu hỏi
 * @author: K24DTCN210-NVMANH (25/11/2025 22:37)
 * --------------------------------------------------- */
public class CreateQuestionRequest {
    
    private Long subjectId;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String tags;
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
    
    // Constructor
    public CreateQuestionRequest() {
    }
    
    // Getters and Setters
    public Long getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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
}
