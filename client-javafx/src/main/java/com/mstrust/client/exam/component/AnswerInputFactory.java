package com.mstrust.client.exam.component;

import com.mstrust.client.exam.dto.QuestionDTO;
import com.mstrust.client.exam.dto.QuestionType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.List;

/* ---------------------------------------------------
 * Answer Input Factory - Tạo input widgets dựa trên QuestionType
 * - Factory method pattern
 * - Support 8 question types
 * - Extract answer values từ widgets
 * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
 * --------------------------------------------------- */
public class AnswerInputFactory {

    /* ---------------------------------------------------
     * Tạo input widget phù hợp với question type
     * @param question QuestionDTO chứa thông tin câu hỏi
     * @returns Node là input widget tương ứng
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    public static Node createInputWidget(QuestionDTO question) {
        if (question == null || question.getType() == null) {
            return createErrorWidget("Invalid question data");
        }

        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                return createMultipleChoiceInput(question);
            case MULTIPLE_SELECT:
                return createMultipleSelectInput(question);
            case TRUE_FALSE:
                return createTrueFalseInput(question);
            case SHORT_ANSWER:
                return createShortAnswerInput(question);
            case ESSAY:
                return createEssayInput(question);
            case CODING:
                return createCodingInput(question);
            case FILL_IN_BLANK:
                return createFillInBlankInput(question);
            case MATCHING:
                return createMatchingInput(question);
            default:
                return createErrorWidget("Unsupported question type: " + question.getType());
        }
    }

    /* ---------------------------------------------------
     * Extract answer value từ input widget
     * @param widget Node là input widget
     * @param questionType QuestionType để biết cách extract
     * @returns String là answer value (JSON format nếu cần)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    public static String extractAnswer(Node widget, QuestionType questionType) {
        if (widget == null) return null;

        switch (questionType) {
            case MULTIPLE_CHOICE:
                return extractMultipleChoiceAnswer(widget);
            case MULTIPLE_SELECT:
                return extractMultipleSelectAnswer(widget);
            case TRUE_FALSE:
                return extractTrueFalseAnswer(widget);
            case SHORT_ANSWER:
            case ESSAY:
                return extractTextAnswer(widget);
            case CODING:
                return extractCodingAnswer(widget);
            case FILL_IN_BLANK:
                return extractFillInBlankAnswer(widget);
            case MATCHING:
                return extractMatchingAnswer(widget);
            default:
                return null;
        }
    }

    /* ---------------------------------------------------
     * Tạo Multiple Choice input (RadioButton group)
     * @param question QuestionDTO
     * @returns VBox chứa radio buttons
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createMultipleChoiceInput(QuestionDTO question) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        ToggleGroup group = new ToggleGroup();
        
        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
                RadioButton rb = new RadioButton(option);
                rb.setToggleGroup(group);
                rb.setWrapText(true);
                rb.setUserData(option); // Store option text
                container.getChildren().add(rb);
            }
        }
        
        container.setUserData(group); // Store ToggleGroup for extraction
        return container;
    }

    /* ---------------------------------------------------
     * Extract answer từ Multiple Choice
     * @param widget VBox chứa radio buttons
     * @returns Selected option text
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractMultipleChoiceAnswer(Node widget) {
        if (!(widget instanceof VBox)) return null;
        
        VBox container = (VBox) widget;
        ToggleGroup group = (ToggleGroup) container.getUserData();
        
        if (group == null || group.getSelectedToggle() == null) return null;
        
        return (String) group.getSelectedToggle().getUserData();
    }

    /* ---------------------------------------------------
     * Tạo Multiple Select input (CheckBox group)
     * @param question QuestionDTO
     * @returns VBox chứa checkboxes
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createMultipleSelectInput(QuestionDTO question) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
                CheckBox cb = new CheckBox(option);
                cb.setWrapText(true);
                cb.setUserData(option);
                container.getChildren().add(cb);
            }
        }
        
        return container;
    }

    /* ---------------------------------------------------
     * Extract answer từ Multiple Select
     * @param widget VBox chứa checkboxes
     * @returns Comma-separated selected options
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractMultipleSelectAnswer(Node widget) {
        if (!(widget instanceof VBox)) return null;
        
        VBox container = (VBox) widget;
        List<String> selected = new ArrayList<>();
        
        for (Node child : container.getChildren()) {
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isSelected()) {
                    selected.add((String) cb.getUserData());
                }
            }
        }
        
        return selected.isEmpty() ? null : String.join(",", selected);
    }

    /* ---------------------------------------------------
     * Tạo True/False input (2 buttons)
     * @param question QuestionDTO
     * @returns VBox chứa 2 buttons
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createTrueFalseInput(QuestionDTO question) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        ToggleGroup group = new ToggleGroup();
        
        RadioButton trueBtn = new RadioButton("Đúng");
        trueBtn.setToggleGroup(group);
        trueBtn.setUserData("true");
        
        RadioButton falseBtn = new RadioButton("Sai");
        falseBtn.setToggleGroup(group);
        falseBtn.setUserData("false");
        
        container.getChildren().addAll(trueBtn, falseBtn);
        container.setUserData(group);
        
        return container;
    }

    /* ---------------------------------------------------
     * Extract answer từ True/False
     * @param widget VBox chứa true/false buttons
     * @returns "true" hoặc "false"
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractTrueFalseAnswer(Node widget) {
        return extractMultipleChoiceAnswer(widget); // Same logic
    }

    /* ---------------------------------------------------
     * Tạo Short Answer input (TextField)
     * @param question QuestionDTO
     * @returns TextField
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createShortAnswerInput(QuestionDTO question) {
        TextField textField = new TextField();
        textField.setPromptText("Nhập câu trả lời ngắn...");
        textField.setPrefWidth(400);
        return textField;
    }

    /* ---------------------------------------------------
     * Tạo Essay input (TextArea)
     * @param question QuestionDTO
     * @returns TextArea
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createEssayInput(QuestionDTO question) {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Nhập câu trả lời tự luận...");
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);
        textArea.setWrapText(true);
        return textArea;
    }

    /* ---------------------------------------------------
     * Extract answer từ TextField hoặc TextArea
     * @param widget TextField hoặc TextArea
     * @returns Text content
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractTextAnswer(Node widget) {
        if (widget instanceof TextField) {
            return ((TextField) widget).getText();
        } else if (widget instanceof TextArea) {
            return ((TextArea) widget).getText();
        }
        return null;
    }

    /* ---------------------------------------------------
     * Tạo Coding input (CodeArea from RichTextFX)
     * @param question QuestionDTO
     * @returns CodeArea
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createCodingInput(QuestionDTO question) {
        CodeArea codeArea = new CodeArea();
        codeArea.setPrefSize(600, 400);
        codeArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");
        
        // Placeholder comment
        if (question.getContent() != null && question.getContent().contains("Java")) {
            codeArea.replaceText("// Viết code Java của bạn ở đây\n\n");
        } else if (question.getContent() != null && question.getContent().contains("Python")) {
            codeArea.replaceText("# Viết code Python của bạn ở đây\n\n");
        } else {
            codeArea.replaceText("// Viết code của bạn ở đây\n\n");
        }
        
        return codeArea;
    }

    /* ---------------------------------------------------
     * Extract answer từ CodeArea
     * @param widget CodeArea
     * @returns Code content
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractCodingAnswer(Node widget) {
        if (widget instanceof CodeArea) {
            return ((CodeArea) widget).getText();
        }
        return null;
    }

    /* ---------------------------------------------------
     * Tạo Fill In Blank input (Multiple TextFields)
     * @param question QuestionDTO
     * @returns VBox chứa các TextFields
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createFillInBlankInput(QuestionDTO question) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        // Count blanks in question content (e.g., "The ___ is blue and the ___ is red")
        String content = question.getContent();
        int blankCount = content == null ? 3 : content.split("___").length - 1;
        
        if (blankCount <= 0) blankCount = 3; // Default 3 blanks
        
        for (int i = 0; i < blankCount; i++) {
            TextField tf = new TextField();
            tf.setPromptText("Chỗ trống " + (i + 1));
            tf.setPrefWidth(300);
            container.getChildren().add(tf);
        }
        
        return container;
    }

    /* ---------------------------------------------------
     * Extract answer từ Fill In Blank
     * @param widget VBox chứa TextFields
     * @returns Comma-separated values
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractFillInBlankAnswer(Node widget) {
        if (!(widget instanceof VBox)) return null;
        
        VBox container = (VBox) widget;
        List<String> blanks = new ArrayList<>();
        
        for (Node child : container.getChildren()) {
            if (child instanceof TextField) {
                blanks.add(((TextField) child).getText());
            }
        }
        
        return String.join(",", blanks);
    }

    /* ---------------------------------------------------
     * Tạo Matching input (ComboBox pairs)
     * @param question QuestionDTO
     * @returns VBox chứa matching pairs
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createMatchingInput(QuestionDTO question) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        Label instruction = new Label("Nối các cặp sau:");
        instruction.setStyle("-fx-font-weight: bold;");
        container.getChildren().add(instruction);
        
        // Simplified: Create 5 pairs by default
        // In real implementation, parse from question.getOptions()
        for (int i = 1; i <= 5; i++) {
            VBox pairBox = new VBox(5);
            
            Label leftLabel = new Label("Item " + i);
            
            ComboBox<String> rightCombo = new ComboBox<>();
            rightCombo.getItems().addAll("Option A", "Option B", "Option C", "Option D", "Option E");
            rightCombo.setPromptText("Chọn đáp án...");
            
            pairBox.getChildren().addAll(leftLabel, rightCombo);
            container.getChildren().add(pairBox);
        }
        
        return container;
    }

    /* ---------------------------------------------------
     * Extract answer từ Matching
     * @param widget VBox chứa matching pairs
     * @returns JSON-like string of matches
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static String extractMatchingAnswer(Node widget) {
        if (!(widget instanceof VBox)) return null;
        
        VBox container = (VBox) widget;
        List<String> matches = new ArrayList<>();
        
        for (Node child : container.getChildren()) {
            if (child instanceof VBox) {
                VBox pairBox = (VBox) child;
                for (Node pairChild : pairBox.getChildren()) {
                    if (pairChild instanceof ComboBox) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> combo = (ComboBox<String>) pairChild;
                        String selected = combo.getValue();
                        if (selected != null) {
                            matches.add(selected);
                        }
                    }
                }
            }
        }
        
        return String.join(",", matches);
    }

    /* ---------------------------------------------------
     * Tạo error widget khi có lỗi
     * @param errorMessage Error message
     * @returns Label hiển thị error
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static Node createErrorWidget(String errorMessage) {
        Label errorLabel = new Label("❌ " + errorMessage);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return errorLabel;
    }

    /* ---------------------------------------------------
     * Set answer value vào widget (restore từ cache)
     * @param widget Node là input widget
     * @param questionType QuestionType
     * @param answerValue String là answer value cần restore
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    public static void setAnswer(Node widget, QuestionType questionType, String answerValue) {
        if (widget == null || answerValue == null || answerValue.isEmpty()) return;

        switch (questionType) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                setMultipleChoiceAnswer(widget, answerValue);
                break;
            case MULTIPLE_SELECT:
                setMultipleSelectAnswer(widget, answerValue);
                break;
            case SHORT_ANSWER:
            case ESSAY:
                setTextAnswer(widget, answerValue);
                break;
            case CODING:
                setCodingAnswer(widget, answerValue);
                break;
            case FILL_IN_BLANK:
                setFillInBlankAnswer(widget, answerValue);
                break;
            case MATCHING:
                setMatchingAnswer(widget, answerValue);
                break;
        }
    }

    /* ---------------------------------------------------
     * Set answer cho Multiple Choice
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setMultipleChoiceAnswer(Node widget, String value) {
        if (!(widget instanceof VBox)) return;
        
        VBox container = (VBox) widget;
        ToggleGroup group = (ToggleGroup) container.getUserData();
        
        if (group != null) {
            for (Toggle toggle : group.getToggles()) {
                if (value.equals(toggle.getUserData())) {
                    group.selectToggle(toggle);
                    break;
                }
            }
        }
    }

    /* ---------------------------------------------------
     * Set answer cho Multiple Select
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setMultipleSelectAnswer(Node widget, String value) {
        if (!(widget instanceof VBox)) return;
        
        VBox container = (VBox) widget;
        String[] selected = value.split(",");
        
        for (Node child : container.getChildren()) {
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                String option = (String) cb.getUserData();
                for (String sel : selected) {
                    if (sel.trim().equals(option)) {
                        cb.setSelected(true);
                        break;
                    }
                }
            }
        }
    }

    /* ---------------------------------------------------
     * Set answer cho text inputs
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setTextAnswer(Node widget, String value) {
        if (widget instanceof TextField) {
            ((TextField) widget).setText(value);
        } else if (widget instanceof TextArea) {
            ((TextArea) widget).setText(value);
        }
    }

    /* ---------------------------------------------------
     * Set answer cho CodeArea
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setCodingAnswer(Node widget, String value) {
        if (widget instanceof CodeArea) {
            ((CodeArea) widget).replaceText(value);
        }
    }

    /* ---------------------------------------------------
     * Set answer cho Fill In Blank
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setFillInBlankAnswer(Node widget, String value) {
        if (!(widget instanceof VBox)) return;
        
        VBox container = (VBox) widget;
        String[] blanks = value.split(",");
        int index = 0;
        
        for (Node child : container.getChildren()) {
            if (child instanceof TextField && index < blanks.length) {
                ((TextField) child).setText(blanks[index].trim());
                index++;
            }
        }
    }

    /* ---------------------------------------------------
     * Set answer cho Matching
     * @author: K24DTCN210-NVMANH (23/11/2025 13:47)
     * --------------------------------------------------- */
    private static void setMatchingAnswer(Node widget, String value) {
        if (!(widget instanceof VBox)) return;
        
        VBox container = (VBox) widget;
        String[] matches = value.split(",");
        int index = 0;
        
        for (Node child : container.getChildren()) {
            if (child instanceof VBox) {
                VBox pairBox = (VBox) child;
                for (Node pairChild : pairBox.getChildren()) {
                    if (pairChild instanceof ComboBox && index < matches.length) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> combo = (ComboBox<String>) pairChild;
                        combo.setValue(matches[index].trim());
                        index++;
                    }
                }
            }
        }
    }
}
