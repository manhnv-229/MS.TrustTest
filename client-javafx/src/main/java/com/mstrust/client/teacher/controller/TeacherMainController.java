package com.mstrust.client.teacher.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/* ---------------------------------------------------
 * Controller cho Teacher Main Dashboard
 * Quản lý navigation giữa các chức năng Teacher/Admin
 * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
 * --------------------------------------------------- */
public class TeacherMainController {

    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Button logoutButton;
    
    // Menu buttons
    @FXML private Button questionBankButton;
    @FXML private Button subjectManagementButton;
    @FXML private Button examManagementButton;
    @FXML private Button gradingButton;
    @FXML private Button monitoringButton;
    @FXML private Button helpButton;
    
    // Admin-only section
    @FXML private VBox adminMenuSection;
    @FXML private Button adminDashboardButton;
    @FXML private Button userManagementButton;
    @FXML private Button organizationButton;
    @FXML private Button systemConfigButton;
    @FXML private Button reportsButton;
    
    // Content area
    @FXML private StackPane contentArea;
    
    // Status bar
    @FXML private Label connectionStatus;
    @FXML private Label lastSyncTime;
    @FXML private Label versionLabel;
    
    private String currentUserRole;
    private String currentUserName;
    private Stage stage;
    private Timer syncTimer;
    private com.mstrust.client.exam.api.ExamApiClient apiClient;
    private com.mstrust.client.exam.ExamClientApplication application;
    
    /* ---------------------------------------------------
     * Initialize controller sau khi FXML loaded
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 16:57) - Thêm Ikonli icons
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        // Setup icons cho menu items
        setupMenuIcons();
        // Setup sync timer
        setupSyncTimer();
    }
    
    /* ---------------------------------------------------
     * Setup Ikonli icons cho các menu items
     * @author: K24DTCN210-NVMANH (27/11/2025 16:57)
     * --------------------------------------------------- */
    private void setupMenuIcons() {
        // Main menu icons
        questionBankButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createQuestionBankIcon());
        subjectManagementButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createSubjectIcon());
        examManagementButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createExamIcon());
        gradingButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createGradingIcon());
        monitoringButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createMonitoringIcon());
        
        // Admin menu icons (sẽ được hiển thị nếu user là admin)
        if (adminDashboardButton != null) {
            adminDashboardButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createDashboardIcon());
        }
        if (userManagementButton != null) {
            userManagementButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createUserManagementIcon());
        }
        if (organizationButton != null) {
            organizationButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createOrganizationIcon());
        }
        if (systemConfigButton != null) {
            systemConfigButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createSettingsIcon());
        }
        if (reportsButton != null) {
            reportsButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createReportsIcon());
        }
        
        // Logout button icon
        logoutButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createLogoutIcon());
        
        // Help button icon  
        if (helpButton != null) {
            helpButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createHelpIcon());
        }
    }
    
    /* ---------------------------------------------------
     * Setup user info và role-based UI
     * @param userName Tên người dùng
     * @param role Vai trò (TEACHER, ADMIN hoặc ROLE_TEACHER, ROLE_ADMIN hoặc comma-separated roles)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 22:15) - Fix lambda final variable
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Xử lý nhiều roles, chọn role cao nhất
     * --------------------------------------------------- */
    public void setupUserInfo(String userName, String role) {
        this.currentUserName = userName;
        this.currentUserRole = role;
        
        // Parse roles: có thể là string với nhiều roles được join bằng dấu phẩy
        // Ví dụ: "ROLE_TEACHER,ROLE_ADMIN" hoặc "ROLE_TEACHER, ROLE_ADMIN"
        List<String> roles = parseRoles(role);
        
        if (roles.isEmpty()) {
            // Fallback: nếu không parse được, dùng role gốc
            final String displayRole = role.startsWith("ROLE_") ? role.substring(5) : role;
            Platform.runLater(() -> {
                userLabel.setText(userName);
                roleLabel.setText("[" + displayRole + "]");
                adminMenuSection.setVisible(false);
                adminMenuSection.setManaged(false);
            });
            return;
        }
        
        // Normalize roles: remove "ROLE_" prefix và uppercase
        List<String> normalizedRoles = new ArrayList<>();
        for (String r : roles) {
            String normalized = r.trim();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            normalizedRoles.add(normalized.toUpperCase());
        }
        
        // Xác định role cao nhất theo thứ tự ưu tiên
        String highestRole = determineHighestRole(normalizedRoles);
        
        final String displayRole = highestRole;
        
        Platform.runLater(() -> {
            userLabel.setText(userName);
            roleLabel.setText("[" + displayRole + "]");
            
            // Show admin menu nếu role cao nhất là ADMIN, DEPT_MANAGER, hoặc CLASS_MANAGER
            if ("ADMIN".equals(displayRole) || 
                "DEPT_MANAGER".equals(displayRole) || 
                "CLASS_MANAGER".equals(displayRole)) {
                adminMenuSection.setVisible(true);
                adminMenuSection.setManaged(true);
            } else {
                adminMenuSection.setVisible(false);
                adminMenuSection.setManaged(false);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Parse roles string thành list
     * Hỗ trợ nhiều format: "ROLE_TEACHER,ROLE_ADMIN", "ROLE_TEACHER, ROLE_ADMIN", etc.
     * @param roleString String chứa roles (có thể là single role hoặc comma-separated)
     * @return List các role strings
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private List<String> parseRoles(String roleString) {
        List<String> roles = new ArrayList<>();
        
        if (roleString == null || roleString.trim().isEmpty()) {
            return roles;
        }
        
        // Split by comma và trim từng role
        String[] parts = roleString.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                roles.add(trimmed);
            }
        }
        
        return roles;
    }
    
    /* ---------------------------------------------------
     * Xác định role cao nhất theo thứ tự ưu tiên
     * Thứ tự: ADMIN > DEPT_MANAGER > CLASS_MANAGER > TEACHER > STUDENT
     * @param roles List các normalized roles (đã remove ROLE_ prefix và uppercase)
     * @return Role cao nhất
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private String determineHighestRole(List<String> roles) {
        if (roles.isEmpty()) {
            return "UNKNOWN";
        }
        
        // Định nghĩa thứ tự ưu tiên (số nhỏ hơn = cao hơn)
        Map<String, Integer> rolePriority = new HashMap<>();
        rolePriority.put("ADMIN", 1);
        rolePriority.put("DEPT_MANAGER", 2);
        rolePriority.put("CLASS_MANAGER", 3);
        rolePriority.put("TEACHER", 4);
        rolePriority.put("STUDENT", 5);
        
        String highestRole = null;
        int highestPriority = Integer.MAX_VALUE;
        
        for (String role : roles) {
            Integer priority = rolePriority.get(role);
            if (priority != null && priority < highestPriority) {
                highestPriority = priority;
                highestRole = role;
            }
        }
        
        // Nếu không tìm thấy role nào trong priority map, trả về role đầu tiên
        if (highestRole == null) {
            highestRole = roles.get(0);
        }
        
        return highestRole;
    }
    
    /* ---------------------------------------------------
     * Setup timer để update sync time
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void setupSyncTimer() {
        syncTimer = new Timer(true); // Daemon thread
        syncTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    lastSyncTime.setText(now.format(formatter));
                });
            }
        }, 0, 30000); // Update mỗi 30 giây
    }
    
    /* ---------------------------------------------------
     * Set stage reference
     * @param stage Primary stage
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:31) - Extract application from stage userData
     * --------------------------------------------------- */
    public void setStage(Stage stage) {
        this.stage = stage;
        // Try to get application instance from stage userData
        if (stage.getUserData() instanceof com.mstrust.client.exam.ExamClientApplication) {
            this.application = (com.mstrust.client.exam.ExamClientApplication) stage.getUserData();
        }
    }
    
    /* ---------------------------------------------------
     * Set API client reference
     * @param apiClient ExamApiClient instance
     * @author: K24DTCN210-NVMANH (25/11/2025 21:47)
     * --------------------------------------------------- */
    public void setApiClient(com.mstrust.client.exam.api.ExamApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /* ---------------------------------------------------
     * Handle Subject Management menu click
     * @author: K24DTCN210-NVMANH (26/11/2025 02:02)
     * --------------------------------------------------- */
    @FXML
    private void handleSubjectManagementClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/subject-management.fxml"));
            Parent subjectManagementView = loader.load();
            
            // Get controller and initialize with API client
            SubjectManagementController controller = loader.getController();
            
            // Create API client with same base URL and auth token
            // Note: Add /api prefix to base URL for Subject Management endpoints
            String baseUrlWithApi = apiClient.getBaseUrl() + "/api";
            com.mstrust.client.teacher.api.SubjectApiClient subjectApiClient = 
                new com.mstrust.client.teacher.api.SubjectApiClient(baseUrlWithApi);
            subjectApiClient.setAuthToken(apiClient.getAuthToken());
            
            controller.initialize(subjectApiClient, stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(subjectManagementView);
            highlightSelectedMenu(subjectManagementButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Quản lý Môn học: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Question Bank menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleQuestionBankClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/question-bank.fxml"));
            Parent questionBankView = loader.load();
            
            // Get controller and initialize with API client
            QuestionBankController controller = loader.getController();
            
            // Create API client with same base URL and auth token
            com.mstrust.client.teacher.api.QuestionBankApiClient questionApiClient = 
                new com.mstrust.client.teacher.api.QuestionBankApiClient(apiClient.getBaseUrl());
            questionApiClient.setAuthToken(apiClient.getAuthToken());
            
            controller.initialize(questionApiClient, stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(questionBankView);
            highlightSelectedMenu(questionBankButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Quản lý Ngân hàng Câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Exam Management menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Set stage và apiClient cho ExamListController
     * --------------------------------------------------- */
    @FXML
    private void handleExamManagementClick() {
        try {
            // Load exam-management.fxml riêng cho teacher/admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/exam-management.fxml"));
            Parent view = loader.load();
            
            // Get controller và set ExamManagementApiClient + stage
            com.mstrust.client.teacher.controller.ExamManagementController controller = loader.getController();
            
            // Tạo ExamManagementApiClient với token từ apiClient
            com.mstrust.client.teacher.api.ExamManagementApiClient examManagementApiClient = 
                new com.mstrust.client.teacher.api.ExamManagementApiClient();
            examManagementApiClient.setToken(apiClient.getAuthToken());
            
            // Initialize với ExamManagementApiClient và stage
            controller.initialize(examManagementApiClient, stage);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            highlightSelectedMenu(examManagementButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", 
                    "Không thể tải Quản lý Đề thi.\n" +
                    "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Grading menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (01/12/2025) - Implement grading UI
     * --------------------------------------------------- */
    @FXML
    private void handleGradingClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/submissions-list.fxml"));
            Parent submissionsListView = loader.load();
            
            // Get controller and initialize with API client
            com.mstrust.client.teacher.controller.grading.SubmissionsListController controller = loader.getController();
            
            // Create GradingApiClient with same base URL and auth token
            com.mstrust.client.teacher.api.GradingApiClient gradingApiClient = 
                new com.mstrust.client.teacher.api.GradingApiClient();
            gradingApiClient.setToken(apiClient.getAuthToken());
            
            controller.initialize(gradingApiClient, stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(submissionsListView);
            highlightSelectedMenu(gradingButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Chấm bài: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Monitoring menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 12:15) - Tích hợp Monitoring Dashboard
     * --------------------------------------------------- */
    @FXML
    private void handleMonitoringClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/monitoring-dashboard.fxml"));
            Parent monitoringView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.teacher.controller.monitoring.MonitoringDashboardController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(monitoringView);
            highlightSelectedMenu(monitoringButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Giám sát Thi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Admin Dashboard (Admin only) - mở Admin Dashboard
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleAdminDashboardClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin-dashboard.fxml"));
            Parent adminDashboardView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.admin.controller.AdminDashboardController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(adminDashboardView);
            highlightSelectedMenu(adminDashboardButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Admin Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle User Management menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Implement user management UI
     * --------------------------------------------------- */
    @FXML
    private void handleUserManagementClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user-management.fxml"));
            Parent userManagementView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.admin.controller.UserManagementController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(userManagementView);
            highlightSelectedMenu(userManagementButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Quản lý Người dùng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Organization menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Implement organization management UI
     * --------------------------------------------------- */
    @FXML
    private void handleOrganizationClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/organization-management.fxml"));
            Parent organizationView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.admin.controller.OrganizationManagementController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(organizationView);
            highlightSelectedMenu(organizationButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Quản lý Tổ chức: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle System Config menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Implement system config UI
     * --------------------------------------------------- */
    @FXML
    private void handleSystemConfigClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/system-config.fxml"));
            Parent systemConfigView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.admin.controller.SystemConfigController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(systemConfigView);
            highlightSelectedMenu(systemConfigButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Cấu hình Hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Reports menu click (Admin only)
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleReportsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/reports-view.fxml"));
            Parent reportsView = loader.load();
            
            // Get controller and initialize
            com.mstrust.client.admin.controller.ReportsController controller = 
                loader.getController();
            
            // Initialize với base URL và auth token
            String baseUrl = apiClient.getBaseUrl();
            controller.initialize(baseUrl, apiClient.getAuthToken(), stage);
            
            // Load view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(reportsView);
            highlightSelectedMenu(reportsButton);
            
        } catch (IOException e) {
            showError("Lỗi tải View", "Không thể tải Báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Help menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleHelpClick() {
        showInfo("Trợ giúp", 
                "MS.TrustTest - Hệ thống thi trắc nghiệm\n\n" +
                "Version: 1.0.0\n" +
                "Developed by: K24DTCN210-NVMANH\n\n" +
                "Để được hỗ trợ, vui lòng liên hệ admin.");
    }
    
    /* ---------------------------------------------------
     * Handle Logout button click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Đăng xuất");
        confirm.setHeaderText("Bạn có chắc muốn đăng xuất?");
        confirm.setContentText("Phiên làm việc hiện tại sẽ kết thúc.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cleanup();
            backToLogin();
        }
    }
    
    /* ---------------------------------------------------
     * Load view vào content area
     * @param fxmlPath Đường dẫn FXML file
     * @param title Tiêu đề view
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            System.out.println("Đã load view: " + title);
        } catch (IOException e) {
            showError("Lỗi tải View", 
                    "Không thể tải " + title + ".\n" +
                    "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Highlight menu item được chọn
     * @param selectedButton Button được chọn
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void highlightSelectedMenu(Button selectedButton) {
        // Remove highlight from all menu buttons
        questionBankButton.getStyleClass().remove("menu-item-selected");
        subjectManagementButton.getStyleClass().remove("menu-item-selected");
        examManagementButton.getStyleClass().remove("menu-item-selected");
        gradingButton.getStyleClass().remove("menu-item-selected");
        monitoringButton.getStyleClass().remove("menu-item-selected");
        
        if (adminMenuSection.isVisible()) {
            adminDashboardButton.getStyleClass().remove("menu-item-selected");
            userManagementButton.getStyleClass().remove("menu-item-selected");
            organizationButton.getStyleClass().remove("menu-item-selected");
            systemConfigButton.getStyleClass().remove("menu-item-selected");
            reportsButton.getStyleClass().remove("menu-item-selected");
        }
        
        // Add highlight to selected button
        selectedButton.getStyleClass().add("menu-item-selected");
    }
    
    /* ---------------------------------------------------
     * Quay lại màn hình login
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:32) - Use application.showLoginScreen() để khởi tạo đúng
     * --------------------------------------------------- */
    private void backToLogin() {
        if (application != null) {
            // Gọi lại showLoginScreen() từ Application để khởi tạo đúng dependencies và CSS
            application.showLoginScreen();
        } else {
            showError("Lỗi", "Không thể quay lại màn hình đăng nhập!");
        }
    }
    
    /* ---------------------------------------------------
     * Cleanup resources trước khi logout
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void cleanup() {
        if (syncTimer != null) {
            syncTimer.cancel();
            syncTimer = null;
        }
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param title Tiêu đề
     * @param message Nội dung
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo thông tin
     * @param title Tiêu đề
     * @param message Nội dung
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
