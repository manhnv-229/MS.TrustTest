package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.api.SubjectApiClient;
import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.dto.SubjectDTO;
import com.mstrust.client.exam.dto.LoginResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    @FXML private javafx.scene.control.Button nextButton;
    @FXML private javafx.scene.control.Button submitButton;
    @FXML private javafx.scene.control.Button previousButton;
    @FXML private javafx.scene.control.Button cancelButton;

    private ExamWizardData wizardData;
    private ExamManagementApiClient apiClient;
    private QuestionBankApiClient questionBankApiClient;
    private SubjectApiClient subjectApiClient;
    private LoginResponse loginResponse;
    private int currentStep = 1;
    private Stage wizardStage; // Reference đến wizard stage
    
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
     * Set wizard stage reference
     * @param stage Stage của wizard
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public void setWizardStage(Stage stage) {
        this.wizardStage = stage;
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
        step1Controller.setApiClient(apiClient);
        
        // Load subject classes from backend
        try {
            List<com.mstrust.client.teacher.dto.SubjectClassDTO> subjectClasses = apiClient.getAllSubjectClasses();
            step1Controller.loadSubjectClasses(subjectClasses);
        } catch (Exception e) {
            showError("Không thể tải danh sách lớp học phần: " + e.getMessage());
            System.err.println("Error loading subject classes: " + e.getMessage());
            e.printStackTrace();
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
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm validation trước khi next
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        // Validate current step trước khi chuyển bước
        if (validateCurrentStep()) {
            nextStep();
        }
    }
    
    /* ---------------------------------------------------
     * Validate current step
     * @return true nếu hợp lệ, false nếu có lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (step1Controller != null) {
                    // Gọi validation của Step1
                    return step1Controller.validateForm();
                }
                break;
            case 2:
                if (step2Controller != null) {
                    // Gọi validation của Step2
                    return step2Controller.validateForm();
                }
                break;
            case 3:
                if (step3Controller != null) {
                    // Gọi validation của Step3
                    return step3Controller.validateForm();
                }
                break;
            case 4:
                if (step4Controller != null) {
                    // Gọi validation của Step4
                    return step4Controller.validateForm();
                }
                break;
            case 5:
                // Step 5 không có validation ở đây (submit sẽ validate)
                return true;
        }
        return true; // Default: cho phép next nếu không có controller
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
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Gọi handleSubmit() thay vì submitExam() trực tiếp
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Lưu dữ liệu Step 4 trước khi submit
     * --------------------------------------------------- */
    @FXML
    private void handleSubmit() {
        // CRITICAL: Lưu dữ liệu Step 4 trước khi validate và submit
        if (step4Controller != null) {
            System.out.println("=== Saving Step 4 data before submit ===");
            step4Controller.saveFormToWizardData();
        }
        
        if (step5Controller != null) {
            // Gọi handleSubmit() từ Step5ReviewController để xử lý async và validation
            step5Controller.handleSubmit();
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
                    System.out.println("Calling step3Controller.saveFormToWizardData()");
                    step3Controller.saveFormToWizardData(); // Force save
                } else {
                    System.out.println("WARNING: step3Controller is NULL!");
                }
                break;
            case 4:
                if (step4Controller != null) {
                    System.out.println("Calling step4Controller.saveFormToWizardData()");
                    step4Controller.saveFormToWizardData(); // Force save
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
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Sửa logic xác nhận để đóng wizard đúng cách
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Dùng wizardStage reference được set từ bên ngoài
     * --------------------------------------------------- */
    public void cancelWizard() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận hủy");
        confirmAlert.setHeaderText("Bạn có chắc muốn hủy tạo đề thi?");
        confirmAlert.setContentText("Tất cả dữ liệu đã nhập sẽ bị mất.");
        
        // Lấy stage từ reference hoặc từ scene
        Stage stageToClose = wizardStage;
        if (stageToClose == null) {
            if (cancelButton != null && cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
                Window window = cancelButton.getScene().getWindow();
                if (window instanceof Stage) {
                    stageToClose = (Stage) window;
                }
            } else if (wizardPane != null && wizardPane.getScene() != null && wizardPane.getScene().getWindow() != null) {
                Window window = wizardPane.getScene().getWindow();
                if (window instanceof Stage) {
                    stageToClose = (Stage) window;
                }
            }
        }
        
        // Set owner cho dialog
        if (stageToClose != null) {
            confirmAlert.initOwner(stageToClose);
        }
        
        // Hiển thị dialog và đợi kết quả
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        // Kiểm tra nếu user nhấn OK (xác nhận hủy)
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Đóng stage trực tiếp
            if (stageToClose != null) {
                stageToClose.close();
            } else {
                // Fallback: thử lấy stage từ wizardPane hoặc cancelButton
                closeWizard();
            }
        }
    }

    /* ---------------------------------------------------
     * Disable tất cả buttons trên wizard sau khi submit thành công
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public void disableAllButtons() {
        if (nextButton != null) {
            nextButton.setDisable(true);
        }
        if (submitButton != null) {
            submitButton.setDisable(true);
        }
        if (previousButton != null) {
            previousButton.setDisable(true);
        }
        if (cancelButton != null) {
            cancelButton.setDisable(true);
        }
    }
    
    /* ---------------------------------------------------
     * Đóng wizard window
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm kiểm tra null để tránh lỗi
     * --------------------------------------------------- */
    public void closeWizard() {
        if (wizardPane != null && wizardPane.getScene() != null) {
            Window window = wizardPane.getScene().getWindow();
            if (window instanceof Stage) {
                ((Stage) window).close();
            } else if (window != null) {
                window.hide();
            }
        }
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
    /* ---------------------------------------------------
     * Hiển thị dialog thông báo thành công
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (28/11/2025 08:25)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm owner window và center dialog
     * --------------------------------------------------- */
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText("Tạo đề thi thành công");
        alert.setContentText(message);
        
        // Set owner window
        if (wizardPane != null && wizardPane.getScene() != null && wizardPane.getScene().getWindow() != null) {
            alert.initOwner(wizardPane.getScene().getWindow());
        }
        
        // Center dialog
        alert.setOnShown(e -> {
            if (alert.getDialogPane().getScene() != null && alert.getDialogPane().getScene().getWindow() != null) {
                Window window = alert.getDialogPane().getScene().getWindow();
                if (wizardPane != null && wizardPane.getScene() != null && wizardPane.getScene().getWindow() != null) {
                    Window owner = wizardPane.getScene().getWindow();
                    window.setX(owner.getX() + (owner.getWidth() - window.getWidth()) / 2);
                    window.setY(owner.getY() + (owner.getHeight() - window.getHeight()) / 2);
                }
            }
        });
        
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
     * Get wizard pane (để các step controllers có thể access owner window)
     * @return BorderPane wizard pane
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public javafx.scene.Node getWizardPane() {
        return wizardPane;
    }

    /* ---------------------------------------------------
     * Update progress indicator theo current step
     * @author: K24DTCN210-NVMANH (28/11/2025 15:40)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Update button visibility cho Step 5
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
        
        // Update button visibility: Ở Step 5, ẩn Next và hiển thị Submit
        if (currentStep == 5) {
            if (nextButton != null) {
                nextButton.setVisible(false);
                nextButton.setManaged(false);
            }
            if (submitButton != null) {
                submitButton.setVisible(true);
                submitButton.setManaged(true);
            }
        } else {
            if (nextButton != null) {
                nextButton.setVisible(true);
                nextButton.setManaged(true);
            }
            if (submitButton != null) {
                submitButton.setVisible(false);
                submitButton.setManaged(false);
            }
        }
        
        // Update Previous button: Disable ở Step 1
        if (previousButton != null) {
            previousButton.setDisable(currentStep == 1);
        }
    }
}
