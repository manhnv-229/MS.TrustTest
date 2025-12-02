package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.ReportsApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/* ---------------------------------------------------
 * Controller cho Reports
 * Quản lý 5 loại báo cáo và export
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class ReportsController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    
    // API Client
    private ReportsApiClient apiClient;
    private Stage stage;
    
    // Current report type
    private String currentReportType = null;
    
    // UI Components
    @FXML private VBox reportDialogPane;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> examFilterCombo;
    @FXML private ComboBox<String> studentFilterCombo;
    @FXML private ComboBox<String> classFilterCombo;
    @FXML private ComboBox<String> teacherFilterCombo;
    @FXML private HBox examIdFilterPane;
    @FXML private HBox studentIdFilterPane;
    @FXML private HBox classIdFilterPane;
    @FXML private HBox teacherIdFilterPane;
    @FXML private RadioButton pdfFormatRadio;
    @FXML private RadioButton excelFormatRadio;
    @FXML private RadioButton csvFormatRadio;
    @FXML private StackPane loadingPane;
    @FXML private Label loadingMessage;
    
    // Toggle Group
    private ToggleGroup formatGroup;
    
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
        this.apiClient = new ReportsApiClient();
        this.apiClient.setToken(authToken);
        
        // Setup toggle group
        formatGroup = new ToggleGroup();
        pdfFormatRadio.setToggleGroup(formatGroup);
        excelFormatRadio.setToggleGroup(formatGroup);
        csvFormatRadio.setToggleGroup(formatGroup);
        pdfFormatRadio.setSelected(true);
        
        // Hide dialog initially
        reportDialogPane.setVisible(false);
        reportDialogPane.setManaged(false);
    }
    
    /* ---------------------------------------------------
     * Handle generate exam statistics report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateExamStatistics() {
        currentReportType = "EXAM_STATISTICS";
        showReportDialog(true, false, false, false);
    }
    
    /* ---------------------------------------------------
     * Handle generate student performance report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateStudentPerformance() {
        currentReportType = "STUDENT_PERFORMANCE";
        showReportDialog(false, true, true, false);
    }
    
    /* ---------------------------------------------------
     * Handle generate teacher activity report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateTeacherActivity() {
        currentReportType = "TEACHER_ACTIVITY";
        showReportDialog(false, false, false, true);
    }
    
    /* ---------------------------------------------------
     * Handle generate monitoring summary report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateMonitoringSummary() {
        currentReportType = "MONITORING_SUMMARY";
        showReportDialog(true, false, false, false);
    }
    
    /* ---------------------------------------------------
     * Handle generate system usage report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateSystemUsage() {
        currentReportType = "SYSTEM_USAGE";
        showReportDialog(false, false, false, false);
    }
    
    /* ---------------------------------------------------
     * Show report dialog với appropriate filters
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showReportDialog(boolean showExam, boolean showStudent, 
                                 boolean showClass, boolean showTeacher) {
        reportDialogPane.setVisible(true);
        reportDialogPane.setManaged(true);
        
        examIdFilterPane.setVisible(showExam);
        examIdFilterPane.setManaged(showExam);
        studentIdFilterPane.setVisible(showStudent);
        studentIdFilterPane.setManaged(showStudent);
        classIdFilterPane.setVisible(showClass);
        classIdFilterPane.setManaged(showClass);
        teacherIdFilterPane.setVisible(showTeacher);
        teacherIdFilterPane.setManaged(showTeacher);
        
        // Reset dates
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }
    
    /* ---------------------------------------------------
     * Handle cancel report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleCancelReport() {
        reportDialogPane.setVisible(false);
        reportDialogPane.setManaged(false);
        currentReportType = null;
    }
    
    /* ---------------------------------------------------
     * Handle generate and download report
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleGenerateAndDownload() {
        if (currentReportType == null) {
            showError("Lỗi", "Vui lòng chọn loại báo cáo.");
            return;
        }
        
        // Get format
        String format = "PDF";
        if (excelFormatRadio.isSelected()) {
            format = "EXCEL";
        } else if (csvFormatRadio.isSelected()) {
            format = "CSV";
        }
        
        // Get dates
        String startDate = null;
        String endDate = null;
        if (startDatePicker.getValue() != null) {
            startDate = startDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDatePicker.getValue() != null) {
            endDate = endDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        // Get filters
        Long examId = null;
        Long studentId = null;
        Long classId = null;
        Long teacherId = null;
        
        // Generate report
        generateReport(currentReportType, examId, studentId, classId, teacherId, 
                      startDate, endDate, format);
    }
    
    /* ---------------------------------------------------
     * Generate report và download
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void generateReport(String reportType, Long examId, Long studentId, 
                               Long classId, Long teacherId, String startDate, 
                               String endDate, String format) {
        
        showLoading(true, "Đang tạo báo cáo...");
        
        new Thread(() -> {
            try {
                InputStream reportStream = null;
                
                switch (reportType) {
                    case "EXAM_STATISTICS":
                        reportStream = apiClient.generateExamStatisticsReport(
                            examId, startDate, endDate, format);
                        break;
                    case "STUDENT_PERFORMANCE":
                        reportStream = apiClient.generateStudentPerformanceReport(
                            studentId, classId, startDate, endDate, format);
                        break;
                    case "TEACHER_ACTIVITY":
                        reportStream = apiClient.generateTeacherActivityReport(
                            teacherId, startDate, endDate, format);
                        break;
                    case "MONITORING_SUMMARY":
                        reportStream = apiClient.generateMonitoringSummaryReport(
                            examId, startDate, endDate, format);
                        break;
                    case "SYSTEM_USAGE":
                        reportStream = apiClient.generateSystemUsageReport(
                            startDate, endDate, format);
                        break;
                }
                
                if (reportStream != null) {
                    // Save to file - tạo final variables cho lambda
                    String extension = format.toLowerCase();
                    final String finalExtension = extension.equals("excel") ? "xlsx" : extension;
                    final InputStream finalReportStream = reportStream;
                    final String finalFormat = format;
                    
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Lưu Báo cáo");
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                            finalFormat + " Files", "*." + finalExtension));
                    
                    Platform.runLater(() -> {
                        File file = fileChooser.showSaveDialog(stage);
                        if (file != null) {
                            try {
                                saveReportToFile(finalReportStream, file);
                                showLoading(false, "");
                                showInfo("Thành công", 
                                    "Đã tạo và tải xuống báo cáo thành công:\n" + file.getAbsolutePath());
                                handleCancelReport();
                            } catch (Exception e) {
                                logger.error("Error saving report", e);
                                showLoading(false, "");
                                showError("Lỗi", "Không thể lưu file: " + e.getMessage());
                            }
                        } else {
                            showLoading(false, "");
                        }
                    });
                }
                
            } catch (ReportsApiClient.ApiException e) {
                logger.error("Error generating report", e);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showError("Lỗi", "Không thể tạo báo cáo: " + e.getMessage());
                });
            } catch (Exception e) {
                logger.error("Error generating report", e);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showError("Lỗi", "Lỗi không xác định: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Save report stream to file
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void saveReportToFile(InputStream inputStream, File file) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    
    /* ---------------------------------------------------
     * Handle refresh
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        handleCancelReport();
    }
    
    /* ---------------------------------------------------
     * Show/hide loading overlay
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showLoading(boolean show, String message) {
        Platform.runLater(() -> {
            loadingPane.setVisible(show);
            loadingMessage.setText(message);
        });
    }
    
    /* ---------------------------------------------------
     * Helper methods
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
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

