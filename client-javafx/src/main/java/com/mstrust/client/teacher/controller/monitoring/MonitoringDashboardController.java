package com.mstrust.client.teacher.controller.monitoring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
// import java.time.Duration; // Conflict with javafx.util.Duration
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/* ---------------------------------------------------
 * Controller cho Monitoring Dashboard
 * Hiển thị real-time monitoring cho teachers
 * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
 * --------------------------------------------------- */
public class MonitoringDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringDashboardController.class);
    
    // FXML Components
    @FXML private ComboBox<ExamOption> examComboBox;
    @FXML private Button refreshButton;
    @FXML private Label statusLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label alertsCountLabel;
    @FXML private TableView<StudentTableItem> studentsTable;
    @FXML private TableColumn<StudentTableItem, String> studentNameColumn;
    @FXML private TableColumn<StudentTableItem, String> studentCodeColumn;
    @FXML private TableColumn<StudentTableItem, String> statusColumn;
    @FXML private TableColumn<StudentTableItem, Integer> alertsCountColumn;
    @FXML private VBox emptyStatePane;
    @FXML private ListView<AlertItem> alertsList;
    @FXML private VBox emptyAlertsPane;
    @FXML private Button clearAlertsButton;
    @FXML private Button viewAllScreenshotsButton;
    @FXML private Button viewActivityLogsButton;
    @FXML private Button exportReportButton;
    
    // Services
    private String baseUrl;
    private String authToken;
    private HttpClient httpClient;
    private Gson gson;
    private ScheduledExecutorService refreshScheduler;
    private com.mstrust.client.teacher.api.ExamManagementApiClient examApiClient;
    private com.mstrust.client.teacher.api.GradingApiClient gradingApiClient;
    
    // State
    private Long selectedExamId;
    private ObservableList<AlertItem> alerts = FXCollections.observableArrayList();
    private ObservableList<StudentTableItem> studentTableItems = FXCollections.observableArrayList();
    private Map<Long, StudentCard> studentCards = new HashMap<>();
    
    /* ---------------------------------------------------
     * Initialize controller
     * @param baseUrl API base URL
     * @param authToken JWT token
     * @param stage Primary stage
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    public void initialize(String baseUrl, String authToken, Stage stage) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        
        // Initialize ExamManagementApiClient để load exams
        this.examApiClient = new com.mstrust.client.teacher.api.ExamManagementApiClient();
        this.examApiClient.setToken(authToken);
        
        // Initialize GradingApiClient để load submissions
        this.gradingApiClient = new com.mstrust.client.teacher.api.GradingApiClient();
        this.gradingApiClient.setToken(authToken);
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();
        
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
        
        setupUI();
        loadExams();
        
        // Auto-refresh mỗi 10 giây
        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Monitoring-Refresh-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Refresh mỗi 5 giây để realtime hơn
        refreshScheduler.scheduleAtFixedRate(this::refreshData, 5, 5, TimeUnit.SECONDS);
    }
    
    /* ---------------------------------------------------
     * Setup UI components
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void setupUI() {
        // Setup exam combo box
        examComboBox.setCellFactory(param -> new ListCell<ExamOption>() {
            @Override
            protected void updateItem(ExamOption item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        
        examComboBox.setButtonCell(new ListCell<ExamOption>() {
            @Override
            protected void updateItem(ExamOption item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        
        examComboBox.setOnAction(e -> onExamSelected());
        
        // Setup students table
        if (studentsTable != null) {
            studentsTable.setItems(studentTableItems);
            studentsTable.setRowFactory(tv -> {
                TableRow<StudentTableItem> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        StudentTableItem item = row.getItem();
                        viewStudentDetail(item.getSubmissionId());
                    }
                });
                return row;
            });
            
            // Setup selection listener để enable/disable buttons
            studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateButtonStates();
            });
            
            // Setup column cell factories để highlight
            if (statusColumn != null) {
                statusColumn.setCellFactory(column -> new TableCell<StudentTableItem, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || status == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(status);
                            if (status.contains("Đang làm")) {
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            } else if (status.contains("Đã nộp")) {
                                setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            }
                        }
                    }
                });
            }
            
            if (alertsCountColumn != null) {
                alertsCountColumn.setCellFactory(column -> new TableCell<StudentTableItem, Integer>() {
                    @Override
                    protected void updateItem(Integer count, boolean empty) {
                        super.updateItem(count, empty);
                        if (empty || count == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(count.toString());
                            if (count > 0) {
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #27ae60;");
                            }
                        }
                    }
                });
            }
        }
        
        // Setup alerts list
        alertsList.setItems(alerts);
        alertsList.setCellFactory(param -> new AlertListCell());
        
        // Update empty states
        updateEmptyStates();
        
        // Initial button states
        updateButtonStates();
    }
    
    /* ---------------------------------------------------
     * Update button states based on selection
     * @author: K24DTCN210-NVMANH (02/12/2025 00:20)
     * --------------------------------------------------- */
    private void updateButtonStates() {
        StudentTableItem selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selectedStudent != null;
        boolean hasExam = selectedExamId != null;
        
        // viewAllScreenshotsButton chỉ available khi chọn sinh viên
        if (viewAllScreenshotsButton != null) {
            viewAllScreenshotsButton.setDisable(!hasSelection);
        }
        
        // viewActivityLogsButton available khi có exam
        if (viewActivityLogsButton != null) {
            viewActivityLogsButton.setDisable(!hasExam);
        }
    }
    
    /* ---------------------------------------------------
     * Load exams từ API
     * Điều kiện:
     * - ExamManagementApiClient đã được initialize với authToken
     * - Backend API /api/exams phải accessible
     * - User phải có quyền TEACHER/ADMIN
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 12:30) - Implement load từ ExamManagementApiClient
     * --------------------------------------------------- */
    private void loadExams() {
        new Thread(() -> {
            try {
                // Load exams từ ExamManagementApiClient
                List<com.mstrust.client.teacher.dto.ExamDTO> exams = examApiClient.getAllExams();
                
                Platform.runLater(() -> {
                    examComboBox.getItems().clear();
                    
                    // Convert ExamDTO sang ExamOption
                    for (com.mstrust.client.teacher.dto.ExamDTO exam : exams) {
                        ExamOption option = new ExamOption(exam.getId(), exam.getTitle());
                        examComboBox.getItems().add(option);
                    }
                    
                    logger.info("Exams loaded: {} exams", exams.size());
                    
                    if (exams.isEmpty()) {
                        statusLabel.setText("Trạng thái: Không có đề thi nào");
                    } else {
                        statusLabel.setText("Trạng thái: Đã tải " + exams.size() + " đề thi");
                    }
                });
            } catch (com.mstrust.client.teacher.api.ExamManagementApiClient.ApiException e) {
                logger.error("API error loading exams: {} - {}", e.getStatusCode(), e.getResponseBody());
                Platform.runLater(() -> {
                    showError("Lỗi API", 
                        "Không thể tải danh sách đề thi.\n" +
                        "Status: " + e.getStatusCode() + "\n" +
                        "Lỗi: " + e.getResponseBody());
                });
            } catch (Exception e) {
                logger.error("Error loading exams", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải danh sách đề thi: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handle exam selection
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void onExamSelected() {
        ExamOption selected = examComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedExamId = selected.getExamId();
            statusLabel.setText("Trạng thái: Đang giám sát đề thi: " + selected.getTitle());
            refreshData();
            updateButtonStates();
        }
    }
    
    /* ---------------------------------------------------
     * Refresh monitoring data
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void refreshData() {
        if (selectedExamId == null) {
            return;
        }
        
        new Thread(() -> {
            try {
                // Load alerts
                loadAlerts();
                
                // Load students và screenshots
                loadStudents();
                
            } catch (Exception e) {
                logger.error("Error refreshing data", e);
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Load alerts từ API
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void loadAlerts() {
        try {
            String url = baseUrl + "/api/alerts/exam/" + selectedExamId + "/unreviewed";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<AlertDTO> alertDTOs = gson.fromJson(response.body(), 
                    new TypeToken<List<AlertDTO>>(){}.getType());
                
                Platform.runLater(() -> {
                    alerts.clear();
                    for (AlertDTO dto : alertDTOs) {
                        alerts.add(new AlertItem(dto));
                    }
                    alertsCountLabel.setText("Cảnh báo: " + alerts.size());
                    updateEmptyStates();
                    
                    // Highlight student cards có alerts
                    highlightStudentsWithAlerts();
                });
            }
        } catch (Exception e) {
            logger.error("Error loading alerts", e);
        }
    }
    
    /* ---------------------------------------------------
     * Load students và screenshots từ API
     * Điều kiện:
     * - selectedExamId phải được set (từ examComboBox selection)
     * - GradingApiClient đã được initialize với authToken
     * - Backend API /api/grading/submissions?examId={examId} phải accessible
     * - Phải có submissions với status IN_PROGRESS hoặc SUBMITTED
     * - Backend API /api/monitoring/screenshots/{submissionId} để load screenshots
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 12:45) - Implement load từ GradingApiClient
     * --------------------------------------------------- */
    private void loadStudents() {
        if (selectedExamId == null) {
            logger.warn("selectedExamId is null, cannot load students");
            return;
        }
        
        logger.info("Loading students for examId: {}", selectedExamId);
        
        new Thread(() -> {
            try {
                // Load submissions của exam này
                // Gọi 2 API: một cho IN_PROGRESS và một cho SUBMITTED
                // Vì GradingService filter out IN_PROGRESS khi status=null
                logger.debug("Calling gradingApiClient.getSubmissionsForGrading for exam {}", selectedExamId);
                
                List<com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO> allSubmissions = 
                    new ArrayList<>();
                
                // Load IN_PROGRESS submissions
                try {
                    List<com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO> inProgress = 
                        gradingApiClient.getSubmissionsForGrading("IN_PROGRESS", selectedExamId);
                    logger.info("Found {} IN_PROGRESS submissions", inProgress.size());
                    allSubmissions.addAll(inProgress);
                } catch (com.mstrust.client.teacher.api.GradingApiClient.ApiException e) {
                    logger.warn("Error loading IN_PROGRESS submissions: {} - {}", 
                        e.getStatusCode(), e.getResponseBody());
                } catch (Exception e) {
                    logger.warn("Error loading IN_PROGRESS submissions: {}", e.getMessage());
                }
                
                // Load SUBMITTED submissions
                try {
                    List<com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO> submitted = 
                        gradingApiClient.getSubmissionsForGrading("SUBMITTED", selectedExamId);
                    logger.info("Found {} SUBMITTED submissions", submitted.size());
                    allSubmissions.addAll(submitted);
                } catch (com.mstrust.client.teacher.api.GradingApiClient.ApiException e) {
                    logger.warn("Error loading SUBMITTED submissions: {} - {}", 
                        e.getStatusCode(), e.getResponseBody());
                } catch (Exception e) {
                    logger.warn("Error loading SUBMITTED submissions: {}", e.getMessage());
                }
                
                // Remove duplicates (nếu có)
                Map<Long, com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO> uniqueSubmissions = 
                    new HashMap<>();
                for (com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO sub : allSubmissions) {
                    uniqueSubmissions.put(sub.getId(), sub);
                }
                List<com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO> submissions = 
                    new ArrayList<>(uniqueSubmissions.values());
                
                logger.info("API returned {} total submissions ({} unique) for exam {}", 
                    allSubmissions.size(), submissions.size(), selectedExamId);
                
                // Log tất cả submissions để debug
                for (com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO sub : submissions) {
                    logger.debug("Submission: id={}, studentName={}, status={}, examId={}", 
                        sub.getId(), sub.getStudentName(), sub.getStatus(), sub.getExamId());
                }
                
                Platform.runLater(() -> {
                    studentTableItems.clear();
                    studentCards.clear();
                    
                    int studentsCount = 0;
                    int filteredCount = 0;
                    
                    // Count alerts per student
                    Map<Long, Integer> alertsCountMap = new HashMap<>();
                    for (AlertItem alert : alerts) {
                        Long submissionId = alert.getDto().getSubmissionId();
                        if (submissionId != null) {
                            alertsCountMap.put(submissionId, alertsCountMap.getOrDefault(submissionId, 0) + 1);
                        }
                    }
                    
                    for (com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO submission : submissions) {
                        String status = submission.getStatus();
                        logger.debug("Processing submission {} with status: {}", submission.getId(), status);
                        
                        // Chỉ hiển thị students đang làm bài (IN_PROGRESS) hoặc đã nộp (SUBMITTED)
                        if (status != null && (
                            "IN_PROGRESS".equalsIgnoreCase(status) || 
                            "SUBMITTED".equalsIgnoreCase(status))) {
                            
                            logger.debug("Adding student: {} (status: {})", submission.getStudentName(), status);
                            
                            // Create table item
                            String statusText = "IN_PROGRESS".equalsIgnoreCase(status) ? "🟢 Đang làm" : 
                                               "SUBMITTED".equalsIgnoreCase(status) ? "📝 Đã nộp" : status;
                            
                            int alertsCount = alertsCountMap.getOrDefault(submission.getId(), 0);
                            
                            StudentTableItem item = new StudentTableItem(
                                submission.getId(),
                                submission.getStudentName(),
                                submission.getStudentCode(),
                                statusText,
                                alertsCount
                            );
                            
                            studentTableItems.add(item);
                            
                            // Store card info
                            studentCards.put(submission.getId(), new StudentCard(
                                submission.getId(), 
                                submission.getStudentName()
                            ));
                            
                            studentsCount++;
                        } else {
                            filteredCount++;
                            logger.debug("Filtered out submission {} with status: {}", submission.getId(), status);
                        }
                    }
                    
                    studentsCountLabel.setText("Số học sinh: " + studentsCount);
                    updateEmptyStates();
                    
                    logger.info("Loaded {} students (filtered out {}) for exam {}", 
                        studentsCount, filteredCount, selectedExamId);
                });
                
            } catch (Exception e) {
                logger.error("Error loading students", e);
                Platform.runLater(() -> {
                    studentTableItems.clear();
                    studentsCountLabel.setText("Số học sinh: 0 (Lỗi)");
                    updateEmptyStates();
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Create student card UI (deprecated - không dùng với TableView)
     * @deprecated Sử dụng TableView thay vì card layout
     * --------------------------------------------------- */
    @Deprecated
    private VBox createStudentCard(com.mstrust.client.teacher.dto.grading.GradingSubmissionListDTO submission) {
        // Không dùng nữa - TableView thay thế
        return new VBox();
    }
    
    /* ---------------------------------------------------
     * Load screenshot cho một student (không dùng nữa với TableView)
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (01/12/2025 12:45)
     * @deprecated TableView không hiển thị screenshots
     * --------------------------------------------------- */
    @Deprecated
    private void loadStudentScreenshot(Long submissionId) {
        // TableView không hiển thị screenshots nữa
        // Screenshots sẽ được xem trong student detail dialog
        logger.debug("Screenshot loading skipped for TableView mode");
    }
    
    /* ---------------------------------------------------
     * View student detail - hiển thị activity logs và alerts
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (01/12/2025 12:45)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:15) - Implement xem activity logs
     * --------------------------------------------------- */
    private void viewStudentDetail(Long submissionId) {
        StudentCard studentCard = studentCards.get(submissionId);
        if (studentCard == null) {
            showError("Lỗi", "Không tìm thấy thông tin học sinh");
            return;
        }
        
        // Load activity logs
        new Thread(() -> {
            try {
                String url = baseUrl + "/api/monitoring/activities/" + submissionId;
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<ActivityLogDTO> activities = gson.fromJson(response.body(), 
                        new TypeToken<List<ActivityLogDTO>>(){}.getType());
                    
                    // Load alerts của student này
                    List<AlertItem> studentAlerts = alerts.stream()
                        .filter(alert -> alert.getDto().getSubmissionId().equals(submissionId))
                        .collect(java.util.stream.Collectors.toList());
                    
                    Platform.runLater(() -> {
                        showStudentDetailDialog(studentCard, activities, studentAlerts);
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Lỗi", "Không thể tải activity logs. Status: " + response.statusCode());
                    });
                }
            } catch (Exception e) {
                logger.error("Error loading student detail", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải thông tin học sinh: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Show student detail dialog với activity logs và alerts
     * @param studentCard Student card info
     * @param activities Activity logs
     * @param alerts Alerts của student
     * @author: K24DTCN210-NVMANH (01/12/2025 23:15)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:45) - Loại bỏ scroll ngoài, chỉ scroll cho từng section
     * --------------------------------------------------- */
    private void showStudentDetailDialog(StudentCard studentCard, List<ActivityLogDTO> activities, 
                                       List<AlertItem> alerts) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết Học sinh: " + studentCard.getStudentName());
        dialog.setHeaderText("Activity Logs và Alerts");
        
        // Main content container - KHÔNG có scroll
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(800);
        content.setPrefHeight(600);
        
        // Alerts section
        Label alertsTitle = new Label("Cảnh báo (" + alerts.size() + ")");
        alertsTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        alertsTitle.setStyle("-fx-text-fill: #e74c3c;");
        
        // Alerts ListView với scroll riêng
        ListView<AlertItem> alertsList = new ListView<>();
        alertsList.setItems(FXCollections.observableArrayList(alerts));
        alertsList.setCellFactory(param -> new AlertListCell());
        alertsList.setPrefHeight(180); // Tăng chiều cao một chút
        alertsList.setMaxHeight(180);
        alertsList.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        // Activities section
        Label activitiesTitle = new Label("Activity Logs (" + activities.size() + ")");
        activitiesTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        activitiesTitle.setStyle("-fx-text-fill: #3498db;");
        
        // Activities ListView với scroll riêng
        ListView<ActivityLogDTO> activitiesList = new ListView<>();
        activitiesList.setItems(FXCollections.observableArrayList(activities));
        activitiesList.setCellFactory(param -> new ActivityLogListCell());
        activitiesList.setPrefHeight(320); // Phần còn lại của không gian
        activitiesList.setMaxHeight(320);
        activitiesList.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        content.getChildren().addAll(alertsTitle, alertsList, activitiesTitle, activitiesList);
        
        // Set content trực tiếp KHÔNG qua ScrollPane
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(840, 680); // Tăng kích thước dialog một chút
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Style cho dialog
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");
        
        dialog.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Show screenshots dialog for selected student
     * @param student Selected student
     * @author: K24DTCN210-NVMANH (02/12/2025 00:15)
     * --------------------------------------------------- */
    private void showScreenshotsDialog(StudentTableItem student) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Screenshots - " + student.getStudentName() + " (" + student.getStudentCode() + ")");
        dialog.setHeaderText("Danh sách Screenshots");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(800);
        content.setPrefHeight(600);
        
        // Refresh button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Screenshots của sinh viên");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("Làm mới");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        headerBox.getChildren().addAll(titleLabel, spacer, refreshBtn);
        
        // Screenshots container (FlowPane for grid layout)
        ScrollPane scrollPane = new ScrollPane();
        FlowPane screenshotsContainer = new FlowPane(15, 15); // hgap, vgap
        screenshotsContainer.setPadding(new Insets(15));
        screenshotsContainer.setAlignment(Pos.TOP_LEFT);
        scrollPane.setContent(screenshotsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        // Load screenshots function
        Runnable loadScreenshots = () -> {
            new Thread(() -> {
                try {
                    String url = baseUrl + "/api/monitoring/screenshots/" + student.getSubmissionId();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();
                    
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        List<ScreenshotDTO> screenshots = gson.fromJson(response.body(), 
                            new TypeToken<List<ScreenshotDTO>>(){}.getType());
                        
                        Platform.runLater(() -> {
                            screenshotsContainer.getChildren().clear();
                            
                            if (screenshots.isEmpty()) {
                                Label emptyLabel = new Label("Chưa có screenshots");
                                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
                                screenshotsContainer.getChildren().add(emptyLabel);
                            } else {
                                for (ScreenshotDTO screenshot : screenshots) {
                                    VBox screenshotBox = createScreenshotBox(screenshot);
                                    screenshotsContainer.getChildren().add(screenshotBox);
                                }
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            screenshotsContainer.getChildren().clear();
                            Label errorLabel = new Label("Lỗi tải screenshots: " + response.statusCode());
                            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f44336;");
                            screenshotsContainer.getChildren().add(errorLabel);
                        });
                    }
                } catch (Exception e) {
                    logger.error("Error loading screenshots", e);
                    Platform.runLater(() -> {
                        screenshotsContainer.getChildren().clear();
                        Label errorLabel = new Label("Lỗi: " + e.getMessage());
                        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f44336;");
                        screenshotsContainer.getChildren().add(errorLabel);
                    });
                }
            }).start();
        };
        
        // Refresh button action
        refreshBtn.setOnAction(e -> loadScreenshots.run());
        
        // Initial load
        loadScreenshots.run();
        
        content.getChildren().addAll(headerBox, scrollPane);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(840, 680);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Show image viewer dialog
     * @param screenshot Screenshot DTO
     * @author: K24DTCN210-NVMANH (02/12/2025 00:25)
     * --------------------------------------------------- */
    private void showImageViewer(ScreenshotDTO screenshot) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Screenshot Viewer - " + screenshot.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        dialog.setResizable(true);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(900);
        content.setPrefHeight(700);
        content.setAlignment(Pos.CENTER);
        
        // Chỉ hiển thị thời gian
        Label timeLabel = new Label("📅 " + screenshot.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 5 0 10 0;");
        timeLabel.setAlignment(Pos.CENTER);
        timeLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Image container
        ScrollPane imageScrollPane = new ScrollPane();
        imageScrollPane.setPrefSize(880, 650);
        imageScrollPane.setStyle("-fx-background-color: #2c2c2c;");
        imageScrollPane.setPannable(true);
        
        // Load image from online URL
        String imageUrl = "https://filett.manhhao.com" + screenshot.getFilePath();
        
        try {
            ImageView imageView = new ImageView();
            Image image = new Image(imageUrl, true); // Load in background
            
            image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() >= 1.0) {
                    Platform.runLater(() -> {
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(800);
                        
                        // Add zoom functionality với scroll wheel
                        imageView.setOnScroll(scrollEvent -> {
                            double zoomFactor = 1.1;
                            double deltaY = scrollEvent.getDeltaY();
                            
                            if (deltaY < 0) {
                                zoomFactor = 1 / zoomFactor;
                            }
                            
                            imageView.setScaleX(imageView.getScaleX() * zoomFactor);
                            imageView.setScaleY(imageView.getScaleY() * zoomFactor);
                            scrollEvent.consume();
                        });
                        
                        imageScrollPane.setContent(imageView);
                    });
                }
            });
            
            // Error handling
            image.errorProperty().addListener((obs, oldError, newError) -> {
                if (newError) {
                    Platform.runLater(() -> {
                        Label errorLabel = new Label("❌ Không thể tải ảnh");
                        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f44336; -fx-text-alignment: center;");
                        imageScrollPane.setContent(errorLabel);
                    });
                }
            });
            
            // Loading indicator
            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(50));
            
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setStyle("-fx-accent: #2196F3;");
            
            Label loadingLabel = new Label("🔄 Đang tải ảnh...");
            loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff;");
            
            loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
            imageScrollPane.setContent(loadingBox);
            
        } catch (Exception e) {
            Label errorLabel = new Label("❌ Lỗi tải ảnh");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f44336;");
            imageScrollPane.setContent(errorLabel);
        }
        
        content.getChildren().addAll(timeLabel, imageScrollPane);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(920, 750);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Create screenshot box UI với thumbnail
     * @param screenshot Screenshot DTO
     * @returns VBox container
     * @author: K24DTCN210-NVMANH (02/12/2025 00:15)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 03:10) - Thêm thumbnail view
     * --------------------------------------------------- */
    private VBox createScreenshotBox(ScreenshotDTO screenshot) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: #f9f9f9; -fx-cursor: hand;");
        box.setMaxWidth(200);
        
        // Thumbnail ImageView
        ImageView thumbnailView = new ImageView();
        thumbnailView.setFitWidth(180);
        thumbnailView.setFitHeight(120);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setSmooth(true);
        thumbnailView.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4;");
        
        // Load thumbnail image
        String imageUrl = "https://filett.manhhao.com" + screenshot.getFilePath();
        try {
            Image image = new Image(imageUrl, 180, 120, true, true, true);
            thumbnailView.setImage(image);
            
            // Add loading indicator
            if (image.isBackgroundLoading()) {
                Label loadingLabel = new Label("⏳ Đang tải...");
                loadingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
                box.getChildren().add(loadingLabel);
                
                image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0) {
                        Platform.runLater(() -> box.getChildren().remove(loadingLabel));
                    }
                });
            }
        } catch (Exception e) {
            // Fallback nếu không load được ảnh
            Label errorLabel = new Label("❌ Lỗi tải ảnh");
            errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f44336;");
            thumbnailView.setImage(null);
            box.getChildren().add(errorLabel);
        }
        
        // Timestamp
        Label timeLabel = new Label(screenshot.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #333;");
        timeLabel.setAlignment(Pos.CENTER);
        timeLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Resolution info (nếu có)
        if (screenshot.getScreenResolution() != null && !screenshot.getScreenResolution().isEmpty()) {
            Label resLabel = new Label(screenshot.getScreenResolution());
            resLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #666;");
            resLabel.setAlignment(Pos.CENTER);
            resLabel.setMaxWidth(Double.MAX_VALUE);
            box.getChildren().addAll(thumbnailView, timeLabel, resLabel);
        } else {
            box.getChildren().addAll(thumbnailView, timeLabel);
        }
        
        // Click để xem full size
        box.setOnMouseClicked(e -> {
            showImageViewer(screenshot);
        });
        
        // Hover effect
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-border-color: #2196F3; -fx-border-radius: 8; -fx-background-color: #f0f8ff; -fx-cursor: hand;");
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: #f9f9f9; -fx-cursor: hand;");
        });
        
        return box;
    }
    
    /* ---------------------------------------------------
     * Show all activity logs dialog
     * @author: K24DTCN210-NVMANH (02/12/2025 00:15)
     * --------------------------------------------------- */
    private void showAllActivityLogsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Activity Logs - Tất cả sinh viên");
        dialog.setHeaderText("Danh sách hoạt động của tất cả sinh viên");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(900);
        content.setPrefHeight(600);
        
        // Header with refresh button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Activity Logs");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("Làm mới");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        headerBox.getChildren().addAll(titleLabel, spacer, refreshBtn);
        
        // Activity logs list
        ListView<ActivityLogItem> logsList = new ListView<>();
        logsList.setCellFactory(param -> new ActivityLogItemCell());
        logsList.setPrefHeight(500);
        
        // Load logs function
        Runnable loadLogs = () -> {
            new Thread(() -> {
                try {
                    List<ActivityLogItem> allLogs = new ArrayList<>();
                    
                    // Load logs for each student
                    for (StudentTableItem student : studentTableItems) {
                        String url = baseUrl + "/api/monitoring/activities/" + student.getSubmissionId();
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", "Bearer " + authToken)
                            .GET()
                            .build();
                        
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            List<ActivityLogDTO> activities = gson.fromJson(response.body(), 
                                new TypeToken<List<ActivityLogDTO>>(){}.getType());
                            
                            for (ActivityLogDTO activity : activities) {
                                ActivityLogItem item = new ActivityLogItem(
                                    student.getStudentName(),
                                    student.getStudentCode(),
                                    activity.getActivityType(),
                                    activity.getDetails(),
                                    activity.getTimestamp()
                                );
                                allLogs.add(item);
                            }
                        }
                    }
                    
                    // Sort by timestamp (newest first)
                    allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                    
                    Platform.runLater(() -> {
                        logsList.getItems().clear();
                        logsList.getItems().addAll(allLogs);
                    });
                    
                } catch (Exception e) {
                    logger.error("Error loading activity logs", e);
                    Platform.runLater(() -> {
                        showError("Lỗi", "Không thể tải activity logs: " + e.getMessage());
                    });
                }
            }).start();
        };
        
        // Refresh button action
        refreshBtn.setOnAction(e -> loadLogs.run());
        
        // Initial load
        loadLogs.run();
        
        content.getChildren().addAll(headerBox, logsList);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(940, 680);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /* ---------------------------------------------------
     * ActivityLogDTO class
     * @author: K24DTCN210-NVMANH (01/12/2025 23:15)
     * --------------------------------------------------- */
    private static class ActivityLogDTO {
        private Long id;
        private String activityType;
        private String details;
        private LocalDateTime timestamp;
        
        Long getId() { return id; }
        String getActivityType() { return activityType; }
        String getDetails() { return details; }
        LocalDateTime getTimestamp() { return timestamp; }
        
        void setId(Long id) { this.id = id; }
        void setActivityType(String activityType) { this.activityType = activityType; }
        void setDetails(String details) { this.details = details; }
        void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    /* ---------------------------------------------------
     * ListCell cho ActivityLogDTO
     * @author: K24DTCN210-NVMANH (01/12/2025 23:15)
     * --------------------------------------------------- */
    private static class ActivityLogListCell extends ListCell<ActivityLogDTO> {
        @Override
        protected void updateItem(ActivityLogDTO item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                HBox box = new HBox(10);
                box.setPadding(new Insets(5));
                
                Label typeLabel = new Label(item.getActivityType());
                typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
                
                Label detailsLabel = new Label(item.getDetails());
                detailsLabel.setWrapText(true);
                detailsLabel.setStyle("-fx-font-size: 11px;");
                
                Label timeLabel = new Label(item.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                
                box.getChildren().addAll(typeLabel, detailsLabel, timeLabel);
                setGraphic(box);
            }
        }
    }
    
    /* ---------------------------------------------------
     * Update empty states visibility
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:30) - Check studentTableItems thay vì studentCards
     * --------------------------------------------------- */
    private void updateEmptyStates() {
        boolean hasStudents = !studentTableItems.isEmpty();
        emptyStatePane.setVisible(!hasStudents);
        emptyStatePane.setManaged(!hasStudents);
        studentsTable.setVisible(hasStudents);
        studentsTable.setManaged(hasStudents);
        
        boolean hasAlerts = !alerts.isEmpty();
        emptyAlertsPane.setVisible(!hasAlerts);
        emptyAlertsPane.setManaged(!hasAlerts);
    }
    
    /* ---------------------------------------------------
     * Highlight student cards có alerts
     * @author: K24DTCN210-NVMANH (01/12/2025 23:20)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 23:30) - Update alerts count trong table
     * --------------------------------------------------- */
    private void highlightStudentsWithAlerts() {
        // Count alerts per student
        Map<Long, Integer> alertsCountMap = new HashMap<>();
        for (AlertItem alert : alerts) {
            Long submissionId = alert.getDto().getSubmissionId();
            if (submissionId != null) {
                alertsCountMap.put(submissionId, alertsCountMap.getOrDefault(submissionId, 0) + 1);
            }
        }
        
        // Update alerts count trong table items
        for (StudentTableItem item : studentTableItems) {
            int count = alertsCountMap.getOrDefault(item.getSubmissionId(), 0);
            item.setAlertsCount(count);
        }
        
        // Refresh table để hiển thị updated counts
        studentsTable.refresh();
    }
    
    /* ---------------------------------------------------
     * Handle refresh button
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        refreshData();
    }
    
    /* ---------------------------------------------------
     * Handle clear alerts button
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    @FXML
    private void handleClearAlerts() {
        alerts.clear();
        alertsCountLabel.setText("Cảnh báo: 0");
        updateEmptyStates();
    }
    
    /* ---------------------------------------------------
     * Handle view all screenshots button
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 00:15) - Implemented screenshots viewer
     * --------------------------------------------------- */
    @FXML
    private void handleViewAllScreenshots() {
        StudentTableItem selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showError("Lỗi", "Vui lòng chọn một sinh viên để xem screenshots.");
            return;
        }
        
        showScreenshotsDialog(selectedStudent);
    }
    
    /* ---------------------------------------------------
     * Handle view activity logs button
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 00:15) - Implemented activity logs viewer
     * --------------------------------------------------- */
    @FXML
    private void handleViewActivityLogs() {
        if (selectedExamId == null) {
            showError("Lỗi", "Vui lòng chọn một đề thi trước.");
            return;
        }
        
        showAllActivityLogsDialog();
    }
    
    /* ---------------------------------------------------
     * Handle export report button
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    @FXML
    private void handleExportReport() {
        showInfo("Xuất báo cáo", "Chức năng xuất báo cáo sẽ được phát triển.");
    }
    
    /* ---------------------------------------------------
     * Show error dialog
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Show info dialog
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Cleanup resources
     * @author: K24DTCN210-NVMANH (01/12/2025 12:00)
     * --------------------------------------------------- */
    public void shutdown() {
        if (refreshScheduler != null && !refreshScheduler.isShutdown()) {
            refreshScheduler.shutdown();
        }
    }
    
    // Inner classes cho data models
    private static class ExamOption {
        private final Long examId;
        private final String title;
        
        ExamOption(Long examId, String title) {
            this.examId = examId;
            this.title = title;
        }
        
        Long getExamId() { return examId; }
        String getTitle() { return title; }
    }
    
    private static class AlertItem {
        private final AlertDTO dto;
        
        AlertItem(AlertDTO dto) {
            this.dto = dto;
        }
        
        AlertDTO getDto() { return dto; }
    }
    
    private static class AlertListCell extends ListCell<AlertItem> {
        @Override
        protected void updateItem(AlertItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                AlertDTO dto = item.getDto();
                VBox box = new VBox(5);
                box.setPadding(new Insets(10));
                box.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1;");
                
                // Student info (nếu có)
                if (dto.getStudentName() != null && !dto.getStudentName().isEmpty()) {
                    HBox studentInfo = new HBox(5);
                    Label studentLabel = new Label("👤 " + dto.getStudentName());
                    studentLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
                    studentLabel.setStyle("-fx-text-fill: #2c3e50;");
                    
                    if (dto.getStudentCode() != null && !dto.getStudentCode().isEmpty()) {
                        Label codeLabel = new Label("(" + dto.getStudentCode() + ")");
                        codeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                        studentInfo.getChildren().addAll(studentLabel, codeLabel);
                    } else {
                        studentInfo.getChildren().add(studentLabel);
                    }
                    box.getChildren().add(studentInfo);
                }
                
                // Severity và Type
                HBox headerBox = new HBox(10);
                Label severityLabel = new Label(dto.getSeverity());
                severityLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
                severityLabel.setTextFill(getSeverityColor(dto.getSeverity()));
                
                Label typeLabel = new Label("Loại: " + dto.getAlertType());
                typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
                
                headerBox.getChildren().addAll(severityLabel, typeLabel);
                box.getChildren().add(headerBox);
                
                // Description
                Label descLabel = new Label(dto.getDescription());
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-font-size: 12px;");
                box.getChildren().add(descLabel);
                
                // Time
                Label timeLabel = new Label("⏰ " + 
                    dto.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                box.getChildren().add(timeLabel);
                
                setGraphic(box);
            }
        }
        
        private Color getSeverityColor(String severity) {
            switch (severity) {
                case "CRITICAL": return Color.RED;
                case "HIGH": return Color.ORANGE;
                case "MEDIUM": return Color.YELLOW;
                case "LOW": return Color.GREEN;
                default: return Color.GRAY;
            }
        }
    }
    
    // Simple data class để track student cards
    private static class StudentCard {
        private final Long submissionId;
        private final String studentName;
        
        StudentCard(Long submissionId, String studentName) {
            this.submissionId = submissionId;
            this.studentName = studentName;
        }
        
        Long getSubmissionId() { return submissionId; }
        String getStudentName() { return studentName; }
    }
    
    /* ---------------------------------------------------
     * Model class cho TableView row
     * @author: K24DTCN210-NVMANH (01/12/2025 23:30)
     * --------------------------------------------------- */
    public static class StudentTableItem {
        private final Long submissionId;
        private final String studentName;
        private final String studentCode;
        private final String status;
        private Integer alertsCount;
        
        public StudentTableItem(Long submissionId, String studentName, String studentCode, 
                               String status, Integer alertsCount) {
            this.submissionId = submissionId;
            this.studentName = studentName;
            this.studentCode = studentCode;
            this.status = status;
            this.alertsCount = alertsCount;
        }
        
        public Long getSubmissionId() { return submissionId; }
        public String getStudentName() { return studentName; }
        public String getStudentCode() { return studentCode; }
        public String getStatus() { return status; }
        public Integer getAlertsCount() { return alertsCount; }
        public void setAlertsCount(Integer alertsCount) { this.alertsCount = alertsCount; }
    }
    
    // DTO classes (simplified)
    private static class AlertDTO {
        private Long id;
        private Long submissionId;
        private String studentName;
        private String studentCode;
        private String alertType;
        private String severity;
        private String description;
        private LocalDateTime createdAt;
        
        // Getters
        Long getId() { return id; }
        Long getSubmissionId() { return submissionId; }
        String getStudentName() { return studentName; }
        String getStudentCode() { return studentCode; }
        String getAlertType() { return alertType; }
        String getSeverity() { return severity; }
        String getDescription() { return description; }
        LocalDateTime getCreatedAt() { return createdAt; }
        
        // Setters for Gson
        void setId(Long id) { this.id = id; }
        void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
        void setStudentName(String studentName) { this.studentName = studentName; }
        void setStudentCode(String studentCode) { this.studentCode = studentCode; }
        void setAlertType(String alertType) { this.alertType = alertType; }
        void setSeverity(String severity) { this.severity = severity; }
        void setDescription(String description) { this.description = description; }
        void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    private static class ScreenshotDTO {
        private Long id;
        private Long submissionId;
        private String filePath;
        private String screenResolution;
        private String windowTitle;
        private LocalDateTime timestamp;
        
        Long getId() { return id; }
        Long getSubmissionId() { return submissionId; }
        String getFilePath() { return filePath; }
        String getScreenResolution() { return screenResolution; }
        String getWindowTitle() { return windowTitle; }
        LocalDateTime getTimestamp() { return timestamp; }
    }
    
    // Gson adapter cho LocalDateTime
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws java.io.IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }
        
        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
    
    /* ---------------------------------------------------
     * ActivityLogItem class - for all students activity logs
     * @author: K24DTCN210-NVMANH (02/12/2025 00:15)
     * --------------------------------------------------- */
    public static class ActivityLogItem {
        private final String studentName;
        private final String studentCode;
        private final String activityType;
        private final String details;
        private final LocalDateTime timestamp;
        
        public ActivityLogItem(String studentName, String studentCode, String activityType, 
                              String details, LocalDateTime timestamp) {
            this.studentName = studentName;
            this.studentCode = studentCode;
            this.activityType = activityType;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        public String getStudentName() { return studentName; }
        public String getStudentCode() { return studentCode; }
        public String getActivityType() { return activityType; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /* ---------------------------------------------------
     * ActivityLogItemCell - Custom cell for activity log items
     * @author: K24DTCN210-NVMANH (02/12/2025 00:15)
     * --------------------------------------------------- */
    private static class ActivityLogItemCell extends ListCell<ActivityLogItem> {
        @Override
        protected void updateItem(ActivityLogItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                VBox container = new VBox(5);
                container.setPadding(new Insets(8));
                
                // Header: Student info + timestamp
                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label studentLabel = new Label(item.getStudentName() + " (" + item.getStudentCode() + ")");
                studentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2196F3;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label timeLabel = new Label(item.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                
                headerBox.getChildren().addAll(studentLabel, spacer, timeLabel);
                
                // Activity info
                HBox activityBox = new HBox(10);
                
                Label typeLabel = new Label(item.getActivityType());
                typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #f44336;");
                
                Label detailsLabel = new Label(item.getDetails());
                detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
                detailsLabel.setWrapText(true);
                
                activityBox.getChildren().addAll(typeLabel, detailsLabel);
                
                container.getChildren().addAll(headerBox, activityBox);
                setGraphic(container);
            }
        }
    }
}

