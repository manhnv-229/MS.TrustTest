package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.util.TimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/* ---------------------------------------------------
 * Submit Confirmation Dialog Controller
 * Controller cho dialog xác nhận nộp bài thi với UI đẹp và thống kê chi tiết
 * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
 * --------------------------------------------------- */
public class SubmitConfirmationDialogController {

    // FXML injected elements
    @FXML private Label totalQuestionsLabel;
    @FXML private Label answeredQuestionsLabel;
    @FXML private Label unansweredQuestionsLabel;
    @FXML private Label completionPercentageLabel;
    @FXML private Label remainingTimeLabel;
    @FXML private VBox warningSection;
    @FXML private Label warningMessageLabel;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    // Dialog result
    private boolean confirmed = false;
    private Stage dialogStage;

    // Statistics data
    private int totalQuestions;
    private int answeredQuestions;
    private int unansweredQuestions;
    private double completionPercentage;
    private long remainingSeconds;

    /* ---------------------------------------------------
     * Initialize dialog với thống kê bài làm
     * @param totalQuestions Tổng số câu hỏi
     * @param answeredQuestions Số câu đã trả lời
     * @param remainingSeconds Thời gian còn lại (giây)
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    public void initialize(int totalQuestions, int answeredQuestions, long remainingSeconds) {
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.unansweredQuestions = totalQuestions - answeredQuestions;
        this.completionPercentage = totalQuestions > 0 ? (answeredQuestions * 100.0 / totalQuestions) : 0.0;
        this.remainingSeconds = remainingSeconds;

        updateUI();
    }

    /* ---------------------------------------------------
     * Set dialog stage để có thể đóng dialog
     * @param stage Dialog stage
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /* ---------------------------------------------------
     * Update UI với thống kê hiện tại
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    private void updateUI() {
        // Update statistics labels
        totalQuestionsLabel.setText(totalQuestions + " câu");
        answeredQuestionsLabel.setText(answeredQuestions + " câu");
        unansweredQuestionsLabel.setText(unansweredQuestions + " câu");
        completionPercentageLabel.setText(String.format("%.1f%%", completionPercentage));
        remainingTimeLabel.setText(TimeFormatter.formatSeconds(remainingSeconds));

        // Show/hide warning section based on unanswered questions
        if (unansweredQuestions > 0) {
            warningSection.setVisible(true);
            warningSection.setManaged(true);
            warningMessageLabel.setText("• Bạn còn " + unansweredQuestions + " câu chưa trả lời!");
        } else {
            warningSection.setVisible(false);
            warningSection.setManaged(false);
        }
    }

    /* ---------------------------------------------------
     * Handle Cancel button - quay lại kiểm tra bài
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        confirmed = false;
        closeDialog();
    }

    /* ---------------------------------------------------
     * Handle Submit button - xác nhận nộp bài
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    @FXML
    private void handleSubmit() {
        confirmed = true;
        closeDialog();
    }

    /* ---------------------------------------------------
     * Đóng dialog
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /* ---------------------------------------------------
     * Check xem user có xác nhận nộp bài không
     * @returns true nếu user xác nhận, false nếu hủy
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    public boolean isConfirmed() {
        return confirmed;
    }

    /* ---------------------------------------------------
     * Static method để show dialog và get result
     * @param ownerStage Parent stage
     * @param totalQuestions Tổng số câu hỏi
     * @param answeredQuestions Số câu đã trả lời
     * @param remainingSeconds Thời gian còn lại
     * @returns true nếu user xác nhận nộp bài
     * @author: K24DTCN210-NVMANH (03/12/2025 14:12)
     * --------------------------------------------------- */
    public static boolean showConfirmationDialog(Stage ownerStage, int totalQuestions, 
                                                int answeredQuestions, long remainingSeconds) {
        try {
            // Load FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(SubmitConfirmationDialogController.class
                .getResource("/view/submit-confirmation-dialog.fxml"));
            
            javafx.scene.layout.BorderPane dialogRoot = loader.load();
            
            // Get controller
            SubmitConfirmationDialogController controller = loader.getController();
            controller.initialize(totalQuestions, answeredQuestions, remainingSeconds);
            
            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Xác nhận nộp bài thi");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.setResizable(false);
            
            // Set scene
            javafx.scene.Scene scene = new javafx.scene.Scene(dialogRoot);
            
            // Add CSS stylesheets
            scene.getStylesheets().add(SubmitConfirmationDialogController.class
                .getResource("/css/exam-common.css").toExternalForm());
            
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            
            // Center dialog on parent
            if (ownerStage != null) {
                dialogStage.setX(ownerStage.getX() + (ownerStage.getWidth() - 650) / 2);
                dialogStage.setY(ownerStage.getY() + (ownerStage.getHeight() - 580) / 2);
            }
            
            // Show and wait
            dialogStage.showAndWait();
            
            return controller.isConfirmed();
            
        } catch (Exception e) {
            System.err.println("[SubmitConfirmationDialog] Error showing dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận nộp bài");
            alert.setHeaderText("Bạn có chắc chắn muốn nộp bài thi?");
            alert.setContentText(String.format("Đã trả lời: %d/%d câu (%.1f%%)",
                answeredQuestions, totalQuestions, 
                totalQuestions > 0 ? (answeredQuestions * 100.0 / totalQuestions) : 0.0));
            
            if (ownerStage != null) {
                alert.initOwner(ownerStage);
            }
            
            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            return result == ButtonType.OK;
        }
    }
}
