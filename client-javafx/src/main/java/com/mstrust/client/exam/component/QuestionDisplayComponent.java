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
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Question Display Component - Hiển thị câu hỏi + answer input
 * - Display question number, content, points
 * - Embed answer input widget từ AnswerInputFactory
 * - "Mark for review" checkbox
 * - Extract current answer
 * @author: K24DTCN210-NVMANH (23/11/2025 13:48)
 * EditBy: K24DTCN210-NVMANH (24/11/2025 14:51) - Phase 8.6: Added answer change listener
 * --------------------------------------------------- */
public class QuestionDisplayComponent extends VBox {
    
    private Label questionHeaderLabel;
    private TextFlow questionContentFlow;
    private VBox answerContainer;
    private CheckBox markForReviewCheckbox;
    private Label saveStatusLabel; // Phase 8.6: Save status indicator
    private Node currentAnswerWidget;
    private QuestionDTO currentQuestion;
    
    // Phase 8.6: Callback for answer changes
    private Consumer<String> onAnswerChanged;

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
        
        // Phase 8.6: Save status indicator
        saveStatusLabel = new Label("💾 Chưa lưu");
        saveStatusLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #FF9800; " +
            "-fx-padding: 5px 10px; " +
            "-fx-background-color: #FFF3E0; " +
            "-fx-background-radius: 3px; " +
            "-fx-border-color: #FFE0B2; " +
            "-fx-border-radius: 3px;"
        );
        saveStatusLabel.setVisible(true);
        
        // Add all to main container
        this.getChildren().addAll(
            questionHeaderLabel,
            questionContentFlow,
            separator,
            answerContainer,
            markForReviewCheckbox,
            saveStatusLabel
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
     * EditBy: K24DTCN210-NVMANH (25/11/2025 14:40) - Reset save status khi load question mới
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
        
        // Phase 8.6: Setup answer change listener
        setupAnswerChangeListener(currentAnswerWidget);
        
        // Clear answer container and add new widget
        answerContainer.getChildren().clear();
        
        Label answerLabel = new Label("📝 CÂU TRẢ LỜI:");
        answerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        answerContainer.getChildren().addAll(answerLabel, currentAnswerWidget);
        
        // Reset mark checkbox
        markForReviewCheckbox.setSelected(false);
        
        // ✅ FIX: Reset save status về "Chưa lưu" khi load câu hỏi mới
        updateSaveStatus("unsaved");
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
    
    /* ---------------------------------------------------
     * Set callback khi answer thay đổi (Phase 8.6)
     * @param callback Consumer nhận String là answer mới
     * @author: K24DTCN210-NVMANH (24/11/2025 14:51)
     * --------------------------------------------------- */
    public void setOnAnswerChanged(Consumer<String> callback) {
        this.onAnswerChanged = callback;
    }
    
    /* ---------------------------------------------------
     * Update save status indicator (Phase 8.6)
     * @param status Save status ("unsaved", "saving", "saved", "error")
     * @author: K24DTCN210-NVMANH (25/11/2025 12:30)
     * --------------------------------------------------- */
    public void updateSaveStatus(String status) {
        if (saveStatusLabel == null) return;
        
        switch (status.toLowerCase()) {
            case "unsaved":
                saveStatusLabel.setText("💾 Chưa lưu");
                saveStatusLabel.setStyle(
                    "-fx-font-size: 11px; " +
                    "-fx-text-fill: #FF9800; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-background-color: #FFF3E0; " +
                    "-fx-background-radius: 3px; " +
                    "-fx-border-color: #FFE0B2; " +
                    "-fx-border-radius: 3px;"
                );
                break;
                
            case "saving":
                saveStatusLabel.setText("⏳ Đang lưu...");
                saveStatusLabel.setStyle(
                    "-fx-font-size: 11px; " +
                    "-fx-text-fill: #2196F3; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-background-color: #E3F2FD; " +
                    "-fx-background-radius: 3px; " +
                    "-fx-border-color: #BBDEFB; " +
                    "-fx-border-radius: 3px;"
                );
                break;
                
            case "saved":
                saveStatusLabel.setText("✅ Đã lưu");
                saveStatusLabel.setStyle(
                    "-fx-font-size: 11px; " +
                    "-fx-text-fill: #4CAF50; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-background-color: #E8F5E9; " +
                    "-fx-background-radius: 3px; " +
                    "-fx-border-color: #C8E6C9; " +
                    "-fx-border-radius: 3px;"
                );
                break;
                
            case "error":
                saveStatusLabel.setText("❌ Lỗi lưu");
                saveStatusLabel.setStyle(
                    "-fx-font-size: 11px; " +
                    "-fx-text-fill: #F44336; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-background-color: #FFEBEE; " +
                    "-fx-background-radius: 3px; " +
                    "-fx-border-color: #FFCDD2; " +
                    "-fx-border-radius: 3px;"
                );
                break;
                
            default:
                saveStatusLabel.setText("💾 Chưa lưu");
        }
        
        saveStatusLabel.setVisible(true);
    }
    
    /* ---------------------------------------------------
     * Setup listener cho answer widget để detect changes (Phase 8.6)
     * Called internally sau khi create answer widget
     * @param widget Answer input widget
     * @author: K24DTCN210-NVMANH (24/11/2025 14:51)
     * --------------------------------------------------- */
    private void setupAnswerChangeListener(Node widget) {
        if (widget == null || onAnswerChanged == null) {
            return;
        }
        
        // TextField (SHORT_ANSWER)
        if (widget instanceof javafx.scene.control.TextField) {
            javafx.scene.control.TextField field = (javafx.scene.control.TextField) widget;
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                onAnswerChanged.accept(newVal != null ? newVal : "");
            });
        }
        
        // TextArea (ESSAY, LONG_ANSWER)
        else if (widget instanceof javafx.scene.control.TextArea) {
            javafx.scene.control.TextArea area = (javafx.scene.control.TextArea) widget;
            area.textProperty().addListener((obs, oldVal, newVal) -> {
                onAnswerChanged.accept(newVal != null ? newVal : "");
            });
        }
        
        // RadioButton group (MULTIPLE_CHOICE single)
        else if (widget instanceof VBox) {
            // VBox chứa RadioButtons
            for (Node child : ((VBox) widget).getChildren()) {
                if (child instanceof javafx.scene.control.RadioButton) {
                    javafx.scene.control.RadioButton radio = (javafx.scene.control.RadioButton) child;
                    radio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            // Get current answer when selection changes
                            String answer = getCurrentAnswer();
                            if (answer != null) {
                                onAnswerChanged.accept(answer);
                            }
                        }
                    });
                }
            }
        }
        
        // CheckBox group (MULTIPLE_CHOICE multi)
        // Similar pattern như RadioButton nhưng listen each checkbox
    }
}
