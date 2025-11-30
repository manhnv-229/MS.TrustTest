package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.dto.MonitoringLevel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import java.util.List;

/* ---------------------------------------------------
 * Controller cho Step 3 của Exam Creation Wizard
 * Xử lý cấu hình settings cho đề thi
 * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
 * --------------------------------------------------- */
public class Step3SettingsController {

    @FXML private Label errorLabel;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private TextField passingScoreField;
    @FXML private CheckBox randomizeQuestionsCheck;
    @FXML private CheckBox randomizeOptionsCheck;
    @FXML private CheckBox allowReviewCheck;
    @FXML private CheckBox showCorrectAnswersCheck;
    @FXML private CheckBox allowCodeExecutionCheck;
    @FXML private ComboBox<String> monitoringLevelCombo;
    @FXML private ComboBox<String> programmingLanguageCombo;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;

    /* ---------------------------------------------------
     * Khởi tạo controller
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Khởi tạo Spinner valueFactory
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        hideError();
        setupSpinner();
    }
    
    /* ---------------------------------------------------
     * Setup Spinner với valueFactory
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void setupSpinner() {
        if (durationSpinner != null) {
            // Setup duration spinner (1-480 minutes, default 60)
            SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 480, 60);
            durationSpinner.setValueFactory(factory);
            durationSpinner.setEditable(true);
        }
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Restore data từ wizardData
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        restoreDataToForm();
    }
    
    /* ---------------------------------------------------
     * Restore data từ wizardData vào form
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm null check cho valueFactory
     * --------------------------------------------------- */
    private void restoreDataToForm() {
        if (wizardData == null) return;
        
        // Restore duration - đảm bảo valueFactory đã được khởi tạo
        if (wizardData.getDurationMinutes() != null && durationSpinner != null) {
            SpinnerValueFactory<Integer> factory = durationSpinner.getValueFactory();
            if (factory != null) {
                factory.setValue(wizardData.getDurationMinutes());
            } else {
                // Nếu chưa có factory, setup lại
                setupSpinner();
                if (durationSpinner.getValueFactory() != null) {
                    durationSpinner.getValueFactory().setValue(wizardData.getDurationMinutes());
                }
            }
        }
        
        // Restore passing score
        if (wizardData.getPassingScore() != null && passingScoreField != null) {
            passingScoreField.setText(wizardData.getPassingScore().toString());
        }
        
        // Restore checkboxes
        if (randomizeQuestionsCheck != null) {
            randomizeQuestionsCheck.setSelected(wizardData.getRandomizeQuestions() != null ? wizardData.getRandomizeQuestions() : false);
        }
        if (randomizeOptionsCheck != null) {
            randomizeOptionsCheck.setSelected(wizardData.getRandomizeOptions() != null ? wizardData.getRandomizeOptions() : false);
        }
        if (allowReviewCheck != null) {
            allowReviewCheck.setSelected(wizardData.getAllowReviewAfterSubmit() != null ? wizardData.getAllowReviewAfterSubmit() : true);
        }
        if (showCorrectAnswersCheck != null) {
            showCorrectAnswersCheck.setSelected(wizardData.getShowCorrectAnswers() != null ? wizardData.getShowCorrectAnswers() : false);
        }
        if (allowCodeExecutionCheck != null) {
            allowCodeExecutionCheck.setSelected(wizardData.getAllowCodeExecution() != null ? wizardData.getAllowCodeExecution() : false);
        }
        
        // Restore monitoring level
        if (wizardData.getMonitoringLevel() != null && monitoringLevelCombo != null) {
            monitoringLevelCombo.setValue(wizardData.getMonitoringLevel().name());
        }
        
        // Restore programming language
        if (wizardData.getProgrammingLanguage() != null && programmingLanguageCombo != null) {
            programmingLanguageCombo.setValue(wizardData.getProgrammingLanguage());
        }
        
        System.out.println("=== STEP3: Restored data from wizardData ===");
    }

    /* ---------------------------------------------------
     * Set parent controller
     * @param parentController Controller cha
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    public void setParentController(ExamCreationWizardController parentController) {
        this.parentController = parentController;
    }

    /* ---------------------------------------------------
     * PUBLIC method để force save form data từ parent controller
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Implement save logic cho tất cả settings
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP3: saveFormToWizardData() CALLED ===");
        
        if (wizardData == null) {
            System.out.println("WARNING: wizardData is null!");
            return;
        }
        
        // Save duration
        if (durationSpinner != null && durationSpinner.getValueFactory() != null) {
            Integer duration = durationSpinner.getValue();
            wizardData.setDurationMinutes(duration);
            System.out.println("Saved duration: " + duration);
        }
        
        // Save passing score
        if (passingScoreField != null && !passingScoreField.getText().trim().isEmpty()) {
            try {
                java.math.BigDecimal passingScore = new java.math.BigDecimal(passingScoreField.getText().trim());
                wizardData.setPassingScore(passingScore);
                System.out.println("Saved passing score: " + passingScore);
            } catch (NumberFormatException e) {
                System.err.println("Invalid passing score format: " + passingScoreField.getText());
            }
        }
        
        // Save checkboxes
        if (randomizeQuestionsCheck != null) {
            wizardData.setRandomizeQuestions(randomizeQuestionsCheck.isSelected());
            System.out.println("Saved randomizeQuestions: " + randomizeQuestionsCheck.isSelected());
        }
        if (randomizeOptionsCheck != null) {
            wizardData.setRandomizeOptions(randomizeOptionsCheck.isSelected());
            System.out.println("Saved randomizeOptions: " + randomizeOptionsCheck.isSelected());
        }
        if (allowReviewCheck != null) {
            wizardData.setAllowReviewAfterSubmit(allowReviewCheck.isSelected());
            System.out.println("Saved allowReviewAfterSubmit: " + allowReviewCheck.isSelected());
        }
        if (showCorrectAnswersCheck != null) {
            wizardData.setShowCorrectAnswers(showCorrectAnswersCheck.isSelected());
            System.out.println("Saved showCorrectAnswers: " + showCorrectAnswersCheck.isSelected());
        }
        if (allowCodeExecutionCheck != null) {
            wizardData.setAllowCodeExecution(allowCodeExecutionCheck.isSelected());
            System.out.println("Saved allowCodeExecution: " + allowCodeExecutionCheck.isSelected());
        }
        
        // Save monitoring level
        if (monitoringLevelCombo != null && monitoringLevelCombo.getValue() != null) {
            try {
                MonitoringLevel level = MonitoringLevel.valueOf(monitoringLevelCombo.getValue());
                wizardData.setMonitoringLevel(level);
                System.out.println("Saved monitoringLevel: " + level);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid monitoring level: " + monitoringLevelCombo.getValue());
            }
        }
        
        // Save programming language
        if (programmingLanguageCombo != null && programmingLanguageCombo.getValue() != null) {
            wizardData.setProgrammingLanguage(programmingLanguageCombo.getValue());
            System.out.println("Saved programmingLanguage: " + programmingLanguageCombo.getValue());
        }
        
        System.out.println("=== STEP3: saveFormToWizardData() COMPLETED ===");
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /* ---------------------------------------------------
     * Validate form data
     * @return true nếu hợp lệ, false nếu có lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public boolean validateForm() {
        hideError();
        
        // Lưu dữ liệu trước khi validate
        saveFormToWizardData();
        
        // Validate sử dụng wizardData.validateStep3()
        List<String> errors = wizardData.validateStep3();
        
        if (!errors.isEmpty()) {
            showValidationError(String.join("\n", errors));
            return false;
        }
        
        return true;
    }
    
    /* ---------------------------------------------------
     * Hiển thị validation error bằng Alert dialog
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText("Vui lòng kiểm tra lại thông tin");
        alert.setContentText(message);
        
        // Set owner window từ parent controller
        if (parentController != null && parentController.getWizardPane() != null) {
            Window owner = parentController.getWizardPane().getScene().getWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
        }
        
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm validation trước khi next
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        boolean isValid = validateForm();
        
        if (isValid && parentController != null) {
            parentController.nextStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Previous
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    @FXML
    private void handlePrevious() {
        if (parentController != null) {
            parentController.previousStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Cancel
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        if (parentController != null) {
            parentController.cancelWizard();
        }
    }
}
