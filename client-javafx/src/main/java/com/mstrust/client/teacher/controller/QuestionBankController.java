package com.mstrust.client.teacher.controller;

import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.dto.QuestionBankDTO;
import com.mstrust.client.teacher.dto.Difficulty;
import com.mstrust.client.teacher.dto.SubjectDTO;
import com.mstrust.client.exam.dto.QuestionType;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho Question Bank Management UI
 * Quản lý danh sách câu hỏi với pagination, filters, CRUD operations
 * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
 * --------------------------------------------------- */
public class QuestionBankController {
    
    // FXML Components - Filters
    @FXML private ComboBox<String> subjectFilter;
    @FXML private ComboBox<Difficulty> difficultyFilter;
    @FXML private ComboBox<QuestionType> typeFilter;
    @FXML private TextField searchField;
    
    // FXML Components - Buttons
    @FXML private Button createButton;
    @FXML private Button importButton;
    @FXML private Button exportButton;
    
    // FXML Components - Statistics
    @FXML private Label totalQuestionsLabel;
    @FXML private Label easyCountLabel;
    @FXML private Label mediumCountLabel;
    @FXML private Label hardCountLabel;
    
    // FXML Components - Table
    @FXML private TableView<QuestionBankDTO> questionsTable;
    @FXML private TableColumn<QuestionBankDTO, String> idColumn;
    @FXML private TableColumn<QuestionBankDTO, String> questionTextColumn;
    @FXML private TableColumn<QuestionBankDTO, String> subjectColumn;
    @FXML private TableColumn<QuestionBankDTO, String> typeColumn;
    @FXML private TableColumn<QuestionBankDTO, String> difficultyColumn;
    @FXML private TableColumn<QuestionBankDTO, String> tagsColumn;
    @FXML private TableColumn<QuestionBankDTO, String> usageCountColumn;
    @FXML private TableColumn<QuestionBankDTO, String> createdByColumn;
    @FXML private TableColumn<QuestionBankDTO, Void> actionsColumn;
    
    // FXML Components - Pagination
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<Integer> pageSizeComboBox;
    
    // FXML Components - Loading
    @FXML private StackPane loadingPane;
    
    // Service
    private QuestionBankApiClient apiClient;
    
    // State
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 0;
    private long totalElements = 0;
    private Stage primaryStage;
    private List<SubjectDTO> subjects = new ArrayList<>();
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API client và stage
     * @param apiClient QuestionBankApiClient instance
     * @param primaryStage Stage chính của application
     * @author: K24DTCN210-NVMANH (25/11/2025 23:03)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 23:03) - Thêm primaryStage parameter
     * --------------------------------------------------- */
    public void initialize(QuestionBankApiClient apiClient, Stage primaryStage) {
        this.apiClient = apiClient;
        this.primaryStage = primaryStage;
        
        // Setup UI components
        setupFilters();
        setupTable();
        setupPagination();
        
        // Load subjects
        loadSubjects();
        
        // Load initial data
        loadQuestions();
    }
    
    /* ---------------------------------------------------
     * Setup các filter dropdowns
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 23:03) - Remove hardcoded subjects
     * --------------------------------------------------- */
    private void setupFilters() {
        // Difficulty filter
        ObservableList<Difficulty> difficulties = FXCollections.observableArrayList(
            Arrays.asList(Difficulty.values())
        );
        difficultyFilter.setItems(difficulties);
        
        // Type filter
        ObservableList<QuestionType> types = FXCollections.observableArrayList(
            Arrays.asList(QuestionType.values())
        );
        typeFilter.setItems(types);
    }
    
    /* ---------------------------------------------------
     * Load danh sách môn học từ backend
     * @author: K24DTCN210-NVMANH (25/11/2025 23:03)
     * --------------------------------------------------- */
    private void loadSubjects() {
        // TODO: Implement API call to get subjects
        // For now, use mock data
        subjects = Arrays.asList(
            new SubjectDTO(1L, "MATH", "Toán học"),
            new SubjectDTO(2L, "PHY", "Vật lý"),
            new SubjectDTO(3L, "CHEM", "Hóa học"),
            new SubjectDTO(4L, "BIO", "Sinh học"),
            new SubjectDTO(5L, "HIST", "Lịch sử"),
            new SubjectDTO(6L, "GEO", "Địa lý"),
            new SubjectDTO(7L, "ENG", "Tiếng Anh"),
            new SubjectDTO(8L, "LIT", "Ngữ văn")
        );
        
        ObservableList<String> subjectNames = FXCollections.observableArrayList(
            subjects.stream().map(SubjectDTO::getSubjectName).collect(Collectors.toList())
        );
        subjectFilter.setItems(subjectNames);
    }
    
    /* ---------------------------------------------------
     * Setup table columns và cell factories
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void setupTable() {
        // ID Column
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getId()))
        );
        
        // Question Text Column - Truncate long text
        questionTextColumn.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            return new SimpleStringProperty(content);
        });
        
        // Subject Column
        subjectColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSubjectName() != null ? 
                cellData.getValue().getSubjectName() : "N/A")
        );
        
        // Type Column
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getQuestionTypeDisplay(cellData.getValue().getType()))
        );
        
        // Difficulty Column
        difficultyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getDifficultyDisplay(cellData.getValue().getDifficulty()))
        );
        
        // Tags Column
        tagsColumn.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags() != null ? 
                String.join(", ", cellData.getValue().getTags()) : "";
            return new SimpleStringProperty(tags);
        });
        
        // Usage Count Column
        usageCountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getUsageCount()))
        );
        
        // Created By Column
        createdByColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCreatedByName() != null ? 
                cellData.getValue().getCreatedByName() : "N/A")
        );
        
        // Actions Column - Add Edit/Delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("👁 Xem");
            private final Button editButton = new Button("✏ Sửa");
            private final Button deleteButton = new Button("🗑 Xóa");
            private final HBox actionButtons = new HBox(5, viewButton, editButton, deleteButton);
            
            {
                viewButton.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                editButton.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                deleteButton.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-color: #dc3545; -fx-text-fill: white;");
                
                viewButton.setOnAction(e -> {
                    QuestionBankDTO question = getTableView().getItems().get(getIndex());
                    handleViewQuestion(question);
                });
                
                editButton.setOnAction(e -> {
                    QuestionBankDTO question = getTableView().getItems().get(getIndex());
                    handleEditQuestion(question);
                });
                
                deleteButton.setOnAction(e -> {
                    QuestionBankDTO question = getTableView().getItems().get(getIndex());
                    handleDeleteQuestion(question);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionButtons);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Setup pagination controls
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void setupPagination() {
        // Page size options
        ObservableList<Integer> pageSizes = FXCollections.observableArrayList(
            10, 20, 50, 100
        );
        pageSizeComboBox.setItems(pageSizes);
        pageSizeComboBox.setValue(20);
        
        // Page size change listener
        pageSizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = newVal;
                currentPage = 0;
                loadQuestions();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Load questions từ backend với filters và pagination
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void loadQuestions() {
        showLoading(true);
        
        new Thread(() -> {
            try {
                // Get filter values
                Long subjectId = null; // TODO: Map subject name to ID
                Difficulty difficulty = difficultyFilter.getValue();
                QuestionType type = typeFilter.getValue();
                String keyword = searchField.getText();
                
                // Call API
                QuestionBankApiClient.QuestionBankResponse response = 
                    apiClient.getQuestions(subjectId, difficulty, type, keyword, currentPage, pageSize);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    questionsTable.setItems(FXCollections.observableArrayList(response.getContent()));
                    totalPages = response.getTotalPages();
                    totalElements = response.getTotalElements();
                    currentPage = response.getNumber();
                    
                    updatePaginationControls();
                    updateStatistics();
                    showLoading(false);
                });
                
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Lỗi tải dữ liệu", "Không thể tải danh sách câu hỏi: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update pagination controls state
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void updatePaginationControls() {
        pageInfoLabel.setText(String.format("Trang %d / %d", currentPage + 1, Math.max(totalPages, 1)));
        
        firstPageButton.setDisable(currentPage == 0);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        lastPageButton.setDisable(currentPage >= totalPages - 1);
    }
    
    /* ---------------------------------------------------
     * Update statistics labels
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void updateStatistics() {
        totalQuestionsLabel.setText(String.format("Tổng số: %d câu hỏi", totalElements));
        
        // Count by difficulty (from current page only - TODO: Get from backend)
        long easyCount = questionsTable.getItems().stream()
            .filter(q -> q.getDifficulty() == Difficulty.EASY).count();
        long mediumCount = questionsTable.getItems().stream()
            .filter(q -> q.getDifficulty() == Difficulty.MEDIUM).count();
        long hardCount = questionsTable.getItems().stream()
            .filter(q -> q.getDifficulty() == Difficulty.HARD).count();
        
        easyCountLabel.setText("Dễ: " + easyCount);
        mediumCountLabel.setText("Trung bình: " + mediumCount);
        hardCountLabel.setText("Khó: " + hardCount);
    }
    
    /* ---------------------------------------------------
     * Hiển thị/ẩn loading indicator
     * @param show true để hiện, false để ẩn
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void showLoading(boolean show) {
        loadingPane.setVisible(show);
        loadingPane.setManaged(show);
    }
    
    /* ---------------------------------------------------
     * Hiển thị error dialog
     * @param title Tiêu đề
     * @param content Nội dung lỗi
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /* ---------------------------------------------------
     * Get display text cho question type
     * @param type QuestionType enum
     * @return Display text tiếng Việt
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private String getQuestionTypeDisplay(QuestionType type) {
        if (type == null) return "N/A";
        switch (type) {
            case MULTIPLE_CHOICE: return "Trắc nghiệm";
            case MULTIPLE_SELECT: return "Nhiều lựa chọn";
            case TRUE_FALSE: return "Đúng/Sai";
            case ESSAY: return "Tự luận";
            case SHORT_ANSWER: return "Trả lời ngắn";
            case CODING: return "Lập trình";
            case FILL_IN_BLANK: return "Điền khuyết";
            case MATCHING: return "Nối cặp";
            default: return type.name();
        }
    }
    
    /* ---------------------------------------------------
     * Get display text cho difficulty
     * @param difficulty Difficulty enum
     * @return Display text tiếng Việt
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private String getDifficultyDisplay(Difficulty difficulty) {
        if (difficulty == null) return "N/A";
        switch (difficulty) {
            case EASY: return "Dễ";
            case MEDIUM: return "Trung bình";
            case HARD: return "Khó";
            default: return difficulty.name();
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    @FXML
    private void handleSearch() {
        currentPage = 0;
        loadQuestions();
    }
    
    @FXML
    private void handleResetFilters() {
        subjectFilter.setValue(null);
        difficultyFilter.setValue(null);
        typeFilter.setValue(null);
        searchField.clear();
        currentPage = 0;
        loadQuestions();
    }
    
    /* ---------------------------------------------------
     * Handler nút "Thêm câu hỏi"
     * Mở dialog CREATE mode
     * @author: K24DTCN210-NVMANH (25/11/2025 23:03)
     * --------------------------------------------------- */
    @FXML
    private void handleCreateQuestion() {
        openQuestionDialog(null); // null = CREATE mode
    }
    
    @FXML
    private void handleImport() {
        showInfo("Chức năng đang phát triển", "Tính năng import câu hỏi sẽ được implement sau.");
    }
    
    @FXML
    private void handleExport() {
        showInfo("Chức năng đang phát triển", "Tính năng export câu hỏi sẽ được implement sau.");
    }
    
    @FXML
    private void handleFirstPage() {
        currentPage = 0;
        loadQuestions();
    }
    
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadQuestions();
        }
    }
    
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadQuestions();
        }
    }
    
    @FXML
    private void handleLastPage() {
        currentPage = totalPages - 1;
        loadQuestions();
    }
    
    /* ---------------------------------------------------
     * Xem chi tiết câu hỏi
     * @param question QuestionBankDTO cần xem
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void handleViewQuestion(QuestionBankDTO question) {
        // TODO: Open view dialog
        showInfo("Xem câu hỏi", "ID: " + question.getId() + "\nNội dung: " + question.getContent());
    }
    
    /* ---------------------------------------------------
     * Sửa câu hỏi
     * Mở dialog EDIT mode với dữ liệu pre-filled
     * @param question QuestionBankDTO cần sửa
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 23:03) - Implement dialog opening
     * --------------------------------------------------- */
    private void handleEditQuestion(QuestionBankDTO question) {
        openQuestionDialog(question); // EDIT mode
    }
    
    /* ---------------------------------------------------
     * Mở dialog tạo/sửa câu hỏi
     * @param question QuestionBankDTO cần sửa (null = CREATE mode)
     * @author: K24DTCN210-NVMANH (25/11/2025 23:03)
     * --------------------------------------------------- */
    private void openQuestionDialog(QuestionBankDTO question) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/question-edit-dialog.fxml")
            );
            
            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setTitle(question == null ? "Tạo câu hỏi mới" : "Chỉnh sửa câu hỏi");
            
            // Load scene
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/css/teacher-styles.css").toExternalForm()
            );
            dialogStage.setScene(scene);
            
            // Setup controller
            QuestionEditDialogController controller = loader.getController();
            controller.setup(dialogStage, apiClient, subjects, question);
            
            // Show and wait
            dialogStage.showAndWait();
            
            // Check if confirmed
            if (controller.isConfirmed()) {
                showInfo("Thành công", 
                    question == null ? "Đã tạo câu hỏi mới thành công!" : "Đã cập nhật câu hỏi thành công!");
                loadQuestions(); // Refresh list
            }
            
        } catch (IOException e) {
            showError("Lỗi", "Không thể mở dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /* ---------------------------------------------------
     * Xóa câu hỏi
     * @param question QuestionBankDTO cần xóa
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void handleDeleteQuestion(QuestionBankDTO question) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa câu hỏi này?");
        confirm.setContentText("ID: " + question.getId() + "\n" + 
                              (question.getContent().length() > 50 ? 
                                  question.getContent().substring(0, 50) + "..." : 
                                  question.getContent()));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteQuestion(question.getId());
        }
    }
    
    /* ---------------------------------------------------
     * Thực hiện xóa câu hỏi qua API
     * @param questionId ID của câu hỏi cần xóa
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void deleteQuestion(Long questionId) {
        showLoading(true);
        
        new Thread(() -> {
            try {
                apiClient.deleteQuestion(questionId);
                
                Platform.runLater(() -> {
                    showLoading(false);
                    showInfo("Thành công", "Đã xóa câu hỏi thành công!");
                    loadQuestions(); // Reload list
                });
                
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Lỗi xóa câu hỏi", "Không thể xóa câu hỏi: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Hiển thị info dialog
     * @param title Tiêu đề
     * @param content Nội dung
     * @author: K24DTCN210-NVMANH (25/11/2025 22:45)
     * --------------------------------------------------- */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
