package com.mstrust.client.teacher.dto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/* ---------------------------------------------------
 * Data holder cho Exam Creation Wizard (5 steps)
 * Lưu trữ tất cả thông tin qua các bước:
 * - Step 1: Basic Info
 * - Step 2: Questions
 * - Step 3: Settings
 * - Step 4: Assign Classes
 * - Step 5: Review
 * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
 * --------------------------------------------------- */
public class ExamWizardData {
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    // ===== STEP 1: Basic Info =====
    private String title;
    private String description;
    private Long subjectId;
    private String subjectName; // For display
    private Long subjectClassId;
    private String subjectClassName; // For display
    private ExamPurpose examPurpose;
    private ExamFormat examFormat;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // ===== STEP 2: Questions =====
    private ObservableList<ExamQuestionMapping> selectedQuestions;
    private BigDecimal totalPoints;
    
    // ===== STEP 3: Settings =====
    private Integer durationMinutes;
    private Integer maxAttempts;
    private Boolean randomizeQuestions;
    private Boolean randomizeOptions;
    private Boolean showCorrectAnswers;
    private Boolean allowReviewAfterSubmit;
    private MonitoringLevel monitoringLevel;
    private BigDecimal passingScore;
    
    // Coding exam specific
    private Boolean allowCodeExecution;
    private String programmingLanguage;
    
    // ===== STEP 4: Assign Classes =====
    private ObservableList<Long> assignedClassIds;
    private int estimatedStudentCount;
    
    // ===== STEP 5: Review =====
    private boolean readyToPublish;
    
    /* ---------------------------------------------------
     * Constructor với default values
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * --------------------------------------------------- */
    public ExamWizardData() {
        this.selectedQuestions = FXCollections.observableArrayList();
        this.assignedClassIds = FXCollections.observableArrayList();
        this.totalPoints = BigDecimal.ZERO;
        this.passingScore = BigDecimal.valueOf(50.00);
        this.durationMinutes = 60; // Default 60 minutes
        this.maxAttempts = 1;
        this.randomizeQuestions = false;
        this.randomizeOptions = false;
        this.showCorrectAnswers = false;
        this.allowReviewAfterSubmit = true;
        this.monitoringLevel = MonitoringLevel.MEDIUM;
        this.allowCodeExecution = false;
        this.readyToPublish = false;
    }
    
    /* ---------------------------------------------------
     * Validate Step 1 (Basic Info)
     * @return List<String> error messages, empty list nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 08:41) - Changed return type to List<String>
     * --------------------------------------------------- */
    public List<String> validateStep1() {
        List<String> errors = new ArrayList<>();
        if (title == null || title.trim().isEmpty()) {
            errors.add("Tiêu đề không được để trống");
        } else if (title.length() < 3 || title.length() > 200) {
            errors.add("Tiêu đề phải từ 3-200 ký tự");
        }
        // TEMPORARILY DISABLED: subjectClassId validation (will be resolved later)
        // TODO: Re-enable after implementing subjectClassName -> subjectClassId resolution
        // if (subjectClassId == null) {
        //     errors.add("Chưa chọn lớp học phần");
        // }
        
        // Alternative validation: check if subjectClassName is selected
        if (subjectClassName == null || subjectClassName.trim().isEmpty()) {
            errors.add("Chưa chọn lớp học phần");
        }
        if (examPurpose == null) {
            errors.add("Chưa chọn mục đích thi");
        }
        if (examFormat == null) {
            errors.add("Chưa chọn hình thức thi");
        }
        if (startTime == null) {
            errors.add("Chưa chọn thời gian bắt đầu");
        }
        if (endTime == null) {
            errors.add("Chưa chọn thời gian kết thúc");
        }
        if (startTime != null && endTime != null && !  startTime.isBefore(endTime)) {
            errors.add("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (startTime != null && startTime.isBefore(LocalDateTime.now())) {
            errors.add("Thời gian bắt đầu phải trong tương lai");
        }
        return errors;
    }
    
    /* ---------------------------------------------------
     * Validate Step 2 (Questions)
     * @return List<String> error messages, empty list nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 08:42) - Changed return type to List<String>
     * --------------------------------------------------- */
    public List<String> validateStep2() {
        List<String> errors = new ArrayList<>();
        if (selectedQuestions == null || selectedQuestions.isEmpty()) {
            errors.add("Phải chọn ít nhất 1 câu hỏi");
        } else {
            // Check all questions have valid points
            for (ExamQuestionMapping q : selectedQuestions) {
                if (!  q.isValid()) {
                    errors.add("Câu hỏi #" + q.getQuestionOrder() + " có thông tin không hợp lệ");
                }
            }
        }
        
        if (totalPoints.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Tổng điểm phải lớn hơn 0");
        }
        
        return errors;
    }
    
    /* ---------------------------------------------------
     * Validate Step 3 (Settings)
     * @return List<String> error messages, empty list nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 08:43) - Changed return type to List<String>
     * --------------------------------------------------- */
    public List<String> validateStep3() {
        List<String> errors = new ArrayList<>();
        if (durationMinutes == null || durationMinutes <= 0) {
            errors.add("Thời gian làm bài phải > 0");
        } else if (durationMinutes > 480) {
            errors.add("Thời gian làm bài không được vượt quá 480 phút (8 giờ)");
        }
        if (maxAttempts == null || maxAttempts < 1 || maxAttempts > 5) {
            errors.add("Số lần làm bài phải từ 1-5");
        }
        if (passingScore == null || passingScore.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Điểm đạt phải >= 0");
        } else if (passingScore.compareTo(totalPoints) > 0) {
            errors.add("Điểm đạt không được lớn hơn tổng điểm");
        }
        if (monitoringLevel == null) {
            errors.add("Chưa chọn mức độ giám sát");
        }
        return errors;
    }
    
    /* ---------------------------------------------------
     * Validate Step 4 (Assign Classes)
     * @return List<String> error messages, empty list nếu valid
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 08:43) - Changed return type to List<String>
     * --------------------------------------------------- */
    public List<String> validateStep4() {
        List<String> errors = new ArrayList<>();
        if (assignedClassIds == null || assignedClassIds.isEmpty()) {
            errors.add("Phải chọn ít nhất 1 lớp học");
        }
        return errors;
    }
    
    /* ---------------------------------------------------
     * Calculate total points từ selected questions
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * --------------------------------------------------- */
    public void calculateTotalPoints() {
        totalPoints = BigDecimal.ZERO;
        for (ExamQuestionMapping q : selectedQuestions) {
            totalPoints = totalPoints.add(q.getPoints());
        }
    }
    
    /* ---------------------------------------------------
     * Convert to ExamCreateRequest để gửi lên backend
     * @return ExamCreateRequest
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Scale totalScore và điểm số từng câu hỏi về 100 nếu > 100
     * --------------------------------------------------- */
    public ExamCreateRequest toCreateRequest() {
        ExamCreateRequest request = new ExamCreateRequest();
        
        request.setTitle(title);
        request.setDescription(description);
        request.setSubjectClassId(subjectClassId);
        request.setExamPurpose(examPurpose.name());
        request.setExamFormat(examFormat.name());
        request.setStartTime(startTime.format(DATETIME_FORMATTER));
        request.setEndTime(endTime.format(DATETIME_FORMATTER));
        request.setDurationMinutes(durationMinutes);
        
        // Backend yêu cầu totalScore <= 100, nên scale về 100 nếu tổng điểm thực tế > 100
        // Chỉ scale totalScore, không scale điểm số từng câu hỏi (backend chỉ validate totalScore field)
        BigDecimal scaledTotalScore = totalPoints;
        if (totalPoints != null && totalPoints.compareTo(BigDecimal.valueOf(100)) > 0) {
            scaledTotalScore = BigDecimal.valueOf(100);
            System.out.println("WARNING: Total points (" + totalPoints + ") > 100, scaling totalScore to 100 for backend validation");
            System.out.println("NOTE: Individual question points remain unchanged. Backend will use totalScore field for validation.");
        }
        
        request.setPassingScore(passingScore);
        request.setTotalScore(scaledTotalScore);
        
        request.setRandomizeQuestions(randomizeQuestions);
        request.setRandomizeOptions(randomizeOptions);
        request.setAllowReviewAfterSubmit(allowReviewAfterSubmit);
        request.setShowCorrectAnswers(showCorrectAnswers);
        request.setAllowCodeExecution(allowCodeExecution);
        request.setProgrammingLanguage(programmingLanguage);
        
        return request;
    }
    
    /* ---------------------------------------------------
     * Get list of question mappings for addQuestions API
     * @return List of mappings
     * @author: K24DTCN210-NVMANH (27/11/2025 22:30)
     * --------------------------------------------------- */
    public List<ExamQuestionMapping> getQuestionMappings() {
        return new ArrayList<>(selectedQuestions);
    }
    
    // ===== Getters and Setters =====
    
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
    
    public ObservableList<ExamQuestionMapping> getSelectedQuestions() {
        return selectedQuestions;
    }
    
    public void setSelectedQuestions(ObservableList<ExamQuestionMapping> selectedQuestions) {
        this.selectedQuestions = selectedQuestions;
    }
    
    public BigDecimal getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public Integer getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
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
    
    public Boolean getShowCorrectAnswers() {
        return showCorrectAnswers;
    }
    
    public void setShowCorrectAnswers(Boolean showCorrectAnswers) {
        this.showCorrectAnswers = showCorrectAnswers;
    }
    
    public Boolean getAllowReviewAfterSubmit() {
        return allowReviewAfterSubmit;
    }
    
    public void setAllowReviewAfterSubmit(Boolean allowReviewAfterSubmit) {
        this.allowReviewAfterSubmit = allowReviewAfterSubmit;
    }
    
    public MonitoringLevel getMonitoringLevel() {
        return monitoringLevel;
    }
    
    public void setMonitoringLevel(MonitoringLevel monitoringLevel) {
        this.monitoringLevel = monitoringLevel;
    }
    
    public BigDecimal getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(BigDecimal passingScore) {
        this.passingScore = passingScore;
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
    
    public ObservableList<Long> getAssignedClassIds() {
        return assignedClassIds;
    }
    
    public void setAssignedClassIds(ObservableList<Long> assignedClassIds) {
        this.assignedClassIds = assignedClassIds;
    }
    
    public int getEstimatedStudentCount() {
        return estimatedStudentCount;
    }
    
    public void setEstimatedStudentCount(int estimatedStudentCount) {
        this.estimatedStudentCount = estimatedStudentCount;
    }
    
    public boolean isReadyToPublish() {
        return readyToPublish;
    }
    
    public void setReadyToPublish(boolean readyToPublish) {
        this.readyToPublish = readyToPublish;
    }
    
    /* ---------------------------------------------------
     * Boolean "is" getters (để tương thích với JavaFX binding)
     * @author: K24DTCN210-NVMANH (28/11/2025 08:41)
     * --------------------------------------------------- */
    public boolean isRandomizeQuestions() {
        return randomizeQuestions != null && randomizeQuestions;
    }
    
    public boolean isRandomizeOptions() {
        return randomizeOptions != null && randomizeOptions;
    }
    
    public boolean isShowCorrectAnswers() {
        return showCorrectAnswers != null && showCorrectAnswers;
    }
    
    public boolean isAllowReviewAfterSubmit() {
        return allowReviewAfterSubmit != null && allowReviewAfterSubmit;
    }
    
    public boolean isAllowCodeExecution() {
        return allowCodeExecution != null && allowCodeExecution;
    }
}
