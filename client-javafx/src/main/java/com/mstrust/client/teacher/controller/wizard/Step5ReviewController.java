package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamCreateRequest;
import com.mstrust.client.teacher.dto.ExamDTO;
import com.mstrust.client.teacher.dto.ExamQuestionDTO;
import com.mstrust.client.teacher.dto.ExamWizardData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    // Loading dialog components
    private Stage loadingDialog;
    private Label loadingStatusLabel;

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
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Đổi thành public và sử dụng loading dialog
     * --------------------------------------------------- */
    @FXML
    public void handleSubmit() {
        hideError();
        hideStatus();
        
        // Validate toàn bộ wizard data
        if (!validateAllSteps()) {
            return;
        }
        
        // Hiển thị loading dialog ngay lập tức (đã ở JavaFX thread, gọi trực tiếp)
        showLoadingDialog("Đang tạo đề thi...");
        
        // Đảm bảo dialog được render trước khi start background thread
        // Sử dụng Platform.runLater với delay nhỏ để đảm bảo UI được update
        Platform.runLater(() -> {
            // Submit in background thread sau khi UI đã được update
            new Thread(() -> {
                try {
                    // Đợi một chút để đảm bảo loading dialog đã hiển thị hoàn toàn
                    Thread.sleep(150);
                    submitExam();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        hideLoadingDialog();
                        showErrorDialog("Lỗi", "Quá trình tạo đề thi bị gián đoạn");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        hideLoadingDialog();
                        showErrorDialog("Lỗi khi tạo đề thi", parseErrorMessage(e));
                        e.printStackTrace();
                    });
                }
            }).start();
        });
    }

    /* ---------------------------------------------------
     * Submit exam creation request
     * @throws IOException Nếu có lỗi API
     * @throws ExamManagementApiClient.ApiException Nếu có lỗi API
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    public void submitExam() throws IOException, ExamManagementApiClient.ApiException {
        // Step 1: Create exam
        updateLoadingDialog("Bước 1/3: Tạo đề thi...");
        ExamCreateRequest request = wizardData.toCreateRequest();
        ExamDTO createdExam = apiClient.createExam(request);
        
        if (createdExam == null || createdExam.getId() == null) {
            throw new IOException("Failed to create exam - no ID returned");
        }
        
        // Step 2: Add questions to exam
        updateLoadingDialog("Bước 2/3: Thêm câu hỏi...");
        List<ExamQuestionDTO> addedQuestions = apiClient.addMultipleQuestions(
            createdExam.getId(),
            wizardData.getSelectedQuestions()
        );
        
        if (addedQuestions == null || addedQuestions.size() != wizardData.getSelectedQuestions().size()) {
            throw new IOException("Failed to add all questions to exam");
        }
        
        // Step 3: Publish exam if requested
        if (publishImmediatelyCheck.isSelected()) {
            updateLoadingDialog("Bước 3/3: Publish đề thi...");
            ExamDTO publishedExam = apiClient.publishExam(createdExam.getId());
            
            if (publishedExam == null) {
                throw new IOException("Failed to publish exam");
            }
        }
        
        // Success! 
        updateLoadingDialog("Hoàn tất!");
        
        // Delay một chút để user thấy "Hoàn tất!" trước khi đóng loading
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Platform.runLater(() -> {
            hideLoadingDialog();
            
            // Disable tất cả buttons trước khi hiển thị success message
            parentController.disableAllButtons();
            
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
            showErrorDialog("Lỗi validation", "Có lỗi validation:\n\n" + allErrors.toString());
            return false;
        }
        
        return true;
    }

    /* ---------------------------------------------------
     * Hiển thị status message (deprecated - dùng loading dialog)
     * @param message Nội dung status
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    /* ---------------------------------------------------
     * Ẩn status message (deprecated - dùng loading dialog)
     * @author: K24DTCN210-NVMANH (28/11/2025 08:30)
     * --------------------------------------------------- */
    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }
    
    /* ---------------------------------------------------
     * Hiển thị loading dialog đẹp mắt và thân thiện với overlay che toàn bộ nội dung
     * @param initialMessage Message ban đầu
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm overlay che toàn bộ, cải thiện layout
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Tách logic để có thể gọi trực tiếp từ JavaFX thread
     * --------------------------------------------------- */
    private void showLoadingDialog(String initialMessage) {
        // Nếu đã ở JavaFX thread, gọi trực tiếp; nếu không, dùng Platform.runLater
        if (Platform.isFxApplicationThread()) {
            showLoadingDialogInternal(initialMessage);
        } else {
            Platform.runLater(() -> showLoadingDialogInternal(initialMessage));
        }
    }
    
    /* ---------------------------------------------------
     * Internal method để hiển thị loading dialog (phải được gọi từ JavaFX thread)
     * @param initialMessage Message ban đầu
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showLoadingDialogInternal(String initialMessage) {
            // Lấy owner window từ nhiều nguồn
            Stage ownerStage = null;
            
            // Thử lấy từ parent controller trước
            if (parentController != null) {
                javafx.scene.Node wizardPane = parentController.getWizardPane();
                if (wizardPane != null && wizardPane.getScene() != null && wizardPane.getScene().getWindow() != null) {
                    ownerStage = (Stage) wizardPane.getScene().getWindow();
                }
            }
            
            // Nếu không lấy được, thử lấy từ các FXML nodes của Step5
            if (ownerStage == null && titleLabel != null) {
                javafx.scene.Node node = titleLabel.getScene() != null ? titleLabel : null;
                if (node != null && node.getScene() != null && node.getScene().getWindow() != null) {
                    ownerStage = (Stage) node.getScene().getWindow();
                }
            }
            
            // Nếu vẫn không lấy được, thử từ bất kỳ node nào
            if (ownerStage == null && questionsListArea != null) {
                javafx.scene.Node node = questionsListArea.getScene() != null ? questionsListArea : null;
                if (node != null && node.getScene() != null && node.getScene().getWindow() != null) {
                    ownerStage = (Stage) node.getScene().getWindow();
                }
            }
            
            if (ownerStage == null) {
                System.err.println("ERROR: Cannot show loading dialog - owner stage is null");
                System.err.println("  parentController: " + (parentController != null ? "not null" : "null"));
                System.err.println("  titleLabel: " + (titleLabel != null ? "not null" : "null"));
                System.err.println("  questionsListArea: " + (questionsListArea != null ? "not null" : "null"));
                // Fallback: tạo dialog không có owner (sẽ hiển thị nhưng có thể không đúng vị trí)
                System.err.println("  WARNING: Creating dialog without owner - may not display correctly");
            }
            
            // Tạo dialog stage với kích thước bằng owner window để che toàn bộ
            loadingDialog = new Stage();
            loadingDialog.initModality(Modality.APPLICATION_MODAL);
            loadingDialog.initStyle(StageStyle.TRANSPARENT);
            if (ownerStage != null) {
                loadingDialog.initOwner(ownerStage);
            }
            loadingDialog.setResizable(false);
            
            // Tạo overlay background (semi-transparent, đẹp hơn)
            StackPane overlayRoot = new StackPane();
            overlayRoot.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.65); " +
                "-fx-background-radius: 0;"
            );
            
            // Tạo content box ở giữa với kích thước vừa đủ
            VBox contentBox = new VBox(25);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setPadding(new Insets(40, 50, 40, 50));
            contentBox.setPrefWidth(400);
            contentBox.setMinWidth(400);
            contentBox.setMaxWidth(400);
            contentBox.setPrefHeight(250);
            contentBox.setMinHeight(250);
            contentBox.setMaxHeight(250);
            contentBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 30, 0, 0, 8);"
            );
            
            // Progress Indicator vừa phải
            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(80, 80);
            progress.setMinSize(80, 80);
            progress.setMaxSize(80, 80);
            progress.setStyle(
                "-fx-progress-color: #2196F3; " +
                "-fx-background-color: transparent;"
            );
            
            // Status label với style đẹp và professional hơn
            loadingStatusLabel = new Label(initialMessage);
            loadingStatusLabel.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif; " +
                "-fx-text-fill: #2c3e50; " +
                "-fx-font-weight: 600; " +
                "-fx-wrap-text: true; " +
                "-fx-text-alignment: center; " +
                "-fx-alignment: center; " +
                "-fx-line-spacing: 4px;"
            );
            loadingStatusLabel.setMaxWidth(350);
            loadingStatusLabel.setWrapText(true);
            loadingStatusLabel.setAlignment(Pos.CENTER);
            loadingStatusLabel.setPadding(new Insets(10, 0, 0, 0));
            
            // Thêm vào content box
            contentBox.getChildren().addAll(progress, loadingStatusLabel);
            
            // Thêm content box vào overlay
            overlayRoot.getChildren().add(contentBox);
            
            // Tạo scene
            Scene scene = new Scene(overlayRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            
            // Set kích thước bằng owner window (hoặc default nếu không có owner)
            final Stage finalOwnerStage = ownerStage; // Make final for lambda
            if (finalOwnerStage != null) {
                loadingDialog.setWidth(finalOwnerStage.getWidth());
                loadingDialog.setHeight(finalOwnerStage.getHeight());
                loadingDialog.setX(finalOwnerStage.getX());
                loadingDialog.setY(finalOwnerStage.getY());
            } else {
                // Fallback: set kích thước mặc định
                loadingDialog.setWidth(800);
                loadingDialog.setHeight(600);
                loadingDialog.centerOnScreen();
            }
            
            loadingDialog.setScene(scene);
            
            // Đảm bảo dialog hiển thị và update position khi owner window thay đổi
            loadingDialog.setOnShown(e -> {
                // Update position nếu owner window đã di chuyển
                if (finalOwnerStage != null) {
                    // Dialog stage vẫn che toàn bộ owner window (cho overlay)
                    loadingDialog.setX(finalOwnerStage.getX());
                    loadingDialog.setY(finalOwnerStage.getY());
                    loadingDialog.setWidth(finalOwnerStage.getWidth());
                    loadingDialog.setHeight(finalOwnerStage.getHeight());
                } else {
                    loadingDialog.centerOnScreen();
                }
                loadingDialog.toFront(); // Đảm bảo dialog ở trên cùng
                System.out.println("Loading dialog shown successfully");
            });
            
            // Show dialog ngay lập tức và force render
            System.out.println("Attempting to show loading dialog...");
            loadingDialog.show();
            loadingDialog.toFront(); // Đảm bảo dialog ở trên cùng
            loadingDialog.requestFocus(); // Request focus để đảm bảo visible
            
            // Force JavaFX to process the show event immediately
            javafx.application.Platform.runLater(() -> {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.toFront();
                        loadingDialog.requestFocus();
                        System.out.println("Loading dialog is showing and focused");
                    } else {
                        System.err.println("WARNING: Loading dialog is not showing after show() call");
                        // Thử show lại
                        loadingDialog.show();
                        loadingDialog.toFront();
                    }
                }
            });
    }
    
    /* ---------------------------------------------------
     * Cập nhật message trong loading dialog
     * @param message Message mới
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void updateLoadingDialog(String message) {
        Platform.runLater(() -> {
            if (loadingStatusLabel != null) {
                loadingStatusLabel.setText(message);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Đóng loading dialog
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void hideLoadingDialog() {
        Platform.runLater(() -> {
            if (loadingDialog != null) {
                loadingDialog.close();
                loadingDialog = null;
                loadingStatusLabel = null;
            }
        });
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
     * Hiển thị lỗi trong Alert dialog
     * @param title Tiêu đề dialog
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Có lỗi xảy ra");
            alert.setContentText(message);
            
            // Set owner window
            if (parentController != null) {
                javafx.scene.Node wizardPane = parentController.getWizardPane();
                if (wizardPane != null && wizardPane.getScene() != null && wizardPane.getScene().getWindow() != null) {
                    alert.initOwner(wizardPane.getScene().getWindow());
                }
            }
            
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Parse error message từ exception để hiển thị đẹp hơn
     * @param e Exception
     * @return Formatted error message
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private String parseErrorMessage(Exception e) {
        String message = e.getMessage();
        
        if (message == null) {
            return "Đã xảy ra lỗi không xác định: " + e.getClass().getSimpleName();
        }
        
        // Parse API error response (JSON format)
        if (message.contains("API Error:") && message.contains("{")) {
            try {
                // Extract JSON part
                int jsonStart = message.indexOf("{");
                int jsonEnd = message.lastIndexOf("}") + 1;
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonStr = message.substring(jsonStart, jsonEnd);
                    
                    // Parse simple JSON để lấy errors
                    if (jsonStr.contains("\"errors\"")) {
                        StringBuilder errorDetails = new StringBuilder();
                        errorDetails.append("Các lỗi validation:\n\n");
                        
                        // Extract errors object: "errors":{"fieldName":"error message"}
                        int errorsStart = jsonStr.indexOf("\"errors\"");
                        if (errorsStart >= 0) {
                            int errorsObjStart = jsonStr.indexOf("{", errorsStart);
                            int errorsObjEnd = jsonStr.indexOf("}", errorsStart + 8);
                            
                            if (errorsObjStart >= 0 && errorsObjEnd > errorsObjStart) {
                                String errorsObj = jsonStr.substring(errorsObjStart, errorsObjEnd + 1);
                                
                                // Extract field errors using regex: "fieldName":"error message"
                                Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
                                Matcher matcher = pattern.matcher(errorsObj);
                                
                                boolean hasErrors = false;
                                while (matcher.find()) {
                                    String fieldName = matcher.group(1);
                                    String errorMsg = matcher.group(2);
                                    
                                    hasErrors = true;
                                    String fieldDisplayName = getFieldDisplayName(fieldName);
                                    
                                    // Special handling for passingScore error
                                    if ("passingScore".equals(fieldName) && errorMsg.contains("must be <= 100")) {
                                        errorDetails.append("• ").append(fieldDisplayName)
                                            .append(": Điểm đạt không được vượt quá tổng điểm của đề thi\n");
                                    } else {
                                        errorDetails.append("• ").append(fieldDisplayName).append(": ").append(errorMsg).append("\n");
                                    }
                                }
                                
                                if (hasErrors) {
                                    return errorDetails.toString();
                                }
                            }
                        }
                    }
                }
            } catch (Exception parseEx) {
                // Nếu parse lỗi, trả về message gốc
                System.err.println("Error parsing error message: " + parseEx.getMessage());
            }
        }
        
        return message;
    }
    
    /* ---------------------------------------------------
     * Get display name cho field name
     * @param fieldName Field name
     * @return Display name
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private String getFieldDisplayName(String fieldName) {
        switch (fieldName) {
            case "passingScore": return "Điểm đạt";
            case "totalScore": return "Tổng điểm";
            case "title": return "Tiêu đề";
            case "subjectClassId": return "Môn học";
            case "startTime": return "Thời gian bắt đầu";
            case "endTime": return "Thời gian kết thúc";
            case "durationMinutes": return "Thời lượng";
            case "examPurpose": return "Mục đích";
            case "examFormat": return "Định dạng";
            default: return fieldName;
        }
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
