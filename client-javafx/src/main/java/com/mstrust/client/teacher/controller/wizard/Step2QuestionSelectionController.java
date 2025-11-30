package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.dto.QuestionBankDTO;
import com.mstrust.client.teacher.dto.Difficulty;
import com.mstrust.client.exam.dto.QuestionType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.concurrent.Task;
import javafx.application.Platform;

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

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;
    private ExamManagementApiClient examApiClient;
    private QuestionBankApiClient questionBankApiClient;
    
    // Observable Lists để quản lý dữ liệu tables
    private ObservableList<QuestionBankDTO> availableQuestions = FXCollections.observableArrayList();
    private ObservableList<QuestionBankDTO> selectedQuestions = FXCollections.observableArrayList();

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
        
        // Load questions when wizard data is set
        loadAvailableQuestions();
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
     * --------------------------------------------------- */
    public void setQuestionBankApiClient(QuestionBankApiClient questionBankApiClient) {
        this.questionBankApiClient = questionBankApiClient;
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
            
            System.out.println("Saved " + selectedQuestions.size() + " selected questions to wizard data");
        }
        
        System.out.println("=== STEP2: saveFormToWizardData() COMPLETED ===");
    }
    
    /* ---------------------------------------------------
     * Load available questions from API
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    private void loadAvailableQuestions() {
        if (questionBankApiClient == null) {
            showError("Question Bank API Client chưa được khởi tạo");
            return;
        }
        
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
                availableQuestions.clear();
                availableQuestions.addAll(loadTask.getValue());
                hideError();
                System.out.println("=== STEP2: Loaded " + availableQuestions.size() + " available questions ===");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                System.err.println("Failed to load questions: " + exception.getMessage());
                showError("Không thể tải danh sách câu hỏi: " + exception.getMessage());
            });
        });
        
        // Run task in background
        new Thread(loadTask).start();
    }

    /* ---------------------------------------------------
     * Xử lý nút Refresh
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        System.out.println("=== STEP2: handleRefresh() CALLED ===");
        hideError();
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
        
        int addedCount = 0;
        for (QuestionBankDTO question : availableQuestions) {
            boolean alreadyAdded = selectedQuestions.stream()
                .anyMatch(q -> q.getId().equals(question.getId()));
                
            if (!alreadyAdded) {
                selectedQuestions.add(question);
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
        selectedQuestions.clear();
        updateSummaryLabels();
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
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (30/11/2025 00:00)
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        if (parentController != null) {
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
