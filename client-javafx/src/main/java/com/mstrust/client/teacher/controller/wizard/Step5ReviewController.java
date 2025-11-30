package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamCreateRequest;
import com.mstrust.client.teacher.dto.ExamDTO;
import com.mstrust.client.teacher.dto.ExamQuestionDTO;
import com.mstrust.client.teacher.dto.ExamWizardData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/* ---------------------------------------------------
 * Controller cho Step 5 của Exam Creation Wizard
 * Review tất cả thông tin và submit đề thi:
 * - Hiển thị summary của tất cả steps
 * - Validate toàn bộ data
 * - Submit exam creation request
 * - Add questions to exam
 * - Optionally publish exam
 * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
 * --------------------------------------------------- */
public class Step5ReviewController {

    // Basic Info Review
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label subjectClassLabel;
    @FXML private Label purposeLabel;
    @FXML private Label formatLabel;
    
    // Questions Review
    @FXML private Label questionCountLabel;
    @FXML private Label totalPointsLabel;
    @FXML private TextArea questionsListArea;
    
    // Settings Review
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Label passingScoreLabel;
    @FXML private Label behaviorSettingsLabel;
    @FXML private Label monitoringLevelLabel;
    
    // Class Assignment Review
    @FXML private Label assignedClassesLabel;
    @FXML private TextArea assignedClassesArea;
    
    // Submit options
    @FXML private CheckBox publishImmediatelyCheck;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;
    private ExamManagementApiClient apiClient;

    /* ---------------------------------------------------
     * Khởi tạo controller và setup UI components
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        hideStatus();
        hideError();
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        loadReviewData();
    }

    /* ---------------------------------------------------
     * Set parent controller
     * @param parentController Controller cha
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    public void setParentController(ExamCreationWizardController parentController) {
        this.parentController = parentController;
    }

    /* ---------------------------------------------------
     * Set API client để submit
     * @param apiClient API client
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    public void setApiClient(ExamManagementApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /* ---------------------------------------------------
     * Load dữ liệu review từ wizardData
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void loadReviewData() {
        if (wizardData == null) return;

        // Basic Info
        titleLabel.setText(wizardData.getTitle());
        descriptionLabel.setText(wizardData.getDescription());
        subjectClassLabel.setText(wizardData.getSubjectClassName());
        purposeLabel.setText(wizardData.getExamPurpose() != null ? 
            wizardData.getExamPurpose().getDisplayName() : "N/A");
        formatLabel.setText(wizardData.getExamFormat() != null ? 
            wizardData.getExamFormat().getDisplayName() : "N/A");
        
        // Questions
        questionCountLabel.setText(String.valueOf(wizardData.getSelectedQuestions().size()));
        wizardData.calculateTotalPoints();
        totalPointsLabel.setText(wizardData.getTotalPoints().toString());
        
        StringBuilder questionsList = new StringBuilder();
        for (int i = 0; i < wizardData.getSelectedQuestions().size(); i++) {
            var q = wizardData.getSelectedQuestions().get(i);
            questionsList.append(String.format("%d.Question #%d - %s điểm\n", 
                i + 1, q.getQuestionId(), q.getPoints()));
        }
        questionsListArea.setText(questionsList.toString());
        
        // Settings - with proper DateTime formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        startTimeLabel.setText(wizardData.getStartTime() != null ?  
            wizardData.getStartTime().format(formatter) : "N/A");
        endTimeLabel.setText(wizardData.getEndTime() != null ?  
            wizardData.getEndTime().format(formatter) : "N/A");
        durationLabel.setText(wizardData.getDurationMinutes() + " phút");
        passingScoreLabel.setText(wizardData.getPassingScore() + " điểm");
        
        StringBuilder behaviorSettings = new StringBuilder();
        if (wizardData.isRandomizeQuestions()) behaviorSettings.append("Xáo trộn câu hỏi, ");
        if (wizardData.isRandomizeOptions()) behaviorSettings.append("Xáo trộn đáp án, ");
        if (wizardData.isAllowReviewAfterSubmit()) behaviorSettings.append("Cho phép xem lại, ");
        if (wizardData.isShowCorrectAnswers()) behaviorSettings.append("Hiển thị đáp án đúng, ");
        if (wizardData.isAllowCodeExecution()) behaviorSettings.append("Cho phép chạy code");
        behaviorSettingsLabel.setText(behaviorSettings.length() > 0 ? 
            behaviorSettings.toString() : "Không có");
        
        monitoringLevelLabel.setText(wizardData.getMonitoringLevel() != null ? 
            wizardData.getMonitoringLevel().getDisplayName() : "N/A");
        
        // Classes
        assignedClassesLabel.setText(String.valueOf(wizardData.getAssignedClassIds().size()));
        
        StringBuilder classesList = new StringBuilder();
        for (Long classId : wizardData.getAssignedClassIds()) {
            classesList.append("Class #").append(classId).append("\n");
        }
        assignedClassesArea.setText(classesList.toString());
    }

    /* ---------------------------------------------------
     * Xử lý nút Submit - tạo đề thi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    @FXML
    private void handleSubmit() {
        hideError();
        hideStatus();
        
        // Validate toàn bộ wizard data
        if (!validateAllSteps()) {
            return;
        }
        
        // Disable buttons during submission
        progressIndicator.setVisible(true);
        showStatus("Đang tạo đề thi...");
        
        // Submit in background thread
        new Thread(() -> {
            try {
                submitExam();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    hideStatus();
                    showError("Lỗi khi tạo đề thi: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Submit exam creation request
     * @throws IOException Nếu có lỗi API
     * @throws ExamManagementApiClient.ApiException Nếu có lỗi API
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    public void submitExam() throws IOException, ExamManagementApiClient.ApiException {
        // Step 1: Create exam
        Platform.runLater(() -> showStatus("Bước 1/3: Tạo đề thi..."));
        ExamCreateRequest request = wizardData.toCreateRequest();
        ExamDTO createdExam = apiClient.createExam(request);
        
        if (createdExam == null || createdExam.getId() == null) {
            throw new IOException("Failed to create exam - no ID returned");
        }
        
        // Step 2: Add questions to exam
        Platform.runLater(() -> showStatus("Bước 2/3: Thêm câu hỏi..."));
        List<ExamQuestionDTO> addedQuestions = apiClient.addMultipleQuestions(
            createdExam.getId(),
            wizardData.getSelectedQuestions()
        );
        
        if (addedQuestions == null || addedQuestions.size() != wizardData.getSelectedQuestions().size()) {
            throw new IOException("Failed to add all questions to exam");
        }
        
        // Step 3: Publish exam if requested
        if (publishImmediatelyCheck.isSelected()) {
            Platform.runLater(() -> showStatus("Bước 3/3: Publish đề thi..."));
            ExamDTO publishedExam = apiClient.publishExam(createdExam.getId());
            
            if (publishedExam == null) {
                throw new IOException("Failed to publish exam");
            }
        }
        
        // Success! 
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            hideStatus();
            
            String successMessage = String.format(
                "Đề thi đã được tạo thành công!\n\n" +
                "ID: %d\n" +
                "Tiêu đề: %s\n" +
                "Số câu hỏi: %d\n" +
                "Tổng điểm: %s\n" +
                "Trạng thái: %s",
                createdExam.getId(),
                createdExam.getTitle(),
                addedQuestions.size(),
                wizardData.getTotalPoints(),
                publishImmediatelyCheck.isSelected() ? "Đã publish" : "Draft"
            );
            
            parentController.showSuccess(successMessage);
            parentController.closeWizard();
        });
    }

    /* ---------------------------------------------------
     * Validate tất cả steps
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private boolean validateAllSteps() {
        StringBuilder allErrors = new StringBuilder();
        
        List<String> errors1 = wizardData.validateStep1();
        if (!errors1.isEmpty()) {
            allErrors.append("Step 1 - Basic Info:\n");
            errors1.forEach(e -> allErrors.append("  - ").append(e).append("\n"));
        }
        
        List<String> errors2 = wizardData.validateStep2();
        if (!errors2.isEmpty()) {
            allErrors.append("Step 2 - Questions:\n");
            errors2.forEach(e -> allErrors.append("  - ").append(e).append("\n"));
        }
        
        List<String> errors3 = wizardData.validateStep3();
        if (! errors3.isEmpty()) {
            allErrors.append("Step 3 - Settings:\n");
            errors3.forEach(e -> allErrors.append("  - ").append(e).append("\n"));
        }
        
        List<String> errors4 = wizardData.validateStep4();
        if (!errors4.isEmpty()) {
            allErrors.append("Step 4 - Class Assignment:\n");
            errors4.forEach(e -> allErrors.append("  - ").append(e).append("\n"));
        }
        
        if (allErrors.length() > 0) {
            showError("Có lỗi validation:\n\n" + allErrors.toString());
            return false;
        }
        
        return true;
    }

    /* ---------------------------------------------------
     * Hiển thị status message
     * @param message Nội dung status
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    /* ---------------------------------------------------
     * Ẩn status message
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /* ---------------------------------------------------
     * Xử lý nút Previous
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    @FXML
    private void handlePrevious() {
        parentController.previousStep();
    }

    /* ---------------------------------------------------
     * Xử lý nút Cancel
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        parentController.cancelWizard();
    }
}
