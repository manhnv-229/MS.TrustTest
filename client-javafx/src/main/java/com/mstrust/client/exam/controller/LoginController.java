package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/* ---------------------------------------------------
 * Controller cho màn hình đăng nhập
 * Xử lý authentication và chuyển sang màn hình danh sách bài thi
 * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
 * --------------------------------------------------- */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private CheckBox rememberMeCheckbox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private HBox loadingBox;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    private ExamApiClient apiClient;
    private Stage stage;
    
    /* ---------------------------------------------------
     * Initialize controller với dependencies
     * @param apiClient ExamApiClient instance
     * @param stage Primary stage của application
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    public void initialize(ExamApiClient apiClient, Stage stage) {
        this.apiClient = apiClient;
        this.stage = stage;
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Load saved credentials if remember me was checked
        loadSavedCredentials();
        
        logger.info("Login controller initialized");
    }
    
    /* ---------------------------------------------------
     * Setup keyboard shortcuts (Enter to login)
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void setupKeyboardShortcuts() {
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> onLogin());
    }
    
    /* ---------------------------------------------------
     * Load saved credentials nếu có (remember me feature)
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void loadSavedCredentials() {
        // TODO: Implement remember me feature with secure storage
        // For now, leave empty
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện click nút đăng nhập
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    @FXML
    private void onLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin đăng nhập và mật khẩu");
            return;
        }
        
        if (!isValidLoginInput(email)) {
            showError("Thông tin đăng nhập không hợp lệ (Email, Mã sinh viên hoặc SĐT)");
            return;
        }
        
        // Show loading state
        setLoading(true);
        hideError();
        
        // Perform login asynchronously
        CompletableFuture.runAsync(() -> performLogin(email, password))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Lỗi kết nối: " + ex.getMessage());
                    logger.error("Login error", ex);
                });
                return null;
            });
    }
    
    /* ---------------------------------------------------
     * Thực hiện login API call
     * @param email Email của user
     * @param password Mật khẩu
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 21:45) - Added role-based redirect
     * --------------------------------------------------- */
    private void performLogin(String email, String password) {
        try {
            logger.info("Attempting login for user: {}", email);
            
            // Call login API (returns LoginResponse with role)
            var loginResponse = apiClient.login(email, password);
            
            logger.info("Login successful for user: {} with role: {}", 
                    email, loginResponse.getRole());
            
            // Save credentials if remember me is checked
            if (rememberMeCheckbox.isSelected()) {
                saveCredentials(email);
            }
            
            // Navigate based on role on JavaFX thread
            Platform.runLater(() -> navigateBasedOnRole(loginResponse));
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                setLoading(false);
                
                String errorMessage = "Đăng nhập thất bại";
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                        errorMessage = "Email hoặc mật khẩu không đúng";
                    } else if (e.getMessage().contains("Connection")) {
                        errorMessage = "Không thể kết nối đến server";
                    } else {
                        errorMessage = "Lỗi: " + e.getMessage();
                    }
                }
                
                showError(errorMessage);
                logger.error("Login failed for user: {}", email, e);
            });
        }
    }
    
    /* ---------------------------------------------------
     * Validate login input format (Email, Student Code, hoặc Phone Number)
     * @param input Input cần validate (có thể là email, mã SV, hoặc SĐT)
     * @return true nếu input hợp lệ
     * @author: K24DTCN210-NVMANH (03/12/2025 11:15)
     * Hỗ trợ đăng nhập đa phương thức: Email, Mã sinh viên, SĐT
     * --------------------------------------------------- */
    private boolean isValidLoginInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        input = input.trim();
        
        // Check if it's a valid email
        if (input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return true;
        }
        
        // Check if it's a valid student code (ví dụ: B21DCCN001, K24DTCN210)
        if (input.matches("^[A-Z][0-9]{2}[A-Z]{2,6}[0-9]{3}$")) {
            return true;
        }
        
        // Check if it's a valid phone number (10-11 digits, có thể bắt đầu bằng 0)
        if (input.matches("^0[0-9]{9,10}$")) {
            return true;
        }
        
        return false;
    }
    
    /* ---------------------------------------------------
     * Save credentials cho remember me feature
     * @param email Email cần lưu
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void saveCredentials(String email) {
        // TODO: Implement secure credential storage
        // For now, leave empty
        logger.info("Remember me selected for: {}", email);
    }
    
    /* ---------------------------------------------------
     * Navigate dựa trên role của user
     * @param loginResponse LoginResponse chứa user info và role
     * @author: K24DTCN210-NVMANH (25/11/2025 21:45)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 21:52) - Handle ROLE_ prefix
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Xử lý nhiều roles, chọn role cao nhất
     * --------------------------------------------------- */
    private void navigateBasedOnRole(com.mstrust.client.exam.dto.LoginResponse loginResponse) {
        String roleString = loginResponse.getRole();
        
        // Parse roles: có thể là string với nhiều roles được join bằng dấu phẩy
        // Ví dụ: "ROLE_TEACHER,ROLE_ADMIN" hoặc "ROLE_TEACHER, ROLE_ADMIN"
        List<String> roles = parseRoles(roleString);
        
        if (roles.isEmpty()) {
            logger.warn("No roles found in: {}", roleString);
            setLoading(false);
            showError("Không tìm thấy role: " + roleString);
            return;
        }
        
        // Normalize roles: remove "ROLE_" prefix và uppercase
        List<String> normalizedRoles = new ArrayList<>();
        for (String role : roles) {
            String normalized = role.trim();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            normalizedRoles.add(normalized.toUpperCase());
        }
        
        // Xác định role cao nhất theo thứ tự ưu tiên
        String highestRole = determineHighestRole(normalizedRoles);
        
        logger.info("User roles: {} -> normalized: {} -> highest: {}", 
            roles, normalizedRoles, highestRole);
        
        // Navigate dựa trên role cao nhất
        if ("STUDENT".equals(highestRole)) {
            // Student → Exam List
            navigateToExamList(loginResponse);
        } else if ("TEACHER".equals(highestRole) || 
                   "CLASS_MANAGER".equals(highestRole) ||
                   "DEPT_MANAGER".equals(highestRole) || 
                   "ADMIN".equals(highestRole)) {
            // Teacher/Manager/Admin → Teacher Dashboard
            navigateToTeacherDashboard(loginResponse);
        } else {
            logger.warn("Unknown highest role: {} (from roles: {})", highestRole, normalizedRoles);
            setLoading(false);
            showError("Role không xác định: " + highestRole);
        }
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
            logger.warn("Role {} not in priority map, using as highest", highestRole);
        }
        
        return highestRole;
    }
    
    /* ---------------------------------------------------
     * Navigate đến màn hình danh sách bài thi (cho Student)
     * @param loginResponse LoginResponse chứa user info
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:44) - Fixed size + center window
     * EditBy: K24DTCN210-NVMANH (01/12/2025 00:30) - Pass user info to controller
     * --------------------------------------------------- */
    private void navigateToExamList(com.mstrust.client.exam.dto.LoginResponse loginResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-list.fxml"));
            Parent root = loader.load();
            
            // Pass dependencies to controller
            ExamListController controller = loader.getController();
            // Use studentCode if available, otherwise fallback to email
            String code = loginResponse.getStudentCode() != null && !loginResponse.getStudentCode().isEmpty() 
                ? loginResponse.getStudentCode() 
                : loginResponse.getEmail();
                
            controller.initialize(apiClient, loginResponse.getUserName(), code, loginResponse.getRole());
            
            Scene scene = new Scene(root, 1200, 800);
            
            // Apply CSS
            try {
                String css = getClass().getResource("/css/teacher-styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("Could not load CSS for teacher dashboard");
            }
            
            stage.setScene(scene);
            stage.setTitle("MS.TrustTest - Danh Sách Bài Thi");
            stage.setResizable(true);
            stage.show();
            
            // ✅ Center window on screen
            com.mstrust.client.exam.util.WindowCenterHelper.centerStageOnScreen(stage);
            
            logger.info("Navigated to exam list screen");
            
        } catch (IOException e) {
            logger.error("Failed to load exam list screen", e);
            setLoading(false);
            showError("Không thể tải danh sách bài thi: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Navigate đến Teacher Dashboard (cho Teacher/Admin)
     * @param loginResponse LoginResponse chứa user info
     * @author: K24DTCN210-NVMANH (25/11/2025 21:46)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:44) - Fixed size + center window
     * --------------------------------------------------- */
    private void navigateToTeacherDashboard(com.mstrust.client.exam.dto.LoginResponse loginResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/teacher-main.fxml"));
            Parent root = loader.load();
            
            // Pass dependencies to controller
            com.mstrust.client.teacher.controller.TeacherMainController controller = loader.getController();
            controller.setupUserInfo(
                loginResponse.getUserName(), 
                loginResponse.getRole()
            );
            controller.setStage(stage);
            controller.setApiClient(apiClient);
            
            Scene scene = new Scene(root, 1500, 950);
            
            // Apply CSS
            try {
                String css = getClass().getResource("/css/teacher-styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("Could not load CSS for teacher dashboard");
            }
            
            stage.setScene(scene);
            stage.setTitle("MS.TrustTest - Quản Lý Giáo Viên");
            stage.setResizable(true);
            stage.show();
            
            // ✅ Center window on screen
            com.mstrust.client.exam.util.WindowCenterHelper.centerStageOnScreen(stage);
            
            logger.info("Navigated to teacher dashboard for role: {}", loginResponse.getRole());
            
        } catch (IOException e) {
            logger.error("Failed to load teacher dashboard", e);
            setLoading(false);
            showError("Không thể tải giao diện quản lý: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Set trạng thái loading
     * @param loading true để hiện loading, false để ẩn
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheckbox.setDisable(loading);
        
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
