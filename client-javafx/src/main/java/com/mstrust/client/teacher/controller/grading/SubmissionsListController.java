package com.mstrust.client.teacher.controller.grading;

import com.mstrust.client.teacher.api.GradingApiClient;
import com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Controller cho Submissions List Screen
 * Hiển thị danh sách bài nộp cần chấm với filters và stats
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
public class SubmissionsListController {
    
    // FXML Components - Filters
    @FXML private ComboBox<String> examFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    
    // FXML Components - Statistics
    @FXML private Label averageScoreLabel;
    @FXML private Label passRateLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label totalSubmissionsLabel;
    
    // FXML Components - Table
    @FXML private TableView<GradingSubmissionListDTO> submissionsTable;
    @FXML private TableColumn<GradingSubmissionListDTO, String> studentColumn;
    @FXML private TableColumn<GradingSubmissionListDTO, String> examColumn;
    @FXML private TableColumn<GradingSubmissionListDTO, String> scoreColumn;
    @FXML private TableColumn<GradingSubmissionListDTO, String> statusColumn;
    @FXML private TableColumn<GradingSubmissionListDTO, String> submitTimeColumn;
    @FXML private TableColumn<GradingSubmissionListDTO, String> progressColumn;
    
    // FXML Components - Loading
    @FXML private StackPane loadingPane;
    @FXML private Label loadingMessage;
    
    // Service
    private GradingApiClient apiClient;
    private Stage primaryStage;
    
    // State
    private ObservableList<GradingSubmissionListDTO> submissions = FXCollections.observableArrayList();
    private List<GradingSubmissionListDTO> allSubmissions = List.of();
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API client và stage
     * @param apiClient GradingApiClient instance
     * @param primaryStage Stage chính của application
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public void initialize(GradingApiClient apiClient, Stage primaryStage) {
        this.apiClient = apiClient;
        this.primaryStage = primaryStage;
        
        setupFilters();
        setupTable();
        loadSubmissions();
    }
    
    /* ---------------------------------------------------
     * Setup các filter dropdowns
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void setupFilters() {
        // Status filter
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả", "SUBMITTED", "GRADED"
        );
        statusFilter.setItems(statuses);
        statusFilter.setValue("Tất cả");
        
        // Exam filter - sẽ được load từ API
        examFilter.setPromptText("Tất cả đề thi");
    }
    
    /* ---------------------------------------------------
     * Setup table columns
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void setupTable() {
        // Student column
        studentColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getStudentName();
            String code = cellData.getValue().getStudentCode();
            return new javafx.beans.property.SimpleStringProperty(name + " (" + code + ")");
        });
        
        // Exam column
        examColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExamTitle()));
        
        // Score column
        scoreColumn.setCellValueFactory(cellData -> {
            Double autoScore = cellData.getValue().getAutoGradedScore();
            Double maxScore = cellData.getValue().getMaxScore();
            if (autoScore != null && maxScore != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.1f / %.1f", autoScore, maxScore));
            }
            return new javafx.beans.property.SimpleStringProperty("- / " + maxScore);
        });
        
        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String display = switch (status) {
                case "SUBMITTED" -> "⏳ Đã nộp";
                case "GRADED" -> "✓ Đã chấm";
                default -> status;
            };
            return new javafx.beans.property.SimpleStringProperty(display);
        });
        
        // Submit time column
        submitTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getSubmitTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getSubmitTime().format(DATE_FORMATTER));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        // Progress column
        progressColumn.setCellValueFactory(cellData -> {
            Double progress = cellData.getValue().getGradingProgress();
            if (progress != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.0f%%", progress));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        // Double-click to open grading view
        submissionsTable.setRowFactory(tv -> {
            TableRow<GradingSubmissionListDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openGradingView(row.getItem());
                }
            });
            return row;
        });
        
        submissionsTable.setItems(submissions);
    }
    
    /* ---------------------------------------------------
     * Load danh sách bài nộp từ API
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void loadSubmissions() {
        showLoading("Đang tải danh sách bài nộp...");
        
        new Thread(() -> {
            try {
                String status = statusFilter.getValue();
                String statusParam = (status == null || status.equals("Tất cả")) ? null : status;
                
                List<GradingSubmissionListDTO> result = apiClient.getSubmissionsForGrading(statusParam, null);
                
                Platform.runLater(() -> {
                    allSubmissions = result;
                    applyFilters();
                    updateStatistics();
                    hideLoading();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Không thể tải danh sách bài nộp: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Áp dụng filters lên danh sách
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void applyFilters() {
        ObservableList<GradingSubmissionListDTO> filtered = FXCollections.observableArrayList(allSubmissions);
        
        // Filter by status
        String status = statusFilter.getValue();
        if (status != null && !status.equals("Tất cả")) {
            filtered.removeIf(s -> !s.getStatus().equals(status));
        }
        
        // Filter by exam (TODO: implement when exam list is available)
        
        submissions.setAll(filtered);
    }
    
    /* ---------------------------------------------------
     * Update statistics panel
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void updateStatistics() {
        if (allSubmissions.isEmpty()) {
            totalSubmissionsLabel.setText("Tổng số: 0");
            averageScoreLabel.setText("Điểm TB: -");
            passRateLabel.setText("Tỷ lệ đạt: -");
            pendingCountLabel.setText("Chờ chấm: 0");
            return;
        }
        
        int total = allSubmissions.size();
        int pending = (int) allSubmissions.stream()
            .filter(s -> "SUBMITTED".equals(s.getStatus()))
            .count();
        
        double totalScore = allSubmissions.stream()
            .filter(s -> s.getAutoGradedScore() != null)
            .mapToDouble(GradingSubmissionListDTO::getAutoGradedScore)
            .sum();
        
        long gradedCount = allSubmissions.stream()
            .filter(s -> s.getAutoGradedScore() != null)
            .count();
        
        double avgScore = gradedCount > 0 ? totalScore / gradedCount : 0;
        
        // Pass rate (assuming 50% is passing)
        long passed = allSubmissions.stream()
            .filter(s -> {
                Double score = s.getAutoGradedScore();
                Double max = s.getMaxScore();
                return score != null && max != null && (score / max) >= 0.5;
            })
            .count();
        
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        
        totalSubmissionsLabel.setText("Tổng số: " + total);
        averageScoreLabel.setText(String.format("Điểm TB: %.1f", avgScore));
        passRateLabel.setText(String.format("Tỷ lệ đạt: %.1f%%", passRate));
        pendingCountLabel.setText("Chờ chấm: " + pending);
    }
    
    /* ---------------------------------------------------
     * Mở grading view cho submission được chọn
     * @param submission Submission cần chấm
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void openGradingView(GradingSubmissionListDTO submission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grading-view.fxml"));
            Parent gradingView = loader.load();
            
            GradingController controller = loader.getController();
            controller.initialize(apiClient, primaryStage, submission.getId());
            
            // Replace content in parent container (StackPane from TeacherMainController)
            javafx.scene.Node parent = submissionsTable.getScene().lookup("#contentArea");
            if (parent instanceof StackPane) {
                StackPane contentArea = (StackPane) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(gradingView);
            } else if (parent instanceof VBox) {
                VBox contentArea = (VBox) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(gradingView);
            }
        } catch (IOException e) {
            showError("Lỗi", "Không thể mở giao diện chấm điểm: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        loadSubmissions();
    }
    
    /* ---------------------------------------------------
     * Handle filter change
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleFilterChange() {
        applyFilters();
        updateStatistics();
    }
    
    /* ---------------------------------------------------
     * Handle export button click
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleExport() {
        showInfo("Xuất Excel", "Chức năng xuất Excel sẽ được implement sau.");
    }
    
    /* ---------------------------------------------------
     * Show loading overlay
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        Platform.runLater(() -> {
            loadingPane.setVisible(true);
            loadingMessage.setText(message);
        });
    }
    
    /* ---------------------------------------------------
     * Hide loading overlay
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void hideLoading() {
        Platform.runLater(() -> {
            loadingPane.setVisible(false);
        });
    }
    
    /* ---------------------------------------------------
     * Show error alert
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Show info alert
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}

