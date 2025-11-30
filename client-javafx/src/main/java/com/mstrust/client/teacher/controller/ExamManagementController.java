package com.mstrust.client.teacher.controller;

import com.mstrust.client.exam.dto.ExamInfoDTO;
import com.mstrust.client.exam.util.TimeFormatter;
import com.mstrust.client.exam.util.IconFactory;
import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mstrust.client.teacher.api.ExamManagementApiClient.ApiException;
import com.mstrust.client.teacher.api.SubjectApiClient;
import com.mstrust.client.teacher.dto.SubjectDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho Exam Management Screen - Teacher/Admin
 * Quản lý hiển thị và thao tác với danh sách đề thi cho giáo viên
 * @author: K24DTCN210-NVMANH (30/11/2025)
 * --------------------------------------------------- */
public class ExamManagementController {
    private static final Logger logger = LoggerFactory.getLogger(ExamManagementController.class);
    
    private ExamManagementApiClient examManagementApiClient;
    private SubjectApiClient subjectApiClient;
    private List<ExamInfoDTO> allExams;
    private List<ExamInfoDTO> filteredExams;
    private Map<Long, ExamDTO> examDTOMap; // Map để lưu ExamDTO gốc theo ID
    private Stage stage; // Stage reference để mở wizard modal
    
    // FXML Components
    @FXML private ComboBox<String> subjectFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> publishedFilterCombo;
    @FXML private Button refreshButton;
    @FXML private Button createExamButton;
    @FXML private VBox examCardsContainer;
    @FXML private VBox emptyStateBox;
    @FXML private Label examCountLabel;
    @FXML private Label lastRefreshLabel;
    
    // Loading overlay components
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingMessage;
    
    /* ---------------------------------------------------
     * Initialize - được gọi sau khi FXML loaded
     * @param apiClient ExamManagementApiClient với auth token
     * @param stage Stage reference để mở wizard modal
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public void initialize(ExamManagementApiClient apiClient, Stage stage) {
        this.examManagementApiClient = apiClient;
        this.stage = stage;
        this.examDTOMap = new HashMap<>();
        
        // Initialize SubjectApiClient
        this.subjectApiClient = new SubjectApiClient("http://localhost:8080/api");
        if (apiClient != null && apiClient.getAuthToken() != null) {
            this.subjectApiClient.setAuthToken(apiClient.getAuthToken());
        }
        
        logger.info("Initializing ExamManagementController");
        
        // Setup filters
        setupFilters();
        
        // Button "Tạo đề thi" luôn hiển thị cho teacher
        // Sử dụng Platform.runLater để đảm bảo FXML đã load xong
        Platform.runLater(() -> {
            if (createExamButton != null) {
                createExamButton.setVisible(true);
                createExamButton.setManaged(true);
                createExamButton.setDisable(false);
                createExamButton.setMinWidth(130);
                createExamButton.setPrefWidth(130);
                // Force style để đảm bảo button hiển thị
                createExamButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5px; -fx-cursor: hand;");
                logger.info("Create exam button set to visible - text: {}, visible: {}, managed: {}, width: {}, parent: {}", 
                    createExamButton.getText(), 
                    createExamButton.isVisible(), 
                    createExamButton.isManaged(),
                    createExamButton.getWidth(),
                    createExamButton.getParent() != null ? createExamButton.getParent().getClass().getSimpleName() : "null");
            } else {
                logger.error("createExamButton is NULL in ExamManagementController!");
            }
        });
        
        // Load initial data
        loadExams();
    }
    
    /* ---------------------------------------------------
     * Setup các combo box filters
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Load subjects từ API
     * --------------------------------------------------- */
    private void setupFilters() {
        // Subject filter - Load từ API
        subjectFilterCombo.getItems().add("Tất cả môn học");
        subjectFilterCombo.setValue("Tất cả môn học");
        subjectFilterCombo.setOnAction(e -> applyFilters());
        
        // Load subjects từ API trong background
        new Thread(() -> {
            try {
                List<SubjectDTO> subjects = subjectApiClient.getAllSubjects();
                Platform.runLater(() -> {
                    for (SubjectDTO subject : subjects) {
                        subjectFilterCombo.getItems().add(subject.getSubjectName());
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to load subjects for filter", e);
            }
        }).start();
        
        // Status filter
        statusFilterCombo.getItems().addAll("Tất cả trạng thái", "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc");
        statusFilterCombo.setValue("Tất cả trạng thái");
        statusFilterCombo.setOnAction(e -> applyFilters());
        
        // Published filter
        publishedFilterCombo.getItems().addAll("Tất cả", "Đã xuất bản", "Chưa xuất bản");
        publishedFilterCombo.setValue("Tất cả");
        publishedFilterCombo.setOnAction(e -> applyFilters());
    }
    
    /* ---------------------------------------------------
     * Load danh sách đề thi từ backend
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void loadExams() {
        refreshButton.setDisable(true);
        showLoading("Đang tải danh sách đề thi...");
        
        new Thread(() -> {
            try {
                logger.info("Loading exams for teacher...");
                List<ExamDTO> examDTOs = examManagementApiClient.getAllExams();
                
                // Clear và rebuild examDTOMap
                examDTOMap.clear();
                for (ExamDTO dto : examDTOs) {
                    examDTOMap.put(dto.getId(), dto);
                }
                
                // Convert ExamDTO sang ExamInfoDTO
                allExams = examDTOs.stream()
                    .map(this::convertToExamInfoDTO)
                    .collect(Collectors.toList());
                
                filteredExams = allExams;
                
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
        info.setStatus(examDTO.getCurrentStatus() != null ? examDTO.getCurrentStatus() : "UNKNOWN");
        info.setSubjectCode(examDTO.getSubjectClassName());
        info.setSubjectName(examDTO.getSubjectName());
        info.setCanStart(false); // Teacher không làm bài
        return info;
    }
    
    /* ---------------------------------------------------
     * Apply filters
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm filter theo subject và published status
     * --------------------------------------------------- */
    private void applyFilters() {
        if (allExams == null) return;
        
        filteredExams = allExams.stream()
            .filter(exam -> {
                // Subject filter
                String subjectFilter = subjectFilterCombo.getValue();
                if (subjectFilter != null && !subjectFilter.equals("Tất cả môn học")) {
                    if (!subjectFilter.equals(exam.getSubjectName())) {
                        return false;
                    }
                }
                
                // Status filter
                String statusFilter = statusFilterCombo.getValue();
                if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái")) {
                    LocalDateTime now = LocalDateTime.now();
                    if (statusFilter.equals("Sắp diễn ra") && !now.isBefore(exam.getStartTime())) {
                        return false;
                    }
                    if (statusFilter.equals("Đang diễn ra") && 
                        (now.isBefore(exam.getStartTime()) || now.isAfter(exam.getEndTime()))) {
                        return false;
                    }
                    if (statusFilter.equals("Đã kết thúc") && !now.isAfter(exam.getEndTime())) {
                        return false;
                    }
                }
                
                // Published filter
                String publishedFilter = publishedFilterCombo.getValue();
                if (publishedFilter != null && !publishedFilter.equals("Tất cả")) {
                    ExamDTO examDTO = examDTOMap.get(exam.getId());
                    if (examDTO != null) {
                        Boolean isPublished = examDTO.getIsPublished();
                        if (publishedFilter.equals("Đã xuất bản") && (isPublished == null || !isPublished)) {
                            return false;
                        }
                        if (publishedFilter.equals("Chưa xuất bản") && (isPublished != null && isPublished)) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        displayExams();
    }
    
    /* ---------------------------------------------------
     * Hiển thị danh sách exam cards
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void displayExams() {
        examCardsContainer.getChildren().clear();
        
        if (filteredExams == null || filteredExams.isEmpty()) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            examCountLabel.setText("Tìm thấy 0 đề thi");
        } else {
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);
            
            for (ExamInfoDTO exam : filteredExams) {
                HBox card = createExamCard(exam);
                examCardsContainer.getChildren().add(card);
            }
            
            examCountLabel.setText(String.format("Tìm thấy %d đề thi", filteredExams.size()));
        }
    }
    
    /* ---------------------------------------------------
     * Tạo exam card cho một đề thi - Design mới gọn gàng, hiện đại
     * @param exam ExamInfoDTO
     * @returns HBox chứa card UI (layout ngang để tiết kiệm không gian)
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Redesign compact card
     * --------------------------------------------------- */
    private HBox createExamCard(ExamInfoDTO exam) {
        HBox card = new HBox(12);
        card.getStyleClass().add("exam-card-compact");
        card.setPrefWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        
        // Left section: Main info
        VBox leftSection = new VBox(6);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        // Title row với status badge
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(exam.getTitle());
        titleLabel.getStyleClass().add("exam-title-compact");
        titleLabel.setWrapText(false);
        titleLabel.setMaxWidth(400);
        titleLabel.setEllipsisString("...");
        
        Label statusBadge = createStatusBadge(exam);
        
        titleRow.getChildren().addAll(titleLabel, statusBadge);
        
        // Info row - compact inline
        HBox infoRow = new HBox(16);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        // Subject
        Label subjectLabel = new Label("📚 " + exam.getSubjectName());
        subjectLabel.getStyleClass().add("exam-info-compact");
        
        // Time
        Label timeLabel = new Label("⏰ " + TimeFormatter.formatDateTime(exam.getStartTime()));
        timeLabel.getStyleClass().add("exam-info-compact");
        
        // Duration
        String duration = exam.getDurationMinutes() != null 
            ? TimeFormatter.formatDuration(exam.getDurationMinutes())
            : "N/A";
        Label durationLabel = new Label("⏱️ " + duration);
        durationLabel.getStyleClass().add("exam-info-compact");
        
        // Questions count
        Label questionsLabel = new Label("📝 " + exam.getTotalQuestions() + " câu");
        questionsLabel.getStyleClass().add("exam-info-compact");
        
        infoRow.getChildren().addAll(subjectLabel, timeLabel, durationLabel, questionsLabel);
        
        leftSection.getChildren().addAll(titleRow, infoRow);
        
        // Right section: Action buttons (compact)
        HBox actionButtons = createCompactActionButtons(exam);
        
        // Add sections to card
        card.getChildren().addAll(leftSection, actionButtons);
        
        return card;
    }
    
    /* ---------------------------------------------------
     * Tạo action buttons compact (icon buttons nhỏ gọn với FontIcon)
     * @param exam ExamInfoDTO
     * @returns HBox chứa các action buttons
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Dùng FontIcon thay vì emoji
     * --------------------------------------------------- */
    private HBox createCompactActionButtons(ExamInfoDTO exam) {
        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // View Details button
        Button viewButton = new Button();
        viewButton.setGraphic(IconFactory.createViewIcon());
        viewButton.getStyleClass().add("icon-button");
        viewButton.setTooltip(new Tooltip("Chi tiết"));
        viewButton.setOnAction(e -> handleViewExamDetails(exam));
        
        // Edit button
        Button editButton = new Button();
        editButton.setGraphic(IconFactory.createEditIconForButton());
        editButton.getStyleClass().add("icon-button");
        editButton.setTooltip(new Tooltip("Sửa"));
        editButton.setOnAction(e -> handleEditExam(exam));
        
        // Publish/Unpublish button
        Button publishButton = new Button();
        ExamDTO examDTO = examDTOMap.get(exam.getId());
        boolean isPublished = examDTO != null && examDTO.getIsPublished() != null && examDTO.getIsPublished();
        if (isPublished) {
            publishButton.setGraphic(IconFactory.createLockIconForButton());
            publishButton.setTooltip(new Tooltip("Ẩn đề thi"));
            publishButton.setOnAction(e -> handleUnpublishExam(exam));
        } else {
            publishButton.setGraphic(IconFactory.createPublishIcon());
            publishButton.setTooltip(new Tooltip("Xuất bản"));
            publishButton.setOnAction(e -> handlePublishExam(exam));
        }
        publishButton.getStyleClass().add("icon-button");
        
        // Delete button
        Button deleteButton = new Button();
        deleteButton.setGraphic(IconFactory.createDeleteIconForButton());
        deleteButton.getStyleClass().add("icon-button-danger");
        deleteButton.setTooltip(new Tooltip("Xóa"));
        deleteButton.setOnAction(e -> handleDeleteExam(exam));
        
        buttonContainer.getChildren().addAll(viewButton, editButton, publishButton, deleteButton);
        return buttonContainer;
    }
    
    /* ---------------------------------------------------
     * Tạo status badge cho exam - compact version
     * @param exam ExamInfoDTO
     * @returns Label styled as badge
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Compact badge design
     * --------------------------------------------------- */
    private Label createStatusBadge(ExamInfoDTO exam) {
        Label badge = new Label();
        badge.getStyleClass().add("status-badge-compact");
        
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
     * Tạo info row (label + value)
     * @param label Nhãn
     * @param value Giá trị
     * @returns HBox chứa label và value
     * @author: K24DTCN210-NVMANH (30/11/2025)
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
     * Tạo action buttons cho teacher mode (Edit, Delete, Publish/Unpublish)
     * @param exam ExamInfoDTO
     * @returns HBox chứa các action buttons
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Dùng isPublished từ ExamDTO
     * --------------------------------------------------- */
    private HBox createTeacherActionButtons(ExamInfoDTO exam) {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // View Details button
        Button viewButton = new Button("👁️ Chi tiết");
        viewButton.getStyleClass().add("secondary-button");
        viewButton.setOnAction(e -> handleViewExamDetails(exam));
        
        // Edit button
        Button editButton = new Button("✏️ Sửa");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> handleEditExam(exam));
        
        // Publish/Unpublish button - dùng isPublished từ ExamDTO
        Button publishButton = new Button();
        ExamDTO examDTO = examDTOMap.get(exam.getId());
        boolean isPublished = examDTO != null && examDTO.getIsPublished() != null && examDTO.getIsPublished();
        if (isPublished) {
            publishButton.setText("🔒 Ẩn");
            publishButton.setOnAction(e -> handleUnpublishExam(exam));
        } else {
            publishButton.setText("📢 Xuất bản");
            publishButton.setOnAction(e -> handlePublishExam(exam));
        }
        publishButton.getStyleClass().add("secondary-button");
        
        // Delete button
        Button deleteButton = new Button("🗑️ Xóa");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(e -> handleDeleteExam(exam));
        
        buttonContainer.getChildren().addAll(viewButton, editButton, publishButton, deleteButton);
        return buttonContainer;
    }
    
    /* ---------------------------------------------------
     * Handler cho View Exam Details
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Implement detail dialog
     * --------------------------------------------------- */
    private void handleViewExamDetails(ExamInfoDTO exam) {
        logger.info("View exam details: {}", exam.getId());
        
        // Get full ExamDTO
        ExamDTO examDTO = examDTOMap.get(exam.getId());
        if (examDTO == null) {
            // Load from API if not in map
            showLoading("Đang tải chi tiết đề thi...");
            new Thread(() -> {
                try {
                    ExamDTO fullExam = examManagementApiClient.getExamById(exam.getId());
                    examDTOMap.put(fullExam.getId(), fullExam);
                    Platform.runLater(() -> {
                        hideLoading();
                        showExamDetailsDialog(fullExam);
                    });
                } catch (Exception e) {
                    logger.error("Failed to load exam details", e);
                    Platform.runLater(() -> {
                        hideLoading();
                        showError("Lỗi", "Không thể tải chi tiết đề thi: " + e.getMessage());
                    });
                }
            }).start();
        } else {
            showExamDetailsDialog(examDTO);
        }
    }
    
    /* ---------------------------------------------------
     * Hiển thị dialog chi tiết đề thi
     * @param examDTO ExamDTO đầy đủ
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showExamDetailsDialog(ExamDTO examDTO) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Chi tiết đề thi");
        detailsAlert.setHeaderText(examDTO.getTitle());
        
        StringBuilder content = new StringBuilder();
        content.append("📚 Môn học: ").append(examDTO.getSubjectName()).append("\n");
        content.append("📝 Lớp: ").append(examDTO.getSubjectClassName() != null ? examDTO.getSubjectClassName() : "N/A").append("\n");
        content.append("⏰ Thời gian bắt đầu: ").append(TimeFormatter.formatDateTime(examDTO.getStartTime())).append("\n");
        content.append("⏰ Thời gian kết thúc: ").append(TimeFormatter.formatDateTime(examDTO.getEndTime())).append("\n");
        content.append("⏱️ Thời lượng: ").append(examDTO.getDurationMinutes() != null ? TimeFormatter.formatDuration(examDTO.getDurationMinutes()) : "N/A").append("\n");
        content.append("📝 Số câu hỏi: ").append(examDTO.getQuestionCount() != null ? examDTO.getQuestionCount() : 0).append("\n");
        content.append("📊 Tổng điểm: ").append(examDTO.getTotalScore() != null ? examDTO.getTotalScore() : "0").append("\n");
        content.append("📢 Trạng thái xuất bản: ").append(examDTO.getIsPublished() != null && examDTO.getIsPublished() ? "Đã xuất bản" : "Chưa xuất bản").append("\n");
        content.append("📋 Mục đích: ").append(examDTO.getExamPurpose() != null ? examDTO.getExamPurpose().toString() : "N/A").append("\n");
        
        if (examDTO.getDescription() != null && !examDTO.getDescription().isEmpty()) {
            content.append("\n📄 Mô tả:\n").append(examDTO.getDescription());
        }
        
        detailsAlert.setContentText(content.toString());
        detailsAlert.setResizable(true);
        detailsAlert.getDialogPane().setPrefWidth(600);
        detailsAlert.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Handler cho Edit Exam
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Load exam và mở wizard edit mode
     * --------------------------------------------------- */
    private void handleEditExam(ExamInfoDTO exam) {
        logger.info("Edit exam: {}", exam.getId());
        
        // Check if exam is published
        ExamDTO examDTO = examDTOMap.get(exam.getId());
        if (examDTO != null && examDTO.getIsPublished() != null && examDTO.getIsPublished()) {
            showError("Không thể sửa", "Không thể sửa đề thi đã xuất bản. Vui lòng ẩn đề thi trước khi sửa.");
            return;
        }
        
        if (stage == null) {
            logger.warn("Stage is null, cannot open wizard");
            showError("Lỗi", "Không thể mở wizard sửa đề thi.");
            return;
        }
        
        // Show loading
        showLoading("Đang tải thông tin đề thi...");
        
        // Load full exam details in background
        new Thread(() -> {
            try {
                ExamDTO fullExam = examManagementApiClient.getExamById(exam.getId());
                examDTOMap.put(fullExam.getId(), fullExam);
                
                Platform.runLater(() -> {
                    hideLoading();
                    // TODO: Mở wizard với edit mode
                    // Hiện tại wizard chưa hỗ trợ edit mode, hiển thị thông báo
                    showInfo("Sửa đề thi", 
                        "Chức năng sửa đề thi đang được phát triển.\n" +
                        "Wizard edit mode sẽ được thêm vào trong phiên bản tiếp theo.");
                });
                
            } catch (Exception e) {
                logger.error("Failed to load exam for editing", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Không thể tải thông tin đề thi: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handler cho Publish Exam
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Implement API call
     * --------------------------------------------------- */
    private void handlePublishExam(ExamInfoDTO exam) {
        logger.info("Publish exam: {}", exam.getId());
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xuất bản");
        confirmAlert.setHeaderText("Bạn có chắc muốn xuất bản đề thi này?");
        confirmAlert.setContentText(String.format(
            "Đề thi: %s\n\nSau khi xuất bản, học sinh sẽ có thể thấy và làm bài thi này.",
            exam.getTitle()
        ));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        // Show loading
        showLoading("Đang xuất bản đề thi...");
        
        // Call API in background
        new Thread(() -> {
            try {
                ExamDTO updatedExam = examManagementApiClient.publishExam(exam.getId());
                
                // Update examDTOMap
                examDTOMap.put(updatedExam.getId(), updatedExam);
                
                Platform.runLater(() -> {
                    hideLoading();
                    showInfo("Thành công", "Đề thi đã được xuất bản thành công!");
                    // Refresh exam list
                    loadExams();
                });
                
            } catch (ApiException e) {
                logger.error("Failed to publish exam: {}", e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    String errorMsg = "Không thể xuất bản đề thi.\n";
                    if (e.getStatusCode() == 400) {
                        errorMsg += "Đề thi không đáp ứng điều kiện xuất bản (cần có ít nhất 1 câu hỏi, thời gian bắt đầu phải trong tương lai).";
                    } else if (e.getStatusCode() == 404) {
                        errorMsg += "Không tìm thấy đề thi.";
                    } else {
                        errorMsg += "Lỗi: " + e.getMessage();
                    }
                    showError("Lỗi xuất bản", errorMsg);
                });
            } catch (IOException e) {
                logger.error("Network error during publish", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi kết nối", "Không thể kết nối đến server. Vui lòng kiểm tra mạng và thử lại.");
                });
            } catch (Exception e) {
                logger.error("Unexpected error during publish", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handler cho Unpublish Exam
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Implement API call
     * --------------------------------------------------- */
    private void handleUnpublishExam(ExamInfoDTO exam) {
        logger.info("Unpublish exam: {}", exam.getId());
        
        // Check if exam is ongoing
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getStartTime()) && now.isBefore(exam.getEndTime())) {
            showError("Không thể ẩn", "Không thể ẩn đề thi đang diễn ra. Vui lòng đợi đến khi đề thi kết thúc.");
            return;
        }
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận ẩn đề thi");
        confirmAlert.setHeaderText("Bạn có chắc muốn ẩn đề thi này?");
        confirmAlert.setContentText(String.format(
            "Đề thi: %s\n\nSau khi ẩn, học sinh sẽ không thể thấy đề thi này nữa.",
            exam.getTitle()
        ));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        // Show loading
        showLoading("Đang ẩn đề thi...");
        
        // Call API in background
        new Thread(() -> {
            try {
                ExamDTO updatedExam = examManagementApiClient.unpublishExam(exam.getId());
                
                // Update examDTOMap
                examDTOMap.put(updatedExam.getId(), updatedExam);
                
                Platform.runLater(() -> {
                    hideLoading();
                    showInfo("Thành công", "Đề thi đã được ẩn thành công!");
                    // Refresh exam list
                    loadExams();
                });
                
            } catch (ApiException e) {
                logger.error("Failed to unpublish exam: {}", e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    String errorMsg = "Không thể ẩn đề thi.\n";
                    if (e.getStatusCode() == 400) {
                        errorMsg += "Không thể ẩn đề thi đang diễn ra.";
                    } else if (e.getStatusCode() == 404) {
                        errorMsg += "Không tìm thấy đề thi.";
                    } else {
                        errorMsg += "Lỗi: " + e.getMessage();
                    }
                    showError("Lỗi ẩn đề thi", errorMsg);
                });
            } catch (IOException e) {
                logger.error("Network error during unpublish", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi kết nối", "Không thể kết nối đến server. Vui lòng kiểm tra mạng và thử lại.");
                });
            } catch (Exception e) {
                logger.error("Unexpected error during unpublish", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handler cho Delete Exam
     * @param exam ExamInfoDTO
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Implement API call
     * --------------------------------------------------- */
    private void handleDeleteExam(ExamInfoDTO exam) {
        logger.info("Delete exam: {}", exam.getId());
        
        // Check if exam is published
        ExamDTO examDTO = examDTOMap.get(exam.getId());
        if (examDTO != null && examDTO.getIsPublished() != null && examDTO.getIsPublished()) {
            showError("Không thể xóa", "Không thể xóa đề thi đã xuất bản. Vui lòng ẩn đề thi trước khi xóa.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa đề thi này?");
        alert.setContentText(String.format(
            "Đề thi: %s\n\nHành động này không thể hoàn tác.",
            exam.getTitle()
        ));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        // Show loading
        showLoading("Đang xóa đề thi...");
        
        // Call API in background
        new Thread(() -> {
            try {
                examManagementApiClient.deleteExam(exam.getId());
                
                // Remove from map
                examDTOMap.remove(exam.getId());
                
                Platform.runLater(() -> {
                    hideLoading();
                    showInfo("Thành công", "Đề thi đã được xóa thành công!");
                    // Refresh exam list
                    loadExams();
                });
                
            } catch (ApiException e) {
                logger.error("Failed to delete exam: {}", e.getMessage());
                Platform.runLater(() -> {
                    hideLoading();
                    String errorMsg = "Không thể xóa đề thi.\n";
                    if (e.getStatusCode() == 400) {
                        errorMsg += "Không thể xóa đề thi đã xuất bản.";
                    } else if (e.getStatusCode() == 404) {
                        errorMsg += "Không tìm thấy đề thi.";
                    } else {
                        errorMsg += "Lỗi: " + e.getMessage();
                    }
                    showError("Lỗi xóa đề thi", errorMsg);
                });
            } catch (IOException e) {
                logger.error("Network error during delete", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi kết nối", "Không thể kết nối đến server. Vui lòng kiểm tra mạng và thử lại.");
                });
            } catch (Exception e) {
                logger.error("Unexpected error during delete", e);
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
                });
            }
        }).start();
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
            
            // Create LoginResponse với token từ examManagementApiClient
            com.mstrust.client.exam.dto.LoginResponse loginResponse = 
                new com.mstrust.client.exam.dto.LoginResponse();
            loginResponse.setToken(examManagementApiClient.getAuthToken());
            
            // Set login response cho wizard (wizard sẽ tự khởi tạo API client)
            wizardController.setLoginResponse(loginResponse);
            
            // Create modal stage for wizard
            Stage wizardStage = new Stage();
            
            // Set wizard stage reference vào controller (để có thể đóng khi cancel)
            wizardController.setWizardStage(wizardStage);
            
            // Start wizard từ Step 1
            wizardController.startWizard();
            wizardStage.setTitle("Tạo đề thi mới - Wizard");
            wizardStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            wizardStage.initOwner(stage);
            
            Scene wizardScene = new Scene(wizardView);
            wizardStage.setScene(wizardScene);
            wizardStage.setResizable(true);
            wizardStage.setMinWidth(1200);
            wizardStage.setMinHeight(700);
            
            // Add listener để refresh khi wizard đóng (kể cả khi cancel hoặc đóng bằng X button)
            wizardStage.setOnCloseRequest(e -> {
                logger.info("Wizard đang đóng (close request), sẽ refresh exam list");
            });
            
            // Show wizard và đợi đóng (bất kể là cancel hay submit)
            wizardStage.showAndWait();
            
            logger.info("Wizard đã đóng (cancel hoặc submit), refresh exam list");
            // Refresh exam list sau khi wizard đóng (kể cả khi cancel)
            loadExams();
            
        } catch (IOException e) {
            logger.error("Failed to open exam creation wizard", e);
            showError("Lỗi mở Wizard", 
                    "Không thể mở wizard tạo đề thi.\n" +
                    "Lỗi: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Handle refresh button click
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    @FXML
    private void onRefresh() {
        logger.info("Refreshing exam list");
        loadExams();
    }
    
    /* ---------------------------------------------------
     * Update last refresh time label
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void updateLastRefreshTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lastRefreshLabel.setText("Cập nhật lần cuối: " + time);
    }
    
    /* ---------------------------------------------------
     * Show loading overlay
     * @param message Loading message
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        Platform.runLater(() -> {
            if (loadingMessage != null) {
                loadingMessage.setText(message);
            }
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.toFront();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Hide loading overlay
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void hideLoading() {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(false);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Show error dialog
     * @param title Tiêu đề
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Hiển thị thông báo thông tin
     * @param title Tiêu đề
     * @param message Nội dung
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông tin");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}

