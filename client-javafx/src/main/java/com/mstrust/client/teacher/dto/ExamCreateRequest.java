package com.mstrust.client.teacher.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Request DTO để tạo Exam mới (Client-side)
 * Mapping với backend CreateExamRequest
 * @author: K24DTCN210-NVMANH (27/11/2025 22:29)
 * --------------------------------------------------- */
public class ExamCreateRequest {
    
    private String title;
    private String description;
    
    @SerializedName("subjectClassId")
    private Long subjectClassId;
    
    @SerializedName("examPurpose")
    private String examPurpose; // Send as String to backend
    
    @SerializedName("examFormat")
    private String examFormat; // Send as String to backend
    
    @SerializedName("startTime")
    private String startTime; // ISO-8601 format: "2025-11-28T10:00:00"
    
    @SerializedName("endTime")
    private String endTime;
    
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    
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
    
    // Constructor
    public ExamCreateRequest() {
        // Defaults
        this.passingScore = BigDecimal.valueOf(50.00);
        this.totalScore = BigDecimal.valueOf(100.00);
        this.randomizeQuestions = false;
        this.randomizeOptions = false;
        this.allowReviewAfterSubmit = true;
        this.showCorrectAnswers = false;
        this.allowCodeExecution = false;
    }
    
    /* ---------------------------------------------------
     * Validation: Kiểm tra request hợp lệ
     * @return String message nếu invalid, null nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:29)
     * --------------------------------------------------- */
    public String validate() {
        if (title == null || title.trim().isEmpty()) {
            return "Tiêu đề không được để trống";
        }
        if (title.length() < 3 || title.length() > 200) {
            return "Tiêu đề phải từ 3-200 ký tự";
        }
        if (subjectClassId == null || subjectClassId <= 0) {
            return "Chưa chọn lớp học phần";
        }
        if (examPurpose == null || examPurpose.isEmpty()) {
            return "Chưa chọn mục đích thi";
        }
        if (examFormat == null || examFormat.isEmpty()) {
            return "Chưa chọn hình thức thi";
        }
        if (startTime == null || startTime.isEmpty()) {
            return "Chưa chọn thời gian bắt đầu";
        }
        if (endTime == null || endTime.isEmpty()) {
            return "Chưa chọn thời gian kết thúc";
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            return "Thời gian làm bài phải > 0";
        }
        if (durationMinutes > 480) {
            return "Thời gian làm bài không được vượt quá 480 phút (8 giờ)";
        }
        if (passingScore == null || passingScore.compareTo(BigDecimal.ZERO) < 0) {
            return "Điểm đạt phải >= 0";
        }
        if (totalScore == null || totalScore.compareTo(BigDecimal.ZERO) <= 0) {
            return "Tổng điểm phải > 0";
        }
        if (passingScore.compareTo(totalScore) > 0) {
            return "Điểm đạt không được lớn hơn tổng điểm";
        }
        
        // Validate startTime < endTime
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            if (! start.isBefore(end)) {
                return "Thời gian bắt đầu phải trước thời gian kết thúc";
            }
        } catch (Exception e) {
            return "Định dạng thời gian không hợp lệ";
        }
        
        return null; // Valid
    }
    
    // Getters and Setters
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
    
    public String getExamPurpose() {
        return examPurpose;
    }
    
    public void setExamPurpose(String examPurpose) {
        this.examPurpose = examPurpose;
    }
    
    public String getExamFormat() {
        return examFormat;
    }
    
    public void setExamFormat(String examFormat) {
        this.examFormat = examFormat;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
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
}
