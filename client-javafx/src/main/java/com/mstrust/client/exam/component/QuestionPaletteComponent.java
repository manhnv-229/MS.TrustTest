package com.mstrust.client.exam.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Question Palette Component - Grid navigation cho câu hỏi
 * - Hiển thị grid các nút câu hỏi (5 cột)
 * - Color coding: Unanswered/Answered/Marked/Current
 * - Click để jump tới câu hỏi
 * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
 * --------------------------------------------------- */
public class QuestionPaletteComponent extends VBox {
    
    private GridPane buttonGrid;
    private Map<Integer, Button> questionButtons;
    private int currentQuestionIndex;
    private Consumer<Integer> onQuestionClickCallback;
    
    // CSS classes for question states
    private static final String CLASS_UNANSWERED = "question-unanswered";
    private static final String CLASS_ANSWERED = "question-answered";
    private static final String CLASS_MARKED = "question-marked";
    private static final String CLASS_CURRENT = "question-current";
    
    private static final int GRID_COLUMNS = 5;

    /* ---------------------------------------------------
     * Constructor - khởi tạo palette với số lượng câu hỏi
     * @param totalQuestions Tổng số câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public QuestionPaletteComponent(int totalQuestions) {
        this.questionButtons = new HashMap<>();
        this.currentQuestionIndex = 0;
        
        initializeUI(totalQuestions);
        applyStyles();
    }

    /* ---------------------------------------------------
     * Khởi tạo UI với grid buttons
     * @param totalQuestions Tổng số câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    private void initializeUI(int totalQuestions) {
        // Header
        Label headerLabel = new Label("DANH SÁCH CÂU HỎI");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);
        
        // Grid for question buttons
        buttonGrid = new GridPane();
        buttonGrid.setHgap(8);
        buttonGrid.setVgap(8);
        buttonGrid.setPadding(new Insets(10));
        
        // Create buttons in grid layout
        int row = 0;
        int col = 0;
        
        for (int i = 0; i < totalQuestions; i++) {
            final int questionIndex = i;
            
            Button btn = new Button(String.valueOf(i + 1));
            btn.getStyleClass().addAll("question-button", CLASS_UNANSWERED);
            
            // Click handler
            btn.setOnAction(e -> handleQuestionClick(questionIndex));
            
            questionButtons.put(i, btn);
            buttonGrid.add(btn, col, row);
            
            col++;
            if (col >= GRID_COLUMNS) {
                col = 0;
                row++;
            }
        }
        
        // ScrollPane wrapper
        ScrollPane scrollPane = new ScrollPane(buttonGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // Legend
        VBox legend = createLegend();
        
        // Add all to main container
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(headerLabel, scrollPane, legend);
        
        // Set first question as current
        if (!questionButtons.isEmpty()) {
            setCurrentQuestion(0);
        }
    }

    /* ---------------------------------------------------
     * Tạo legend giải thích màu sắc
     * @returns VBox chứa legend
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    private VBox createLegend() {
        VBox legend = new VBox(5);
        legend.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label title = new Label("Chú thích:");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label unanswered = createLegendItem("⬜", "Chưa trả lời");
        Label answered = createLegendItem("✅", "Đã trả lời");
        Label marked = createLegendItem("🔖", "Đánh dấu");
        Label current = createLegendItem("➡️", "Câu hiện tại");
        
        legend.getChildren().addAll(title, unanswered, answered, marked, current);
        
        return legend;
    }

    /* ---------------------------------------------------
     * Tạo một legend item
     * @param icon Icon text
     * @param text Description text
     * @returns Label chứa legend item
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    private Label createLegendItem(String icon, String text) {
        Label label = new Label(icon + " " + text);
        label.setStyle("-fx-font-size: 11px;");
        return label;
    }

    /* ---------------------------------------------------
     * Xử lý khi click vào nút câu hỏi
     * @param questionIndex Index của câu hỏi (0-based)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    private void handleQuestionClick(int questionIndex) {
        if (onQuestionClickCallback != null) {
            onQuestionClickCallback.accept(questionIndex);
        }
    }

    /* ---------------------------------------------------
     * Set callback khi click câu hỏi
     * @param callback Consumer nhận questionIndex
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public void setOnQuestionClick(Consumer<Integer> callback) {
        this.onQuestionClickCallback = callback;
    }

    /* ---------------------------------------------------
     * Set câu hỏi hiện tại (highlight)
     * @param questionIndex Index của câu hỏi (0-based)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public void setCurrentQuestion(int questionIndex) {
        // Remove current class from previous
        Button prevButton = questionButtons.get(currentQuestionIndex);
        if (prevButton != null) {
            prevButton.getStyleClass().remove(CLASS_CURRENT);
        }
        
        // Add current class to new
        Button currentButton = questionButtons.get(questionIndex);
        if (currentButton != null) {
            // Remove current first if exists (to re-add at end)
            currentButton.getStyleClass().remove(CLASS_CURRENT);
            currentButton.getStyleClass().add(CLASS_CURRENT);
        }
        
        this.currentQuestionIndex = questionIndex;
    }

    /* ---------------------------------------------------
     * Update trạng thái câu hỏi
     * @param questionIndex Index của câu hỏi (0-based)
     * @param status Trạng thái: "unanswered", "answered", "marked"
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public void updateQuestionStatus(int questionIndex, String status) {
        Button btn = questionButtons.get(questionIndex);
        if (btn == null) return;
        
        // Remove all status classes except current
        btn.getStyleClass().removeAll(CLASS_UNANSWERED, CLASS_ANSWERED, CLASS_MARKED);
        
        // Add new status class
        switch (status.toLowerCase()) {
            case "answered":
                btn.getStyleClass().add(CLASS_ANSWERED);
                break;
            case "marked":
                btn.getStyleClass().add(CLASS_MARKED);
                break;
            case "unanswered":
            default:
                btn.getStyleClass().add(CLASS_UNANSWERED);
                break;
        }
        
        // Re-add current if this is current question
        if (questionIndex == currentQuestionIndex) {
            btn.getStyleClass().remove(CLASS_CURRENT);
            btn.getStyleClass().add(CLASS_CURRENT);
        }
    }

    /* ---------------------------------------------------
     * Get số lượng câu đã trả lời
     * @returns Số câu đã trả lời
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public int getAnsweredCount() {
        return (int) questionButtons.values().stream()
                .filter(btn -> btn.getStyleClass().contains(CLASS_ANSWERED))
                .count();
    }

    /* ---------------------------------------------------
     * Get số lượng câu đánh dấu
     * @returns Số câu đánh dấu
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    public int getMarkedCount() {
        return (int) questionButtons.values().stream()
                .filter(btn -> btn.getStyleClass().contains(CLASS_MARKED))
                .count();
    }

    /* ---------------------------------------------------
     * Apply CSS styles cho component
     * @author: K24DTCN210-NVMANH (23/11/2025 13:46)
     * --------------------------------------------------- */
    private void applyStyles() {
        this.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;"
        );
    }
}
