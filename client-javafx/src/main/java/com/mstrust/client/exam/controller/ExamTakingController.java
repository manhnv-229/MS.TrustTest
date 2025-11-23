package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.component.QuestionDisplayComponent;
import com.mstrust.client.exam.component.QuestionPaletteComponent;
import com.mstrust.client.exam.component.TimerComponent;
import com.mstrust.client.exam.dto.QuestionDTO;
import com.mstrust.client.exam.dto.SaveAnswerRequest;
import com.mstrust.client.exam.dto.StartExamResponse;
import com.mstrust.client.exam.model.ExamSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/* ---------------------------------------------------
 * Exam Taking Controller - Main controller cho exam taking interface
 * - Initialize exam session (call startExam API)
 * - Load questions từ backend
 * - Manage ExamSession model
 * - Coordinate 3 components: Timer + Palette + QuestionDisplay
 * - Handle navigation (Previous/Next/Jump)
 * - Handle save answer (manual + auto)
 * - Handle submit exam
 * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
 * --------------------------------------------------- */
public class ExamTakingController {

    // FXML injected nodes
    @FXML private HBox timerContainer;
    @FXML private Label examTitleLabel;
    @FXML private Label studentInfoLabel;
    @FXML private VBox paletteContainer;
    @FXML private VBox questionDisplayContainer;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button saveButton;
    @FXML private Button submitButton;
    
    // Components
    private TimerComponent timerComponent;
    private QuestionPaletteComponent paletteComponent;
    private QuestionDisplayComponent questionDisplayComponent;
    
    // Model & API
    private ExamSession examSession;
    private ExamApiClient apiClient;
    
    // State tracking
    private Map<Long, String> answersCache; // questionId -> answer
    private Map<Long, Boolean> markedForReview; // questionId -> marked
    private boolean isAutoSaveEnabled = true;
    private Thread autoSaveThread;

    /* ---------------------------------------------------
     * Constructor
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    public ExamTakingController() {
        this.answersCache = new HashMap<>();
        this.markedForReview = new HashMap<>();
    }

    /* ---------------------------------------------------
     * Initialize exam với examId và authToken
     * Called từ ExamListController
     * @param examId ID của đề thi
     * @param authToken Bearer token
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    public void initializeExam(Long examId, String authToken) {
        this.apiClient = new ExamApiClient(authToken);
        
        // Show loading
        showLoading("Đang khởi tạo bài thi...");
        
        new Thread(() -> {
            try {
                // 1. Start exam (POST /api/exam-taking/start/{examId})
                StartExamResponse response = apiClient.startExam(examId);
                
                // 2. Load questions (GET /api/exam-taking/questions/{submissionId})  
                List<QuestionDTO> questions = apiClient.getQuestionsForSubmission(response.getSubmissionId());
                
                // 3. Create ExamSession model
                examSession = new ExamSession();
                examSession.setSubmissionId(response.getSubmissionId());
                examSession.setExamTitle(response.getExamTitle());
                examSession.setQuestions(questions);
                examSession.setRemainingSeconds(response.getRemainingSeconds().longValue());
                examSession.setCurrentQuestionIndex(0);
                
                // 4. Initialize UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        initializeComponents(response);
                        displayCurrentQuestion();
                        startAutoSave(response.getAutoSaveIntervalSeconds());
                        hideLoading();
                    } catch (Exception e) {
                        showError("Lỗi khởi tạo UI", e.getMessage());
                    }
                });
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Lỗi khởi tạo bài thi", e.getMessage());
                    hideLoading();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    showError("Lỗi khởi tạo bài thi", "Bị gián đoạn: " + e.getMessage());
                    hideLoading();
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Initialize các components (Timer, Palette, QuestionDisplay)
     * @param response StartExamResponse từ API
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void initializeComponents(StartExamResponse response) {
        // 1. Initialize Timer Component
        long totalSeconds = response.getDurationMinutes() * 60L;
        timerComponent = new TimerComponent(totalSeconds);
        timerComponent.setOnTimeExpired(this::handleTimeExpired);
        timerContainer.getChildren().clear();
        timerContainer.getChildren().add(timerComponent);
        
        // 2. Initialize Question Palette Component
        int totalQuestions = examSession.getQuestions().size();
        paletteComponent = new QuestionPaletteComponent(totalQuestions);
        paletteComponent.setOnQuestionClick(this::jumpToQuestion);
        paletteContainer.getChildren().clear();
        paletteContainer.getChildren().add(paletteComponent);
        
        // 3. Initialize Question Display Component
        questionDisplayComponent = new QuestionDisplayComponent();
        questionDisplayContainer.getChildren().clear();
        questionDisplayContainer.getChildren().add(questionDisplayComponent);
        
        // 4. Update header info
        examTitleLabel.setText(response.getExamTitle());
        studentInfoLabel.setText("Thí sinh: " + getCurrentStudentName());
        
        // 5. Setup button states
        updateNavigationButtons();
    }

    /* ---------------------------------------------------
     * Hiển thị câu hỏi hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void displayCurrentQuestion() {
        if (examSession == null || examSession.getQuestions().isEmpty()) {
            return;
        }
        
        int index = examSession.getCurrentQuestionIndex();
        QuestionDTO question = examSession.getQuestions().get(index);
        
        // Update palette
        paletteComponent.setCurrentQuestion(index);
        
        // Display question
        questionDisplayComponent.displayQuestion(question);
        
        // Restore answer từ cache (nếu có)
        String cachedAnswer = answersCache.get(question.getId());
        if (cachedAnswer != null) {
            questionDisplayComponent.setCurrentAnswer(cachedAnswer);
        }
        
        // Restore mark status
        Boolean marked = markedForReview.get(question.getId());
        if (marked != null && marked) {
            questionDisplayComponent.setMarkedForReview(true);
        }
        
        // Update navigation buttons
        updateNavigationButtons();
        
        // Focus vào answer input
        questionDisplayComponent.focusAnswerInput();
    }

    /* ---------------------------------------------------
     * Handle Previous button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onPrevious() {
        saveCurrentAnswer();
        examSession.previousQuestion();
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Handle Next button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onNext() {
        saveCurrentAnswer();
        examSession.nextQuestion();
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Handle manual Save button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onSave() {
        saveButton.setDisable(true);
        saveCurrentAnswer();
        
        // Re-enable sau 1s
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> saveButton.setDisable(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> saveButton.setDisable(false));
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Save câu trả lời hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void saveCurrentAnswer() {
        QuestionDTO currentQuestion = questionDisplayComponent.getCurrentQuestion();
        if (currentQuestion == null) return;
        
        String answer = questionDisplayComponent.getCurrentAnswer();
        boolean marked = questionDisplayComponent.isMarkedForReview();
        
        // Cache answer locally
        if (answer != null && !answer.isEmpty()) {
            answersCache.put(currentQuestion.getId(), answer);
        }
        markedForReview.put(currentQuestion.getId(), marked);
        
        // Update palette status
        int index = examSession.getCurrentQuestionIndex();
        if (answer != null && !answer.isEmpty()) {
            paletteComponent.updateQuestionStatus(index, marked ? "marked" : "answered");
        } else {
            paletteComponent.updateQuestionStatus(index, marked ? "marked" : "unanswered");
        }
        
        // Send to backend (background thread)
        new Thread(() -> {
            try {
                SaveAnswerRequest request = SaveAnswerRequest.builder()
                        .questionId(currentQuestion.getId())
                        .answerText(answer)
                        .isAutoSave(false)
                        .build();
                
                apiClient.saveAnswer(examSession.getSubmissionId(), request);
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    System.err.println("Lỗi lưu câu trả lời: " + e.getMessage());
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Jump to specific question (từ palette click)
     * @param questionIndex Index của câu hỏi (0-based)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void jumpToQuestion(int questionIndex) {
        saveCurrentAnswer();
        examSession.jumpToQuestion(questionIndex);
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Update navigation buttons state
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void updateNavigationButtons() {
        if (examSession == null) return;
        
        int current = examSession.getCurrentQuestionIndex();
        int total = examSession.getQuestions().size();
        
        previousButton.setDisable(current == 0);
        nextButton.setDisable(current == total - 1);
    }

    /* ---------------------------------------------------
     * Handle Submit exam button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onSubmit() {
        // Save current answer first
        saveCurrentAnswer();
        
        // Confirmation dialog
        int answered = paletteComponent.getAnsweredCount();
        int total = examSession.getQuestions().size();
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận nộp bài");
        alert.setHeaderText("Bạn có chắc chắn muốn nộp bài?");
        alert.setContentText(String.format(
            "Đã trả lời: %d/%d câu\n" +
            "Câu chưa trả lời: %d câu\n\n" +
            "Sau khi nộp bài, bạn không thể chỉnh sửa!",
            answered, total, total - answered
        ));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            submitExam();
        }
    }

    /* ---------------------------------------------------
     * Submit exam to backend
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void submitExam() {
        submitButton.setDisable(true);
        showLoading("Đang nộp bài...");
        
        new Thread(() -> {
            try {
                apiClient.submitExam(examSession.getSubmissionId());
                
                Platform.runLater(() -> {
                    stopAutoSave();
                    timerComponent.stop();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Nộp bài thành công");
                    alert.setHeaderText("Bài thi đã được nộp!");
                    alert.setContentText("Vui lòng chờ kết quả chấm điểm.");
                    alert.showAndWait();
                    
                    // TODO: Navigate to results screen or exam list
                    hideLoading();
                });
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    showError("Lỗi nộp bài", e.getMessage());
                    hideLoading();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    showError("Lỗi nộp bài", "Bị gián đoạn");
                    hideLoading();
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Handle khi hết giờ (auto-submit)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void handleTimeExpired() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Hết giờ");
            alert.setHeaderText("Thời gian làm bài đã hết!");
            alert.setContentText("Bài thi sẽ tự động được nộp.");
            alert.show();
            
            // Auto submit
            submitExam();
        });
    }

    /* ---------------------------------------------------
     * Start auto-save thread
     * @param intervalSeconds Interval in seconds (e.g., 30)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void startAutoSave(Integer intervalSeconds) {
        if (intervalSeconds == null || intervalSeconds <= 0) {
            intervalSeconds = 30; // Default 30 seconds
        }
        
        final int interval = intervalSeconds * 1000; // Convert to milliseconds
        
        autoSaveThread = new Thread(() -> {
            while (isAutoSaveEnabled && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(interval);
                    
                    Platform.runLater(() -> {
                        if (isAutoSaveEnabled) {
                            saveCurrentAnswer();
                        }
                    });
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }

    /* ---------------------------------------------------
     * Stop auto-save thread
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void stopAutoSave() {
        isAutoSaveEnabled = false;
        if (autoSaveThread != null && autoSaveThread.isAlive()) {
            autoSaveThread.interrupt();
        }
    }

    /* ---------------------------------------------------
     * Get current student name (mock - replace with actual logic)
     * @returns Student name
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private String getCurrentStudentName() {
        // TODO: Get from authentication context
        return "Nguyễn Văn A";
    }

    /* ---------------------------------------------------
     * Show loading overlay
     * @param message Loading message
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void showLoading(String message) {
        // TODO: Implement loading overlay
        System.out.println("Loading: " + message);
    }

    /* ---------------------------------------------------
     * Hide loading overlay
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void hideLoading() {
        // TODO: Hide loading overlay
        System.out.println("Loading complete");
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param title Error title
     * @param content Error content
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Cleanup khi controller destroyed
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    public void shutdown() {
        stopAutoSave();
        if (timerComponent != null) {
            timerComponent.stop();
        }
    }
}
