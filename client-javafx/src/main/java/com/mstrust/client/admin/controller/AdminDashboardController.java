package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.AdminApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ---------------------------------------------------
 * Controller cho Admin Dashboard
 * Hiển thị statistics, charts, system health
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class AdminDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    // API Client
    private AdminApiClient apiClient;
    private Stage stage;
    
    // Statistics Labels
    @FXML private Label totalUsersLabel;
    @FXML private Label activeExamsLabel;
    @FXML private Label totalSubmissionsLabel;
    @FXML private Label alertsCountLabel;
    
    // System Health
    @FXML private ProgressBar cpuProgressBar;
    @FXML private Label cpuLabel;
    @FXML private ProgressBar memoryProgressBar;
    @FXML private Label memoryLabel;
    @FXML private ProgressBar diskProgressBar;
    @FXML private Label diskLabel;
    @FXML private Label dbConnectionsLabel;
    
    // Charts
    @FXML private LineChart<String, Number> examsPerDayChart;
    @FXML private LineChart<String, Number> passRateChart;
    @FXML private PieChart alertDistributionChart;
    
    // Chart loading overlays
    @FXML private StackPane examsChartContainer;
    @FXML private StackPane passRateChartContainer;
    @FXML private StackPane alertChartContainer;
    @FXML private VBox examsChartLoading;
    @FXML private VBox passRateChartLoading;
    @FXML private VBox alertChartLoading;
    
    // Activities
    @FXML private ListView<String> activitiesListView;
    
    // Buttons
    @FXML private Button refreshButton;
    
    // Data
    private ObservableList<String> activities = FXCollections.observableArrayList();
    
    /* ---------------------------------------------------
     * Initialize controller
     * @param baseUrl Base URL của API
     * @param authToken JWT token
     * @param stage Stage reference
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void initialize(String baseUrl, String authToken, Stage stage) {
        this.stage = stage;
        
        // Initialize API client
        this.apiClient = new AdminApiClient();
        this.apiClient.setToken(authToken);
        
        // Setup activities list
        activitiesListView.setItems(activities);
        
        // Initialize charts
        initializeCharts();
        
        // Show loading for charts
        showChartLoading();
        
        // Load data
        loadDashboardData();
    }
    
    /* ---------------------------------------------------
     * Initialize charts với empty data
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void initializeCharts() {
        // Exams Per Day Chart
        XYChart.Series<String, Number> examsSeries = new XYChart.Series<>();
        examsSeries.setName("Đề thi");
        examsPerDayChart.getData().add(examsSeries);
        examsPerDayChart.setAnimated(false);
        
        // Pass Rate Chart
        XYChart.Series<String, Number> passRateSeries = new XYChart.Series<>();
        passRateSeries.setName("Tỷ lệ đỗ (%)");
        passRateChart.getData().add(passRateSeries);
        passRateChart.setAnimated(false);
        
        // Alert Distribution Chart
        alertDistributionChart.setAnimated(false);
    }
    
    /* ---------------------------------------------------
     * Load dashboard data từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadDashboardData() {
        new Thread(() -> {
            try {
                // Load user statistics
                Map<String, Object> userStats = apiClient.getUserStatistics();
                Platform.runLater(() -> updateUserStatistics(userStats));
                
                // Load dashboard statistics (nếu có endpoint)
                try {
                    Map<String, Object> dashboardStats = apiClient.getDashboardStatistics();
                    Platform.runLater(() -> {
                        updateDashboardStatistics(dashboardStats);
                        hideChartLoading();
                    });
                } catch (AdminApiClient.ApiException e) {
                    // Endpoint chưa có, bỏ qua - chỉ log ở debug level
                    if (logger.isDebugEnabled()) {
                        logger.debug("Dashboard statistics endpoint not available: {}", e.getMessage());
                    }
                    Platform.runLater(() -> hideChartLoading());
                }
                
                // Load system health
                try {
                    Map<String, Object> health = apiClient.getSystemHealth();
                    Platform.runLater(() -> updateSystemHealth(health));
                } catch (AdminApiClient.ApiException e) {
                    // Endpoint chưa có, dùng mock data - chỉ log ở debug level
                    if (logger.isDebugEnabled()) {
                        logger.debug("System health endpoint not available: {}", e.getMessage());
                    }
                    Platform.runLater(() -> updateSystemHealthMock());
                }
                
            } catch (Exception e) {
                logger.error("Error loading dashboard data", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải dữ liệu dashboard: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update user statistics
     * @param stats User statistics map
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateUserStatistics(Map<String, Object> stats) {
        if (stats == null) return;
        
        // Total users
        if (stats.containsKey("totalUsers")) {
            Long total = getLongValue(stats.get("totalUsers"));
            totalUsersLabel.setText(String.valueOf(total));
        }
        
        // Users by role
        if (stats.containsKey("usersByRole")) {
            @SuppressWarnings("unchecked")
            Map<String, Long> usersByRole = (Map<String, Long>) stats.get("usersByRole");
            // Có thể hiển thị thêm thông tin này
        }
    }
    
    /* ---------------------------------------------------
     * Update dashboard statistics
     * @param stats Dashboard statistics map
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateDashboardStatistics(Map<String, Object> stats) {
        if (stats == null) return;
        
        // Active exams today
        if (stats.containsKey("activeExamsToday")) {
            Long count = getLongValue(stats.get("activeExamsToday"));
            activeExamsLabel.setText(String.valueOf(count));
        }
        
        // Total submissions
        if (stats.containsKey("totalSubmissions")) {
            Long count = getLongValue(stats.get("totalSubmissions"));
            totalSubmissionsLabel.setText(String.valueOf(count));
        }
        
        // Alerts count (last 7 days)
        if (stats.containsKey("alertsLast7Days")) {
            Long count = getLongValue(stats.get("alertsLast7Days"));
            alertsCountLabel.setText(String.valueOf(count));
        }
        
        // Update charts
        updateCharts(stats);
        
        // Hide loading after charts updated
        hideChartLoading();
    }
    
    /* ---------------------------------------------------
     * Update charts với data từ stats
     * @param stats Dashboard statistics
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateCharts(Map<String, Object> stats) {
        boolean hasData = false;
        
        // Exams per day chart
        if (stats.containsKey("examsPerDay")) {
            @SuppressWarnings("unchecked")
            Map<String, Number> examsData = (Map<String, Number>) stats.get("examsPerDay");
            XYChart.Series<String, Number> series = examsPerDayChart.getData().get(0);
            series.getData().clear();
            examsData.forEach((day, count) -> {
                series.getData().add(new XYChart.Data<>(day, count));
            });
            hasData = true;
        }
        
        // Pass rate chart
        if (stats.containsKey("passRateTrend")) {
            @SuppressWarnings("unchecked")
            Map<String, Number> passRateData = (Map<String, Number>) stats.get("passRateTrend");
            XYChart.Series<String, Number> series = passRateChart.getData().get(0);
            series.getData().clear();
            passRateData.forEach((time, rate) -> {
                series.getData().add(new XYChart.Data<>(time, rate));
            });
            hasData = true;
        }
        
        // Alert distribution pie chart
        if (stats.containsKey("alertDistribution")) {
            @SuppressWarnings("unchecked")
            Map<String, Number> alertData = (Map<String, Number>) stats.get("alertDistribution");
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            alertData.forEach((severity, count) -> {
                pieData.add(new PieChart.Data(severity + " (" + count + ")", count.doubleValue()));
            });
            alertDistributionChart.setData(pieData);
            hasData = true;
        }
        
        // Hide loading nếu có data hoặc không có endpoint
        if (hasData) {
            hideChartLoading();
        }
    }
    
    /* ---------------------------------------------------
     * Update system health
     * @param health System health map
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateSystemHealth(Map<String, Object> health) {
        if (health == null) return;
        
        // CPU usage
        if (health.containsKey("cpuUsage")) {
            double cpuUsage = getDoubleValue(health.get("cpuUsage"));
            cpuProgressBar.setProgress(cpuUsage / 100.0);
            cpuLabel.setText(String.format("%.1f%%", cpuUsage));
        }
        
        // Memory usage
        if (health.containsKey("memoryUsage")) {
            double memoryUsage = getDoubleValue(health.get("memoryUsage"));
            memoryProgressBar.setProgress(memoryUsage / 100.0);
            memoryLabel.setText(String.format("%.1f%%", memoryUsage));
        }
        
        // Disk usage
        if (health.containsKey("diskUsage")) {
            double diskUsage = getDoubleValue(health.get("diskUsage"));
            diskProgressBar.setProgress(diskUsage / 100.0);
            diskLabel.setText(String.format("%.1f%%", diskUsage));
        }
        
        // DB connections
        if (health.containsKey("dbConnections")) {
            String connections = String.valueOf(health.get("dbConnections"));
            dbConnectionsLabel.setText(connections);
        }
    }
    
    /* ---------------------------------------------------
     * Update system health với mock data (khi endpoint chưa có)
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateSystemHealthMock() {
        cpuProgressBar.setProgress(0.35);
        cpuLabel.setText("35.0%");
        memoryProgressBar.setProgress(0.62);
        memoryLabel.setText("62.0%");
        diskProgressBar.setProgress(0.48);
        diskLabel.setText("48.0%");
        dbConnectionsLabel.setText("5/20");
    }
    
    /* ---------------------------------------------------
     * Show loading indicators cho charts
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showChartLoading() {
        if (examsChartLoading != null) {
            examsChartLoading.setVisible(true);
            examsChartLoading.setManaged(true);
        }
        if (passRateChartLoading != null) {
            passRateChartLoading.setVisible(true);
            passRateChartLoading.setManaged(true);
        }
        if (alertChartLoading != null) {
            alertChartLoading.setVisible(true);
            alertChartLoading.setManaged(true);
        }
    }
    
    /* ---------------------------------------------------
     * Hide loading indicators cho charts
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void hideChartLoading() {
        if (examsChartLoading != null) {
            examsChartLoading.setVisible(false);
            examsChartLoading.setManaged(false);
        }
        if (passRateChartLoading != null) {
            passRateChartLoading.setVisible(false);
            passRateChartLoading.setManaged(false);
        }
        if (alertChartLoading != null) {
            alertChartLoading.setVisible(false);
            alertChartLoading.setManaged(false);
        }
    }
    
    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        refreshButton.setDisable(true);
        showChartLoading();
        loadDashboardData();
        refreshButton.setDisable(false);
    }
    
    /* ---------------------------------------------------
     * Handle quick action buttons
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleUserManagement() {
        // Sẽ được implement trong TeacherMainController
        showInfo("Quản lý Người dùng", "Chức năng này sẽ được mở từ menu.");
    }
    
    @FXML
    private void handleOrganization() {
        showInfo("Quản lý Tổ chức", "Chức năng này sẽ được mở từ menu.");
    }
    
    @FXML
    private void handleSystemConfig() {
        showInfo("Cấu hình Hệ thống", "Chức năng này sẽ được mở từ menu.");
    }
    
    @FXML
    private void handleReports() {
        showInfo("Báo cáo", "Chức năng này sẽ được mở từ menu.");
    }
    
    /* ---------------------------------------------------
     * Helper: Get Long value from Object
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private Long getLongValue(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /* ---------------------------------------------------
     * Helper: Get Double value from Object
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /* ---------------------------------------------------
     * Show error alert
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Show info alert
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

