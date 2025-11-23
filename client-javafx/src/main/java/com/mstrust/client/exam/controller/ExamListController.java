package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.dto.ExamInfoDTO;
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
    
    private final ExamApiClient examApiClient;
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

    /* ---------------------------------------------------
     * Constructor - khởi tạo API client
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    public ExamListController() {
        this.examApiClient = new ExamApiClient();
    }

    /* ---------------------------------------------------
     * Initialize - được gọi sau khi FXML loaded
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
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
     * --------------------------------------------------- */
    private void loadExams() {
        // Disable refresh button
        refreshButton.setDisable(true);
        
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
                });
                
            } catch (Exception e) {
                logger.error("Failed to load exams", e);
                Platform.runLater(() -> {
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
                TimeFormatter.formatDuration(exam.getDurationMinutes())),
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
     * --------------------------------------------------- */
    private void startExamSession(ExamInfoDTO exam) {
        try {
            logger.info("Starting exam session for: {}", exam.getTitle());
            
            // 1. Load exam-taking.fxml
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-taking.fxml")
            );
            Parent root = loader.load();
            
            // 2. Get ExamTakingController
            ExamTakingController controller = loader.getController();
            
            // 3. Initialize exam với examId và authToken
            String authToken = examApiClient.getAuthToken();
            controller.initializeExam(exam.getExamId(), authToken);
            
            // 4. Create new scene
            Scene scene = new Scene(root, 1400, 900);
            
            // 5. Load CSS
            String css = getClass().getResource("/css/exam-common.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // 6. Get current stage and switch scene
            Stage stage = (Stage) examCardsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Làm bài thi: " + exam.getTitle());
            stage.setMaximized(true); // Maximize window for better exam experience
            
            logger.info("Successfully navigated to exam taking screen");
            
        } catch (IOException e) {
            logger.error("Failed to load exam-taking.fxml", e);
            showError("Lỗi tải giao diện", 
                "Không thể mở màn hình làm bài thi: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during exam start", e);
            showError("Lỗi", 
                "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Handle filter changed event
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    @FXML
    private void onFilterChanged() {
        if (allExams == null) return;
        
        String selectedSubject = subjectFilterCombo.getValue();
        String selectedStatus = statusFilterCombo.getValue();
        
        // Filter exams
        filteredExams = allExams.stream()
            .filter(exam -> filterBySubject(exam, selectedSubject))
            .filter(exam -> filterByStatus(exam, selectedStatus))
            .collect(Collectors.toList());
        
        displayExams();
    }

    /* ---------------------------------------------------
     * Filter by subject
     * @param exam ExamInfoDTO
     * @param subject Subject filter value
     * @returns true nếu pass filter
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private boolean filterBySubject(ExamInfoDTO exam, String subject) {
        if (subject == null || subject.equals("Tất cả môn học")) {
            return true;
        }
        return exam.getSubjectName().equals(subject);
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

    /* ---------------------------------------------------
     * Set auth token cho API client
     * @param token JWT token
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    public void setAuthToken(String token) {
        examApiClient.setAuthToken(token);
    }
}
