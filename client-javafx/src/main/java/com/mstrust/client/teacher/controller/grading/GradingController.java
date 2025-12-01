package com.mstrust.client.teacher.controller.grading;

import com.mstrust.client.teacher.api.GradingApiClient;
import com.mstrust.client.teacher.dto.grading.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Controller cho Grading Interface
 * Giao diện chấm điểm từng câu hỏi
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
public class GradingController {
    private static final Logger logger = LoggerFactory.getLogger(GradingController.class);
    
    // FXML Components - Header
    @FXML private Button backButton;
    @FXML private Label studentNameLabel;
    @FXML private Label examTitleLabel;
    @FXML private Label currentScoreLabel;
    @FXML private Label statusLabel;
    
    // FXML Components - Questions List (Left)
    @FXML private ListView<AnswerForGradingDTO> questionsList;
    @FXML private VBox questionsLoadingPane;
    
    // FXML Components - Answer Display (Right)
    @FXML private Label questionNumberLabel;
    @FXML private TextFlow questionContentArea;
    @FXML private TextFlow studentAnswerArea;
    @FXML private TextFlow correctAnswerArea;
    @FXML private TextField pointsInput;
    @FXML private Label maxPointsLabel;
    @FXML private TextArea feedbackArea;
    @FXML private Label questionTypeLabel;
    @FXML private Label autoGradedLabel;
    
    // FXML Components - Navigation
    @FXML private Button previousQuestionButton;
    @FXML private Button nextQuestionButton;
    @FXML private Button saveButton;
    @FXML private Button finalizeButton;
    @FXML private Button previousStudentButton;
    @FXML private Button nextStudentButton;
    
    // FXML Components - Loading
    @FXML private StackPane loadingPane;
    @FXML private Label loadingMessage;
    
    // Service
    private GradingApiClient apiClient;
    private Stage primaryStage;
    private Long submissionId;
    
    // State
    private GradingDetailDTO submissionDetail;
    private ObservableList<AnswerForGradingDTO> answers = FXCollections.observableArrayList();
    private int currentQuestionIndex = 0;
    private javafx.animation.PauseTransition autoSavePause; // Debounce cho auto-save
    private boolean isUpdatingList = false; // Flag để tránh trigger listener khi đang update
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API client, stage và submission ID
     * @param apiClient GradingApiClient instance
     * @param primaryStage Stage chính của application
     * @param submissionId ID của submission cần chấm
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    public void initialize(GradingApiClient apiClient, Stage primaryStage, Long submissionId) {
        this.apiClient = apiClient;
        this.primaryStage = primaryStage;
        this.submissionId = submissionId;
        
        setupQuestionsList();
        loadSubmissionDetail();
    }
    
    /* ---------------------------------------------------
     * Setup questions list với cell factory
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025) - Thêm listener để auto-save khi nhập điểm
     * --------------------------------------------------- */
    private void setupQuestionsList() {
        // Auto-save khi user nhập điểm (với debounce để tránh save quá nhiều)
        autoSavePause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
        autoSavePause.setOnFinished(e -> {
            if (currentQuestionIndex >= 0 && currentQuestionIndex < answers.size()) {
                saveCurrentAnswerToModel();
            }
        });
        
        // Listener cho pointsInput - chỉ auto-save khi user thực sự gõ (không phải khi program set)
        pointsInput.textProperty().addListener((obs, oldVal, newVal) -> {
            // Bỏ qua nếu đang update (chuyển câu, load dữ liệu, etc.)
            if (isUpdatingList) {
                return;
            }
            
            // Chỉ save nếu đã có answer được chọn và câu hỏi không phải auto-graded
            if (currentQuestionIndex >= 0 && currentQuestionIndex < answers.size()) {
                AnswerForGradingDTO currentAnswer = answers.get(currentQuestionIndex);
                // Không auto-save cho câu đã auto-graded
                if (currentAnswer != null && currentAnswer.getIsAutoGraded() != null && currentAnswer.getIsAutoGraded()) {
                    return;
                }
                
                // Validate input
                try {
                    if (newVal != null && !newVal.trim().isEmpty()) {
                        Double.parseDouble(newVal.trim());
                    }
                    // Cancel pause cũ và bắt đầu pause mới (debounce)
                    autoSavePause.stop();
                    autoSavePause.play();
                } catch (NumberFormatException e) {
                    // Invalid input - không save, nhưng vẫn cho phép user tiếp tục gõ
                }
            }
        });
        questionsList.setCellFactory(lv -> new ListCell<AnswerForGradingDTO>() {
            @Override
            protected void updateItem(AnswerForGradingDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Lấy index để hiển thị số thứ tự
                    int index = getIndex();
                    
                    // Tạo cell layout
                    HBox cell = new HBox(10);
                    cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    // Question number
                    Label numberLabel = new Label("Q" + (index + 1));
                    numberLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 40;");
                    
                    // Status icon - kiểm tra đã chấm chưa
                    String statusIcon;
                    if (item.getIsAutoGraded() != null && item.getIsAutoGraded()) {
                        statusIcon = "✓"; // Auto-graded
                    } else if (item.getCurrentScore() != null && item.getCurrentScore() > 0) {
                        statusIcon = "✓"; // Đã chấm thủ công
                    } else {
                        statusIcon = "⏳"; // Chờ chấm
                    }
                    Label statusLabel = new Label(statusIcon);
                    statusLabel.setStyle("-fx-font-size: 14px;");
                    
                    // Score display - lấy trực tiếp từ item parameter
                    String scoreText;
                    Double currentScore = item.getCurrentScore();
                    Double maxScore = item.getMaxScore();
                    
                    if (maxScore != null) {
                        if (currentScore != null) {
                            scoreText = String.format("%.1f/%.1f", currentScore, maxScore);
                        } else {
                            scoreText = String.format("0.0/%.1f", maxScore);
                        }
                    } else {
                        // Fallback nếu maxScore bị null
                        if (currentScore != null) {
                            scoreText = String.format("%.1f/?", currentScore);
                        } else {
                            scoreText = "0.0/?";
                        }
                    }
                    Label scoreLabel = new Label(scoreText);
                    scoreLabel.setStyle("-fx-text-fill: #666;");
                    
                    cell.getChildren().addAll(numberLabel, statusLabel, scoreLabel);
                    setGraphic(cell);
                }
            }
        });
        
        questionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Bỏ qua nếu đang update list (tránh vòng lặp)
            if (isUpdatingList) {
                logger.debug("Skipping selection change - isUpdatingList = true");
                return;
            }
            
            if (newVal != null) {
                int newIndex = questionsList.getSelectionModel().getSelectedIndex();
                logger.info("=== SELECTION CHANGED ===");
                logger.info("Old index: {}, New index: {}", currentQuestionIndex, newIndex);
                
                // Lưu điểm số của câu cũ trước khi chuyển (nếu có câu cũ và index khác nhau)
                if (oldVal != null && currentQuestionIndex >= 0 && currentQuestionIndex < answers.size() 
                        && currentQuestionIndex != newIndex) {
                    logger.info("Saving old answer at index {} before switching", currentQuestionIndex);
                    
                    // Hủy debounce timer trước khi save thủ công
                    if (autoSavePause != null) {
                        autoSavePause.stop();
                    }
                    
                    saveCurrentAnswerToModel();
                }
                
                currentQuestionIndex = newIndex;
                // Lấy answer từ list để đảm bảo có dữ liệu mới nhất
                if (currentQuestionIndex >= 0 && currentQuestionIndex < answers.size()) {
                    AnswerForGradingDTO answer = answers.get(currentQuestionIndex);
                    logger.info("Getting answer from list at index {}. Answer maxScore: {}", 
                        currentQuestionIndex, answer != null ? answer.getMaxScore() : "null");
                    displayQuestion(answer);
                } else {
                    logger.warn("Invalid index: {} (answers size: {})", currentQuestionIndex, answers.size());
                }
            }
        });
    }
    
    /* ---------------------------------------------------
     * Load submission detail từ API
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void loadSubmissionDetail() {
        showLoading("Đang tải chi tiết bài nộp...");
        
        new Thread(() -> {
            try {
                GradingDetailDTO detail = apiClient.getSubmissionDetail(submissionId);
                
                Platform.runLater(() -> {
                    this.submissionDetail = detail;
                    setupHeader();
                    loadAnswers();
                    updateUIState(); // Check status và disable buttons nếu cần
                    if (!answers.isEmpty()) {
                        questionsList.getSelectionModel().select(0);
                        displayQuestion(answers.get(0));
                    }
                    hideLoading();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Không thể tải chi tiết bài nộp: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Setup header với student và exam info
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void setupHeader() {
        if (submissionDetail == null) return;
        
        // Student info
        Map<String, Object> student = submissionDetail.getStudent();
        if (student != null) {
            String name = (String) student.get("name");
            String code = (String) student.get("studentCode");
            studentNameLabel.setText(name + " (" + code + ")");
        }
        
        // Exam info
        if (submissionDetail.getExam() != null) {
            examTitleLabel.setText(submissionDetail.getExam().getTitle());
        }
        
        // Current score
        updateScoreDisplay();
    }
    
    /* ---------------------------------------------------
     * Load answers vào list
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 15:50) - Thêm loading indicator
     * --------------------------------------------------- */
    private void loadAnswers() {
        if (submissionDetail == null || submissionDetail.getAnswers() == null) return;
        
        // Show loading for questions list
        questionsLoadingPane.setVisible(true);
        
        logger.info("=== LOAD ANSWERS ===");
        logger.info("Total answers: {}", submissionDetail.getAnswers().size());
        
        // Log từng answer để kiểm tra maxScore
        for (int i = 0; i < submissionDetail.getAnswers().size(); i++) {
            AnswerForGradingDTO answer = submissionDetail.getAnswers().get(i);
            logger.info("Answer[{}]: questionId={}, currentScore={}, maxScore={}, questionType={}", 
                i, answer.getQuestionId(), answer.getCurrentScore(), answer.getMaxScore(), answer.getQuestionType());
        }
        
        // Simulate loading delay để user thấy loading indicator (optional)
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        pause.setOnFinished(event -> {
            answers.setAll(submissionDetail.getAnswers());
            questionsList.setItems(answers);
            questionsLoadingPane.setVisible(false);
            
            logger.info("Answers loaded into ObservableList. Size: {}", answers.size());
        });
        pause.play();
    }
    
    /* ---------------------------------------------------
     * Lưu điểm số hiện tại vào answer object (tạm thời, chưa save lên server)
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 22:15) - Đơn giản hóa: chỉ set, không remove/add
     * --------------------------------------------------- */
    private void saveCurrentAnswerToModel() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= answers.size()) return;
        
        AnswerForGradingDTO currentAnswer = answers.get(currentQuestionIndex);
        if (currentAnswer == null) return;
        
        logger.info("=== SAVE CURRENT ANSWER TO MODEL ===");
        logger.info("Question Index: {}", currentQuestionIndex);
        logger.info("Current Answer - currentScore: {}, maxScore: {}", 
            currentAnswer.getCurrentScore(), currentAnswer.getMaxScore());
        
        // Lưu điểm số từ input - luôn parse và lưu (kể cả 0)
        Double newScore = 0.0; // Default là 0 nếu input rỗng
        try {
            String pointsText = pointsInput.getText().trim();
            logger.info("Points input text: '{}'", pointsText);
            if (!pointsText.isEmpty()) {
                newScore = Double.parseDouble(pointsText);
            }
        } catch (NumberFormatException e) {
            // Nếu parse lỗi, giữ nguyên giá trị cũ
            newScore = currentAnswer.getCurrentScore() != null ? currentAnswer.getCurrentScore() : 0.0;
            logger.warn("Failed to parse points text, using old score: {}", newScore);
        }
        
        // Lưu feedback
        String feedback = feedbackArea.getText();
        String newFeedback = (feedback != null && !feedback.trim().isEmpty()) ? feedback.trim() : null;
        
        // So sánh để xem có thay đổi không
        Double oldScore = currentAnswer.getCurrentScore();
        boolean scoreChanged = (oldScore == null && newScore != 0.0) || 
                              (oldScore != null && !oldScore.equals(newScore));
        boolean feedbackChanged = (newFeedback == null && currentAnswer.getFeedback() != null) ||
                                 (newFeedback != null && !newFeedback.equals(currentAnswer.getFeedback()));
        
        logger.info("New score: {}, Old score: {}, Score changed: {}", newScore, oldScore, scoreChanged);
        logger.info("Current maxScore before update: {}", currentAnswer.getMaxScore());
        
        // Tạo object mới với điểm số đã cập nhật
        AnswerForGradingDTO updatedAnswer = AnswerForGradingDTO.builder()
            .answerId(currentAnswer.getAnswerId())
            .questionId(currentAnswer.getQuestionId())
            .questionText(currentAnswer.getQuestionText())
            .questionType(currentAnswer.getQuestionType())
            .studentAnswer(currentAnswer.getStudentAnswer())
            .correctAnswer(currentAnswer.getCorrectAnswer())
            .currentScore(newScore)
            .maxScore(currentAnswer.getMaxScore())  // Giữ nguyên maxScore
            .isAutoGraded(currentAnswer.getIsAutoGraded())
            .feedback(newFeedback)
            .isCorrect(currentAnswer.getIsCorrect())
            .build();
        
        logger.info("Updated Answer - currentScore: {}, maxScore: {}", 
            updatedAnswer.getCurrentScore(), updatedAnswer.getMaxScore());
        
        // ĐƠN GIẢN: Chỉ set vào list, không remove/add - tránh side effects
        isUpdatingList = true;
        try {
            // Update trực tiếp vào list tại index hiện tại
            answers.set(currentQuestionIndex, updatedAnswer);
            
            // Cập nhật tổng điểm trong submissionDetail
            updateSubmissionTotalScore();
            
            // Refresh ListView để hiển thị điểm số mới
            questionsList.refresh();
            
            logger.info("Successfully updated answer at index {} in list", currentQuestionIndex);
        } finally {
            isUpdatingList = false;
        }
    }
    
    /* ---------------------------------------------------
     * Display question và answer trong detail area
     * LUÔN lấy answer từ answers list để đảm bảo dữ liệu mới nhất
     * @param answer Answer cần hiển thị (có thể là reference, nhưng sẽ lấy lại từ list)
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025) - Luôn lấy từ list để đảm bảo sync
     * --------------------------------------------------- */
    private void displayQuestion(AnswerForGradingDTO answer) {
        // LUÔN lấy answer từ list để đảm bảo có dữ liệu mới nhất
        if (currentQuestionIndex >= 0 && currentQuestionIndex < answers.size()) {
            answer = answers.get(currentQuestionIndex);
        }
        
        if (answer == null) {
            logger.warn("displayQuestion called with null answer");
            return;
        }
        
        logger.info("=== DISPLAY QUESTION ===");
        logger.info("Question Index: {}", currentQuestionIndex);
        logger.info("Answer - questionId: {}, currentScore: {}, maxScore: {}, questionType: {}", 
            answer.getQuestionId(), answer.getCurrentScore(), answer.getMaxScore(), answer.getQuestionType());
        
        // Question number
        questionNumberLabel.setText("Câu " + (currentQuestionIndex + 1));
        
        // Question type
        questionTypeLabel.setText("Loại: " + answer.getQuestionType());
        
        // Question content
        questionContentArea.getChildren().clear();
        Text questionText = new Text(answer.getQuestionText() != null ? answer.getQuestionText() : "");
        questionContentArea.getChildren().add(questionText);
        
        // Student answer
        studentAnswerArea.getChildren().clear();
        Text studentAnswerText = new Text(answer.getStudentAnswer() != null ? answer.getStudentAnswer() : "(Chưa trả lời)");
        studentAnswerArea.getChildren().add(studentAnswerText);
        
        // Correct answer (for reference)
        correctAnswerArea.getChildren().clear();
        if (answer.getCorrectAnswer() != null && !answer.getCorrectAnswer().isEmpty()) {
            Text correctLabel = new Text("Đáp án đúng: ");
            correctLabel.setStyle("-fx-font-weight: bold;");
            Text correctAnswerText = new Text(answer.getCorrectAnswer());
            correctAnswerArea.getChildren().addAll(correctLabel, correctAnswerText);
        }
        
        // Points input và Feedback - Set flag để tránh trigger listener khi program set text
        isUpdatingList = true;
        try {
            pointsInput.clear();
            Double maxScore = answer.getMaxScore();
            logger.info("MaxScore from answer: {}", maxScore);
            
            if (maxScore != null) {
                String maxScoreText = String.format("/ %.1f", maxScore);
                maxPointsLabel.setText(maxScoreText);
                logger.info("Set maxPointsLabel to: {}", maxScoreText);
                // Hiển thị điểm số nếu có (kể cả 0)
                if (answer.getCurrentScore() != null) {
                    String scoreText = String.valueOf(answer.getCurrentScore());
                    pointsInput.setText(scoreText);
                    logger.info("Set pointsInput to: {}", scoreText);
                } else {
                    pointsInput.setText("0");
                    logger.info("Set pointsInput to: 0 (currentScore is null)");
                }
            } else {
                maxPointsLabel.setText("/ ?");
                pointsInput.setText("0");
                logger.warn("MaxScore is NULL! Setting maxPointsLabel to '/ ?'");
            }
            
            // Feedback
            feedbackArea.clear();
            if (answer.getFeedback() != null && !answer.getFeedback().isEmpty()) {
                feedbackArea.setText(answer.getFeedback());
            }
        } finally {
            isUpdatingList = false;
        }
        
        // Check nếu submission đã được graded thì disable tất cả
        boolean isGraded = submissionDetail != null && 
                           submissionDetail.getStatus() == com.mstrust.client.teacher.dto.SubmissionStatus.GRADED;
        
        if (isGraded) {
            // Nếu đã graded, disable tất cả và hiển thị thông báo
            autoGradedLabel.setText("✓ Đã hoàn tất chấm điểm - Không thể chỉnh sửa");
            autoGradedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
            pointsInput.setEditable(false);
            pointsInput.setDisable(true);
            pointsInput.setStyle("-fx-background-color: #f5f5f5; -fx-opacity: 0.6;");
            feedbackArea.setEditable(false);
            feedbackArea.setDisable(true);
            feedbackArea.setStyle("-fx-background-color: #f5f5f5; -fx-opacity: 0.6;");
        } else {
            // Auto-graded indicator và disable input nếu đã auto-graded
            boolean isAutoGraded = answer.getIsAutoGraded() != null && answer.getIsAutoGraded();
            if (isAutoGraded) {
                autoGradedLabel.setText("✓ Đã chấm tự động");
                autoGradedLabel.setStyle("-fx-text-fill: #4CAF50;");
                // Disable input cho câu đã auto-graded
                pointsInput.setEditable(false);
                pointsInput.setDisable(true);
                pointsInput.setStyle("-fx-background-color: #f5f5f5; -fx-opacity: 0.7;");
                feedbackArea.setEditable(false);
                feedbackArea.setDisable(true);
                feedbackArea.setStyle("-fx-background-color: #f5f5f5; -fx-opacity: 0.7;");
            } else {
                autoGradedLabel.setText("⏳ Cần chấm thủ công");
                autoGradedLabel.setStyle("-fx-text-fill: #FF9800;");
                // Enable input cho câu cần chấm thủ công
                pointsInput.setEditable(true);
                pointsInput.setDisable(false);
                pointsInput.setStyle(""); // Reset style
                feedbackArea.setEditable(true);
                feedbackArea.setDisable(false);
                feedbackArea.setStyle(""); // Reset style
            }
        }
        
        // Navigation buttons
        previousQuestionButton.setDisable(currentQuestionIndex == 0);
        nextQuestionButton.setDisable(currentQuestionIndex >= answers.size() - 1);
    }
    
    /* ---------------------------------------------------
     * Update score display in header
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void updateScoreDisplay() {
        if (submissionDetail == null) return;
        
        Double current = submissionDetail.getCurrentScore();
        Double max = submissionDetail.getMaxScore();
        
        if (current != null && max != null) {
            currentScoreLabel.setText(String.format("Điểm: %.1f / %.1f", current, max));
        } else if (max != null) {
            currentScoreLabel.setText(String.format("Điểm: 0.0 / %.1f", max));
        } else {
            currentScoreLabel.setText("Điểm: - / -");
        }
    }
    
    /* ---------------------------------------------------
     * Cập nhật tổng điểm từ danh sách answers
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void updateSubmissionTotalScore() {
        if (submissionDetail == null || answers == null) return;
        
        // Tính tổng điểm từ tất cả answers
        double totalScore = answers.stream()
            .mapToDouble(answer -> answer.getCurrentScore() != null ? answer.getCurrentScore() : 0.0)
            .sum();
        
        // Cập nhật vào submissionDetail
        submissionDetail.setCurrentScore(totalScore);
        
        // Cập nhật hiển thị
        updateScoreDisplay();
        
        logger.debug("Updated total score: {}", totalScore);
    }
    
    /* ---------------------------------------------------
     * Handle previous question button
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 22:15) - Hủy debounce timer trước khi save
     * --------------------------------------------------- */
    @FXML
    private void handlePreviousQuestion() {
        // Hủy debounce timer nếu đang chạy để tránh conflict
        if (autoSavePause != null) {
            autoSavePause.stop();
        }
        
        // Lưu điểm số hiện tại trước khi chuyển
        saveCurrentAnswerToModel();
        
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            questionsList.getSelectionModel().select(currentQuestionIndex);
        }
    }
    
    /* ---------------------------------------------------
     * Handle next question button
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * EditBy: K24DTCN210-NVMANH (01/12/2025 22:15) - Hủy debounce timer trước khi save
     * --------------------------------------------------- */
    @FXML
    private void handleNextQuestion() {
        // Hủy debounce timer nếu đang chạy để tránh conflict
        if (autoSavePause != null) {
            autoSavePause.stop();
        }
        
        // Lưu điểm số hiện tại trước khi chuyển
        saveCurrentAnswerToModel();
        
        if (currentQuestionIndex < answers.size() - 1) {
            currentQuestionIndex++;
            questionsList.getSelectionModel().select(currentQuestionIndex);
        }
    }
    
    /* ---------------------------------------------------
     * Handle save button - save current answer grading
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleSave() {
        AnswerForGradingDTO currentAnswer = answers.get(currentQuestionIndex);
        if (currentAnswer == null) return;
        
        // Validate points input
        String pointsText = pointsInput.getText();
        if (pointsText == null || pointsText.trim().isEmpty()) {
            showError("Lỗi", "Vui lòng nhập điểm số");
            return;
        }
        
        // Parse and validate points
        try {
            double points = Double.parseDouble(pointsText.trim());
            
            if (points < 0) {
                showError("Lỗi", "Điểm phải lớn hơn hoặc bằng 0");
                return;
            }
            
            if (currentAnswer.getMaxScore() != null && points > currentAnswer.getMaxScore()) {
                showError("Lỗi", "Điểm phải từ 0 đến " + currentAnswer.getMaxScore());
                return;
            }
            
            // Create request (đảm bảo score không null)
            GradeAnswerRequest request = GradeAnswerRequest.builder()
                .score(points)
                .feedback(feedbackArea.getText() != null ? feedbackArea.getText().trim() : "")
                .build();
            
            // Log request để debug
            logger.info("=== HANDLE SAVE ===");
            logger.info("AnswerId: {}, Score: {}, Feedback: {}", 
                currentAnswer.getAnswerId(), request.getScore(), request.getFeedback());
            
            showLoading("Đang lưu...");
            
            new Thread(() -> {
                try {
                    apiClient.gradeAnswer(currentAnswer.getAnswerId(), request);
                    
                    Platform.runLater(() -> {
                        // Update local data - tạo answer object mới với điểm đã lưu
                        AnswerForGradingDTO updatedAnswer = AnswerForGradingDTO.builder()
                            .answerId(currentAnswer.getAnswerId())
                            .questionId(currentAnswer.getQuestionId())
                            .questionText(currentAnswer.getQuestionText())
                            .questionType(currentAnswer.getQuestionType())
                            .studentAnswer(currentAnswer.getStudentAnswer())
                            .correctAnswer(currentAnswer.getCorrectAnswer())
                            .currentScore(points)
                            .maxScore(currentAnswer.getMaxScore())
                            .isAutoGraded(currentAnswer.getIsAutoGraded())
                            .feedback(feedbackArea.getText() != null ? feedbackArea.getText().trim() : null)
                            .isCorrect(currentAnswer.getIsCorrect())
                            .build();
                        
                        // Cập nhật vào list tại vị trí hiện tại
                        answers.set(currentQuestionIndex, updatedAnswer);
                        
                        // Cập nhật tổng điểm trong submissionDetail (chỉ cập nhật local, không reload)
                        updateSubmissionTotalScore();
                        
                        // Refresh UI
                        questionsList.refresh();
                        
                        hideLoading();
                        showInfo("Thành công", "Đã lưu điểm cho câu " + (currentQuestionIndex + 1));
                        
                        logger.info("Successfully saved answer {} with score {} - local data preserved", 
                            currentQuestionIndex + 1, points);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        hideLoading();
                        showError("Lỗi", "Không thể lưu điểm: " + e.getMessage());
                        logger.error("Failed to save grade", e);
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showError("Lỗi", "Điểm không hợp lệ. Vui lòng nhập số.");
            logger.warn("Invalid points format: {}", pointsText, e);
        }
    }
    
    /* ---------------------------------------------------
     * Handle finalize button - finalize grading
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleFinalize() {
        // Check if all manual questions are graded
        long ungradedCount = answers.stream()
            .filter(a -> a.getIsAutoGraded() == null || !a.getIsAutoGraded())
            .filter(a -> a.getCurrentScore() == null || a.getCurrentScore() == 0)
            .count();
        
        if (ungradedCount > 0) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Còn " + ungradedCount + " câu chưa được chấm điểm.");
            confirm.setContentText("Bạn có muốn tiếp tục hoàn tất chấm điểm không?");
            
            if (confirm.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) 
                    == javafx.scene.control.ButtonType.CANCEL) {
                return;
            }
        }
        
        // Finalize grading
        FinalizeGradingRequest request = FinalizeGradingRequest.builder()
            .generalFeedback("")
            .build();
        
        showLoading("Đang hoàn tất chấm điểm...");
        
        new Thread(() -> {
            try {
                apiClient.finalizeGrading(submissionId, request);
                
                Platform.runLater(() -> {
                    hideLoading();
                    showInfo("Thành công", "Đã hoàn tất chấm điểm!");
                    // Reload to show updated status
                    loadSubmissionDetail();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Lỗi", "Không thể hoàn tất chấm điểm: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handle previous student button (TODO: implement navigation)
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handlePreviousStudent() {
        showInfo("Thông báo", "Chức năng chuyển học sinh trước sẽ được implement sau.");
    }
    
    /* ---------------------------------------------------
     * Handle next student button (TODO: implement navigation)
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleNextStudent() {
        showInfo("Thông báo", "Chức năng chuyển học sinh sau sẽ được implement sau.");
    }
    
    /* ---------------------------------------------------
     * Handle back button - quay lại submissions list
     * @author: K24DTCN210-NVMANH (01/12/2025 16:30)
     * --------------------------------------------------- */
    @FXML
    private void handleBack() {
        try {
            // Load submissions list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/submissions-list.fxml"));
            Parent submissionsView = loader.load();
            
            // Get controller và initialize
            com.mstrust.client.teacher.controller.grading.SubmissionsListController controller = 
                loader.getController();
            controller.initialize(apiClient, primaryStage);
            
            // Replace current view với submissions list
            javafx.scene.Node parent = backButton.getScene().lookup("#contentArea");
            if (parent instanceof StackPane) {
                StackPane contentArea = (StackPane) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(submissionsView);
            } else if (parent instanceof VBox) {
                VBox contentArea = (VBox) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(submissionsView);
            }
            
            logger.info("Successfully navigated back to submissions list");
        } catch (Exception e) {
            logger.error("Failed to navigate back", e);
            showError("Lỗi", "Không thể quay lại danh sách bài nộp: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Show loading overlay
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        Platform.runLater(() -> {
            loadingPane.setVisible(true);
            loadingMessage.setText(message);
        });
    }
    
    /* ---------------------------------------------------
     * Hide loading overlay
     * @author: K24DTCN210-NVMANH (01/12/2025)
     * --------------------------------------------------- */
    private void hideLoading() {
        Platform.runLater(() -> {
            loadingPane.setVisible(false);
        });
    }
    
    /* ---------------------------------------------------
     * Update UI state dựa trên submission status
     * Disable buttons nếu submission đã được finalize (GRADED)
     * @author: K24DTCN210-NVMANH (01/12/2025 15:43)
     * EditBy: K24DTCN210-NVMANH (01/12/2025) - Thêm disable navigation buttons và status label
     * --------------------------------------------------- */
    private void updateUIState() {
        if (submissionDetail == null) return;
        
        com.mstrust.client.teacher.dto.SubmissionStatus status = submissionDetail.getStatus();
        logger.info("=== UPDATE UI STATE ===");
        logger.info("Submission status: {}", status);
        
        boolean isGraded = (status == com.mstrust.client.teacher.dto.SubmissionStatus.GRADED);
        
        if (isGraded) {
            // Disable tất cả controls liên quan đến editing
            pointsInput.setDisable(true);
            feedbackArea.setDisable(true);
            saveButton.setDisable(true);
            finalizeButton.setDisable(true);
            
            // Disable navigation buttons
            previousQuestionButton.setDisable(true);
            nextQuestionButton.setDisable(true);
            previousStudentButton.setDisable(true);
            nextStudentButton.setDisable(true);
            
            // Update style để show readonly state
            pointsInput.setStyle("-fx-opacity: 0.6; -fx-background-color: #f5f5f5;");
            feedbackArea.setStyle("-fx-opacity: 0.6; -fx-background-color: #f5f5f5;");
            
            // Show status label in header
            if (statusLabel != null) {
                statusLabel.setText("✓ ĐÃ CHẤM");
                statusLabel.setVisible(true);
            }
            
            // Show warning label
            autoGradedLabel.setText("✓ Đã hoàn tất chấm điểm - Không thể chỉnh sửa");
            autoGradedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            logger.info("UI set to READ-ONLY mode (submission already graded)");
        } else {
            // Enable editing
            pointsInput.setDisable(false);
            feedbackArea.setDisable(false);
            saveButton.setDisable(false);
            finalizeButton.setDisable(false);
            
            // Enable navigation buttons
            previousQuestionButton.setDisable(false);
            nextQuestionButton.setDisable(false);
            previousStudentButton.setDisable(false);
            nextStudentButton.setDisable(false);
            
            // Hide status label
            if (statusLabel != null) {
                statusLabel.setVisible(false);
            }
            
            logger.info("UI set to EDITABLE mode (submission not yet finalized)");
        }
    }
    
    /* ---------------------------------------------------
     * Show error alert
     * @author: K24DTCN210-NVMANH (01/12/2025)
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
     * Show info alert
     * @author: K24DTCN210-NVMANH (01/12/2025)
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
}

