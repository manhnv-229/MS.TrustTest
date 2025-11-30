package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.dto.ExamWizardData;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/* ---------------------------------------------------
 * Controller cho Step 3 của Exam Creation Wizard
 * Xử lý cấu hình settings cho đề thi
 * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
 * --------------------------------------------------- */
public class Step3SettingsController {

    @FXML private Label errorLabel;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;

    /* ---------------------------------------------------
     * Khởi tạo controller
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        hideError();
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
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
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP3: saveFormToWizardData() CALLED ===");
        // TODO: Implement settings data saving logic later
        System.out.println("Duration: " + (wizardData != null ? wizardData.getDurationMinutes() : "wizardData is null"));
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
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (29/11/2025 16:29)
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        if (parentController != null) {
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
