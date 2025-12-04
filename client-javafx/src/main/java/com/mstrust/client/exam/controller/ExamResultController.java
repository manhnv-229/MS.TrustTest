package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.dto.ExamResultDTO;
import com.mstrust.client.exam.util.TimeFormatter;
import com.mstrust.client.util.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/* ---------------------------------------------------
 * Controller cho Exam Result Screen
 * - Load result từ backend API
 * - Display submission info
 * - Display grading status (in progress / graded)
 * - Display score và chi tiết nếu đã chấm
 * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
 * EditBy: K24DTCN210-NVMANH (04/12/2025 22:45) - Handle "Results not available" gracefully
 * --------------------------------------------------- */
public class ExamResultController {

    // FXML injected nodes
    @FXML private Label examTitleLabel;
    @FXML private Label submissionTimeLabel;
    @FXML private Label submissionIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label submitTimeDetailLabel;
    
    // Grading status card
    @FXML private VBox gradingStatusCard;
    @FXML private VBox gradingInProgressContainer;
    @FXML private VBox gradedContainer;
    
    // Score labels (when graded)
    @FXML private Label scoreLabel;
    @FXML private Label maxScoreLabel;
    @FXML private Label percentageLabel;
    @FXML private Label gradeLabel;
    
    // Question results
    @FXML private VBox questionResultsCard;
    @FXML private VBox questionResultsContainer;
    
    @FXML private Button backToListButton;
    
    // Data
    private ExamApiClient apiClient;
    private Long submissionId;
    private ExamResultDTO resultData;
    
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");

    /* ---------------------------------------------------
     * Initialize controller với submissionId và authToken
     * @param submissionId ID của submission
     * @param authToken Bearer token
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    public void initialize(Long submissionId, String authToken) {
        this.submissionId = submissionId;
        this.apiClient = new ExamApiClient(authToken);
        
        // Load result from API
        loadExamResult();
    }

    /* ---------------------------------------------------
     * Load exam result từ backend
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * EditBy: K24DTCN210-NVMANH (04/12/2025) - Handle "Results not available" error
     * --------------------------------------------------- */
    private void loadExamResult() {
        new Thread(() -> {
            try {
                // Call API to get result
                ExamApiClient.ExamResultResponse apiResponse = 
                        apiClient.getExamResult(submissionId);
                
                // Convert to DTO
                resultData = convertToDTO(apiResponse);
                
                // Update UI on JavaFX thread
                Platform.runLater(this::displayResult);
                
            } catch (IOException e) {
                // Check for "Results are not available yet" error
                if (e.getMessage() != null && e.getMessage().contains("Results are not available yet")) {
                    Platform.runLater(this::displaySubmissionSuccessOnly);
                } else {
                    Platform.runLater(() -> {
                        showError("Lỗi tải kết quả", 
                                "Không thể tải kết quả bài thi: " + e.getMessage());
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    showError("Lỗi tải kết quả", "Bị gián đoạn khi tải kết quả");
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Convert API response to DTO
     * @param apiResponse Response từ API
     * @returns ExamResultDTO
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private ExamResultDTO convertToDTO(ExamApiClient.ExamResultResponse apiResponse) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setSubmissionId(apiResponse.getSubmissionId());
        dto.setExamTitle(apiResponse.getExamTitle());
        dto.setTotalScore(apiResponse.getTotalScore());
        dto.setMaxScore(apiResponse.getMaxScore());
        dto.setStatus(apiResponse.getStatus());
        dto.setSubmittedAt(apiResponse.getSubmittedAt());
        
        // Convert answers if present
        if (apiResponse.getAnswers() != null && !apiResponse.getAnswers().isEmpty()) {
            java.util.List<ExamResultDTO.AnswerResultDTO> answerDTOs = 
                    new java.util.ArrayList<>();
            
            for (ExamApiClient.AnswerResult apiAnswer : apiResponse.getAnswers()) {
                ExamResultDTO.AnswerResultDTO answerDTO = 
                        new ExamResultDTO.AnswerResultDTO();
                answerDTO.setQuestionId(apiAnswer.getQuestionId());
                answerDTO.setQuestionContent(apiAnswer.getQuestionContent());
                answerDTO.setStudentAnswer(apiAnswer.getStudentAnswer());
                answerDTO.setCorrectAnswer(apiAnswer.getCorrectAnswer());
                answerDTO.setScore(apiAnswer.getScore());
                answerDTO.setMaxScore(apiAnswer.getMaxScore());
                answerDTO.setFeedback(apiAnswer.getFeedback());
                answerDTOs.add(answerDTO);
            }
            
            dto.setAnswers(answerDTOs);
        }
        
        return dto;
    }

    /* ---------------------------------------------------
     * Display result data on UI
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private void displayResult() {
        if (resultData == null) return;
        
        // 1. Update header
        examTitleLabel.setText(resultData.getExamTitle());
        if (resultData.getSubmittedAt() != null) {
            submissionTimeLabel.setText("Nộp bài lúc: " + 
                    resultData.getSubmittedAt().format(TIME_FORMATTER));
        }
        
        // 2. Update submission info
        submissionIdLabel.setText(String.valueOf(resultData.getSubmissionId()));
        
        if (resultData.getSubmittedAt() != null) {
            submitTimeDetailLabel.setText(
                    resultData.getSubmittedAt().format(TIME_FORMATTER));
        }
        
        // 3. Update grading status
        if (resultData.isGraded()) {
            displayGradedResult();
        } else {
            displayGradingInProgress();
        }
    }

    /* ---------------------------------------------------
     * Display submission success only (when result not available)
     * @author: K24DTCN210-NVMANH (04/12/2025)
     * --------------------------------------------------- */
    private void displaySubmissionSuccessOnly() {
        // Hide graded container
        gradedContainer.setVisible(false);
        gradedContainer.setManaged(false);
        
        // Show grading in progress container (reused for message)
        gradingInProgressContainer.setVisible(true);
        gradingInProgressContainer.setManaged(true);
        
        // Hide question results
        questionResultsCard.setVisible(false);
        questionResultsCard.setManaged(false);
        
        // Update status label
        statusLabel.setText("Nộp bài thành công! Kết quả chưa được công bố.");
        statusLabel.getStyleClass().removeAll("status-submitted", "status-graded");
        statusLabel.getStyleClass().add("status-grading");
        
        // Update other labels if possible (we don't have resultData, so clear them or set defaults)
        examTitleLabel.setText("Kết Quả Bài Thi");
        submissionTimeLabel.setText("");
        submissionIdLabel.setText(String.valueOf(submissionId));
        submitTimeDetailLabel.setText("");
    }

    /* ---------------------------------------------------
     * Display grading in progress state
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private void displayGradingInProgress() {
        // Hide graded container
        gradedContainer.setVisible(false);
        gradedContainer.setManaged(false);
        
        // Show grading in progress container
        gradingInProgressContainer.setVisible(true);
        gradingInProgressContainer.setManaged(true);
        
        // Hide question results
        questionResultsCard.setVisible(false);
        questionResultsCard.setManaged(false);
        
        // Update status label
        statusLabel.setText("Đã nộp - Đang chờ chấm điểm ⏳");
        statusLabel.getStyleClass().removeAll("status-submitted", "status-graded");
        statusLabel.getStyleClass().add("status-grading");
    }

    /* ---------------------------------------------------
     * Display graded result
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private void displayGradedResult() {
        // Hide grading in progress
        gradingInProgressContainer.setVisible(false);
        gradingInProgressContainer.setManaged(false);
        
        // Show graded container
        gradedContainer.setVisible(true);
        gradedContainer.setManaged(true);
        
        // Update score labels
        if (resultData.getTotalScore() != null) {
            scoreLabel.setText(String.format("%.1f", resultData.getTotalScore()));
        }
        if (resultData.getMaxScore() != null) {
            maxScoreLabel.setText(String.format("%.1f", resultData.getMaxScore()));
        }
        
        // Update percentage
        double percentage = resultData.getPercentage();
        percentageLabel.setText(String.format("%.1f%%", percentage));
        
        // Update grade
        String grade = resultData.getGrade();
        gradeLabel.setText(grade);
        
        // Apply color based on grade
        gradeLabel.getStyleClass().removeAll(
                "grade-a", "grade-b", "grade-c", "grade-d", "grade-f");
        gradeLabel.getStyleClass().add("grade-" + grade.toLowerCase());
        
        // Update status label
        statusLabel.setText("Đã chấm điểm ✓");
        statusLabel.getStyleClass().removeAll("status-submitted", "status-grading");
        statusLabel.getStyleClass().add("status-graded");
        
        // Display question results if available
        if (resultData.getAnswers() != null && !resultData.getAnswers().isEmpty()) {
            displayQuestionResults();
        }
    }

    /* ---------------------------------------------------
     * Display chi tiết kết quả từng câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private void displayQuestionResults() {
        questionResultsCard.setVisible(true);
        questionResultsCard.setManaged(true);
        
        questionResultsContainer.getChildren().clear();
        
        int questionNumber = 1;
        for (ExamResultDTO.AnswerResultDTO answer : resultData.getAnswers()) {
            VBox questionBox = createQuestionResultBox(questionNumber++, answer);
            questionResultsContainer.getChildren().add(questionBox);
        }
    }

    /* ---------------------------------------------------
     * Tạo box hiển thị kết quả 1 câu hỏi
     * @param questionNumber Số thứ tự câu hỏi
     * @param answer Dữ liệu câu trả lời
     * @returns VBox chứa UI của câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private VBox createQuestionResultBox(int questionNumber, 
                                         ExamResultDTO.AnswerResultDTO answer) {
        VBox box = new VBox(10);
        box.getStyleClass().add("question-result-box");
        box.setPadding(new Insets(15));
        
        // Header: Question number + status icon + score
        HBox header = new HBox(15);
        header.setStyle("-fx-alignment: center-left;");
        
        Label numberLabel = new Label("Câu " + questionNumber);
        numberLabel.getStyleClass().add("question-number");
        
        Label statusIcon = new Label(getStatusIcon(answer.getAnswerStatus()));
        statusIcon.setStyle("-fx-font-size: 18px;");
        
        Label scoreText = new Label(String.format("%.1f/%.1f điểm", 
                answer.getScore() != null ? answer.getScore() : 0.0,
                answer.getMaxScore() != null ? answer.getMaxScore() : 0.0));
        scoreText.getStyleClass().add("question-score");
        
        header.getChildren().addAll(numberLabel, statusIcon, scoreText);
        
        // Question content (truncated if too long)
        String content = answer.getQuestionContent();
        if (content != null && content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("question-content-small");
        
        // Student answer
        VBox answerBox = new VBox(5);
        Label answerTitle = new Label("Câu trả lời của bạn:");
        answerTitle.getStyleClass().add("answer-title");
        
        String studentAnswer = answer.getStudentAnswer();
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            studentAnswer = "(Chưa trả lời)";
        } else if (studentAnswer.length() > 200) {
            studentAnswer = studentAnswer.substring(0, 200) + "...";
        }
        
        Label answerLabel = new Label(studentAnswer);
        answerLabel.setWrapText(true);
        answerLabel.getStyleClass().add("student-answer");
        
        answerBox.getChildren().addAll(answerTitle, answerLabel);
        
        // Feedback if exists
        if (answer.getFeedback() != null && !answer.getFeedback().trim().isEmpty()) {
            VBox feedbackBox = new VBox(5);
            Label feedbackTitle = new Label("Nhận xét:");
            feedbackTitle.getStyleClass().add("feedback-title");
            
            Label feedbackLabel = new Label(answer.getFeedback());
            feedbackLabel.setWrapText(true);
            feedbackLabel.getStyleClass().add("feedback-text");
            
            feedbackBox.getChildren().addAll(feedbackTitle, feedbackLabel);
            box.getChildren().addAll(header, contentLabel, answerBox, feedbackBox);
        } else {
            box.getChildren().addAll(header, contentLabel, answerBox);
        }
        
        // Apply status style
        box.getStyleClass().add("status-" + answer.getAnswerStatus());
        
        return box;
    }

    /* ---------------------------------------------------
     * Get status icon based on answer status
     * @param status "correct", "partial", "incorrect", "unanswered"
     * @returns Emoji icon
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private String getStatusIcon(String status) {
        return switch (status) {
            case "correct" -> "✅";
            case "partial" -> "🟡";
            case "incorrect" -> "❌";
            case "unanswered" -> "⚪";
            default -> "❓";
        };
    }

    /* ---------------------------------------------------
     * Handle back to exam list button
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    @FXML
    private void onBackToExamList() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/exam-list.fxml"));
            Parent root = loader.load();
            
            ExamListController controller = loader.getController();
            controller.initialize(apiClient);
            
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) backToListButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Danh Sách Đề Thi - MS.TrustTest");
            
        } catch (IOException e) {
            showError("Lỗi điều hướng", 
                    "Không thể quay lại danh sách đề thi: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param title Tiêu đề
     * @param content Nội dung lỗi
     * @author: K24DTCN210-NVMANH (23/11/2025 18:55)
     * --------------------------------------------------- */
    private void showError(String title, String content) {
        DialogUtils.showError(title, content);
    }
}
