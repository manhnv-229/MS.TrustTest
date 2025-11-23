package com.mstrust.client.exam.component;

import com.mstrust.client.exam.dto.QuestionDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/* ---------------------------------------------------
 * Question Display Component - Hiển thị câu hỏi + answer input
 * - Display question number, content, points
 * - Embed answer input widget từ AnswerInputFactory
 * - "Mark for review" checkbox
 * - Extract current answer
 * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
 * --------------------------------------------------- */
public class QuestionDisplayComponent extends VBox {
    
    private Label questionHeaderLabel;
    private TextFlow questionContentFlow;
    private VBox answerContainer;
    private CheckBox markForReviewCheckbox;
    private Node currentAnswerWidget;
    private QuestionDTO currentQuestion;

    /* ---------------------------------------------------
     * Constructor - khởi tạo component
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public QuestionDisplayComponent() {
        initializeUI();
        applyStyles();
    }

    /* ---------------------------------------------------
     * Khởi tạo UI components
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    private void initializeUI() {
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        
        // Question header (Câu 1 - 5 điểm)
        questionHeaderLabel = new Label();
        questionHeaderLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2196F3;"
        );
        
        // Question content
        questionContentFlow = new TextFlow();
        questionContentFlow.setPadding(new Insets(10, 0, 10, 0));
        
        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        
        // Answer container (will hold answer input widget)
        answerContainer = new VBox(10);
        answerContainer.setPadding(new Insets(10));
        answerContainer.setStyle(
            "-fx-background-color: #f9f9f9; " +
            "-fx-border-color: #ddd; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        Label answerLabel = new Label("📝 CÂU TRẢ LỜI:");
        answerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        answerContainer.getChildren().add(answerLabel);
        
        // Mark for review checkbox
        markForReviewCheckbox = new CheckBox("🔖 Đánh dấu để xem lại sau");
        markForReviewCheckbox.setStyle("-fx-font-size: 12px;");
        
        // Add all to main container
        this.getChildren().addAll(
            questionHeaderLabel,
            questionContentFlow,
            separator,
            answerContainer,
            markForReviewCheckbox
        );
        
        // Wrap in ScrollPane for long content
        ScrollPane scrollPane = new ScrollPane(this);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
    }

    /* ---------------------------------------------------
     * Hiển thị câu hỏi
     * @param question QuestionDTO cần hiển thị
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void displayQuestion(QuestionDTO question) {
        if (question == null) {
            showEmptyState();
            return;
        }
        
        this.currentQuestion = question;
        
        // Update header
        String header = String.format("Câu %d", question.getOrderNumber());
        if (question.getPoints() != null) {
            header += String.format(" - %.1f điểm", question.getPoints());
        }
        questionHeaderLabel.setText(header);
        
        // Update content
        updateQuestionContent(question.getContent());
        
        // Create and embed answer widget
        currentAnswerWidget = AnswerInputFactory.createInputWidget(question);
        
        // Clear answer container and add new widget
        answerContainer.getChildren().clear();
        
        Label answerLabel = new Label("📝 CÂU TRẢ LỜI:");
        answerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        answerContainer.getChildren().addAll(answerLabel, currentAnswerWidget);
        
        // Reset mark checkbox
        markForReviewCheckbox.setSelected(false);
    }

    /* ---------------------------------------------------
     * Update question content với word wrap
     * @param content Question content text
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    private void updateQuestionContent(String content) {
        questionContentFlow.getChildren().clear();
        
        if (content == null || content.isEmpty()) {
            content = "[Nội dung câu hỏi không có]";
        }
        
        Text text = new Text(content);
        text.setStyle("-fx-font-size: 14px; -fx-line-spacing: 1.5;");
        text.setWrappingWidth(700); // Enable word wrap
        
        questionContentFlow.getChildren().add(text);
    }

    /* ---------------------------------------------------
     * Hiển thị empty state
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    private void showEmptyState() {
        questionHeaderLabel.setText("Không có câu hỏi");
        questionContentFlow.getChildren().clear();
        answerContainer.getChildren().clear();
        
        Label emptyLabel = new Label("Chọn một câu hỏi để bắt đầu");
        emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        emptyLabel.setAlignment(Pos.CENTER);
        answerContainer.getChildren().add(emptyLabel);
        
        markForReviewCheckbox.setSelected(false);
        markForReviewCheckbox.setDisable(true);
    }

    /* ---------------------------------------------------
     * Get câu trả lời hiện tại
     * @returns String là answer value
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public String getCurrentAnswer() {
        if (currentQuestion == null || currentAnswerWidget == null) {
            return null;
        }
        
        return AnswerInputFactory.extractAnswer(
            currentAnswerWidget, 
            currentQuestion.getType()
        );
    }

    /* ---------------------------------------------------
     * Set câu trả lời (restore từ cache)
     * @param answerValue Answer value cần restore
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void setCurrentAnswer(String answerValue) {
        if (currentQuestion == null || currentAnswerWidget == null) {
            return;
        }
        
        AnswerInputFactory.setAnswer(
            currentAnswerWidget,
            currentQuestion.getType(),
            answerValue
        );
    }

    /* ---------------------------------------------------
     * Check xem đã trả lời chưa
     * @returns true nếu có answer
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public boolean hasAnswer() {
        String answer = getCurrentAnswer();
        return answer != null && !answer.trim().isEmpty();
    }

    /* ---------------------------------------------------
     * Get marked for review status
     * @returns true nếu được đánh dấu
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public boolean isMarkedForReview() {
        return markForReviewCheckbox.isSelected();
    }

    /* ---------------------------------------------------
     * Set marked for review status
     * @param marked true để đánh dấu
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void setMarkedForReview(boolean marked) {
        markForReviewCheckbox.setSelected(marked);
    }

    /* ---------------------------------------------------
     * Get current question
     * @returns QuestionDTO hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public QuestionDTO getCurrentQuestion() {
        return currentQuestion;
    }

    /* ---------------------------------------------------
     * Clear component về trạng thái rỗng
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void clear() {
        currentQuestion = null;
        currentAnswerWidget = null;
        showEmptyState();
    }

    /* ---------------------------------------------------
     * Apply CSS styles
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    private void applyStyles() {
        this.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #ddd; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
    }

    /* ---------------------------------------------------
     * Get answer container (để test hoặc custom)
     * @returns VBox là answer container
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public VBox getAnswerContainer() {
        return answerContainer;
    }

    /* ---------------------------------------------------
     * Get mark checkbox (để custom event handlers)
     * @returns CheckBox
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public CheckBox getMarkForReviewCheckbox() {
        return markForReviewCheckbox;
    }

    /* ---------------------------------------------------
     * Set enabled state cho inputs
     * @param enabled true để enable
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void setInputsEnabled(boolean enabled) {
        if (currentAnswerWidget != null) {
            currentAnswerWidget.setDisable(!enabled);
        }
        markForReviewCheckbox.setDisable(!enabled);
    }

    /* ---------------------------------------------------
     * Focus vào answer input
     * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
     * --------------------------------------------------- */
    public void focusAnswerInput() {
        if (currentAnswerWidget != null) {
            currentAnswerWidget.requestFocus();
        }
    }
}
