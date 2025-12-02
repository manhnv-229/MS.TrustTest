package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.SystemConfigApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/* ---------------------------------------------------
 * Controller cho System Configuration
 * Quản lý 5 tabs: Monitoring, Exam, Email, Security, Maintenance
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class SystemConfigController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigController.class);
    
    // API Client
    private SystemConfigApiClient apiClient;
    private Stage stage;
    
    // UI Components - Monitoring Tab
    @FXML private Spinner<Integer> screenshotIntervalSpinner;
    @FXML private Spinner<Double> screenshotQualitySpinner;
    @FXML private TextField screenshotMaxWidthField;
    @FXML private TextField screenshotMaxHeightField;
    @FXML private CheckBox enableScreenCaptureCheck;
    @FXML private CheckBox enableWindowFocusCheck;
    @FXML private CheckBox enableProcessCheck;
    @FXML private CheckBox enableClipboardCheck;
    @FXML private CheckBox enableKeystrokeCheck;
    @FXML private TextArea processBlacklistArea;
    @FXML private TextField windowSwitchThresholdField;
    @FXML private TextField clipboardThresholdField;
    
    // UI Components - Exam Tab
    @FXML private TextField defaultDurationField;
    @FXML private TextField defaultMaxAttemptsField;
    @FXML private CheckBox autoGradeMultipleChoiceCheck;
    @FXML private CheckBox autoGradeTrueFalseCheck;
    @FXML private CheckBox autoGradeMultipleSelectCheck;
    @FXML private CheckBox autoGradeFillInBlankCheck;
    @FXML private CheckBox autoGradeMatchingCheck;
    
    // UI Components - Email Tab
    @FXML private TextField smtpHostField;
    @FXML private TextField smtpPortField;
    @FXML private TextField smtpUsernameField;
    @FXML private PasswordField smtpPasswordField;
    @FXML private TextField smtpFromEmailField;
    @FXML private CheckBox smtpUseTlsCheck;
    @FXML private TextField testEmailField;
    
    // UI Components - Security Tab
    @FXML private TextField jwtExpirationField;
    @FXML private TextField passwordMinLengthField;
    @FXML private CheckBox passwordRequireUppercaseCheck;
    @FXML private CheckBox passwordRequireLowercaseCheck;
    @FXML private CheckBox passwordRequireNumberCheck;
    @FXML private CheckBox passwordRequireSpecialCheck;
    @FXML private TextField sessionTimeoutField;
    @FXML private TextField maxLoginAttemptsField;
    
    // UI Components - Maintenance Tab
    @FXML private ToggleButton maintenanceModeToggle;
    @FXML private Label maintenanceModeLabel;
    @FXML private TextArea systemInfoArea;
    
    // Buttons
    @FXML private Button saveButton;
    @FXML private Button refreshButton;
    
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
        this.apiClient = new SystemConfigApiClient();
        this.apiClient.setToken(authToken);
        
        // Setup spinners
        setupSpinners();
        
        // Load config từ API hoặc dùng default values
        loadSystemConfig();
    }
    
    /* ---------------------------------------------------
     * Setup spinners với default values
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupSpinners() {
        // Screenshot interval spinner (10-300 seconds)
        SpinnerValueFactory<Integer> intervalFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 30);
        screenshotIntervalSpinner.setValueFactory(intervalFactory);
        
        // Screenshot quality spinner (0.1-1.0)
        SpinnerValueFactory<Double> qualityFactory = 
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1.0, 0.7, 0.1);
        screenshotQualitySpinner.setValueFactory(qualityFactory);
    }
    
    /* ---------------------------------------------------
     * Load system config từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadSystemConfig() {
        new Thread(() -> {
            try {
                Map<String, Object> config = apiClient.getSystemConfig();
                Platform.runLater(() -> updateUIFromConfig(config));
            } catch (SystemConfigApiClient.ApiException e) {
                // Endpoint chưa có, dùng default values
                logger.warn("System config endpoint not available: {}", e.getMessage());
                Platform.runLater(() -> {
                    // UI đã có default values, không cần update
                });
            } catch (Exception e) {
                logger.error("Error loading system config", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải cấu hình hệ thống: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update UI từ config data
     * @param config Config map
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @SuppressWarnings("unchecked")
    private void updateUIFromConfig(Map<String, Object> config) {
        if (config == null) return;
        
        // Monitoring settings
        if (config.containsKey("monitoring")) {
            Map<String, Object> monitoring = (Map<String, Object>) config.get("monitoring");
            if (monitoring.containsKey("screenshotInterval")) {
                screenshotIntervalSpinner.getValueFactory().setValue(
                    getIntValue(monitoring.get("screenshotInterval")));
            }
            if (monitoring.containsKey("screenshotQuality")) {
                screenshotQualitySpinner.getValueFactory().setValue(
                    getDoubleValue(monitoring.get("screenshotQuality")));
            }
            // ... update other monitoring fields
        }
        
        // Exam settings
        if (config.containsKey("exam")) {
            Map<String, Object> exam = (Map<String, Object>) config.get("exam");
            if (exam.containsKey("defaultDuration")) {
                defaultDurationField.setText(String.valueOf(exam.get("defaultDuration")));
            }
            // ... update other exam fields
        }
        
        // Email settings
        if (config.containsKey("email")) {
            Map<String, Object> email = (Map<String, Object>) config.get("email");
            if (email.containsKey("smtpHost")) {
                smtpHostField.setText((String) email.get("smtpHost"));
            }
            // ... update other email fields
        }
        
        // Security settings
        if (config.containsKey("security")) {
            Map<String, Object> security = (Map<String, Object>) config.get("security");
            if (security.containsKey("jwtExpiration")) {
                jwtExpirationField.setText(String.valueOf(security.get("jwtExpiration")));
            }
            // ... update other security fields
        }
        
        // Maintenance settings
        if (config.containsKey("maintenanceMode")) {
            boolean maintenanceMode = (Boolean) config.get("maintenanceMode");
            maintenanceModeToggle.setSelected(maintenanceMode);
            updateMaintenanceModeLabel(maintenanceMode);
        }
    }
    
    /* ---------------------------------------------------
     * Handle save all button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleSaveAll() {
        saveButton.setDisable(true);
        
        new Thread(() -> {
            try {
                Map<String, Object> configData = collectConfigFromUI();
                apiClient.saveSystemConfig(configData);
                
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    showInfo("Thành công", "Đã lưu cấu hình hệ thống thành công.");
                });
            } catch (Exception e) {
                logger.error("Error saving system config", e);
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    showError("Lỗi", "Không thể lưu cấu hình: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Collect config data từ UI
     * @return Map chứa config data
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private Map<String, Object> collectConfigFromUI() {
        Map<String, Object> config = new HashMap<>();
        
        // Monitoring settings
        Map<String, Object> monitoring = new HashMap<>();
        monitoring.put("screenshotInterval", screenshotIntervalSpinner.getValue());
        monitoring.put("screenshotQuality", screenshotQualitySpinner.getValue());
        monitoring.put("screenshotMaxWidth", Integer.parseInt(screenshotMaxWidthField.getText()));
        monitoring.put("screenshotMaxHeight", Integer.parseInt(screenshotMaxHeightField.getText()));
        monitoring.put("enableScreenCapture", enableScreenCaptureCheck.isSelected());
        monitoring.put("enableWindowFocus", enableWindowFocusCheck.isSelected());
        monitoring.put("enableProcess", enableProcessCheck.isSelected());
        monitoring.put("enableClipboard", enableClipboardCheck.isSelected());
        monitoring.put("enableKeystroke", enableKeystrokeCheck.isSelected());
        monitoring.put("processBlacklist", processBlacklistArea.getText());
        monitoring.put("windowSwitchThreshold", Integer.parseInt(windowSwitchThresholdField.getText()));
        monitoring.put("clipboardThreshold", Integer.parseInt(clipboardThresholdField.getText()));
        config.put("monitoring", monitoring);
        
        // Exam settings
        Map<String, Object> exam = new HashMap<>();
        exam.put("defaultDuration", Integer.parseInt(defaultDurationField.getText()));
        exam.put("defaultMaxAttempts", Integer.parseInt(defaultMaxAttemptsField.getText()));
        exam.put("autoGradeMultipleChoice", autoGradeMultipleChoiceCheck.isSelected());
        exam.put("autoGradeTrueFalse", autoGradeTrueFalseCheck.isSelected());
        exam.put("autoGradeMultipleSelect", autoGradeMultipleSelectCheck.isSelected());
        exam.put("autoGradeFillInBlank", autoGradeFillInBlankCheck.isSelected());
        exam.put("autoGradeMatching", autoGradeMatchingCheck.isSelected());
        config.put("exam", exam);
        
        // Email settings
        Map<String, Object> email = new HashMap<>();
        email.put("smtpHost", smtpHostField.getText());
        email.put("smtpPort", Integer.parseInt(smtpPortField.getText()));
        email.put("smtpUsername", smtpUsernameField.getText());
        email.put("smtpPassword", smtpPasswordField.getText());
        email.put("smtpFromEmail", smtpFromEmailField.getText());
        email.put("smtpUseTls", smtpUseTlsCheck.isSelected());
        config.put("email", email);
        
        // Security settings
        Map<String, Object> security = new HashMap<>();
        security.put("jwtExpiration", Integer.parseInt(jwtExpirationField.getText()));
        security.put("passwordMinLength", Integer.parseInt(passwordMinLengthField.getText()));
        security.put("passwordRequireUppercase", passwordRequireUppercaseCheck.isSelected());
        security.put("passwordRequireLowercase", passwordRequireLowercaseCheck.isSelected());
        security.put("passwordRequireNumber", passwordRequireNumberCheck.isSelected());
        security.put("passwordRequireSpecial", passwordRequireSpecialCheck.isSelected());
        security.put("sessionTimeout", Integer.parseInt(sessionTimeoutField.getText()));
        security.put("maxLoginAttempts", Integer.parseInt(maxLoginAttemptsField.getText()));
        config.put("security", security);
        
        // Maintenance settings
        config.put("maintenanceMode", maintenanceModeToggle.isSelected());
        
        return config;
    }
    
    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        loadSystemConfig();
    }
    
    /* ---------------------------------------------------
     * Handle test email button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleTestEmail() {
        String emailAddress = testEmailField.getText();
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            showError("Lỗi", "Vui lòng nhập email để test.");
            return;
        }
        
        new Thread(() -> {
            try {
                apiClient.testEmail(emailAddress);
                Platform.runLater(() -> {
                    showInfo("Thành công", "Email test đã được gửi đến " + emailAddress);
                });
            } catch (Exception e) {
                logger.error("Error testing email", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể gửi email test: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handle maintenance mode toggle
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleMaintenanceModeToggle() {
        boolean isMaintenanceMode = maintenanceModeToggle.isSelected();
        updateMaintenanceModeLabel(isMaintenanceMode);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(isMaintenanceMode ? 
            "Bật Maintenance Mode?" : "Tắt Maintenance Mode?");
        confirm.setContentText(isMaintenanceMode ? 
            "Hệ thống sẽ tạm dừng để bảo trì." : 
            "Hệ thống sẽ hoạt động bình thường trở lại.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                // Revert toggle
                maintenanceModeToggle.setSelected(!isMaintenanceMode);
                updateMaintenanceModeLabel(!isMaintenanceMode);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Update maintenance mode label
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateMaintenanceModeLabel(boolean isMaintenanceMode) {
        if (isMaintenanceMode) {
            maintenanceModeToggle.setText("Bật");
            maintenanceModeLabel.setText("⚠️ Hệ thống đang ở chế độ bảo trì");
            maintenanceModeLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else {
            maintenanceModeToggle.setText("Tắt");
            maintenanceModeLabel.setText("✅ Hệ thống đang hoạt động bình thường");
            maintenanceModeLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }
    }
    
    /* ---------------------------------------------------
     * Handle clear cache button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleClearCache() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Bạn có chắc muốn xóa cache?");
        confirm.setContentText("Thao tác này sẽ xóa tất cả cache của hệ thống.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        apiClient.clearCache();
                        Platform.runLater(() -> {
                            showInfo("Thành công", "Đã xóa cache thành công.");
                        });
                    } catch (Exception e) {
                        logger.error("Error clearing cache", e);
                        Platform.runLater(() -> {
                            showError("Lỗi", "Không thể xóa cache: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Handle create backup button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleCreateBackup() {
        showInfo("Tạo Backup", "Chức năng tạo backup sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    /* ---------------------------------------------------
     * Handle restore backup button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRestoreBackup() {
        showInfo("Restore Backup", "Chức năng restore backup sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    /* ---------------------------------------------------
     * Handle view logs button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleViewLogs() {
        systemInfoArea.setText("System Logs:\n" +
            "Chức năng xem logs sẽ được implement trong bản cập nhật tiếp theo.\n" +
            "Logs sẽ được hiển thị ở đây.");
    }
    
    /* ---------------------------------------------------
     * Helper methods
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private int getIntValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

