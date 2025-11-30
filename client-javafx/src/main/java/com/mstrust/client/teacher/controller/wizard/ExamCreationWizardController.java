package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.api.SubjectApiClient;
import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.dto.SubjectDTO;
import com.mstrust.client.exam.dto.LoginResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Main Controller cho Exam Creation Wizard
 * Quản lý navigation giữa 5 steps:
 * - Step 1: Basic Info
 * - Step 2: Question Selection
 * - Step 3: Settings
 * - Step 4: Class Assignment
 * - Step 5: Review & Submit
 * 
 * Lifecycle: initialize → loadStep1 → nextStep → ...→ submitExam
 * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
 * EditBy: K24DTCN210-NVMANH (30/11/2025 00:02) - Add QuestionBankApiClient
 * --------------------------------------------------- */
public class ExamCreationWizardController {

    @FXML private BorderPane wizardPane;
    @FXML private StackPane stepContainer;
    @FXML private javafx.scene.control.Label step1Label;
    @FXML private javafx.scene.control.Label step2Label;
    @FXML private javafx.scene.control.Label step3Label;
    @FXML private javafx.scene.control.Label step4Label;
    @FXML private javafx.scene.control.Label step5Label;
    @FXML private javafx.scene.control.ProgressBar progressBar;

    private ExamWizardData wizardData;
    private ExamManagementApiClient apiClient;
    private QuestionBankApiClient questionBankApiClient;
    private SubjectApiClient subjectApiClient;
    private LoginResponse loginResponse;
    private int currentStep = 1;
    
    // Step controllers
    private Step1BasicInfoController step1Controller;
    private Step2QuestionSelectionController step2Controller;
    private Step3SettingsController step3Controller;
    private Step4ClassAssignmentController step4Controller;
    private Step5ReviewController step5Controller;

    /* ---------------------------------------------------
     * Khởi tạo wizard controller
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (30/11/2025 00:02) - Add QuestionBankApiClient init
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        wizardData = new ExamWizardData();
        apiClient = new ExamManagementApiClient();
        questionBankApiClient = new QuestionBankApiClient("http://localhost:8080");
        subjectApiClient = new SubjectApiClient("http://localhost:8080/api");
    }

    /* ---------------------------------------------------
     * Set login response để khởi tạo API client
     * @param loginResponse Đối tượng chứa token và user info
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (30/11/2025 00:02) - Set token cho QuestionBankApiClient
     * --------------------------------------------------- */
    public void setLoginResponse(LoginResponse loginResponse) {
        this.loginResponse = loginResponse;
        apiClient.setToken(loginResponse);
        questionBankApiClient.setAuthToken(loginResponse.getToken());
        subjectApiClient.setAuthToken(loginResponse.getToken());
    }

    /* ---------------------------------------------------
     * Bắt đầu wizard bằng cách load Step 1
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public void startWizard() {
        loadStep(1);
    }

    /* ---------------------------------------------------
     * Load step tương ứng
     * @param stepNumber Số step cần load (1-5)
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private void loadStep(int stepNumber) {
        try {
            currentStep = stepNumber;
            Node stepView = null;
            
            switch (stepNumber) {
                case 1:
                    stepView = loadStep1();
                    break;
                case 2:
                    stepView = loadStep2();
                    break;
                case 3:
                    stepView = loadStep3();
                    break;
                case 4:
                    stepView = loadStep4();
                    break;
                case 5:
                    stepView = loadStep5();
                    break;
                default:
                    showError("Invalid step number: " + stepNumber);
                    return;
            }
            
            if (stepView != null) {
                stepContainer.getChildren().clear();
                stepContainer.getChildren().add(stepView);
                updateProgressIndicator();
            }
            
        } catch (IOException e) {
            showError("Failed to load step " + stepNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ---------------------------------------------------
     * Load Step 1 - Basic Info
     * @return Node chứa view của step 1
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private Node loadStep1() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/wizard/step1-basic-info.fxml")
        );
        Node view = loader.load();
        
        step1Controller = loader.getController();
        step1Controller.setWizardData(wizardData);
        step1Controller.setParentController(this);
        
        // Load subjects from backend
        try {
            List<SubjectDTO> subjects = subjectApiClient.getAllSubjects();
            List<String> subjectNames = subjects.stream()
                .map(SubjectDTO::getSubjectName)
                .collect(java.util.stream.Collectors.toList());
            step1Controller.loadSubjectClasses(subjectNames);
        } catch (IOException e) {
            showError("Không thể tải danh sách môn học: " + e.getMessage());
        }
        
        return view;
    }

    /* ---------------------------------------------------
     * Load Step 2 - Question Selection
     * @return Node chứa view của step 2
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (30/11/2025 00:02) - Add QuestionBankApiClient injection
     * --------------------------------------------------- */
    private Node loadStep2() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/wizard/step2-question-selection.fxml")
        );
        Node view = loader.load();
        
        step2Controller = loader.getController();
        step2Controller.setWizardData(wizardData);
        step2Controller.setParentController(this);
        step2Controller.setApiClient(apiClient);
        step2Controller.setQuestionBankApiClient(questionBankApiClient);
        
        return view;
    }

    /* ---------------------------------------------------
     * Load Step 3 - Settings
     * @return Node chứa view của step 3
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private Node loadStep3() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/wizard/step3-settings.fxml")
        );
        Node view = loader.load();
        
        step3Controller = loader.getController();
        step3Controller.setWizardData(wizardData);
        step3Controller.setParentController(this);
        
        return view;
    }

    /* ---------------------------------------------------
     * Load Step 4 - Class Assignment
     * @return Node chứa view của step 4
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private Node loadStep4() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/wizard/step4-class-assignment.fxml")
        );
        Node view = loader.load();
        
        step4Controller = loader.getController();
        step4Controller.setWizardData(wizardData);
        step4Controller.setParentController(this);
        step4Controller.setApiClient(apiClient);
        
        return view;
    }

    /* ---------------------------------------------------
     * Load Step 5 - Review & Submit
     * @return Node chứa view của step 5
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private Node loadStep5() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/wizard/step5-review.fxml")
        );
        Node view = loader.load();
        
        step5Controller = loader.getController();
        step5Controller.setWizardData(wizardData);
        step5Controller.setParentController(this);
        step5Controller.setApiClient(apiClient);
        
        return view;
    }

    /* ---------------------------------------------------
     * Handler cho nút Next trong FXML
     * @author: K24DTCN210-NVMANH (28/11/2025 11:00)
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        nextStep();
    }

    /* ---------------------------------------------------
     * Handler cho nút Previous trong FXML
     * @author: K24DTCN210-NVMANH (28/11/2025 11:00)
     * --------------------------------------------------- */
    @FXML
    private void handlePrevious() {
        previousStep();
    }

    /* ---------------------------------------------------
     * Handler cho nút Cancel trong FXML
     * @author: K24DTCN210-NVMANH (28/11/2025 11:00)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        cancelWizard();
    }

    /* ---------------------------------------------------
     * Handler cho nút Submit trong FXML (Step 5)
     * Gọi submit logic từ Step5ReviewController
     * @author: K24DTCN210-NVMANH (28/11/2025 11:00)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 11:42) - Catch IOException và ApiException
     * --------------------------------------------------- */
    @FXML
    private void handleSubmit() {
        if (step5Controller != null) {
            try {
                step5Controller.submitExam();
            } catch (IOException | ExamManagementApiClient.ApiException e) {
                showError("Lỗi khi submit đề thi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /* ---------------------------------------------------
     * Chuyển sang step tiếp theo
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (29/11/2025 16:17) - Added saveCurrentStepData() call
     * --------------------------------------------------- */
    public void nextStep() {
        // DEBUG: Log wizard data BEFORE saving current step
        System.out.println("=== WIZARD DEBUG: nextStep() from " + currentStep + " ===");
        System.out.println("=== BEFORE SAVE CURRENT STEP ===");
        logWizardData();
        
        // CRITICAL: Save current step data BEFORE loading next step
        saveCurrentStepData();
        
        System.out.println("=== AFTER SAVE CURRENT STEP ===");
        logWizardData();
        
        if (currentStep < 5) {
            loadStep(currentStep + 1);
        }
    }

    /* ---------------------------------------------------
     * Save current step data để đảm bảo data persistence
     * @author: K24DTCN210-NVMANH (29/11/2025 16:17)
     * EditBy: K24DTCN210-NVMANH (30/11/2025 00:02) - Add step2Controller.saveFormToWizardData()
     * --------------------------------------------------- */
    private void saveCurrentStepData() {
        System.out.println("=== saveCurrentStepData() for step " + currentStep + " ===");
        switch (currentStep) {
            case 1:
                if (step1Controller != null) {
                    System.out.println("Calling step1Controller.saveFormToWizardData()");
                    step1Controller.saveFormToWizardData(); // Force save
                } else {
                    System.out.println("WARNING: step1Controller is NULL!");
                }
                break;
            case 2:
                if (step2Controller != null) {
                    System.out.println("Calling step2Controller.saveFormToWizardData()");
                    step2Controller.saveFormToWizardData(); // Force save
                } else {
                    System.out.println("WARNING: step2Controller is NULL!");
                }
                break;
            case 3:
                if (step3Controller != null) {
                    // TODO: step3Controller.saveFormToWizardData(); // Force save
                    System.out.println("TODO: Implement step3Controller.saveFormToWizardData()");
                } else {
                    System.out.println("WARNING: step3Controller is NULL!");
                }
                break;
            case 4:
                if (step4Controller != null) {
                    // TODO: step4Controller.saveFormToWizardData(); // Force save
                    System.out.println("TODO: Implement step4Controller.saveFormToWizardData()");
                } else {
                    System.out.println("WARNING: step4Controller is NULL!");
                }
                break;
        }
        System.out.println("=== saveCurrentStepData() COMPLETED ===");
    }

    /* ---------------------------------------------------
     * Log wizard data cho debugging
     * @author: K24DTCN210-NVMANH (29/11/2025 16:17)
     * --------------------------------------------------- */
    private void logWizardData() {
        if (wizardData != null) {
            System.out.println("WizardData Object Hash: " + System.identityHashCode(wizardData));
            System.out.println("Title: " + wizardData.getTitle());
            System.out.println("Start Time: " + wizardData.getStartTime());
            System.out.println("End Time: " + wizardData.getEndTime());
            System.out.println("Subject Class Name: " + wizardData.getSubjectClassName());
            System.out.println("Subject Class ID: " + wizardData.getSubjectClassId());
            System.out.println("Exam Purpose: " + wizardData.getExamPurpose());
            System.out.println("Exam Format: " + wizardData.getExamFormat());
        } else {
            System.out.println("ERROR: wizardData is NULL in parent controller!");
        }
        System.out.println("=========================================");
    }

    /* ---------------------------------------------------
     * Quay lại step trước
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public void previousStep() {
        if (currentStep > 1) {
            loadStep(currentStep - 1);
        }
    }

    /* ---------------------------------------------------
     * Hủy wizard và đóng window
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public void cancelWizard() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận hủy");
        confirmAlert.setHeaderText("Bạn có chắc muốn hủy tạo đề thi?  ");
        confirmAlert.setContentText("Tất cả dữ liệu đã nhập sẽ bị mất.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            closeWizard();
        }
    }

    /* ---------------------------------------------------
     * Đóng wizard window
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public void closeWizard() {
        Stage stage = (Stage) wizardPane.getScene().getWindow();
        stage.close();
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Đã xảy ra lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo thành công
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText("Tạo đề thi thành công");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Get wizard data (để các step controllers có thể access)
     * @return ExamWizardData object
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public ExamWizardData getWizardData() {
        return wizardData;
    }

    /* ---------------------------------------------------
     * Get API client (để các step controllers có thể access)
     * @return ExamManagementApiClient object
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * --------------------------------------------------- */
    public ExamManagementApiClient getApiClient() {
        return apiClient;
    }

    /* ---------------------------------------------------
     * Update progress indicator theo current step
     * @author: K24DTCN210-NVMANH (28/11/2025 15:40)
     * --------------------------------------------------- */
    private void updateProgressIndicator() {
        // Reset all step labels
        step1Label.getStyleClass().removeAll("step-active", "step-completed");
        step2Label.getStyleClass().removeAll("step-active", "step-completed");
        step3Label.getStyleClass().removeAll("step-active", "step-completed");
        step4Label.getStyleClass().removeAll("step-active", "step-completed");
        step5Label.getStyleClass().removeAll("step-active", "step-completed");
        
        // Add completed style for previous steps
        for (int i = 1; i < currentStep; i++) {
            switch (i) {
                case 1: step1Label.getStyleClass().add("step-completed"); break;
                case 2: step2Label.getStyleClass().add("step-completed"); break;
                case 3: step3Label.getStyleClass().add("step-completed"); break;
                case 4: step4Label.getStyleClass().add("step-completed"); break;
            }
        }
        
        // Add active style for current step
        switch (currentStep) {
            case 1: step1Label.getStyleClass().add("step-active"); break;
            case 2: step2Label.getStyleClass().add("step-active"); break;
            case 3: step3Label.getStyleClass().add("step-active"); break;
            case 4: step4Label.getStyleClass().add("step-active"); break;
            case 5: step5Label.getStyleClass().add("step-active"); break;
        }
        
        // Update progress bar
        double progress = currentStep / 5.0;
        progressBar.setProgress(progress);
    }
}
