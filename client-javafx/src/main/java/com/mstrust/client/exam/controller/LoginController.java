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
            showError("Vui lòng nhập đầy đủ email và mật khẩu");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("Email không hợp lệ");
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
     * --------------------------------------------------- */
    private void performLogin(String email, String password) {
        try {
            logger.info("Attempting login for user: {}", email);
            
            // Call login API (returns token and sets it in apiClient)
            apiClient.login(email, password);
            
            logger.info("Login successful for user: {}", email);
            
            // Save credentials if remember me is checked
            if (rememberMeCheckbox.isSelected()) {
                saveCredentials(email);
            }
            
            // Navigate to exam list on JavaFX thread
            Platform.runLater(this::navigateToExamList);
            
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
     * Validate email format
     * @param email Email cần validate
     * @return true nếu email hợp lệ
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
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
     * Navigate đến màn hình danh sách bài thi
     * @author: K24DTCN210-NVMANH (24/11/2025 08:00)
     * --------------------------------------------------- */
    private void navigateToExamList() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-list.fxml"));
            Parent root = loader.load();
            
            // Pass dependencies to controller
            ExamListController controller = loader.getController();
            controller.initialize(apiClient);
            
            Scene scene = new Scene(root, 1200, 800);
            
            // Apply CSS
            try {
                String css = getClass().getResource("/css/exam-common.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("Could not load CSS");
            }
            
            stage.setScene(scene);
            stage.setTitle("MS.TrustTest - Danh Sách Bài Thi");
            stage.setResizable(true);
            stage.setMaximized(true);
            
            logger.info("Navigated to exam list screen");
            
        } catch (IOException e) {
            logger.error("Failed to load exam list screen", e);
            setLoading(false);
            showError("Không thể tải danh sách bài thi: " + e.getMessage());
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
