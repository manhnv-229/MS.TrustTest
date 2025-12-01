package com.mstrust.client.monitoring.test;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.monitoring.MonitoringCoordinator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/* ---------------------------------------------------
 * Test Application để test các monitors
 * Cho phép start/stop monitoring và xem stats
 * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
 * --------------------------------------------------- */
public class MonitorTestApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MonitorTestApplication.class);
    
    private MonitoringCoordinator coordinator;
    private MonitoringApiClient apiClient;
    private boolean isMonitoring = false;
    private Long testSubmissionId = 1L; // Test submission ID
    private String testAuthToken = "test-token"; // Test token
    
    private Label statusLabel;
    private TextArea statsArea;
    private Button startButton;
    private Button stopButton;
    private TextField submissionIdField;
    private TextField authTokenField;
    private Timer statsTimer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Monitor Test Application - Phase 11");
        
        // Initialize API client và coordinator
        apiClient = new MonitoringApiClient();
        coordinator = new MonitoringCoordinator(apiClient);
        
        // Create UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        // Title
        Label titleLabel = new Label("Anti-Cheat Monitors Test");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Configuration section
        VBox configBox = new VBox(5);
        configBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
        Label configLabel = new Label("Configuration:");
        configLabel.setStyle("-fx-font-weight: bold;");
        
        HBox submissionIdBox = new HBox(5);
        submissionIdBox.getChildren().addAll(
            new Label("Submission ID:"),
            submissionIdField = new TextField("1")
        );
        
        HBox tokenBox = new HBox(5);
        tokenBox.getChildren().addAll(
            new Label("Auth Token:"),
            authTokenField = new TextField("test-token")
        );
        
        configBox.getChildren().addAll(configLabel, submissionIdBox, tokenBox);
        
        // Control buttons
        HBox buttonBox = new HBox(10);
        startButton = new Button("Start Monitoring");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startMonitoring());
        
        stopButton = new Button("Stop Monitoring");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopMonitoring());
        
        Button refreshButton = new Button("Refresh Stats");
        refreshButton.setOnAction(e -> refreshStats());
        
        buttonBox.getChildren().addAll(startButton, stopButton, refreshButton);
        
        // Status label
        statusLabel = new Label("Status: Not Running");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Stats area
        Label statsLabel = new Label("Monitoring Stats:");
        statsLabel.setStyle("-fx-font-weight: bold;");
        statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefRowCount(20);
        statsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        
        // Instructions
        VBox instructionsBox = new VBox(5);
        instructionsBox.setStyle("-fx-border-color: #2196F3; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #E3F2FD;");
        Label instructionsLabel = new Label("Test Instructions:");
        instructionsLabel.setStyle("-fx-font-weight: bold;");
        TextArea instructions = new TextArea(
            "1. Enter Submission ID và Auth Token\n" +
            "2. Click 'Start Monitoring' để bắt đầu\n" +
            "3. Thử các hành động sau:\n" +
            "   - Alt+Tab để switch windows (WindowFocusMonitor)\n" +
            "   - Copy/Paste text (ClipboardMonitor)\n" +
            "   - Gõ phím (KeystrokeAnalyzer)\n" +
            "   - Mở blacklisted app (ProcessMonitor)\n" +
            "   - Chờ screenshot tự động (ScreenCaptureMonitor)\n" +
            "4. Click 'Refresh Stats' để xem thống kê\n" +
            "5. Click 'Stop Monitoring' để dừng"
        );
        instructions.setEditable(false);
        instructions.setPrefRowCount(8);
        instructions.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        instructionsBox.getChildren().addAll(instructionsLabel, instructions);
        
        // Add all to root
        root.getChildren().addAll(
            titleLabel,
            configBox,
            buttonBox,
            statusLabel,
            statsLabel,
            statsArea,
            instructionsBox
        );
        
        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            stopMonitoring();
            Platform.exit();
        });
        primaryStage.show();
        
        // Initial stats
        refreshStats();
    }

    /* ---------------------------------------------------
     * Start monitoring
     * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
     * --------------------------------------------------- */
    private void startMonitoring() {
        if (isMonitoring) {
            return;
        }
        
        try {
            // Get values from fields
            testSubmissionId = Long.parseLong(submissionIdField.getText());
            testAuthToken = authTokenField.getText();
            
            // Start coordinator
            coordinator.startMonitoring(testSubmissionId, testAuthToken);
            isMonitoring = true;
            
            // Update UI
            startButton.setDisable(true);
            stopButton.setDisable(false);
            statusLabel.setText("Status: ✅ Monitoring Active");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            
            // Start stats refresh timer (every 2 seconds)
            statsTimer = new Timer(true);
            statsTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> refreshStats());
                }
            }, 0, 2000);
            
            logger.info("Monitoring started for submission: {}", testSubmissionId);
            
        } catch (Exception e) {
            logger.error("Error starting monitoring", e);
            showError("Error starting monitoring: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Stop monitoring
     * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
     * --------------------------------------------------- */
    private void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        try {
            // Stop coordinator
            coordinator.stopMonitoring();
            isMonitoring = false;
            
            // Stop stats timer
            if (statsTimer != null) {
                statsTimer.cancel();
                statsTimer = null;
            }
            
            // Update UI
            startButton.setDisable(false);
            stopButton.setDisable(true);
            statusLabel.setText("Status: ⏹️ Monitoring Stopped");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f44336;");
            
            // Final stats refresh
            refreshStats();
            
            logger.info("Monitoring stopped");
            
        } catch (Exception e) {
            logger.error("Error stopping monitoring", e);
            showError("Error stopping monitoring: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Refresh stats display
     * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
     * --------------------------------------------------- */
    private void refreshStats() {
        if (!isMonitoring) {
            statsArea.setText("Monitoring not running.\nClick 'Start Monitoring' to begin.");
            return;
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Monitoring Stats ===\n");
            sb.append("Time: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
            sb.append("\n");
            
            // Get stats from coordinator
            String coordinatorStats = coordinator.getStats();
            sb.append(coordinatorStats);
            sb.append("\n\n");
            
            // Individual monitor stats
            sb.append("--- Individual Monitor Details ---\n");
            sb.append("ScreenCaptureMonitor: ").append(
                coordinator.getScreenCaptureMonitor().isRunning() ? "✅ Running" : "❌ Stopped"
            ).append("\n");
            sb.append("  - Captures: ").append(
                coordinator.getScreenCaptureMonitor().getCaptureCount()
            ).append("\n");
            sb.append("  - Last capture: ").append(
                coordinator.getScreenCaptureMonitor().getLastCaptureTime() != null ?
                coordinator.getScreenCaptureMonitor().getLastCaptureTime().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A"
            ).append("\n\n");
            
            sb.append("WindowFocusMonitor: ").append(
                coordinator.getWindowFocusMonitor().isRunning() ? "✅ Running" : "❌ Stopped"
            ).append("\n");
            sb.append("  - Window switches: ").append(
                coordinator.getWindowFocusMonitor().getSwitchCount()
            ).append("\n\n");
            
            sb.append("ProcessMonitor: ").append(
                coordinator.getProcessMonitor().isRunning() ? "✅ Running" : "❌ Stopped"
            ).append("\n");
            sb.append("  - Total processes: ").append(
                coordinator.getProcessMonitor().getTotalProcessesDetected()
            ).append("\n");
            sb.append("  - Blacklisted: ").append(
                coordinator.getProcessMonitor().getDetectedBlacklistedProcesses().size()
            ).append("\n");
            if (!coordinator.getProcessMonitor().getDetectedBlacklistedProcesses().isEmpty()) {
                sb.append("  - Detected: ").append(
                    String.join(", ", coordinator.getProcessMonitor().getDetectedBlacklistedProcesses())
                ).append("\n");
            }
            sb.append("\n");
            
            sb.append("ClipboardMonitor: ").append(
                coordinator.getClipboardMonitor().isRunning() ? "✅ Running" : "❌ Stopped"
            ).append("\n");
            sb.append("  - Operations: ").append(
                coordinator.getClipboardMonitor().getOperationCount()
            ).append("\n\n");
            
            sb.append("KeystrokeAnalyzer: ").append(
                coordinator.getKeystrokeAnalyzer().isRunning() ? "✅ Running" : "❌ Stopped"
            ).append("\n");
            sb.append("  - Total keystrokes: ").append(
                coordinator.getKeystrokeAnalyzer().getTotalKeystrokes()
            ).append("\n");
            sb.append("  - Average WPM: ").append(
                String.format("%.2f", coordinator.getKeystrokeAnalyzer().getAverageWPM())
            ).append("\n");
            
            statsArea.setText(sb.toString());
            
        } catch (Exception e) {
            logger.error("Error refreshing stats", e);
            statsArea.setText("Error refreshing stats: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param message Error message
     * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
     * --------------------------------------------------- */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Main method
     * @param args Command line arguments
     * @author: K24DTCN210-NVMANH (01/12/2025 11:30)
     * --------------------------------------------------- */
    public static void main(String[] args) {
        launch(args);
    }
}

