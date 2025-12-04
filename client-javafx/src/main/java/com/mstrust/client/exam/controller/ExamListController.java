package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.dto.ExamInfoDTO;
import com.mstrust.client.exam.dto.StartExamResponse;
import com.mstrust.client.exam.util.TimeFormatter;
import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamDTO;
import com.mstrust.client.util.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.HPos;
import com.mstrust.client.exam.util.IconFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho Exam List Screen - Phase 8.2
 * Quản lý hiển thị danh sách đề thi và filters
 * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
 * --------------------------------------------------- */
public class ExamListController {
    private static final Logger logger = LoggerFactory.getLogger(ExamListController.class);
    
    private ExamApiClient examApiClient; // Cho student
    private ExamManagementApiClient examManagementApiClient; // Cho teacher
    private List<ExamInfoDTO> allExams;
    private List<ExamInfoDTO> filteredExams;
    private Stage stage; // Stage reference để mở wizard modal (teacher mode)
    
    // User info
    private String currentUserName;
    private String currentUserEmail;
    
    // FXML Components
    @FXML private Label pageTitleLabel;
    @FXML private ComboBox<String> subjectFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button refreshButton;
    @FXML private Button createExamButton;
    @FXML private GridPane examCardsContainer;
    @FXML private VBox examCardsContainerWrapper;
    @FXML private VBox emptyStateBox;
    @FXML private Label examCountLabel;
    @FXML private Label lastRefreshLabel;
    
    // Phase 8.6: Loading overlay components
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingMessage;
    
    // User info components (added for logout feature)
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Button logoutButton;

    /* ---------------------------------------------------
     * Initialize - được gọi sau khi FXML loaded
     * Nhận ExamApiClient đã authenticated từ login (cho student)
     * @param apiClient ExamApiClient với auth token
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 08:04) - Accept apiClient parameter
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Setup create exam button visibility
     * EditBy: K24DTCN210-NVMANH (01/12/2025 00:30) - Support user info setup
     * --------------------------------------------------- */
    public void initialize(ExamApiClient apiClient) {
        this.examApiClient = apiClient;
        logger.info("Initializing ExamListController (student mode)");
        
        // Setup filters
        setupFilters();
        
        // Setup create exam button - ẩn mặc định (chỉ hiển thị khi được set stage từ teacher)
        if (createExamButton != null) {
            createExamButton.setVisible(false);
            createExamButton.setManaged(false);
        }
        
        // Setup responsive layout listener
        setupResponsiveLayout();
        
        // Load initial data
        loadExams();
    }
    
    /* ---------------------------------------------------
     * Initialize với user info (cho student)
     * @param apiClient ExamApiClient với auth token
     * @param userName Tên người dùng
     * @param role Vai trò (STUDENT hoặc ROLE_STUDENT)
     * @author: K24DTCN210-NVMANH (01/12/2025 00:30)
     * --------------------------------------------------- */
    public void initialize(ExamApiClient apiClient, String userName, String role) {
        initialize(apiClient, userName, null, role);
    }

    /* ---------------------------------------------------
     * Initialize với user info đầy đủ (cho student)
     * @param apiClient ExamApiClient với auth token
     * @param userName Tên người dùng
     * @param email Email người dùng
     * @param role Vai trò
     * @author: K24DTCN210-NVMANH (04/12/2025)
     * --------------------------------------------------- */
    public void initialize(ExamApiClient apiClient, String userName, String email, String role) {
        this.currentUserName = userName;
        this.currentUserEmail = email;
        initialize(apiClient);
        setupUserInfo(userName, role);
    }
    
    /* ---------------------------------------------------
     * Initialize với ExamManagementApiClient (cho teacher)
     * @param managementApiClient ExamManagementApiClient với auth token
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public void initialize(ExamManagementApiClient managementApiClient) {
        this.examManagementApiClient = managementApiClient;
        logger.info("Initializing ExamListController (teacher mode)");
        
        // Update title cho teacher mode
        if (pageTitleLabel != null) {
            pageTitleLabel.setText("📝 Quản Lý Đề Thi");
        }
        
        // Setup filters
        setupFilters();
        
        // Setup create exam button - hiển thị cho teacher
        // Sử dụng Platform.runLater để đảm bảo FXML đã load xong
        Platform.runLater(() -> {
            if (createExamButton != null) {
                createExamButton.setVisible(true);
                createExamButton.setManaged(true);
                createExamButton.setDisable(false);
                logger.info("Create exam button set to visible (teacher mode) - button: {}", createExamButton.getText());
            } else {
                logger.error("createExamButton is null in teacher mode initialize!");
            }
        });
        
        // Load initial data
        loadExams();
    }
    
    /* ---------------------------------------------------
     * Set stage reference (cho teacher để mở wizard)
     * @param stage Stage reference
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public void setStage(Stage stage) {
        this.stage = stage;
        logger.info("setStage called with stage: {}", stage != null ? "not null" : "null");
        logger.info("createExamButton: {}", createExamButton != null ? "not null" : "null");
        
        // Hiển thị button "Tạo đề thi" khi có stage (teacher mode)
        if (createExamButton != null && stage != null) {
            logger.info("Showing create exam button");
            createExamButton.setVisible(true);
            createExamButton.setManaged(true);
        } else {
            logger.warn("Cannot show create exam button - createExamButton: {}, stage: {}", 
                createExamButton != null, stage != null);
        }
    }

    /* ---------------------------------------------------
     * Setup responsive layout listener cho GridPane
     * @author: K24DTCN210-NVMANH (02/12/2025 19:00)
     * EditBy: K24DTCN210-NVMANH (03/12/2025 09:10) - Updated for GridPane
     * --------------------------------------------------- */
    private void setupResponsiveLayout() {
        // Listener để responsive khi container resize
        if (examCardsContainer != null) {
            examCardsContainer.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if (filteredExams != null && !filteredExams.isEmpty() && newWidth.doubleValue() > 0) {
                    Platform.runLater(() -> refreshGridLayout());
                }
            });
        }
    }
    
    /* ---------------------------------------------------
     * Refresh grid layout với width mới
     * @author: K24DTCN210-NVMANH (03/12/2025 09:10)
     * --------------------------------------------------- */
    private void refreshGridLayout() {
        if (filteredExams == null || filteredExams.isEmpty()) return;
        
        double containerWidth = examCardsContainer.getWidth();
        int newColumns = calculateOptimalColumns(containerWidth);
        double newCardWidth = calculateCardWidthForGrid(containerWidth, newColumns);
        
        // Kiểm tra nếu layout thay đổi thì rebuild grid
        int currentColumns = examCardsContainer.getColumnConstraints().size();
        if (currentColumns != newColumns) {
            // Rebuild toàn bộ grid với layout mới
            displayExams();
        } else {
            // Chỉ update card width nếu số cột không đổi
            setupGridColumns(newColumns, newCardWidth);
            examCardsContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    VBox card = (VBox) node;
                    card.setPrefWidth(newCardWidth);
                    card.setMaxWidth(newCardWidth);
                }
            });
        }
    }
    
    /* ---------------------------------------------------
     * Setup các combo box filters
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (03/12/2025 16:55) - Load subjects từ API
     * --------------------------------------------------- */
    private void setupFilters() {
        // Status filter - setup trước
        statusFilterCombo.getItems().addAll(
            "Tất cả trạng thái",
            "Sắp diễn ra",
            "Đang diễn ra",
            "Đã kết thúc"
        );
        statusFilterCombo.setValue("Tất cả trạng thái");
        
        // Subject filter - load từ API
        loadSubjectsFromAPI();
        
        // Add listeners cho filters
        setupFilterListeners();
    }
    
    /* ---------------------------------------------------
     * Load danh sách môn học từ API
     * @author: K24DTCN210-NVMANH (03/12/2025 16:55)
     * --------------------------------------------------- */
    private void loadSubjectsFromAPI() {
        // Chỉ load subjects cho student mode
        if (examApiClient == null) {
            // Teacher mode - dùng hardcode subjects tạm thời
            subjectFilterCombo.getItems().addAll(
                "Tất cả môn học",
                "Toán",
                "Lý", 
                "Hóa",
                "Sinh",
                "Văn",
                "Anh"
            );
            subjectFilterCombo.setValue("Tất cả môn học");
            return;
        }
        
        // Student mode - load từ API
        new Thread(() -> {
            try {
                List<Map<String, String>> subjects = examApiClient.getAvailableSubjects();
                
                Platform.runLater(() -> {
                    subjectFilterCombo.getItems().clear();
                    subjectFilterCombo.getItems().add("Tất cả môn học");
                    
                    for (Map<String, String> subject : subjects) {
                        String displayText = subject.get("subjectName") + " (" + subject.get("subjectCode") + ")";
                        subjectFilterCombo.getItems().add(displayText);
                    }
                    
                    subjectFilterCombo.setValue("Tất cả môn học");
                    logger.info("Loaded {} subjects for filter", subjects.size());
                });
                
            } catch (Exception e) {
                logger.error("Failed to load subjects for filter", e);
                Platform.runLater(() -> {
                    // Fallback to default subjects
                    subjectFilterCombo.getItems().addAll(
                        "Tất cả môn học",
                        "Toán",
                        "Lý",
                        "Hóa", 
                        "Sinh",
                        "Văn",
                        "Anh"
                    );
                    subjectFilterCombo.setValue("Tất cả môn học");
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Setup listeners cho filter ComboBoxes
     * @author: K24DTCN210-NVMANH (03/12/2025 16:55)
     * --------------------------------------------------- */
    private void setupFilterListeners() {
        // Subject filter listener
        subjectFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.info("Subject filter changed to: {}", newValue);
                applyFilters();
            }
        });
        
        // Status filter listener  
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.info("Status filter changed to: {}", newValue);
                applyFilters();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Apply filters để lọc danh sách đề thi
     * @author: K24DTCN210-NVMANH (03/12/2025 16:55)
     * --------------------------------------------------- */
    private void applyFilters() {
        if (allExams == null || allExams.isEmpty()) {
            filteredExams = allExams;
            displayExams();
            return;
        }
        
        String selectedSubject = subjectFilterCombo.getValue();
        String selectedStatus = statusFilterCombo.getValue();
        
        filteredExams = allExams.stream()
            .filter(exam -> filterBySubject(exam, selectedSubject))
            .filter(exam -> filterByStatus(exam, selectedStatus))
            .collect(java.util.stream.Collectors.toList());
        
        logger.info("Applied filters - Subject: {}, Status: {} - {} exams found", 
            selectedSubject, selectedStatus, filteredExams.size());
        
        displayExams();
    }
    
    /* ---------------------------------------------------
     * Filter by subject
     * @param exam ExamInfoDTO
     * @param subjectFilter Subject filter value
     * @returns true nếu pass filter
     * @author: K24DTCN210-NVMANH (03/12/2025 16:55)
     * --------------------------------------------------- */
    private boolean filterBySubject(ExamInfoDTO exam, String subjectFilter) {
        if (subjectFilter == null || subjectFilter.equals("Tất cả môn học")) {
            return true;
        }
        
        // Extract subject code from display text "Tên môn (CODE)"
        String subjectCode = null;
        String subjectName = null;
        
        if (subjectFilter.contains("(") && subjectFilter.contains(")")) {
            // Format: "Tên môn (CODE)"
            int startIndex = subjectFilter.lastIndexOf("(");
            int endIndex = subjectFilter.lastIndexOf(")");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                subjectCode = subjectFilter.substring(startIndex + 1, endIndex);
                subjectName = subjectFilter.substring(0, startIndex).trim();
            }
        } else {
            // Fallback - assume it's subject name only
            subjectName = subjectFilter;
        }
        
        // Check against exam's subject
        if (subjectCode != null && exam.getSubjectCode() != null) {
            return exam.getSubjectCode().equals(subjectCode);
        }
        
        if (subjectName != null && exam.getSubjectName() != null) {
            return exam.getSubjectName().contains(subjectName) || 
                   subjectName.contains(exam.getSubjectName());
        }
        
        // Fallback - check both subject name and code
        return (exam.getSubjectName() != null && exam.getSubjectName().contains(subjectFilter)) ||
               (exam.getSubjectCode() != null && exam.getSubjectCode().contains(subjectFilter));
    }

    /* ---------------------------------------------------
     * Load danh sách đề thi từ backend
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:03) - Phase 8.6: Use loading overlay
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Support teacher mode với ExamManagementApiClient
     * --------------------------------------------------- */
    private void loadExams() {
        // Disable refresh button
        refreshButton.setDisable(true);
        
        // Show loading overlay
        showLoading("Đang tải danh sách đề thi...");
        
        // Load in background thread
        new Thread(() -> {
            try {
                if (examManagementApiClient != null) {
                    // Teacher mode: dùng ExamManagementApiClient
                    logger.info("Loading exams for teacher...");
                    List<ExamDTO> examDTOs = examManagementApiClient.getAllExams();
                    // Convert ExamDTO sang ExamInfoDTO
                    allExams = examDTOs.stream()
                        .map(this::convertToExamInfoDTO)
                        .collect(Collectors.toList());
                } else if (examApiClient != null) {
                    // Student mode: dùng ExamApiClient
                    logger.info("Loading available exams for student...");
                    allExams = examApiClient.getAvailableExams();
                } else {
                    throw new IllegalStateException("No API client initialized");
                }
                
                filteredExams = allExams;
                
                // Update UI in JavaFX thread
                Platform.runLater(() -> {
                    displayExams();
                    updateLastRefreshTime();
                    refreshButton.setDisable(false);
                    hideLoading();
                });
                
            } catch (Exception e) {
                logger.error("Failed to load exams", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Không thể tải danh sách đề thi", e.getMessage());
                    refreshButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Convert ExamDTO (teacher) sang ExamInfoDTO (display)
     * @param examDTO ExamDTO từ ExamManagementApiClient
     * @return ExamInfoDTO để hiển thị
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private ExamInfoDTO convertToExamInfoDTO(ExamDTO examDTO) {
        ExamInfoDTO info = new ExamInfoDTO();
        info.setId(examDTO.getId());
        info.setTitle(examDTO.getTitle());
        info.setDescription(examDTO.getDescription());
        info.setDuration(examDTO.getDurationMinutes());
        info.setStartTime(examDTO.getStartTime());
        info.setEndTime(examDTO.getEndTime());
        info.setTotalQuestions(examDTO.getQuestionCount());
        info.setTotalPoints(examDTO.getTotalScore() != null ? examDTO.getTotalScore().doubleValue() : 0.0);
        info.setPassingScore(examDTO.getPassingScore() != null ? examDTO.getPassingScore().doubleValue() : null);
        info.setStatus(examDTO.getCurrentStatus() != null ? examDTO.getCurrentStatus() : "UNKNOWN");
        info.setSubjectCode(examDTO.getSubjectClassName());
        info.setSubjectName(examDTO.getSubjectName());
        
        // Teacher mode - no attempt info
        info.setMaxAttempts(null);
        info.setAttemptsMade(0);
        info.setRemainingAttempts(null);
        info.setHasActiveSubmission(false);
        info.setHasPassed(false);
        info.setHighestScore(null);
        info.setIsEligible(false);
        info.setIneligibleReason("Teacher mode");
        
        // Debug logging
        logger.info("Converting ExamDTO to ExamInfoDTO: {} - Duration: {} - SubjectName: {} - SubjectClassName: {}", 
            examDTO.getTitle(), examDTO.getDurationMinutes(), examDTO.getSubjectName(), examDTO.getSubjectClassName());
        
        // Class names - có thể cần lấy từ examDTO nếu có
        info.setCanStart(false); // Teacher không làm bài
        return info;
    }

    /* ---------------------------------------------------
     * Hiển thị danh sách exam cards với GridPane layout
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (03/12/2025 09:10) - Changed to GridPane for proper grid layout
     * --------------------------------------------------- */
    private void displayExams() {
        examCardsContainer.getChildren().clear();
        examCardsContainer.getColumnConstraints().clear();
        examCardsContainer.getRowConstraints().clear();
        
        if (filteredExams == null || filteredExams.isEmpty()) {
            // Show empty state
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            examCountLabel.setText("Tìm thấy 0 đề thi");
        } else {
            // Hide empty state
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);
            
            // Calculate responsive grid layout
            Platform.runLater(() -> {
                double containerWidth = examCardsContainer.getWidth();
                if (containerWidth <= 0) {
                    containerWidth = 1000; // Default container width
                }
                
                // Tính số cột tối ưu
                int columns = calculateOptimalColumns(containerWidth);
                double cardWidth = calculateCardWidthForGrid(containerWidth, columns);
                
                // Setup column constraints
                setupGridColumns(columns, cardWidth);
                
                // Add cards to grid
                int row = 0;
                int col = 0;
                
                for (ExamInfoDTO exam : filteredExams) {
                    logger.info("Creating card for exam: {} at position [{},{}]", 
                        exam.getTitle(), row, col);
                    
                    VBox card = createExamCard(exam, cardWidth);
                    examCardsContainer.add(card, col, row);
                    
                    col++;
                    if (col >= columns) {
                        col = 0;
                        row++;
                    }
                }
                
                examCountLabel.setText(String.format("Tìm thấy %d đề thi", filteredExams.size()));
            });
        }
    }

    /* ---------------------------------------------------
     * Tạo exam card với layout responsive
     * @param exam ExamInfoDTO
     * @param cardWidth Chiều rộng card được tính toán
     * @returns VBox chứa card UI
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 19:00) - Responsive card width
     * --------------------------------------------------- */
    private VBox createExamCard(ExamInfoDTO exam, double cardWidth) {
        VBox card = new VBox(8);
        card.getStyleClass().add("exam-card-clean");
        
        // Add special style for out of attempts or ineligible exams
        if (examApiClient != null) { // Only for student mode
            if (isOutOfAttempts(exam)) {
                card.getStyleClass().add("exam-card-out-of-attempts");
            } else if (exam.getIsEligible() != null && !exam.getIsEligible()) {
                card.getStyleClass().add("exam-card-ineligible");
            } else if (exam.getHasPassed() != null && exam.getHasPassed()) {
                card.getStyleClass().add("exam-card-passed");
            }
        }
        
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        card.setPrefHeight(190); // Fixed height để cards đều nhau
        
        // Header: Title + Status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label(exam.getTitle());
        title.getStyleClass().add("exam-title-clean");
        title.setWrapText(true);
        title.setMaxWidth(220); // Giảm để dành chỗ cho status badge
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Label status = createStatusBadge(exam);
        status.setMinWidth(Region.USE_PREF_SIZE); // Đảm bảo hiển thị đầy đủ
        status.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        header.getChildren().addAll(title, status);
        
        // Subject và exam type line
        HBox subjectLine = new HBox(15);
        subjectLine.setAlignment(Pos.CENTER_LEFT);
        
        // Subject info - Format: [Mã môn] - [Tên môn]
        HBox subjectInfo = new HBox(6);
        subjectInfo.setAlignment(Pos.CENTER_LEFT);
        
        String subjectDisplayName = formatSubjectDisplay(exam.getSubjectCode(), exam.getSubjectName());
        
        subjectInfo.getChildren().addAll(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BOOK, 12, IconFactory.COLOR_PRIMARY),
            new Label(subjectDisplayName)
        );
        
        // Exam type info (nếu có)
        HBox examTypeInfo = new HBox(6);
        examTypeInfo.setAlignment(Pos.CENTER_LEFT);
        String examType = determineExamType(exam); // Xác định loại đề thi
        examTypeInfo.getChildren().addAll(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TAG, 12, IconFactory.COLOR_INFO),
            new Label(examType)
        );
        
        subjectLine.getChildren().addAll(subjectInfo, examTypeInfo);
        subjectLine.getStyleClass().add("exam-subject-clean");
        
        // Info grid - 3 rows với thông tin chi tiết
        VBox infoGrid = new VBox(6);
        
        // Sử dụng GridPane để layout cân đối hơn
        GridPane infoGridPane = new GridPane();
        infoGridPane.setHgap(10);
        infoGridPane.setVgap(6);
        
        // Column constraints để chia đều không gian
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        infoGridPane.getColumnConstraints().addAll(col1, col2);
        
        // Row 0: Thời gian bắt đầu và kết thúc
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLOCK, 12, IconFactory.COLOR_PRIMARY),
            "Bắt đầu: " + TimeFormatter.formatDateTime(exam.getStartTime())
        ), 0, 0);
        
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CALENDAR_TIMES, 12, IconFactory.COLOR_DANGER),
            "Kết thúc: " + TimeFormatter.formatDateTime(exam.getEndTime())
        ), 1, 0);
        
        // Row 1: Thời lượng và số câu hỏi
        String durationText;
        if (exam.getDurationMinutes() != null) {
            durationText = "Thời gian: " + exam.getDurationMinutes() + " phút";
        } else if (exam.getDuration() != null) {
            durationText = "Thời gian: " + exam.getDuration() + " phút";
        } else {
            durationText = "Thời gian: Không xác định";
        }
        
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HOURGLASS_HALF, 12, IconFactory.COLOR_WARNING),
            durationText
        ), 0, 1);
        
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.QUESTION_CIRCLE, 12, IconFactory.COLOR_INFO),
            exam.getTotalQuestions() + " câu hỏi"
        ), 1, 1);
        
        // Row 2: Điểm số và điểm đạt
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.STAR, 12, IconFactory.COLOR_SUCCESS),
            "Điểm tối đa: " + (exam.getTotalPoints() != null ? String.format("%.0f", exam.getTotalPoints()) : "Chưa xác định")
        ), 0, 2);
        
        infoGridPane.add(createFullWidthInfoItem(
            IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TROPHY, 12, IconFactory.COLOR_WARNING),
            "Điểm đạt: " + (exam.getPassingScore() != null ? String.format("%.0f", exam.getPassingScore()) : "Chưa xác định")
        ), 1, 2);
        
        // Row 3: Attempt information (chỉ hiển thị cho student mode)
        if (examApiClient != null && exam.getMaxAttempts() != null) {
            String attemptText = formatAttemptInfo(exam);
            String scoreText = formatScoreInfo(exam);
            
            infoGridPane.add(createFullWidthInfoItem(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.REDO_ALT, 12, 
                    isOutOfAttempts(exam) ? IconFactory.COLOR_DANGER : IconFactory.COLOR_INFO),
                attemptText
            ), 0, 3);
            
            if (scoreText != null) {
                infoGridPane.add(createFullWidthInfoItem(
                    IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHART_LINE, 12, 
                        exam.getHasPassed() != null && exam.getHasPassed() ? IconFactory.COLOR_SUCCESS : IconFactory.COLOR_INFO),
                    scoreText
                ), 1, 3);
            }
        }
        
        infoGrid.getChildren().add(infoGridPane);
        
        // Countdown warning if needed
        HBox countdown = createCountdownLabel(exam);
        if (countdown != null) {
            infoGrid.getChildren().add(countdown);
        }
        
        // Action button
        Button actionBtn = createSimpleActionButton(exam);
        
        // Assemble card
        card.getChildren().addAll(header, subjectLine, infoGrid, actionBtn);
        
        return card;
    }
    
    /* ---------------------------------------------------
     * Format thông tin số lần làm bài
     * @param exam ExamInfoDTO
     * @returns String mô tả số lần làm bài
     * @author: K24DTCN210-NVMANH (03/12/2025 17:05)
     * --------------------------------------------------- */
    private String formatAttemptInfo(ExamInfoDTO exam) {
        Integer attemptsMade = exam.getAttemptsMade() != null ? exam.getAttemptsMade() : 0;
        Integer maxAttempts = exam.getMaxAttempts();
        
        if (maxAttempts == null || maxAttempts == 0) {
            return "Lần làm: " + attemptsMade + " (Không giới hạn)";
        } else {
            Integer remaining = exam.getRemainingAttempts() != null ? exam.getRemainingAttempts() : (maxAttempts - attemptsMade);
            return "Lần làm: " + attemptsMade + "/" + maxAttempts + " (Còn " + remaining + ")";
        }
    }
    
    /* ---------------------------------------------------
     * Format thông tin điểm số cao nhất
     * @param exam ExamInfoDTO
     * @returns String mô tả điểm số hoặc null nếu chưa làm
     * @author: K24DTCN210-NVMANH (03/12/2025 17:05)
     * --------------------------------------------------- */
    private String formatScoreInfo(ExamInfoDTO exam) {
        if (exam.getAttemptsMade() == null || exam.getAttemptsMade() == 0) {
            return null; // Chưa làm lần nào
        }
        
        if (exam.getHighestScore() != null) {
            String scoreText = "Điểm cao nhất: " + String.format("%.1f", exam.getHighestScore());
            if (exam.getHasPassed() != null && exam.getHasPassed()) {
                scoreText += " ✓";
            }
            return scoreText;
        } else {
            return "Chưa có điểm";
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem đã hết lượt làm bài chưa
     * @param exam ExamInfoDTO
     * @returns true nếu đã hết lượt
     * @author: K24DTCN210-NVMANH (03/12/2025 17:05)
     * --------------------------------------------------- */
    private boolean isOutOfAttempts(ExamInfoDTO exam) {
        if (exam.getMaxAttempts() == null || exam.getMaxAttempts() == 0) {
            return false; // Unlimited attempts
        }
        
        Integer remaining = exam.getRemainingAttempts();
        if (remaining != null) {
            return remaining <= 0;
        }
        
        Integer attemptsMade = exam.getAttemptsMade() != null ? exam.getAttemptsMade() : 0;
        return attemptsMade >= exam.getMaxAttempts();
    }
    
    /* ---------------------------------------------------
     * Tạo exam card với default width (backward compatibility)
     * @param exam ExamInfoDTO
     * @returns VBox chứa card UI
     * @author: K24DTCN210-NVMANH (02/12/2025 19:00)
     * --------------------------------------------------- */
    private VBox createExamCard(ExamInfoDTO exam) {
        return createExamCard(exam, 380); // Default width
    }
    
    /* ---------------------------------------------------
     * Tạo action buttons cho teacher mode (Edit, Delete, Publish/Unpublish) - Compact version
     * @param exam ExamInfoDTO
     * @returns HBox chứa các action buttons
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 16:51) - Compact buttons with IKonli icons
     * --------------------------------------------------- */
    private HBox createTeacherActionButtons(ExamInfoDTO exam) {
        HBox buttonContainer = new HBox(6);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // View Details button
        Button viewButton = new Button();
        viewButton.setGraphic(IconFactory.createViewIcon());
        viewButton.getStyleClass().add("compact-button");
        viewButton.setTooltip(new Tooltip("Xem chi tiết"));
        viewButton.setOnAction(e -> handleViewExamDetails(exam));
        
        // Edit button
        Button editButton = new Button();
        editButton.setGraphic(IconFactory.createEditIconForButton());
        editButton.getStyleClass().add("compact-button");
        editButton.setTooltip(new Tooltip("Chỉnh sửa"));
        editButton.setOnAction(e -> handleEditExam(exam));
        
        // Publish/Unpublish button (cần lấy từ ExamDTO)
        Button publishButton = new Button();
        // TODO: Lấy isPublished từ ExamDTO, tạm thời dùng status
        boolean isPublished = exam.getStatus() != null && 
            (exam.getStatus().contains("PUBLISHED") || exam.getStatus().contains("ONGOING"));
        if (isPublished) {
            publishButton.setGraphic(IconFactory.createLockIconForButton());
            publishButton.setTooltip(new Tooltip("Ẩn đề thi"));
            publishButton.setOnAction(e -> handleUnpublishExam(exam));
        } else {
            publishButton.setGraphic(IconFactory.createPublishIcon());
            publishButton.setTooltip(new Tooltip("Xuất bản"));
            publishButton.setOnAction(e -> handlePublishExam(exam));
        }
        publishButton.getStyleClass().add("compact-button");
        
        // Delete button
        Button deleteButton = new Button();
        deleteButton.setGraphic(IconFactory.createDeleteIconForButton());
        deleteButton.getStyleClass().add("compact-button-danger");
        deleteButton.setTooltip(new Tooltip("Xóa đề thi"));
        deleteButton.setOnAction(e -> handleDeleteExam(exam));
        
        buttonContainer.getChildren().addAll(viewButton, editButton, publishButton, deleteButton);
        return buttonContainer;
    }
    
    /* ---------------------------------------------------
     * Handler cho View Exam Details (teacher)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void handleViewExamDetails(ExamInfoDTO exam) {
        logger.info("View exam details: {}", exam.getId());
        // TODO: Mở dialog hoặc view chi tiết exam
        showInfo("Chi tiết đề thi", "Chức năng xem chi tiết sẽ được phát triển sau.");
    }
    
    /* ---------------------------------------------------
     * Handler cho Edit Exam (teacher)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void handleEditExam(ExamInfoDTO exam) {
        logger.info("Edit exam: {}", exam.getId());
        // TODO: Mở wizard edit exam
        showInfo("Sửa đề thi", "Chức năng sửa đề thi sẽ được phát triển sau.");
    }
    
    /* ---------------------------------------------------
     * Handler cho Publish Exam (teacher)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void handlePublishExam(ExamInfoDTO exam) {
        logger.info("Publish exam: {}", exam.getId());
        // TODO: Call API publish exam
        showInfo("Xuất bản đề thi", "Chức năng xuất bản sẽ được phát triển sau.");
    }
    
    /* ---------------------------------------------------
     * Handler cho Unpublish Exam (teacher)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void handleUnpublishExam(ExamInfoDTO exam) {
        logger.info("Unpublish exam: {}", exam.getId());
        // TODO: Call API unpublish exam
        showInfo("Ẩn đề thi", "Chức năng ẩn đề thi sẽ được phát triển sau.");
    }
    
    /* ---------------------------------------------------
     * Handler cho Delete Exam (teacher)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void handleDeleteExam(ExamInfoDTO exam) {
        logger.info("Delete exam: {}", exam.getId());
        
        Optional<ButtonType> result = DialogUtils.showAlert(
            Alert.AlertType.CONFIRMATION,
            "Xác nhận xóa",
            "Bạn có chắc muốn xóa đề thi này?",
            String.format("Đề thi: %s\n\nHành động này không thể hoàn tác.", exam.getTitle()),
            stage // Use current stage as owner
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Call API delete exam
            showInfo("Xóa đề thi", "Chức năng xóa đề thi sẽ được phát triển sau.");
        }
    }

    /* ---------------------------------------------------
     * Tạo status badge cho exam
     * @param exam ExamInfoDTO
     * @returns Label styled as badge
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private Label createStatusBadge(ExamInfoDTO exam) {
        Label badge = new Label();
        badge.getStyleClass().add("status-badge");
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            badge.setText("Sắp diễn ra");
            badge.getStyleClass().add("status-upcoming");
        } else if (now.isAfter(exam.getEndTime())) {
            badge.setText("Đã kết thúc");
            badge.getStyleClass().add("status-ended");
        } else {
            badge.setText("Đang diễn ra");
            badge.getStyleClass().add("status-ongoing");
        }
        
        return badge;
    }

    /* ---------------------------------------------------
     * Tính toán số cột tối ưu cho GridPane
     * @param containerWidth Chiều rộng container
     * @returns int số cột tối ưu
     * @author: K24DTCN210-NVMANH (03/12/2025 09:10)
     * --------------------------------------------------- */
    private int calculateOptimalColumns(double containerWidth) {
        // Trừ padding
        double availableWidth = containerWidth - 40; // 20px padding mỗi bên
        
        // Xác định số cột dựa trên kích thước màn hình
        if (availableWidth >= 1400) {
            return 4; // 4 cột cho màn hình rất lớn
        } else if (availableWidth >= 1050) {
            return 3; // 3 cột cho màn hình lớn
        } else if (availableWidth >= 700) {
            return 2; // 2 cột cho màn hình trung bình
        } else {
            return 1; // 1 cột cho màn hình nhỏ
        }
    }
    
    /* ---------------------------------------------------
     * Tính toán card width cho GridPane
     * @param containerWidth Chiều rộng container
     * @param columns Số cột
     * @returns double card width
     * @author: K24DTCN210-NVMANH (03/12/2025 09:10)
     * --------------------------------------------------- */
    private double calculateCardWidthForGrid(double containerWidth, int columns) {
        // Trừ padding và gaps
        double availableWidth = containerWidth - 40; // 20px padding mỗi bên
        double totalGaps = (columns - 1) * 20; // 20px gap giữa các cột
        double cardWidth = (availableWidth - totalGaps) / columns;
        
        // Đảm bảo card width trong khoảng hợp lý
        cardWidth = Math.max(300, Math.min(450, cardWidth));
        
        logger.info("Container width: {}, Columns: {}, Card width: {}", 
            containerWidth, columns, cardWidth);
        
        return cardWidth;
    }
    
    /* ---------------------------------------------------
     * Setup column constraints cho GridPane
     * @param columns Số cột
     * @param cardWidth Chiều rộng card
     * @author: K24DTCN210-NVMANH (03/12/2025 09:10)
     * --------------------------------------------------- */
    private void setupGridColumns(int columns, double cardWidth) {
        examCardsContainer.getColumnConstraints().clear();
        
        for (int i = 0; i < columns; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(cardWidth);
            colConstraints.setMinWidth(cardWidth);
            colConstraints.setMaxWidth(cardWidth);
            colConstraints.setHalignment(HPos.CENTER);
            examCardsContainer.getColumnConstraints().add(colConstraints);
        }
    }
    
    /* ---------------------------------------------------
     * Format hiển thị môn học: [Mã môn] - [Tên môn]
     * @param subjectCode Mã môn học
     * @param subjectName Tên môn học
     * @returns String formatted subject display
     * @author: K24DTCN210-NVMANH (02/12/2025 18:30)
     * --------------------------------------------------- */
    private String formatSubjectDisplay(String subjectCode, String subjectName) {
        boolean hasCode = subjectCode != null && !subjectCode.trim().isEmpty();
        boolean hasName = subjectName != null && !subjectName.trim().isEmpty();
        
        if (hasCode && hasName) {
            return subjectCode + " - " + subjectName;
        } else if (hasCode) {
            return subjectCode;
        } else if (hasName) {
            return subjectName;
        } else {
            return "Chưa xác định môn học";
        }
    }
    
    /* ---------------------------------------------------
     * Xác định loại đề thi dựa trên thông tin exam
     * @param exam ExamInfoDTO
     * @returns String loại đề thi
     * @author: K24DTCN210-NVMANH (02/12/2025 18:00)
     * --------------------------------------------------- */
    private String determineExamType(ExamInfoDTO exam) {
        // Logic xác định loại đề thi dựa trên các thuộc tính
        if (exam.getTitle().toLowerCase().contains("giữa kỳ")) {
            return "Giữa kỳ";
        } else if (exam.getTitle().toLowerCase().contains("cuối kỳ")) {
            return "Cuối kỳ";
        } else if (exam.getTitle().toLowerCase().contains("test")) {
            return "Kiểm tra";
        } else if (exam.getDurationMinutes() != null) {
            if (exam.getDurationMinutes() >= 90) {
                return "Thi chính thức";
            } else if (exam.getDurationMinutes() >= 45) {
                return "Kiểm tra";
            } else {
                return "Trắc nghiệm";
            }
        }
        return "Bài thi";
    }
    
    /* ---------------------------------------------------
     * Tạo full width info item cho GridPane
     * @param icon FontIcon
     * @param text Text content
     * @returns HBox chứa icon và text, sử dụng toàn bộ width
     * @author: K24DTCN210-NVMANH (02/12/2025 18:45)
     * --------------------------------------------------- */
    private HBox createFullWidthInfoItem(FontIcon icon, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefWidth(Region.USE_COMPUTED_SIZE);
        item.setMaxWidth(Double.MAX_VALUE);
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("exam-info-clean");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        
        item.getChildren().addAll(icon, textLabel);
        return item;
    }
    
    /* ---------------------------------------------------
     * Tạo info item với FontAwesome icon
     * @param icon FontIcon
     * @param text Text content
     * @returns HBox chứa icon và text
     * @author: K24DTCN210-NVMANH (02/12/2025 17:45)
     * --------------------------------------------------- */
    private HBox createIconInfoItem(FontIcon icon, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefWidth(180); // Tăng width để chứa datetime dài hơn
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("exam-info-clean");
        textLabel.setWrapText(true); // Cho phép wrap text nếu quá dài
        
        item.getChildren().addAll(icon, textLabel);
        return item;
    }
    
    /* ---------------------------------------------------
     * Tạo clean info item với emoji icon
     * @param emoji Emoji icon
     * @param text Text content
     * @returns HBox chứa emoji và text
     * @author: K24DTCN210-NVMANH (02/12/2025 17:30)
     * --------------------------------------------------- */
    private HBox createCleanInfoItem(String emoji, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefWidth(150);
        
        Label emojiLabel = new Label(emoji);
        emojiLabel.getStyleClass().add("exam-emoji");
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("exam-info-clean");
        
        item.getChildren().addAll(emojiLabel, textLabel);
        return item;
    }
    
    /* ---------------------------------------------------
     * Tạo simple action button
     * @param exam ExamInfoDTO
     * @returns Button
     * @author: K24DTCN210-NVMANH (02/12/2025 17:30)
     * --------------------------------------------------- */
    private Button createSimpleActionButton(ExamInfoDTO exam) {
        Button button = new Button();
        button.setPrefWidth(Double.MAX_VALUE);
        button.setPrefHeight(32);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            HBox content = new HBox(6);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLOCK, 14, IconFactory.COLOR_GRAY),
                new Label("Chưa tới giờ thi")
            );
            button.setGraphic(content);
            button.getStyleClass().add("exam-button-disabled");
            button.setDisable(true);
        } else if (now.isAfter(exam.getEndTime())) {
            HBox content = new HBox(6);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, 14, IconFactory.COLOR_DANGER),
                new Label("Đã kết thúc")
            );
            button.setGraphic(content);
            button.getStyleClass().add("exam-button-disabled");
            button.setDisable(true);
        } else {
            if (examManagementApiClient != null) {
                HBox content = new HBox(6);
                content.setAlignment(Pos.CENTER);
                content.getChildren().addAll(
                    IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EYE, 14, IconFactory.COLOR_WHITE),
                    new Label("Xem chi tiết")
                );
                button.setGraphic(content);
                button.getStyleClass().add("exam-button-secondary");
                button.setOnAction(e -> handleViewExamDetails(exam));
            } else {
                // Student mode - check eligibility and attempts
                boolean hasActiveSubmission = exam.getHasActiveSubmission() != null && exam.getHasActiveSubmission();
                
                // Chỉ disable nếu không eligible VÀ không có bài thi đang làm dở
                if (exam.getIsEligible() != null && !exam.getIsEligible() && !hasActiveSubmission) {
                    // Not eligible - show reason
                    HBox content = new HBox(6);
                    content.setAlignment(Pos.CENTER);
                    String reason = exam.getIneligibleReason() != null ? exam.getIneligibleReason() : "Không thể làm bài";
                    content.getChildren().addAll(
                        IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BAN, 14, IconFactory.COLOR_DANGER),
                        new Label(reason)
                    );
                    button.setGraphic(content);
                    button.getStyleClass().add("exam-button-disabled");
                    button.setDisable(true);
                } else if (isOutOfAttempts(exam) && !hasActiveSubmission) {
                    // Out of attempts AND no active submission
                    HBox content = new HBox(6);
                    content.setAlignment(Pos.CENTER);
                    content.getChildren().addAll(
                        IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, 14, IconFactory.COLOR_DANGER),
                        new Label("Đã hết lượt làm bài")
                    );
                    button.setGraphic(content);
                    button.getStyleClass().add("exam-button-disabled");
                    button.setDisable(true);
                } else {
                    // Can start or continue exam
                    HBox content = new HBox(6);
                    content.setAlignment(Pos.CENTER);
                    
                    String buttonText;
                    if (hasActiveSubmission) {
                        buttonText = "Tiếp tục làm bài";
                    } else if (exam.getAttemptsMade() != null && exam.getAttemptsMade() > 0) {
                        buttonText = "Làm lại bài thi";
                    } else {
                        buttonText = "Bắt đầu làm bài";
                    }
                    
                    content.getChildren().addAll(
                        IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY, 14, IconFactory.COLOR_WHITE),
                        new Label(buttonText)
                    );
                    button.setGraphic(content);
                    button.getStyleClass().add("exam-button-primary");
                    button.setOnAction(e -> handleStartExam(exam));
                }
            }
        }
        
        return button;
    }
    
    /* ---------------------------------------------------
     * Tạo ultra compact info row với icon nhỏ
     * @param icon FontIcon cho info
     * @param value Giá trị hiển thị
     * @returns HBox chứa icon và value
     * @author: K24DTCN210-NVMANH (02/12/2025 17:15)
     * --------------------------------------------------- */
    private HBox createUltraCompactInfoRow(FontIcon icon, String value) {
        HBox row = new HBox(3);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(100);
        
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("exam-info-ultra-compact");
        
        row.getChildren().addAll(icon, valueNode);
        return row;
    }
    
    /* ---------------------------------------------------
     * Tạo compact info row với icon
     * @param icon FontIcon cho info
     * @param value Giá trị hiển thị
     * @returns HBox chứa icon và value
     * @author: K24DTCN210-NVMANH (02/12/2025 16:51)
     * --------------------------------------------------- */
    private HBox createCompactInfoRow(FontIcon icon, String value) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("exam-info-compact");
        
        row.getChildren().addAll(icon, valueNode);
        return row;
    }
    
    /* ---------------------------------------------------
     * Tạo info row (label + value) - Legacy method for compatibility
     * @param label Nhãn
     * @param value Giá trị
     * @returns HBox chứa label và value
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("exam-info-label");
        labelNode.setMinWidth(120);
        
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("exam-info-value");
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    /* ---------------------------------------------------
     * Tạo countdown label nếu exam chưa bắt đầu - Compact version
     * @param exam ExamInfoDTO
     * @returns HBox hoặc null
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 16:51) - Compact version with icon
     * --------------------------------------------------- */
    private HBox createCountdownLabel(ExamInfoDTO exam) {
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            String timeRemaining = TimeFormatter.formatTimeRemaining(exam.getStartTime());
            
            HBox countdownBox = new HBox(6);
            countdownBox.setAlignment(Pos.CENTER_LEFT);
            countdownBox.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HOURGLASS_START, 12, IconFactory.COLOR_WARNING),
                new Label("Bắt đầu sau: " + timeRemaining)
            );
            countdownBox.getStyleClass().add("countdown-info");
            return countdownBox;
        }
        
        return null;
    }

    /* ---------------------------------------------------
     * Tạo compact action button cho layout mới
     * @param exam ExamInfoDTO
     * @returns Button
     * @author: K24DTCN210-NVMANH (02/12/2025 17:15)
     * --------------------------------------------------- */
    private Button createCompactActionButton(ExamInfoDTO exam) {
        Button button = new Button();
        button.setPrefWidth(120);
        button.setPrefHeight(35);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLOCK, 16, IconFactory.COLOR_GRAY),
                new Label("Chưa tới giờ")
            );
            button.setGraphic(content);
            button.getStyleClass().add("disabled-button-compact");
            button.setDisable(true);
        } else if (now.isAfter(exam.getEndTime())) {
            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, 16, IconFactory.COLOR_DANGER),
                new Label("Đã kết thúc")
            );
            button.setGraphic(content);
            button.getStyleClass().add("disabled-button-compact");
            button.setDisable(true);
        } else {
            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY, 16, IconFactory.COLOR_WHITE),
                new Label("Bắt đầu")
            );
            button.setGraphic(content);
            button.getStyleClass().add("start-exam-button-compact");
            button.setOnAction(e -> handleStartExam(exam));
        }
        
        return button;
    }
    
    /* ---------------------------------------------------
     * Tạo action button (Bắt đầu hoặc disabled) - Compact version
     * @param exam ExamInfoDTO
     * @returns Button
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 16:51) - Compact button with IKonli icons
     * --------------------------------------------------- */
    private Button createActionButton(ExamInfoDTO exam) {
        Button button = new Button();
        button.setPrefWidth(Double.MAX_VALUE);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(exam.getStartTime())) {
            HBox content = new HBox(5);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLOCK, 14, IconFactory.COLOR_GRAY),
                new Label("Chưa đến giờ thi")
            );
            button.setGraphic(content);
            button.getStyleClass().add("disabled-button");
            button.setDisable(true);
        } else if (now.isAfter(exam.getEndTime())) {
            HBox content = new HBox(5);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, 14, IconFactory.COLOR_DANGER),
                new Label("Đã kết thúc")
            );
            button.setGraphic(content);
            button.getStyleClass().add("disabled-button");
            button.setDisable(true);
        } else {
            HBox content = new HBox(5);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                IconFactory.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY, 14, IconFactory.COLOR_WHITE),
                new Label("Bắt đầu làm bài")
            );
            button.setGraphic(content);
            button.getStyleClass().add("start-exam-button");
            button.setOnAction(e -> handleStartExam(exam));
        }
        
        return button;
    }

    /* ---------------------------------------------------
     * Xử lý khi click "Bắt đầu làm bài"
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void handleStartExam(ExamInfoDTO exam) {
        logger.info("Starting exam: {}", exam.getExamId());
        
        // Show confirmation dialog with overlay
        Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();
        
        DialogUtils.showAlert(
            Alert.AlertType.CONFIRMATION,
            "Xác nhận bắt đầu thi",
            "Bạn có chắc muốn bắt đầu làm bài?",
            String.format(
                "Đề thi: %s\nThời gian: %d phút\n\nSau khi bắt đầu, thời gian sẽ bắt đầu đếm ngược.",
                exam.getTitle(),
                exam.getDurationMinutes()
            ),
            currentStage
        ).ifPresent(response -> {
            if (response == ButtonType.OK) {
                startExamSession(exam);
            }
        });
    }

    /* ---------------------------------------------------
     * Bắt đầu exam session (gọi API và chuyển màn hình)
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (23/11/2025 14:20)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 14:20) - Implement navigation to ExamTakingController
     * EditBy: K24DTCN210-NVMANH (24/11/2025 09:14) - Phase 8.6: Pass Stage to ExamTakingController
     * EditBy: K24DTCN210-NVMANH (24/11/2025 11:51) - Validate API before loading UI
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:03) - Phase 8.6: Use loading overlay instead of Alert
     * --------------------------------------------------- */
    private void startExamSession(ExamInfoDTO exam) {
        // Show loading overlay
        showLoading("Đang khởi tạo bài thi...");
        
        // Call API in background thread
        new Thread(() -> {
            try {
                logger.info("Calling startExam API for exam: {}", exam.getExamId());
                
                // 1. Call API FIRST to validate
                StartExamResponse response = examApiClient.startExam(exam.getExamId());
                
                // 2. If successful, navigate to exam screen on JavaFX thread
                Platform.runLater(() -> {
                    hideLoading();
                    navigateToExamScreen(exam, response);
                });
                
            } catch (ExamApiClient.ExamStartException e) {
                // Handle specific exam start errors
                logger.error("ExamStartException: {}", e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    handleExamStartError(e, exam);
                });
                
            } catch (IOException | InterruptedException e) {
                // Handle network errors
                logger.error("Network error during exam start", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi kết nối", 
                        "Không thể kết nối đến server. Vui lòng kiểm tra mạng và thử lại.");
                });
            } catch (Exception e) {
                // Handle unexpected errors
                logger.error("Unexpected error during exam start", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", 
                        "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Navigate to exam taking screen after API success
     * @param exam ExamInfoDTO
     * @param response StartExamResponse từ API (already called by ExamListController)
     * @author: K24DTCN210-NVMANH (24/11/2025 11:51)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 13:42) - Pass response để tránh double API call
     * --------------------------------------------------- */
    private void navigateToExamScreen(ExamInfoDTO exam, StartExamResponse response) {
        try {
            logger.info("Starting exam session for: {}", exam.getTitle());
            
            // 1. Load exam-taking.fxml
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/exam-taking.fxml")
            );
            Parent root = loader.load();
            
            // 2. Get ExamTakingController
            ExamTakingController controller = loader.getController();
            
            // 3. Get current stage
            Stage stage = (Stage) examCardsContainer.getScene().getWindow();
            
            // 4. Set stage to controller for full-screen support
            controller.setStage(stage);
            
            // 5. Pass user info to controller
            if (currentUserName != null) {
                // Use email as student code if available, otherwise fallback to generic code
                String code = currentUserEmail != null ? currentUserEmail : "STUDENT";
                controller.setUserInfo(currentUserName, code);
            }

            // 6. Initialize exam với response ĐÃ CÓ (không call API lần nữa!)
            String authToken = examApiClient.getAuthToken();
            controller.initializeExamWithResponse(response, authToken);
            
            // 7. Create new scene
            Scene scene = new Scene(root, 1400, 900);
            
            // 7. Load CSS
            String css = getClass().getResource("/css/exam-common.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // 8. Switch scene
            stage.setScene(scene);
            stage.setTitle("Làm bài thi: " + exam.getTitle());
            stage.setMaximized(true);
            
            logger.info("Successfully navigated to exam taking screen");
            
        } catch (IOException e) {
            logger.error("Failed to load exam-taking.fxml", e);
            showError("Lỗi tải giao diện", 
                "Không thể mở màn hình làm bài thi: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Handle exam start errors với friendly dialogs
     * @param e ExamStartException
     * @param exam ExamInfoDTO of the exam
     * @author: K24DTCN210-NVMANH (24/11/2025 11:40)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 12:17) - Added max attempts error handling
     * --------------------------------------------------- */
    private void handleExamStartError(ExamApiClient.ExamStartException e, ExamInfoDTO exam) {
        Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();

        if (e.isActiveSubmissionError()) {
            // User có submission đang active
            ButtonType contactTeacherBtn = new ButtonType("Liên hệ GV");
            ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            DialogUtils.showAlert(
                Alert.AlertType.WARNING,
                "Bài thi đang diễn ra",
                "Bạn đã có một bài thi đang làm dở",
                "Đề thi: " + exam.getTitle() + "\n\n" +
                "Bạn đã bắt đầu làm bài thi này trước đó và chưa nộp bài.\n" +
                "Vui lòng liên hệ giáo viên để được hỗ trợ hoặc reset bài thi.",
                currentStage,
                contactTeacherBtn, closeBtn
            );
            
        } else if (e.isMaxAttemptsError()) {
            // User đã hết số lần thi
            String message = e.getMessage();
            ButtonType contactTeacherBtn = new ButtonType("Liên hệ GV");
            ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            DialogUtils.showAlert(
                Alert.AlertType.ERROR,
                "Hết số lần thi",
                "Bạn đã hết số lần thi cho đề này",
                "Đề thi: " + exam.getTitle() + "\n\n" +
                message + "\n\n" +
                "Bạn đã sử dụng hết số lần thi được phép cho đề thi này.\n" +
                "Vui lòng liên hệ giáo viên nếu cần được thi lại.",
                currentStage,
                contactTeacherBtn, closeBtn
            );
            
        } else if (e.isTimeExpiredError()) {
            // Hết thời gian làm bài
            DialogUtils.showAlert(
                Alert.AlertType.WARNING,
                "Hết Thời Gian",
                "Thời gian làm bài đã hết",
                "Đề thi: " + exam.getTitle() + "\n\n" +
                "Bài thi đã kết thúc hoặc thời gian làm bài của bạn đã hết.\n" +
                "Hệ thống sẽ cập nhật lại danh sách đề thi.",
                currentStage
            );
            
            // Refresh list
            onRefresh();
            
        } else {
            // Other errors
            DialogUtils.showAlert(
                Alert.AlertType.ERROR,
                "Không thể bắt đầu bài thi",
                null,
                e.getMessage(),
                currentStage
            );
        }
    }

    /* ---------------------------------------------------
     * Filter by status
     * @param exam ExamInfoDTO
     * @param status Status filter value
     * @returns true nếu pass filter
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private boolean filterByStatus(ExamInfoDTO exam, String status) {
        if (status == null || status.equals("Tất cả trạng thái")) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case "Sắp diễn ra":
                return now.isBefore(exam.getStartTime());
            case "Đang diễn ra":
                return now.isAfter(exam.getStartTime()) && now.isBefore(exam.getEndTime());
            case "Đã kết thúc":
                return now.isAfter(exam.getEndTime());
            default:
                return true;
        }
    }

    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    @FXML
    private void onRefresh() {
        logger.info("Refreshing exam list");
        loadExams();
    }
    
    /* ---------------------------------------------------
     * Handle Create Exam button click - Launch wizard trong modal window
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleCreateExam() {
        if (stage == null) {
            logger.warn("Stage is null, cannot open wizard");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/wizard/exam-creation-wizard.fxml")
            );
            Parent wizardView = loader.load();
            
            // Get controller và set login response (chứa token)
            com.mstrust.client.teacher.controller.wizard.ExamCreationWizardController wizardController = 
                loader.getController();
            
            // Create LoginResponse với token từ examApiClient
            com.mstrust.client.exam.dto.LoginResponse loginResponse = 
                new com.mstrust.client.exam.dto.LoginResponse();
            loginResponse.setToken(examApiClient.getAuthToken());
            
            // Set login response cho wizard (wizard sẽ tự khởi tạo API client)
            wizardController.setLoginResponse(loginResponse);
            
            // Start wizard từ Step 1
            wizardController.startWizard();
            
            // Create modal stage for wizard
            Stage wizardStage = new Stage();
            wizardStage.setTitle("Tạo đề thi mới - Wizard");
            wizardStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            wizardStage.initOwner(stage);
            
            // Sử dụng kích thước từ FXML (1600x1000) và cho phép resize
            Scene wizardScene = new Scene(wizardView);
            wizardStage.setScene(wizardScene);
            wizardStage.setResizable(true);
            wizardStage.setMinWidth(1200);
            wizardStage.setMinHeight(700);
            
            // Show wizard và đợi đóng
            wizardStage.showAndWait();
            
            logger.info("Wizard đã đóng, refresh exam list");
            // Refresh exam list sau khi wizard đóng
            loadExams();
            
        } catch (IOException e) {
            logger.error("Failed to open exam creation wizard", e);
            DialogUtils.showError("Không thể mở wizard tạo đề thi", "Lỗi: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Update last refresh time label
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void updateLastRefreshTime() {
        String time = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        lastRefreshLabel.setText("Cập nhật lần cuối: " + time);
    }

    /* ---------------------------------------------------
     * Show loading overlay với message (Phase 8.6)
     * @param message Loading message
     * @author: K24DTCN210-NVMANH (25/11/2025 11:03)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                if (loadingMessage != null) {
                    loadingMessage.setText(message);
                }
                loadingOverlay.setVisible(true);
                loadingOverlay.toFront();
            });
        }
    }
    
    /* ---------------------------------------------------
     * Hide loading overlay (Phase 8.6)
     * @author: K24DTCN210-NVMANH (25/11/2025 11:03)
     * --------------------------------------------------- */
    private void hideLoading() {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
            });
        }
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param title Tiêu đề
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (23/11/2025 12:05)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();
        DialogUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", title, message, currentStage);
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo thông tin
     * @param title Tiêu đề
     * @param message Nội dung
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();
        DialogUtils.showAlert(Alert.AlertType.INFORMATION, "Thông tin", title, message, currentStage);
    }
    
    /* ---------------------------------------------------
     * Setup user info và role-based UI
     * @param userName Tên người dùng
     * @param role Vai trò (STUDENT hoặc ROLE_STUDENT)
     * @author: K24DTCN210-NVMANH (01/12/2025 00:30)
     * --------------------------------------------------- */
    public void setupUserInfo(String userName, String role) {
        // Normalize role for display: remove ROLE_ prefix nếu có
        final String displayRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        
        Platform.runLater(() -> {
            if (userLabel != null) {
                userLabel.setText(userName);
            }
            if (roleLabel != null) {
                roleLabel.setText("[" + displayRole + "]");
            }
        });
    }
    
    /* ---------------------------------------------------
     * Handle Logout button click
     * @author: K24DTCN210-NVMANH (01/12/2025 00:30)
     * --------------------------------------------------- */
    @FXML
    private void handleLogout() {
        Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();
        Optional<ButtonType> result = DialogUtils.showAlert(
            Alert.AlertType.CONFIRMATION,
            "Xác nhận Đăng xuất",
            "Bạn có chắc muốn đăng xuất?",
            "Phiên làm việc hiện tại sẽ kết thúc.",
            currentStage
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            backToLogin();
        }
    }
    
    /* ---------------------------------------------------
     * Quay lại màn hình login
     * @author: K24DTCN210-NVMANH (01/12/2025 00:30)
     * --------------------------------------------------- */
    private void backToLogin() {
        try {
            // Get application instance from stage userData
            Stage currentStage = (Stage) examCardsContainer.getScene().getWindow();
            Object userData = currentStage.getUserData();
            
            if (userData instanceof com.mstrust.client.exam.ExamClientApplication) {
                com.mstrust.client.exam.ExamClientApplication application = 
                    (com.mstrust.client.exam.ExamClientApplication) userData;
                application.showLoginScreen();
                logger.info("Logged out and returned to login screen");
            } else {
                // Fallback: load login screen manually
                logger.warn("Application instance not found in stage userData, using fallback");
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml"));
                Parent root = loader.load();
                
                com.mstrust.client.exam.controller.LoginController controller = loader.getController();
                controller.initialize(examApiClient, currentStage);
                
                Scene scene = new Scene(root, 400, 500);
                
                // Apply CSS
                try {
                    String css = getClass().getResource("/css/exam-common.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } catch (Exception e) {
                    logger.warn("Could not load CSS");
                }
                
                currentStage.setScene(scene);
                currentStage.setResizable(false);
                currentStage.show();
            }
        } catch (IOException e) {
            logger.error("Failed to return to login screen", e);
            showError("Lỗi", "Không thể quay lại màn hình đăng nhập: " + e.getMessage());
        }
    }

}
