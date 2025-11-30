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
    @FXML private Button createExamButton;
    @FXML private Button gradingButton;
    @FXML private Button monitoringButton;
    @FXML private Button helpButton;
    
    // Admin-only section
    @FXML private VBox adminMenuSection;
    @FXML private Button userManagementButton;
    @FXML private Button organizationButton;
    @FXML private Button systemConfigButton;
    
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
        if (userManagementButton != null) {
            userManagementButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createUserManagementIcon());
        }
        if (organizationButton != null) {
            organizationButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createOrganizationIcon());
        }
        if (systemConfigButton != null) {
            systemConfigButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createSettingsIcon());
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
     * @param role Vai trò (TEACHER, ADMIN hoặc ROLE_TEACHER, ROLE_ADMIN)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 22:15) - Fix lambda final variable
     * --------------------------------------------------- */
    public void setupUserInfo(String userName, String role) {
        this.currentUserName = userName;
        this.currentUserRole = role;
        
        // Normalize role for display: remove ROLE_ prefix nếu có
        final String displayRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        
        Platform.runLater(() -> {
            userLabel.setText(userName);
            roleLabel.setText("[" + displayRole + "]");
            
            // Show admin menu nếu role là ADMIN
            if ("ADMIN".equals(displayRole)) {
                adminMenuSection.setVisible(true);
                adminMenuSection.setManaged(true);
            }
        });
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
     * --------------------------------------------------- */
    @FXML
    private void handleExamManagementClick() {
        loadView("/view/exam-list.fxml", "Quản lý Đề thi");
        highlightSelectedMenu(examManagementButton);
    }
    
    /* ---------------------------------------------------
     * Handle Create Exam Wizard menu click - Launch wizard trong modal window
     * @author: K24DTCN210-NVMANH (28/11/2025 10:11)
     * EditBy: K24DTCN210-NVMANH (28/11/2025 10:45) - Fix API client initialization
     * --------------------------------------------------- */
    @FXML
    private void handleCreateExam() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/wizard/exam-creation-wizard.fxml")
            );
            Parent wizardView = loader.load();
            
            // Get controller và set login response (chứa token)
            com.mstrust.client.teacher.controller.wizard.ExamCreationWizardController wizardController = 
                loader.getController();
            
            // Create LoginResponse với token từ apiClient
            com.mstrust.client.exam.dto.LoginResponse loginResponse = 
                new com.mstrust.client.exam.dto.LoginResponse();
            loginResponse.setToken(apiClient.getAuthToken());
            
            // Set login response cho wizard (wizard sẽ tự khởi tạo API client)
            wizardController.setLoginResponse(loginResponse);
            
            // Start wizard từ Step 1
            wizardController.startWizard();
            
            // Create modal stage for wizard
            Stage wizardStage = new Stage();
            wizardStage.setTitle("Tạo đề thi mới - Wizard");
            wizardStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            wizardStage.initOwner(stage);
            
            Scene wizardScene = new Scene(wizardView, 900, 700);
            wizardStage.setScene(wizardScene);
            wizardStage.setResizable(false);
            
            // Show wizard và đợi đóng
            wizardStage.showAndWait();
            
            System.out.println("Wizard đã đóng");
            
        } catch (IOException e) {
            showError("Lỗi mở Wizard", 
                    "Không thể mở wizard tạo đề thi.\n" +
                    "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Handle Grading menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleGradingClick() {
        showInfo("Chức năng Chấm bài", 
                "Chức năng chấm bài sẽ được tích hợp từ Phase 7.\n" +
                "Hiện tại đang trong quá trình phát triển.");
    }
    
    /* ---------------------------------------------------
     * Handle Monitoring menu click
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleMonitoringClick() {
        showInfo("Chức năng Giám sát", 
                "Chức năng giám sát thi sẽ được tích hợp từ Phase 6.\n" +
                "Hiện tại đang trong quá trình phát triển.");
    }
    
    /* ---------------------------------------------------
     * Handle User Management menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleUserManagementClick() {
        showInfo("Quản lý Người dùng", 
                "Chức năng quản lý người dùng sẽ được phát triển trong phase tới.");
    }
    
    /* ---------------------------------------------------
     * Handle Organization menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleOrganizationClick() {
        showInfo("Quản lý Tổ chức", 
                "Chức năng quản lý tổ chức đã có Backend APIs (Phase 3).\n" +
                "UI sẽ được phát triển trong phase tới.");
    }
    
    /* ---------------------------------------------------
     * Handle System Config menu click (Admin only)
     * @author: K24DTCN210-NVMANH (25/11/2025 21:05)
     * --------------------------------------------------- */
    @FXML
    private void handleSystemConfigClick() {
        showInfo("Cấu hình Hệ thống", 
                "Chức năng cấu hình hệ thống sẽ được phát triển trong phase tới.");
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
            userManagementButton.getStyleClass().remove("menu-item-selected");
            organizationButton.getStyleClass().remove("menu-item-selected");
            systemConfigButton.getStyleClass().remove("menu-item-selected");
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
