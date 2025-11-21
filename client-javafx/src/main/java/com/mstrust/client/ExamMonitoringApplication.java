package com.mstrust.client;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.config.AppConfig;
import com.mstrust.client.monitoring.MonitoringCoordinator;
import com.mstrust.client.util.WindowDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/* ---------------------------------------------------
 * Main JavaFX Application cho Exam Monitoring Client
 * Test harness để demo monitoring functionality
 * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
 * --------------------------------------------------- */
public class ExamMonitoringApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ExamMonitoringApplication.class);
    
    private MonitoringCoordinator coordinator;
    private MonitoringApiClient apiClient;
    private AppConfig config;
    
    // UI Components
    private TextField submissionIdField;
    private TextField authTokenField;
    private Button startButton;
    private Button stopButton;
    private TextArea statusArea;
    private Label statusIndicator;
    private Timer statusUpdateTimer;

    /* ---------------------------------------------------
     * JavaFX start method
     * @param primaryStage Primary stage
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Exam Monitoring Application...");
        
        // Initialize
        config = AppConfig.getInstance();
        apiClient = new MonitoringApiClient();
        coordinator = new MonitoringCoordinator(apiClient);
        
        // Check Windows compatibility
        if (!WindowDetector.isWindows()) {
            showAlert("Warning", "This application is optimized for Windows.\n" +
                    "Some features may not work on other platforms.");
        }
        
        // Create UI
        VBox root = createUI();
        
        // Setup scene
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("MS.TrustTest - Exam Monitoring Client");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> handleClose());
        
        // Show
        primaryStage.show();
        
        logger.info("Application started successfully");
    }

    /* ---------------------------------------------------
     * Tạo UI
     * @returns VBox root layout
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private VBox createUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Exam Monitoring Client");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Config section
        GridPane configGrid = createConfigSection();
        
        // Control section
        HBox controlBox = createControlSection();
        
        // Status section
        VBox statusBox = createStatusSection();
        
        // Add all to root
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            configGrid,
            new Separator(),
            controlBox,
            statusBox
        );
        
        return root;
    }

    /* ---------------------------------------------------
     * Tạo config section
     * @returns GridPane
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private GridPane createConfigSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Submission ID
        Label subIdLabel = new Label("Submission ID:");
        submissionIdField = new TextField("1");
        submissionIdField.setPromptText("Enter submission ID");
        
        // Auth Token
        Label tokenLabel = new Label("Auth Token:");
        authTokenField = new TextField();
        authTokenField.setPromptText("JWT token from login");
        
        // API URL info
        Label apiLabel = new Label("API URL:");
        Label apiValue = new Label(config.getApiBaseUrl());
        apiValue.setStyle("-fx-text-fill: #666;");
        
        grid.add(subIdLabel, 0, 0);
        grid.add(submissionIdField, 1, 0);
        grid.add(tokenLabel, 0, 1);
        grid.add(authTokenField, 1, 1);
        grid.add(apiLabel, 0, 2);
        grid.add(apiValue, 1, 2);
        
        return grid;
    }

    /* ---------------------------------------------------
     * Tạo control section
     * @returns HBox
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private HBox createControlSection() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        // Start button
        startButton = new Button("▶ Start Monitoring");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startMonitoring());
        
        // Stop button
        stopButton = new Button("⏸ Stop Monitoring");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopMonitoring());
        
        box.getChildren().addAll(startButton, stopButton);
        
        return box;
    }

    /* ---------------------------------------------------
     * Tạo status section
     * @returns VBox
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private VBox createStatusSection() {
        VBox box = new VBox(10);
        
        // Status indicator
        HBox indicatorBox = new HBox(10);
        indicatorBox.setAlignment(Pos.CENTER_LEFT);
        
        statusIndicator = new Label("● Stopped");
        statusIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        indicatorBox.getChildren().add(statusIndicator);
        
        // Status text area
        Label statusLabel = new Label("Monitoring Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefRowCount(10);
        statusArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
        statusArea.setText("Ready to start monitoring...\n");
        
        box.getChildren().addAll(indicatorBox, statusLabel, statusArea);
        
        return box;
    }

    /* ---------------------------------------------------
     * Start monitoring
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void startMonitoring() {
        try {
            // Validate inputs
            String subIdText = submissionIdField.getText().trim();
            String token = authTokenField.getText().trim();
            
            if (subIdText.isEmpty()) {
                showAlert("Error", "Please enter Submission ID");
                return;
            }
            
            if (token.isEmpty()) {
                showAlert("Error", "Please enter Auth Token");
                return;
            }
            
            Long submissionId = Long.parseLong(subIdText);
            
            // Start coordinator
            coordinator.startMonitoring(submissionId, token);
            
            // Update UI
            startButton.setDisable(true);
            stopButton.setDisable(false);
            submissionIdField.setDisable(true);
            authTokenField.setDisable(true);
            
            updateStatusIndicator(true);
            appendStatus("✓ Monitoring started for Submission ID: " + submissionId);
            appendStatus("  - Screenshot capture: Every " + config.getScreenshotIntervalSeconds() + "s");
            appendStatus("  - Activity logging: Every " + config.getActivityBatchIntervalSeconds() + "s");
            appendStatus("  - Process checking: Every 30s");
            
            // Start status update timer
            startStatusUpdateTimer();
            
            logger.info("Monitoring started via UI");
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid Submission ID format");
        } catch (Exception e) {
            logger.error("Error starting monitoring", e);
            showAlert("Error", "Failed to start monitoring: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Stop monitoring
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void stopMonitoring() {
        coordinator.stopMonitoring();
        
        // Update UI
        startButton.setDisable(false);
        stopButton.setDisable(true);
        submissionIdField.setDisable(false);
        authTokenField.setDisable(false);
        
        updateStatusIndicator(false);
        appendStatus("⏸ Monitoring stopped");
        
        // Stop timer
        if (statusUpdateTimer != null) {
            statusUpdateTimer.cancel();
            statusUpdateTimer = null;
        }
        
        logger.info("Monitoring stopped via UI");
    }

    /* ---------------------------------------------------
     * Start status update timer
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timer(true);
        statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (coordinator.isRunning()) {
                        String stats = coordinator.getStats();
                        // Update status area periodically
                        // (In real app, display stats more elegantly)
                    }
                });
            }
        }, 5000, 10000); // Update every 10 seconds
    }

    /* ---------------------------------------------------
     * Update status indicator
     * @param running true nếu đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void updateStatusIndicator(boolean running) {
        Platform.runLater(() -> {
            if (running) {
                statusIndicator.setText("● Running");
                statusIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");
            } else {
                statusIndicator.setText("● Stopped");
                statusIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            }
        });
    }

    /* ---------------------------------------------------
     * Append status message
     * @param message Message to append
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void appendStatus(String message) {
        Platform.runLater(() -> {
            statusArea.appendText(message + "\n");
        });
    }

    /* ---------------------------------------------------
     * Show alert dialog
     * @param title Title
     * @param content Content
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /* ---------------------------------------------------
     * Handle application close
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    private void handleClose() {
        logger.info("Application closing...");
        
        if (statusUpdateTimer != null) {
            statusUpdateTimer.cancel();
        }
        
        coordinator.shutdown();
        
        logger.info("Application closed");
    }

    /* ---------------------------------------------------
     * Main method
     * @param args Command line arguments
     * @author: K24DTCN210-NVMANH (21/11/2025 11:36)
     * --------------------------------------------------- */
    public static void main(String[] args) {
        launch(args);
    }
}
