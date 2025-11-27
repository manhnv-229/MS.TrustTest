package com.mstrust.client.exam;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.controller.LoginController;
import com.mstrust.client.exam.util.WindowCenterHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/* ---------------------------------------------------
 * Main Application class cho MS.TrustTest Exam Client
 * Khởi tạo ứng dụng JavaFX, hiển thị màn hình đăng nhập
 * và quản lý vòng đời của ứng dụng
 * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
 * --------------------------------------------------- */
public class ExamClientApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ExamClientApplication.class);
    
    private Stage primaryStage;
    private ExamApiClient apiClient;
    private String apiBaseUrl;
    
    /* ---------------------------------------------------
     * Entry point của JavaFX application
     * @param primaryStage Stage chính của ứng dụng
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * --------------------------------------------------- */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Load configuration
            loadConfiguration();
            
            // Initialize API client
            apiClient = new ExamApiClient(apiBaseUrl);
            
            // Configure stage
            configureStage();
            
            // Show login screen
            showLoginScreen();
            
            logger.info("MS.TrustTest Exam Client started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            showErrorAndExit("Không thể khởi động ứng dụng: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Load cấu hình từ file config.properties
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * --------------------------------------------------- */
    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            
            if (input == null) {
                logger.warn("config.properties not found, using default configuration");
                apiBaseUrl = "http://localhost:8080";
                return;
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            apiBaseUrl = prop.getProperty("api.base.url", "http://localhost:8080");
            
            logger.info("Configuration loaded: API Base URL = {}", apiBaseUrl);
            
        } catch (IOException e) {
            logger.warn("Error loading configuration, using defaults", e);
            apiBaseUrl = "http://localhost:8080";
        }
    }
    
    /* ---------------------------------------------------
     * Cấu hình các thuộc tính của primary stage
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:33) - Store application instance in stage userData
     * --------------------------------------------------- */
    private void configureStage() {
        primaryStage.setTitle("MS.TrustTest - Hệ Thống Thi Trực Tuyến");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Store application instance in stage userData để các controller có thể access
        primaryStage.setUserData(this);
        
        // Handle close request
        primaryStage.setOnCloseRequest(this::handleExit);
    }
    
    /* ---------------------------------------------------
     * Hiển thị màn hình đăng nhập
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 15:30) - Made public for logout flow
     * --------------------------------------------------- */
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            
            // Pass dependencies to controller
            LoginController controller = loader.getController();
            controller.initialize(apiClient, primaryStage);
            
            Scene scene = new Scene(root, 400, 500);
            
            // Apply CSS if available
            try {
                String css = getClass().getResource("/css/exam-common.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("Could not load CSS, using default styling");
            }
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
            // ✅ Center login window on screen (phải gọi sau show())
            WindowCenterHelper. centerStageOnScreen(primaryStage);
            
        } catch (IOException e) {
            logger.error("Failed to load login screen", e);
            showErrorAndExit("Không thể tải màn hình đăng nhập: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện đóng cửa sổ
     * @param event WindowEvent
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * --------------------------------------------------- */
    private void handleExit(WindowEvent event) {
        logger.info("Application exit requested");
        
        // Cleanup resources if needed
        try {
            if (apiClient != null) {
                // Close any open connections
                logger.info("Cleaning up API client resources");
            }
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
        
        logger.info("MS.TrustTest Exam Client stopped");
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi và thoát ứng dụng
     * @param message Thông báo lỗi
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 15:04) - Added dialog centering
     * --------------------------------------------------- */
    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Lỗi Khởi Động");
        alert.setHeaderText("Không thể khởi động ứng dụng");
        alert.setContentText(message);
        
        // ✅ Center error dialog
        WindowCenterHelper.centerWindowOnShown(alert.getDialogPane().getScene().getWindow());
        
        alert.showAndWait();
        System.exit(1);
    }
    
    /* ---------------------------------------------------
     * Stop method - cleanup khi ứng dụng đóng
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * --------------------------------------------------- */
    @Override
    public void stop() {
        logger.info("Application stop method called");
    }
    
    /* ---------------------------------------------------
     * Main method - entry point của Java application
     * @param args Command line arguments
     * @author: K24DTCN210-NVMANH (24/11/2025 07:57)
     * --------------------------------------------------- */
    public static void main(String[] args) {
        logger.info("Starting MS.TrustTest Exam Client...");
        launch(args);
    }
}
