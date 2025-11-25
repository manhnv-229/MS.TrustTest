package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.dto.ExamInfoDTO;
import com.mstrust.client.exam.dto.StartExamResponse;
import com.mstrust.client.exam.util.TimeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho Exam List Screen - Phase 8.2
 * Quản lý hiển thị danh sách đề thi và filters
 * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
 * --------------------------------------------------- */
public class ExamListController {
    private static final Logger logger = LoggerFactory.getLogger(ExamListController.class);
    
    private ExamApiClient examApiClient;
    private List<ExamInfoDTO> allExams;
    private List<ExamInfoDTO> filteredExams;
    
    // FXML Components
    @FXML private ComboBox<String> subjectFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button refreshButton;
    @FXML private VBox examCardsContainer;
    @FXML private VBox emptyStateBox;
    @FXML private Label examCountLabel;
    @FXML private Label lastRefreshLabel;
    
    // Phase 8.6: Loading overlay components
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingMessage;

    /* ---------------------------------------------------
     * Initialize - được gọi sau khi FXML loaded
     * Nhận ExamApiClient đã authenticated từ login
     * @param apiClient ExamApiClient với auth token
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 08:04) - Accept apiClient parameter
     * --------------------------------------------------- */
    public void initialize(ExamApiClient apiClient) {
        this.examApiClient = apiClient;
        logger.info("Initializing ExamListController");
        
        // Setup filters
        setupFilters();
        
        // Load initial data
        loadExams();
    }

    /* ---------------------------------------------------
     * Setup các combo box filters
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void setupFilters() {
        // Subject filter
        subjectFilterCombo.getItems().addAll(
            "Tất cả môn học",
            "Toán",
            "Lý",
            "Hóa",
            "Sinh",
            "Văn",
            "Anh"
        );
        subjectFilterCombo.setValue("Tất cả môn học");
        
        // Status filter
        statusFilterCombo.getItems().addAll(
            "Tất cả trạng thái",
            "Sắp diễn ra",
            "Đang diễn ra",
            "Đã kết thúc"
        );
        statusFilterCombo.setValue("Tất cả trạng thái");
    }

    /* ---------------------------------------------------
     * Load danh sách đề thi từ backend
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:03) - Phase 8.6: Use loading overlay
     * --------------------------------------------------- */
    private void loadExams() {
        // Disable refresh button
        refreshButton.setDisable(true);
        
        // Show loading overlay
        showLoading("Đang tải danh sách đề thi...");
        
        // Load in background thread
        new Thread(() -> {
            try {
                logger.info("Loading available exams...");
                allExams = examApiClient.getAvailableExams();
                filteredExams = allExams;
                
                // Update UI in JavaFX thread
                Platform.runLater(() -> {
                    displayExams();
                    updateLastRefreshTime();
                    refreshButton.setDisable(false);
                    hideLoading();
                });
                
            } catch (Exception e) {
                logger.error("Failed to load exams", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Không thể tải danh sách đề thi", e.getMessage());
                    refreshButton.setDisable(false);
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Hiển thị danh sách exam cards
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void displayExams() {
        examCardsContainer.getChildren().clear();
        
        if (filteredExams == null || filteredExams.isEmpty()) {
            // Show empty state
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            examCountLabel.setText("Tìm thấy 0 đề thi");
        } else {
            // Hide empty state
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);
            
            // Create cards for each exam
            for (ExamInfoDTO exam : filteredExams) {
                VBox card = createExamCard(exam);
                examCardsContainer.getChildren().add(card);
            }
            
            examCountLabel.setText(String.format("Tìm thấy %d đề thi", filteredExams.size()));
        }
    }

    /* ---------------------------------------------------
     * Tạo exam card cho một đề thi
     * @param exam ExamInfoDTO
     * @returns VBox chứa card UI
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private VBox createExamCard(ExamInfoDTO exam) {
        VBox card = new VBox(15);
        card.getStyleClass().add("exam-card");
        card.setPrefWidth(Double.MAX_VALUE);
        
        // Header row (Title + Status badge)
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(exam.getTitle());
        titleLabel.getStyleClass().add("exam-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        Label statusBadge = createStatusBadge(exam);
        
        headerRow.getChildren().addAll(titleLabel, statusBadge);
        
        // Subject row
        Label subjectLabel = new Label("📚 " + exam.getSubjectName());
        subjectLabel.getStyleClass().add("exam-subtitle");
        
        // Info grid
        VBox infoGrid = new VBox(8);
        infoGrid.getChildren().addAll(
            createInfoRow("⏰ Thời gian:", 
                TimeFormatter.formatDateTime(exam.getStartTime()) + 
                " - " + TimeFormatter.formatTime(exam.getEndTime())),
            createInfoRow("⏱️ Thời lượng:", 
                exam.getDurationMinutes() != null 
                    ? TimeFormatter.formatDuration(exam.getDurationMinutes())
                    : "Không xác định"),
            createInfoRow("📝 Số câu hỏi:", 
                String.valueOf(exam.getTotalQuestions()))
        );
        
        // Countdown hoặc status message
        Label countdownLabel = createCountdownLabel(exam);
        if (countdownLabel != null) {
            infoGrid.getChildren().add(countdownLabel);
        }
        
        // Action button
        Button actionButton = createActionButton(exam);
        
        // Add all to card
        card.getChildren().addAll(
            headerRow,
            subjectLabel,
            new Separator(),
            infoGrid,
            actionButton
        );
        
        return card;
    }

    /* ---------------------------------------------------
     * Tạo status badge cho exam
     * @param exam ExamInfoDTO
     * @returns Label styled as badge
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private Label createStatusBadge(ExamInfoDTO exam) {
        Label badge = new Label();
        badge.getStyleClass().add("status-badge");
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            badge.setText("Sắp diễn ra");
            badge.getStyleClass().add("status-upcoming");
        } else if (now.isAfter(exam.getEndTime())) {
            badge.setText("Đã kết thúc");
            badge.getStyleClass().add("status-ended");
        } else {
            badge.setText("Đang diễn ra");
            badge.getStyleClass().add("status-ongoing");
        }
        
        return badge;
    }

    /* ---------------------------------------------------
     * Tạo info row (label + value)
     * @param label Nhãn
     * @param value Giá trị
     * @returns HBox chứa label và value
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("exam-info-label");
        labelNode.setMinWidth(120);
        
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("exam-info-value");
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    /* ---------------------------------------------------
     * Tạo countdown label nếu exam chưa bắt đầu
     * @param exam ExamInfoDTO
     * @returns Label hoặc null
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private Label createCountdownLabel(ExamInfoDTO exam) {
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            String timeRemaining = TimeFormatter.formatTimeRemaining(exam.getStartTime());
            Label label = new Label("⏳ Bắt đầu sau: " + timeRemaining);
            label.getStyleClass().add("exam-info-value");
            label.setStyle("-fx-text-fill: #FF9800;"); // Warning color
            return label;
        }
        
        return null;
    }

    /* ---------------------------------------------------
     * Tạo action button (Bắt đầu hoặc disabled)
     * @param exam ExamInfoDTO
     * @returns Button
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private Button createActionButton(ExamInfoDTO exam) {
        Button button = new Button();
        button.setPrefWidth(Double.MAX_VALUE);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            button.setText("Chưa đến giờ thi");
            button.setDisable(true);
        } else if (now.isAfter(exam.getEndTime())) {
            button.setText("Đã kết thúc");
            button.setDisable(true);
        } else {
            button.setText("🚀 Bắt đầu làm bài");
            button.getStyleClass().add("success-button");
            button.setOnAction(e -> handleStartExam(exam));
        }
        
        return button;
    }

    /* ---------------------------------------------------
     * Xử lý khi click "Bắt đầu làm bài"
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void handleStartExam(ExamInfoDTO exam) {
        logger.info("Starting exam: {}", exam.getExamId());
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận bắt đầu thi");
        alert.setHeaderText("Bạn có chắc muốn bắt đầu làm bài?");
        alert.setContentText(String.format(
            "Đề thi: %s\nThời gian: %d phút\n\nSau khi bắt đầu, thời gian sẽ bắt đầu đếm ngược.",
            exam.getTitle(),
            exam.getDurationMinutes()
        ));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                startExamSession(exam);
            }
        });
    }

    /* ---------------------------------------------------
     * Bắt đầu exam session (gọi API và chuyển màn hình)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (23/11/2025 14:20)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 14:20) - Implement navigation to ExamTakingController
     * EditBy: K24DTCN210-NVMANH (24/11/2025 09:14) - Phase 8.6: Pass Stage to ExamTakingController
     * EditBy: K24DTCN210-NVMANH (24/11/2025 11:51) - Validate API before loading UI
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:03) - Phase 8.6: Use loading overlay instead of Alert
     * --------------------------------------------------- */
    private void startExamSession(ExamInfoDTO exam) {
        // Show loading overlay
        showLoading("Đang khởi tạo bài thi...");
        
        // Call API in background thread
        new Thread(() -> {
            try {
                logger.info("Calling startExam API for exam: {}", exam.getExamId());
                
                // 1. Call API FIRST to validate
                StartExamResponse response = examApiClient.startExam(exam.getExamId());
                
                // 2. If successful, navigate to exam screen on JavaFX thread
                Platform.runLater(() -> {
                    hideLoading();
                    navigateToExamScreen(exam, response);
                });
                
            } catch (ExamApiClient.ExamStartException e) {
                // Handle specific exam start errors
                logger.error("ExamStartException: {}", e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    handleExamStartError(e, exam);
                });
                
            } catch (IOException | InterruptedException e) {
                // Handle network errors
                logger.error("Network error during exam start", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi kết nối", 
                        "Không thể kết nối đến server. Vui lòng kiểm tra mạng và thử lại.");
                });
            } catch (Exception e) {
                // Handle unexpected errors
                logger.error("Unexpected error during exam start", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", 
                        "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Navigate to exam taking screen after API success
     * @param exam ExamInfoDTO
     * @param response StartExamResponse từ API (already called by ExamListController)
     * @author: K24DTCN210-NVMANH (24/11/2025 11:51)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 13:42) - Pass response để tránh double API call
     * --------------------------------------------------- */
    private void navigateToExamScreen(ExamInfoDTO exam, StartExamResponse response) {
        try {
            logger.info("Starting exam session for: {}", exam.getTitle());
            
            // 1. Load exam-taking.fxml
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-taking.fxml")
            );
            Parent root = loader.load();
            
            // 2. Get ExamTakingController
            ExamTakingController controller = loader.getController();
            
            // 3. Get current stage
            Stage stage = (Stage) examCardsContainer.getScene().getWindow();
            
            // 4. Set stage to controller for full-screen support
            controller.setStage(stage);
            
            // 5. Initialize exam với response ĐÃ CÓ (không call API lần nữa!)
            String authToken = examApiClient.getAuthToken();
            controller.initializeExamWithResponse(response, authToken);
            
            // 6. Create new scene
            Scene scene = new Scene(root, 1400, 900);
            
            // 7. Load CSS
            String css = getClass().getResource("/css/exam-common.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // 8. Switch scene
            stage.setScene(scene);
            stage.setTitle("Làm bài thi: " + exam.getTitle());
            stage.setMaximized(true);
            
            logger.info("Successfully navigated to exam taking screen");
            
        } catch (IOException e) {
            logger.error("Failed to load exam-taking.fxml", e);
            showError("Lỗi tải giao diện", 
                "Không thể mở màn hình làm bài thi: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Handle exam start errors với friendly dialogs
     * @param e ExamStartException
     * @param exam ExamInfoDTO of the exam
     * @author: K24DTCN210-NVMANH (24/11/2025 11:40)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 12:17) - Added max attempts error handling
     * --------------------------------------------------- */
    private void handleExamStartError(ExamApiClient.ExamStartException e, ExamInfoDTO exam) {
        if (e.isActiveSubmissionError()) {
            // User có submission đang active
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Bài thi đang diễn ra");
            alert.setHeaderText("Bạn đã có một bài thi đang làm dở");
            alert.setContentText(
                "Đề thi: " + exam.getTitle() + "\n\n" +
                "Bạn đã bắt đầu làm bài thi này trước đó và chưa nộp bài.\n" +
                "Vui lòng liên hệ giáo viên để được hỗ trợ hoặc reset bài thi."
            );
            
            // Add custom buttons
            ButtonType contactTeacherBtn = new ButtonType("Liên hệ GV");
            ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(contactTeacherBtn, closeBtn);
            
            alert.showAndWait();
            
        } else if (e.isMaxAttemptsError()) {
            // User đã hết số lần thi
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hết số lần thi");
            alert.setHeaderText("Bạn đã hết số lần thi cho đề này");
            
            // Extract số lần thi từ message (VD: "Maximum attempts reached (1)")
            String message = e.getMessage();
            alert.setContentText(
                "Đề thi: " + exam.getTitle() + "\n\n" +
                message + "\n\n" +
                "Bạn đã sử dụng hết số lần thi được phép cho đề thi này.\n" +
                "Vui lòng liên hệ giáo viên nếu cần được thi lại."
            );
            
            // Add custom buttons
            ButtonType contactTeacherBtn = new ButtonType("Liên hệ GV");
            ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(contactTeacherBtn, closeBtn);
            
            alert.showAndWait();
            
        } else {
            // Other errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Không thể bắt đầu bài thi");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /* ---------------------------------------------------
     * Filter by status
     * @param exam ExamInfoDTO
     * @param status Status filter value
     * @returns true nếu pass filter
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private boolean filterByStatus(ExamInfoDTO exam, String status) {
        if (status == null || status.equals("Tất cả trạng thái")) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case "Sắp diễn ra":
                return now.isBefore(exam.getStartTime());
            case "Đang diễn ra":
                return now.isAfter(exam.getStartTime()) && now.isBefore(exam.getEndTime());
            case "Đã kết thúc":
                return now.isAfter(exam.getEndTime());
            default:
                return true;
        }
    }

    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    @FXML
    private void onRefresh() {
        logger.info("Refreshing exam list");
        loadExams();
    }

    /* ---------------------------------------------------
     * Update last refresh time label
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void updateLastRefreshTime() {
        String time = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        lastRefreshLabel.setText("Cập nhật lần cuối: " + time);
    }

    /* ---------------------------------------------------
     * Show loading overlay với message (Phase 8.6)
     * @param message Loading message
     * @author: K24DTCN210-NVMANH (25/11/2025 11:03)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                if (loadingMessage != null) {
                    loadingMessage.setText(message);
                }
                loadingOverlay.setVisible(true);
                loadingOverlay.toFront();
            });
        }
    }
    
    /* ---------------------------------------------------
     * Hide loading overlay (Phase 8.6)
     * @author: K24DTCN210-NVMANH (25/11/2025 11:03)
     * --------------------------------------------------- */
    private void hideLoading() {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
            });
        }
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param title Tiêu đề
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
