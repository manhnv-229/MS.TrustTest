package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.dto.QuestionBankDTO;
import com.mstrust.client.teacher.dto.Difficulty;
import com.mstrust.client.exam.dto.QuestionType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.List;

/* ---------------------------------------------------
 * Controller cho Step 2 của Exam Creation Wizard
 * Xử lý việc chọn câu hỏi cho đề thi
 * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
 * --------------------------------------------------- */
public class Step2QuestionSelectionController {

    // FXML Controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private TableView<QuestionBankDTO> availableQuestionsTable;
    @FXML private TableColumn<QuestionBankDTO, String> availableContentCol;
    @FXML private TableColumn<QuestionBankDTO, String> availableTypeCol;
    @FXML private TableColumn<QuestionBankDTO, String> availableDifficultyCol;
    @FXML private TableView<QuestionBankDTO> selectedQuestionsTable;
    @FXML private TableColumn<QuestionBankDTO, Integer> selectedOrderCol;
    @FXML private TableColumn<QuestionBankDTO, String> selectedContentCol;
    @FXML private TableColumn<QuestionBankDTO, Double> selectedPointsCol;
    @FXML private Label questionCountLabel;
    @FXML private Label totalPointsLabel;
    @FXML private Label errorLabel;
    @FXML private StackPane loadingPane;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;
    private ExamManagementApiClient examApiClient;
    private QuestionBankApiClient questionBankApiClient;
    
    // Observable Lists để quản lý dữ liệu tables
    private ObservableList<QuestionBankDTO> availableQuestions = FXCollections.observableArrayList();
    private ObservableList<QuestionBankDTO> selectedQuestions = FXCollections.observableArrayList();
    
    // Cache để tránh load lại từ API mỗi lần
    private boolean questionsLoaded = false;
    
    // Lưu tất cả questions ban đầu để có thể restore khi xóa
    private List<QuestionBankDTO> allQuestionsCache = new java.util.ArrayList<>();

    /* ---------------------------------------------------
     * Khởi tạo controller
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        hideError();
        setupControls();
        setupTables();
    }

    /* ---------------------------------------------------
     * Setup initial controls
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void setupControls() {
        // Setup difficulty filter
        if (difficultyFilter != null) {
            difficultyFilter.getItems().addAll("Tất cả", "EASY", "MEDIUM", "HARD");
            difficultyFilter.setValue("Tất cả");
        }
        
        // Initialize labels
        updateSummaryLabels();
    }
    
    /* ---------------------------------------------------
     * Setup TableView columns
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void setupTables() {
        // Available Questions Table
        if (availableContentCol != null) {
            availableContentCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        }
        if (availableTypeCol != null) {
            availableTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (availableDifficultyCol != null) {
            availableDifficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        }
        
        // Selected Questions Table  
        if (selectedOrderCol != null) {
            selectedOrderCol.setCellValueFactory(cellData -> {
                int index = selectedQuestionsTable.getItems().indexOf(cellData.getValue()) + 1;
                return new javafx.beans.property.SimpleIntegerProperty(index).asObject();
            });
        }
        if (selectedContentCol != null) {
            selectedContentCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        }
        if (selectedPointsCol != null) {
            // Use constant default points since QuestionBankDTO doesn't have this field
            selectedPointsCol.setCellValueFactory(cellData -> {
                return new javafx.beans.property.SimpleDoubleProperty(5.0).asObject();
            });
        }
        
        // Bind data to tables
        if (availableQuestionsTable != null) {
            availableQuestionsTable.setItems(availableQuestions);
        }
        if (selectedQuestionsTable != null) {
            selectedQuestionsTable.setItems(selectedQuestions);
        }
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Restore selectedQuestions và chỉ load API nếu chưa load
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        
        // Debug print để kiểm tra data
        System.out.println("=== STEP 2 DEBUG: setWizardData() ===");
        if (wizardData != null) {
            System.out.println("Title: " + wizardData.getTitle());
            System.out.println("Start Time: " + wizardData.getStartTime());
            System.out.println("End Time: " + wizardData.getEndTime());
            System.out.println("Subject Class ID: " + wizardData.getSubjectClassId());
            System.out.println("Subject Class Name: " + wizardData.getSubjectClassName());
            System.out.println("Exam Purpose: " + wizardData.getExamPurpose());
            System.out.println("Exam Format: " + wizardData.getExamFormat());
            System.out.println("Selected Questions Count: " + wizardData.getSelectedQuestions().size());
        } else {
            System.out.println("WizardData is null!");
        }
        System.out.println("=====================================");
        
        // Restore selectedQuestions từ wizardData trước
        restoreSelectedQuestions();
        
        // Chỉ load questions từ API nếu chưa load hoặc cần refresh
        if (!questionsLoaded && wizardData != null && questionBankApiClient != null) {
            tryLoadAvailableQuestions();
        } else if (questionsLoaded) {
            // Đã load rồi, chỉ cần filter lại available questions
            filterAvailableQuestions();
        }
    }

    /* ---------------------------------------------------
     * Set parent controller
     * @param parentController Controller cha
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    public void setParentController(ExamCreationWizardController parentController) {
        this.parentController = parentController;
    }

    /* ---------------------------------------------------
     * Set API client (required by ExamCreationWizardController)
     * @param examApiClient Exam management API client
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    public void setApiClient(ExamManagementApiClient examApiClient) {
        this.examApiClient = examApiClient;
    }
    
    /* ---------------------------------------------------
     * Set QuestionBank API client
     * @param questionBankApiClient Question bank API client
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Tự động load questions sau khi set client
     * --------------------------------------------------- */
    public void setQuestionBankApiClient(QuestionBankApiClient questionBankApiClient) {
        this.questionBankApiClient = questionBankApiClient;
        
        // Tự động load questions khi client đã được set và wizardData đã sẵn sàng
        tryLoadAvailableQuestions();
    }

    /* ---------------------------------------------------
     * PUBLIC method để force save form data từ parent controller
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP2: saveFormToWizardData() CALLED ===");
        
        if (wizardData != null) {
            // Save selected questions to wizard data
            wizardData.getSelectedQuestions().clear();
            
            int order = 1;
            for (QuestionBankDTO question : selectedQuestions) {
                com.mstrust.client.teacher.dto.ExamQuestionMapping mapping = 
                    new com.mstrust.client.teacher.dto.ExamQuestionMapping();
                mapping.setQuestionId(question.getId());
                mapping.setQuestionOrder(order++);
                mapping.setPoints(BigDecimal.valueOf(5.0)); // Default points for each question
                
                wizardData.getSelectedQuestions().add(mapping);
            }
            
            // Tính toán totalPoints sau khi lưu questions
            wizardData.calculateTotalPoints();
            
            System.out.println("Saved " + selectedQuestions.size() + " selected questions to wizard data");
            System.out.println("Total points: " + wizardData.getTotalPoints());
        }
        
        System.out.println("=== STEP2: saveFormToWizardData() COMPLETED ===");
    }
    
    /* ---------------------------------------------------
     * Kiểm tra và load questions nếu đủ điều kiện
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void tryLoadAvailableQuestions() {
        // Chỉ load khi cả wizardData và questionBankApiClient đều đã sẵn sàng
        if (wizardData != null && questionBankApiClient != null) {
            loadAvailableQuestions();
        } else {
            System.out.println("=== STEP2: Chưa thể load questions - wizardData: " + 
                (wizardData != null ? "OK" : "NULL") + 
                ", questionBankApiClient: " + 
                (questionBankApiClient != null ? "OK" : "NULL") + " ===");
        }
    }
    
    /* ---------------------------------------------------
     * Load available questions from API
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm loading indicator
     * --------------------------------------------------- */
    private void loadAvailableQuestions() {
        if (questionBankApiClient == null) {
            showError("Question Bank API Client chưa được khởi tạo");
            return;
        }
        
        // Hiển thị loading indicator
        showLoading(true);
        
        // Run API call in background thread
        Task<List<QuestionBankDTO>> loadTask = new Task<List<QuestionBankDTO>>() {
            @Override
            protected List<QuestionBankDTO> call() throws Exception {
                // Get filters
                String keyword = (searchField != null) ? searchField.getText() : null;
                Difficulty difficulty = null;
                if (difficultyFilter != null && !"Tất cả".equals(difficultyFilter.getValue())) {
                    try {
                        difficulty = Difficulty.valueOf(difficultyFilter.getValue());
                    } catch (IllegalArgumentException e) {
                        // Invalid difficulty, ignore
                    }
                }
                
                // Load from subject if available
                Long subjectId = (wizardData != null) ? wizardData.getSubjectClassId() : null;
                
                // Call API
                QuestionBankApiClient.QuestionBankResponse response = 
                    questionBankApiClient.getQuestions(subjectId, difficulty, null, keyword, 0, 50);
                
                return response.getContent();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                List<QuestionBankDTO> loadedQuestions = loadTask.getValue();
                
                // Lưu cache tất cả questions ban đầu
                allQuestionsCache.clear();
                allQuestionsCache.addAll(loadedQuestions);
                
                availableQuestions.clear();
                availableQuestions.addAll(loadedQuestions);
                questionsLoaded = true;
                
                // Restore selectedQuestions sau khi load availableQuestions
                restoreSelectedQuestions();
                
                // Filter out already selected questions
                filterAvailableQuestions();
                
                // Ẩn loading indicator
                showLoading(false);
                
                hideError();
                System.out.println("=== STEP2: Loaded " + availableQuestions.size() + " available questions ===");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                System.err.println("Failed to load questions: " + exception.getMessage());
                
                // Ẩn loading indicator
                showLoading(false);
                
                showError("Không thể tải danh sách câu hỏi: " + exception.getMessage());
            });
        });
        
        // Run task in background
        new Thread(loadTask).start();
    }

    /* ---------------------------------------------------
     * Restore selectedQuestions từ wizardData
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Tìm từ allQuestionsCache thay vì availableQuestions
     * --------------------------------------------------- */
    private void restoreSelectedQuestions() {
        if (wizardData == null || wizardData.getSelectedQuestions().isEmpty()) {
            selectedQuestions.clear();
            updateSummaryLabels();
            return;
        }
        
        // Nếu chưa load questions, sẽ restore sau khi load xong
        if (allQuestionsCache.isEmpty() && availableQuestions.isEmpty()) {
            System.out.println("=== STEP2: Questions chưa load, sẽ restore sau ===");
            return;
        }
        
        // Tìm từ allQuestionsCache (chứa tất cả questions ban đầu) hoặc availableQuestions
        List<QuestionBankDTO> sourceList = allQuestionsCache.isEmpty() ? availableQuestions : allQuestionsCache;
        
        // Restore selected questions từ wizardData
        selectedQuestions.clear();
        for (com.mstrust.client.teacher.dto.ExamQuestionMapping mapping : wizardData.getSelectedQuestions()) {
            QuestionBankDTO question = sourceList.stream()
                .filter(q -> q.getId().equals(mapping.getQuestionId()))
                .findFirst()
                .orElse(null);
            
            if (question != null) {
                selectedQuestions.add(question);
            } else {
                // Nếu không tìm thấy, có thể question đã bị xóa hoặc không có quyền
                System.out.println("WARNING: Question ID " + mapping.getQuestionId() + " not found in questions list");
            }
        }
        
        updateSummaryLabels();
        System.out.println("=== STEP2: Restored " + selectedQuestions.size() + " selected questions ===");
    }
    
    /* ---------------------------------------------------
     * Filter available questions: Remove already selected
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void filterAvailableQuestions() {
        if (wizardData != null && !wizardData.getSelectedQuestions().isEmpty()) {
            List<Long> selectedIds = wizardData.getSelectedQuestions().stream()
                .map(com.mstrust.client.teacher.dto.ExamQuestionMapping::getQuestionId)
                .collect(java.util.stream.Collectors.toList());
            
            availableQuestions.removeIf(q -> selectedIds.contains(q.getId()));
        }
        
        // Also remove from selectedQuestions observable list
        List<Long> selectedIds = selectedQuestions.stream()
            .map(QuestionBankDTO::getId)
            .collect(java.util.stream.Collectors.toList());
        
        availableQuestions.removeIf(q -> selectedIds.contains(q.getId()));
    }
    
    /* ---------------------------------------------------
     * Xử lý nút Refresh
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Reset cache và load lại
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        System.out.println("=== STEP2: handleRefresh() CALLED ===");
        hideError();
        questionsLoaded = false; // Reset cache
        loadAvailableQuestions();
    }

    /* ---------------------------------------------------
     * Xử lý thêm câu hỏi được chọn
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleAddQuestion() {
        System.out.println("=== STEP2: handleAddQuestion() CALLED ===");
        hideError();
        
        QuestionBankDTO selectedQuestion = availableQuestionsTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            // Check if already added
            boolean alreadyAdded = selectedQuestions.stream()
                .anyMatch(q -> q.getId().equals(selectedQuestion.getId()));
                
            if (!alreadyAdded) {
                selectedQuestions.add(selectedQuestion);
                // Remove from available questions immediately
                availableQuestions.remove(selectedQuestion);
                updateSummaryLabels();
                System.out.println("Added question: " + selectedQuestion.getQuestionText());
            } else {
                showError("Câu hỏi này đã được chọn rồi!");
            }
        } else {
            showError("Vui lòng chọn một câu hỏi để thêm!");
        }
    }

    /* ---------------------------------------------------
     * Xử lý thêm tất cả câu hỏi
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleAddAllQuestions() {
        System.out.println("=== STEP2: handleAddAllQuestions() CALLED ===");
        hideError();
        
        // Create a copy of availableQuestions to iterate (since we'll be removing items)
        List<QuestionBankDTO> questionsToAdd = new java.util.ArrayList<>(availableQuestions);
        
        int addedCount = 0;
        for (QuestionBankDTO question : questionsToAdd) {
            boolean alreadyAdded = selectedQuestions.stream()
                .anyMatch(q -> q.getId().equals(question.getId()));
                
            if (!alreadyAdded) {
                selectedQuestions.add(question);
                // Remove from available questions immediately
                availableQuestions.remove(question);
                addedCount++;
            }
        }
        
        updateSummaryLabels();
        System.out.println("Added " + addedCount + " questions");
    }

    /* ---------------------------------------------------
     * Xử lý xóa câu hỏi được chọn
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleRemoveQuestion() {
        System.out.println("=== STEP2: handleRemoveQuestion() CALLED ===");
        hideError();
        
        QuestionBankDTO selectedQuestion = selectedQuestionsTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            selectedQuestions.remove(selectedQuestion);
            
            // Thêm lại vào availableQuestions nếu chưa có
            boolean alreadyInAvailable = availableQuestions.stream()
                .anyMatch(q -> q.getId().equals(selectedQuestion.getId()));
            
            if (!alreadyInAvailable) {
                // Tìm trong cache để đảm bảo có đầy đủ thông tin
                QuestionBankDTO questionToAdd = allQuestionsCache.stream()
                    .filter(q -> q.getId().equals(selectedQuestion.getId()))
                    .findFirst()
                    .orElse(selectedQuestion);
                
                availableQuestions.add(questionToAdd);
                // Sort lại để giữ thứ tự (nếu cần)
                availableQuestions.sort((q1, q2) -> Long.compare(q1.getId(), q2.getId()));
            }
            
            updateSummaryLabels();
            System.out.println("Removed question: " + selectedQuestion.getQuestionText());
        } else {
            showError("Vui lòng chọn một câu hỏi để xóa!");
        }
    }

    /* ---------------------------------------------------
     * Xử lý xóa tất cả câu hỏi đã chọn
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleRemoveAllQuestions() {
        System.out.println("=== STEP2: handleRemoveAllQuestions() CALLED ===");
        hideError();
        
        // Lưu danh sách câu hỏi sẽ xóa để thêm lại vào available
        List<QuestionBankDTO> questionsToRestore = new java.util.ArrayList<>(selectedQuestions);
        
        selectedQuestions.clear();
        
        // Thêm lại tất cả câu hỏi đã xóa vào availableQuestions
        for (QuestionBankDTO question : questionsToRestore) {
            boolean alreadyInAvailable = availableQuestions.stream()
                .anyMatch(q -> q.getId().equals(question.getId()));
            
            if (!alreadyInAvailable) {
                // Tìm trong cache để đảm bảo có đầy đủ thông tin
                QuestionBankDTO questionToAdd = allQuestionsCache.stream()
                    .filter(q -> q.getId().equals(question.getId()))
                    .findFirst()
                    .orElse(question);
                
                availableQuestions.add(questionToAdd);
            }
        }
        
        // Sort lại để giữ thứ tự (nếu cần)
        availableQuestions.sort((q1, q2) -> Long.compare(q1.getId(), q2.getId()));
        
        updateSummaryLabels();
        System.out.println("Removed all questions, restored " + questionsToRestore.size() + " to available");
    }

    /* ---------------------------------------------------
     * Cập nhật labels summary
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void updateSummaryLabels() {
        int count = selectedQuestions.size();
        // Use default 5.0 points per question since QuestionBankDTO doesn't have defaultPoints field
        double totalPoints = count * 5.0;
            
        if (questionCountLabel != null) {
            questionCountLabel.setText("Số câu hỏi: " + count);
        }
        if (totalPointsLabel != null) {
            totalPointsLabel.setText("Tổng điểm: " + String.format("%.1f", totalPoints));
        }
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /* ---------------------------------------------------
     * Hiển thị/ẩn loading indicator
     * @param show true để hiển thị, false để ẩn
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showLoading(boolean show) {
        if (loadingPane != null) {
            loadingPane.setVisible(show);
            loadingPane.setManaged(show);
        }
    }

    /* ---------------------------------------------------
     * Validate form data
     * @return true nếu hợp lệ, false nếu có lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Hiển thị lỗi bằng Alert dialog
     * --------------------------------------------------- */
    public boolean validateForm() {
        hideError();
        
        // Lưu dữ liệu trước khi validate
        saveFormToWizardData();
        
        // Validate sử dụng wizardData.validateStep2()
        List<String> errors = wizardData.validateStep2();
        
        if (!errors.isEmpty()) {
            showValidationError(String.join("\n", errors));
            return false;
        }
        
        return true;
    }
    
    /* ---------------------------------------------------
     * Hiển thị validation error bằng Alert dialog
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText("Vui lòng kiểm tra lại thông tin");
        alert.setContentText(message);
        
        // Set owner window từ parent controller
        if (parentController != null && parentController.getWizardPane() != null) {
            Window owner = parentController.getWizardPane().getScene().getWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
        }
        
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm validation trước khi next
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        boolean isValid = validateForm();
        
        if (isValid && parentController != null) {
            parentController.nextStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Previous
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handlePrevious() {
        if (parentController != null) {
            parentController.previousStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Cancel
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        if (parentController != null) {
            parentController.cancelWizard();
        }
    }
}
