package com.mstrust.client.teacher.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mstrust.client.exam.dto.QuestionType;
import com.mstrust.client.teacher.api.QuestionBankApiClient;
import com.mstrust.client.teacher.dto.CreateQuestionRequest;
import com.mstrust.client.teacher.dto.Difficulty;
import com.mstrust.client.teacher.dto.QuestionBankDTO;
import com.mstrust.client.teacher.dto.SubjectDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho dialog tạo mới/chỉnh sửa câu hỏi
 * Hỗ trợ 8 loại câu hỏi với dynamic UI
 * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
 * --------------------------------------------------- */
public class QuestionEditDialogController {
    
    // Common Fields
    @FXML private Label dialogTitle;
    @FXML private ComboBox<SubjectDTO> subjectCombo;
    @FXML private ComboBox<QuestionType> questionTypeCombo;
    @FXML private ComboBox<Difficulty> difficultyCombo;
    @FXML private TextField tagsField;
    @FXML private TextArea questionTextArea;
    
    // Type-specific sections
    @FXML private VBox multipleChoiceSection;
    @FXML private VBox trueFalseSection;
    @FXML private VBox essaySection;
    @FXML private VBox shortAnswerSection;
    @FXML private VBox codingSection;
    @FXML private VBox fillInBlankSection;
    @FXML private VBox matchingSection;
    
    // Multiple Choice/Select fields
    @FXML private ListView<String> optionsListView;
    @FXML private VBox correctAnswerContainer;
    private ObservableList<String> options = FXCollections.observableArrayList();
    private ToggleGroup correctAnswerGroup;
    private List<CheckBox> correctAnswerCheckBoxes = new ArrayList<>();
    
    // True/False fields
    @FXML private RadioButton trueRadio;
    @FXML private RadioButton falseRadio;
    
    // Essay fields
    @FXML private TextField minWordsField;
    @FXML private TextField maxWordsField;
    @FXML private TextArea gradingCriteriaArea;
    
    // Short Answer fields
    @FXML private TextField shortAnswerField;
    @FXML private TextArea shortAnswerGradingArea;
    
    // Coding fields
    @FXML private ComboBox<String> programmingLanguageCombo;
    @FXML private TextArea starterCodeArea;
    @FXML private TextArea testCasesArea;
    @FXML private TextField timeLimitField;
    @FXML private TextField memoryLimitField;
    
    // Fill in Blank fields
    @FXML private TextArea fillInBlankTextArea;
    @FXML private TextArea blankAnswersArea;
    
    // Matching fields
    @FXML private ListView<String> leftItemsListView;
    @FXML private ListView<String> rightItemsListView;
    @FXML private TextArea correctMatchesArea;
    private ObservableList<String> leftItems = FXCollections.observableArrayList();
    private ObservableList<String> rightItems = FXCollections.observableArrayList();
    
    // Attachments
    @FXML private Label attachmentLabel;
    @FXML private ListView<String> attachmentsListView;
    private List<String> attachmentPaths = new ArrayList<>();
    
    @FXML private Button saveButton;
    
    // State
    private Stage dialogStage;
    private QuestionBankApiClient apiClient;
    private QuestionBankDTO existingQuestion; // null = CREATE mode
    private boolean confirmed = false;
    private List<SubjectDTO> subjects;
    private Gson gson = new Gson();
    
    /* ---------------------------------------------------
     * Khởi tạo controller sau khi FXML được load
     * Setup các ComboBox và ListView
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    @FXML
    private void initialize() {
        // Setup Question Type ComboBox
        questionTypeCombo.setItems(FXCollections.observableArrayList(QuestionType.values()));
        questionTypeCombo.setCellFactory(lv -> new ListCell<QuestionType>() {
            @Override
            protected void updateItem(QuestionType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        questionTypeCombo.setButtonCell(new ListCell<QuestionType>() {
            @Override
            protected void updateItem(QuestionType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        
        // Setup Difficulty ComboBox
        difficultyCombo.setItems(FXCollections.observableArrayList(Difficulty.values()));
        difficultyCombo.setCellFactory(lv -> new ListCell<Difficulty>() {
            @Override
            protected void updateItem(Difficulty item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        difficultyCombo.setButtonCell(new ListCell<Difficulty>() {
            @Override
            protected void updateItem(Difficulty item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        
        // Setup Programming Languages
        programmingLanguageCombo.setItems(FXCollections.observableArrayList(
            "Java", "Python", "C++", "C#", "JavaScript", "C", "Go", "Rust"
        ));
        
        // Setup ListViews
        optionsListView.setItems(options);
        leftItemsListView.setItems(leftItems);
        rightItemsListView.setItems(rightItems);
        
        // Setup ToggleGroup for True/False
        ToggleGroup trueFalseGroup = new ToggleGroup();
        trueRadio.setToggleGroup(trueFalseGroup);
        falseRadio.setToggleGroup(trueFalseGroup);
    }
    
    /* ---------------------------------------------------
     * Thiết lập dialog với API client và dữ liệu câu hỏi
     * @param stage Stage của dialog
     * @param apiClient API client để gọi backend
     * @param subjects Danh sách môn học
     * @param question Câu hỏi cần chỉnh sửa (null = CREATE mode)
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    public void setup(Stage stage, QuestionBankApiClient apiClient, 
                     List<SubjectDTO> subjects, QuestionBankDTO question) {
        this.dialogStage = stage;
        this.apiClient = apiClient;
        this.subjects = subjects;
        this.existingQuestion = question;
        
        // Setup Subject ComboBox
        subjectCombo.setItems(FXCollections.observableArrayList(subjects));
        subjectCombo. setCellFactory(lv -> new ListCell<SubjectDTO>() {
            @Override
            protected void updateItem(SubjectDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getSubjectName());
            }
        });
        subjectCombo. setButtonCell(new ListCell<SubjectDTO>() {
            @Override
            protected void updateItem(SubjectDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getSubjectName());
            }
        });
        
        // Load data if EDIT mode
        if (question != null) {
            dialogTitle.setText("Chỉnh sửa câu hỏi");
            loadQuestionData();
        } else {
            dialogTitle.setText("Tạo câu hỏi mới");
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu câu hỏi vào form (EDIT mode)
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadQuestionData() {
        // Load common fields
        SubjectDTO subject = subjects.stream()
            .filter(s -> s.getId().equals(existingQuestion.getSubjectId()))
            .findFirst().orElse(null);
        subjectCombo.setValue(subject);
        
        questionTypeCombo.setValue(existingQuestion.getType());
        difficultyCombo.setValue(existingQuestion.getDifficulty());
        tagsField.setText(existingQuestion.getTags());
        questionTextArea.setText(existingQuestion.getContent());
        
        // Show appropriate section and load type-specific data
        onQuestionTypeChange();
        loadTypeSpecificData();
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu specific cho từng loại câu hỏi
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadTypeSpecificData() {
        QuestionType type = existingQuestion.getType();
        
        switch (type) {
            case MULTIPLE_CHOICE:
            case MULTIPLE_SELECT:
                loadMultipleChoiceData();
                break;
            case TRUE_FALSE:
                loadTrueFalseData();
                break;
            case ESSAY:
                loadEssayData();
                break;
            case SHORT_ANSWER:
                loadShortAnswerData();
                break;
            case CODING:
                loadCodingData();
                break;
            case FILL_IN_BLANK:
                loadFillInBlankData();
                break;
            case MATCHING:
                loadMatchingData();
                break;
        }
        
        // Load attachments
        if (existingQuestion.getAttachments() != null && !existingQuestion.getAttachments().isEmpty()) {
            try {
                JsonArray arr = gson.fromJson(existingQuestion.getAttachments(), JsonArray.class);
                arr.forEach(el -> attachmentPaths.add(el.getAsString()));
                updateAttachmentDisplay();
            } catch (Exception e) {
                System.err.println("Error loading attachments: " + e.getMessage());
            }
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Multiple Choice/Select
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadMultipleChoiceData() {
        try {
            if (existingQuestion.getOptions() != null) {
                JsonArray arr = gson.fromJson(existingQuestion.getOptions(), JsonArray.class);
                arr.forEach(el -> options.add(el.getAsString()));
                updateCorrectAnswerControls();
            }
            
            if (existingQuestion.getCorrectAnswer() != null) {
                if (existingQuestion.getType() == QuestionType.MULTIPLE_CHOICE) {
                    // Single answer
                    int index = Integer.parseInt(existingQuestion.getCorrectAnswer());
                    if (index < correctAnswerGroup.getToggles().size()) {
                        correctAnswerGroup.getToggles().get(index).setSelected(true);
                    }
                } else {
                    // Multiple answers
                    JsonArray arr = gson.fromJson(existingQuestion.getCorrectAnswer(), JsonArray.class);
                    arr.forEach(el -> {
                        int index = el.getAsInt();
                        if (index < correctAnswerCheckBoxes.size()) {
                            correctAnswerCheckBoxes.get(index).setSelected(true);
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading multiple choice data: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu True/False
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadTrueFalseData() {
        if (existingQuestion.getCorrectAnswer() != null) {
            boolean isTrue = existingQuestion.getCorrectAnswer().equals("true");
            if (isTrue) {
                trueRadio.setSelected(true);
            } else {
                falseRadio.setSelected(true);
            }
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Essay
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadEssayData() {
        if (existingQuestion.getMinWords() != null) {
            minWordsField.setText(existingQuestion.getMinWords().toString());
        }
        if (existingQuestion.getMaxWords() != null) {
            maxWordsField.setText(existingQuestion.getMaxWords().toString());
        }
        if (existingQuestion.getGradingCriteria() != null) {
            gradingCriteriaArea.setText(existingQuestion.getGradingCriteria());
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Short Answer
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadShortAnswerData() {
        if (existingQuestion.getCorrectAnswer() != null) {
            shortAnswerField.setText(existingQuestion.getCorrectAnswer());
        }
        if (existingQuestion.getGradingCriteria() != null) {
            shortAnswerGradingArea.setText(existingQuestion.getGradingCriteria());
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Coding
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadCodingData() {
        if (existingQuestion.getProgrammingLanguage() != null) {
            programmingLanguageCombo.setValue(existingQuestion.getProgrammingLanguage());
        }
        if (existingQuestion.getStarterCode() != null) {
            starterCodeArea.setText(existingQuestion.getStarterCode());
        }
        if (existingQuestion.getTestCases() != null) {
            testCasesArea.setText(existingQuestion.getTestCases());
        }
        if (existingQuestion.getTimeLimitSeconds() != null) {
            timeLimitField.setText(existingQuestion.getTimeLimitSeconds().toString());
        }
        if (existingQuestion.getMemoryLimitMb() != null) {
            memoryLimitField.setText(existingQuestion.getMemoryLimitMb().toString());
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Fill in Blank
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadFillInBlankData() {
        if (existingQuestion.getBlankPositions() != null) {
            blankAnswersArea.setText(existingQuestion.getBlankPositions());
        }
    }
    
    /* ---------------------------------------------------
     * Load dữ liệu Matching
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void loadMatchingData() {
        try {
            if (existingQuestion.getLeftItems() != null) {
                JsonArray arr = gson.fromJson(existingQuestion.getLeftItems(), JsonArray.class);
                arr.forEach(el -> leftItems.add(el.getAsString()));
            }
            if (existingQuestion.getRightItems() != null) {
                JsonArray arr = gson.fromJson(existingQuestion.getRightItems(), JsonArray.class);
                arr.forEach(el -> rightItems.add(el.getAsString()));
            }
            if (existingQuestion.getCorrectMatches() != null) {
                correctMatchesArea.setText(existingQuestion.getCorrectMatches());
            }
        } catch (Exception e) {
            System.err.println("Error loading matching data: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Handler khi thay đổi loại câu hỏi
     * Show/hide các sections tương ứng
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    @FXML
    private void onQuestionTypeChange() {
        QuestionType type = questionTypeCombo.getValue();
        if (type == null) return;
        
        // Hide all sections
        multipleChoiceSection.setVisible(false);
        multipleChoiceSection.setManaged(false);
        trueFalseSection.setVisible(false);
        trueFalseSection.setManaged(false);
        essaySection.setVisible(false);
        essaySection.setManaged(false);
        shortAnswerSection.setVisible(false);
        shortAnswerSection.setManaged(false);
        codingSection.setVisible(false);
        codingSection.setManaged(false);
        fillInBlankSection.setVisible(false);
        fillInBlankSection.setManaged(false);
        matchingSection.setVisible(false);
        matchingSection.setManaged(false);
        
        // Show relevant section
        switch (type) {
            case MULTIPLE_CHOICE:
            case MULTIPLE_SELECT:
                multipleChoiceSection.setVisible(true);
                multipleChoiceSection.setManaged(true);
                updateCorrectAnswerControls();
                break;
            case TRUE_FALSE:
                trueFalseSection.setVisible(true);
                trueFalseSection.setManaged(true);
                break;
            case ESSAY:
                essaySection.setVisible(true);
                essaySection.setManaged(true);
                break;
            case SHORT_ANSWER:
                shortAnswerSection.setVisible(true);
                shortAnswerSection.setManaged(true);
                break;
            case CODING:
                codingSection.setVisible(true);
                codingSection.setManaged(true);
                break;
            case FILL_IN_BLANK:
                fillInBlankSection.setVisible(true);
                fillInBlankSection.setManaged(true);
                break;
            case MATCHING:
                matchingSection.setVisible(true);
                matchingSection.setManaged(true);
                break;
        }
    }
    
    /* ---------------------------------------------------
     * Cập nhật controls cho đáp án đúng (Multiple Choice/Select)
     * Radio buttons cho MULTIPLE_CHOICE, CheckBoxes cho MULTIPLE_SELECT
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void updateCorrectAnswerControls() {
        correctAnswerContainer.getChildren().clear();
        correctAnswerCheckBoxes.clear();
        
        QuestionType type = questionTypeCombo.getValue();
        if (type == QuestionType.MULTIPLE_CHOICE) {
            // Use RadioButtons
            correctAnswerGroup = new ToggleGroup();
            for (int i = 0; i < options.size(); i++) {
                RadioButton rb = new RadioButton(options.get(i));
                rb.setToggleGroup(correctAnswerGroup);
                correctAnswerContainer.getChildren().add(rb);
            }
        } else if (type == QuestionType.MULTIPLE_SELECT) {
            // Use CheckBoxes
            for (String option : options) {
                CheckBox cb = new CheckBox(option);
                correctAnswerCheckBoxes.add(cb);
                correctAnswerContainer.getChildren().add(cb);
            }
        }
    }
    
    // Option Management Handlers
    @FXML
    private void handleAddOption() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm lựa chọn");
        dialog.setHeaderText("Nhập nội dung lựa chọn:");
        dialog.setContentText("Lựa chọn:");
        
        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                options.add(text.trim());
                updateCorrectAnswerControls();
            }
        });
    }
    
    @FXML
    private void handleRemoveOption() {
        int selected = optionsListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            options.remove(selected);
            updateCorrectAnswerControls();
        } else {
            showWarning("Chọn lựa chọn", "Vui lòng chọn một lựa chọn để xóa");
        }
    }
    
    @FXML
    private void handleEditOption() {
        int selected = optionsListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            String currentText = options.get(selected);
            TextInputDialog dialog = new TextInputDialog(currentText);
            dialog.setTitle("Sửa lựa chọn");
            dialog.setHeaderText("Nhập nội dung mới:");
            dialog.setContentText("Lựa chọn:");
            
            dialog.showAndWait().ifPresent(text -> {
                if (!text.trim().isEmpty()) {
                    options.set(selected, text.trim());
                    updateCorrectAnswerControls();
                }
            });
        } else {
            showWarning("Chọn lựa chọn", "Vui lòng chọn một lựa chọn để sửa");
        }
    }
    
    // Matching Items Handlers
    @FXML
    private void handleAddLeftItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm item trái");
        dialog.setHeaderText("Nhập nội dung:");
        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                leftItems.add(text.trim());
            }
        });
    }
    
    @FXML
    private void handleRemoveLeftItem() {
        int selected = leftItemsListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            leftItems.remove(selected);
        }
    }
    
    @FXML
    private void handleAddRightItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm item phải");
        dialog.setHeaderText("Nhập nội dung:");
        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                rightItems.add(text.trim());
            }
        });
    }
    
    @FXML
    private void handleRemoveRightItem() {
        int selected = rightItemsListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            rightItems.remove(selected);
        }
    }
    
    // Attachment Handler
    @FXML
    private void handleSelectAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp đính kèm");
        File file = fileChooser.showOpenDialog(dialogStage);
        
        if (file != null) {
            attachmentPaths.add(file.getAbsolutePath());
            updateAttachmentDisplay();
        }
    }
    
    /* ---------------------------------------------------
     * Cập nhật hiển thị danh sách tệp đính kèm
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private void updateAttachmentDisplay() {
        if (attachmentPaths.isEmpty()) {
            attachmentLabel.setText("Chưa có tệp đính kèm");
            attachmentsListView.setVisible(false);
            attachmentsListView.setManaged(false);
        } else {
            attachmentLabel.setText(attachmentPaths.size() + " tệp đính kèm");
            attachmentsListView.setItems(FXCollections.observableArrayList(attachmentPaths));
            attachmentsListView.setVisible(true);
            attachmentsListView.setManaged(true);
        }
    }
    
    /* ---------------------------------------------------
     * Handler nút Save
     * Validate form, build request, gọi API
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        try {
            saveButton.setDisable(true);
            
            CreateQuestionRequest request = buildRequest();
            
            QuestionBankDTO result;
            if (existingQuestion != null) {
                // Update
                result = apiClient.updateQuestion(existingQuestion.getId(), request);
            } else {
                // Create
                result = apiClient.createQuestion(request);
            }
            
            confirmed = true;
            dialogStage.close();
            
        } catch (Exception e) {
            showError("Lỗi", "Không thể lưu câu hỏi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            saveButton.setDisable(false);
        }
    }
    
    /* ---------------------------------------------------
     * Validate form trước khi save
     * @returns true nếu form hợp lệ
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (subjectCombo.getValue() == null) {
            errors.append("- Vui lòng chọn môn học\n");
        }
        if (questionTypeCombo.getValue() == null) {
            errors.append("- Vui lòng chọn loại câu hỏi\n");
        }
        if (difficultyCombo.getValue() == null) {
            errors.append("- Vui lòng chọn độ khó\n");
        }
        if (questionTextArea.getText().trim().isEmpty()) {
            errors.append("- Vui lòng nhập nội dung câu hỏi\n");
        }
        
        // Type-specific validation
        QuestionType type = questionTypeCombo.getValue();
        if (type != null) {
            switch (type) {
                case MULTIPLE_CHOICE:
                case MULTIPLE_SELECT:
                    if (options.isEmpty()) {
                        errors.append("- Vui lòng thêm ít nhất một lựa chọn\n");
                    }
                    if (!validateCorrectAnswer()) {
                        errors.append("- Vui lòng chọn đáp án đúng\n");
                    }
                    break;
                case TRUE_FALSE:
                    if (trueRadio.getToggleGroup().getSelectedToggle() == null) {
                        errors.append("- Vui lòng chọn đáp án Đúng hoặc Sai\n");
                    }
                    break;
                case CODING:
                    if (programmingLanguageCombo.getValue() == null) {
                        errors.append("- Vui lòng chọn ngôn ngữ lập trình\n");
                    }
                    break;
                case FILL_IN_BLANK:
                    if (!questionTextArea.getText().contains("_____")) {
                        errors.append("- Nội dung câu hỏi điền khuyết phải có ít nhất một chỗ trống (_____)\n");
                    }
                    break;
                case MATCHING:
                    if (leftItems.isEmpty() || rightItems.isEmpty()) {
                        errors.append("- Vui lòng thêm items cho cả hai cột\n");
                    }
                    break;
            }
        }
        
        if (errors.length() > 0) {
            showError("Lỗi nhập liệu", errors.toString());
            return false;
        }
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate đáp án đúng cho Multiple Choice/Select
     * @returns true nếu có đáp án được chọn
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private boolean validateCorrectAnswer() {
        QuestionType type = questionTypeCombo.getValue();
        if (type == QuestionType.MULTIPLE_CHOICE) {
            return correctAnswerGroup != null && correctAnswerGroup.getSelectedToggle() != null;
        } else if (type == QuestionType.MULTIPLE_SELECT) {
            return correctAnswerCheckBoxes.stream().anyMatch(CheckBox::isSelected);
        }
        return false;
    }
    
    /* ---------------------------------------------------
     * Build CreateQuestionRequest từ form data
     * @returns Request object để gửi lên API
     * @author: K24DTCN210-NVMANH (19/11/2025 23:01)
     * --------------------------------------------------- */
    private CreateQuestionRequest buildRequest() {
        CreateQuestionRequest request = new CreateQuestionRequest();
        
        // Common fields
        request.setSubjectId(subjectCombo.getValue().getId());
        request.setQuestionType(questionTypeCombo.getValue());
        request.setDifficulty(difficultyCombo.getValue());
        request.setTags(tagsField.getText().trim());
        request.setQuestionText(questionTextArea.getText().trim());
        
        // Type-specific fields
        QuestionType type = questionTypeCombo.getValue();
        switch (type) {
            case MULTIPLE_CHOICE:
            case MULTIPLE_SELECT:
                buildMultipleChoiceRequest(request);
                break;
            case TRUE_FALSE:
                buildTrueFalseRequest(request);
                break;
            case ESSAY:
                buildEssayRequest(request);
                break;
            case SHORT_ANSWER:
                buildShortAnswerRequest(request);
                break;
            case CODING:
                buildCodingRequest(request);
                break;
            case FILL_IN_BLANK:
                buildFillInBlankRequest(request);
                break;
            case MATCHING:
                buildMatchingRequest(request);
                break;
        }
        
        // Attachments
        if (!attachmentPaths.isEmpty()) {
            request.setAttachments(gson.toJson(attachmentPaths));
        }
        
        return request;
    }
    
    private void buildMultipleChoiceRequest(CreateQuestionRequest request) {
        request.setOptions(gson.toJson(options));
        
        if (questionTypeCombo.getValue() == QuestionType.MULTIPLE_CHOICE) {
            // Single answer - store index
            int selectedIndex = correctAnswerGroup.getToggles().indexOf(correctAnswerGroup.getSelectedToggle());
            request.setCorrectAnswer(String.valueOf(selectedIndex));
        } else {
            // Multiple answers - store array of indices
            List<Integer> selected = new ArrayList<>();
            for (int i = 0; i < correctAnswerCheckBoxes.size(); i++) {
                if (correctAnswerCheckBoxes.get(i).isSelected()) {
                    selected.add(i);
                }
            }
            request.setCorrectAnswer(gson.toJson(selected));
        }
    }
    
    private void buildTrueFalseRequest(CreateQuestionRequest request) {
        request.setCorrectAnswer(trueRadio.isSelected() ? "true" : "false");
    }
    
    private void buildEssayRequest(CreateQuestionRequest request) {
        if (!minWordsField.getText().isEmpty()) {
            request.setMinWords(Integer.parseInt(minWordsField.getText().trim()));
        }
        if (!maxWordsField.getText().isEmpty()) {
            request.setMaxWords(Integer.parseInt(maxWordsField.getText().trim()));
        }
        request.setGradingCriteria(gradingCriteriaArea.getText().trim());
    }
    
    private void buildShortAnswerRequest(CreateQuestionRequest request) {
        request.setCorrectAnswer(shortAnswerField.getText().trim());
        request.setGradingCriteria(shortAnswerGradingArea.getText().trim());
    }
    
    private void buildCodingRequest(CreateQuestionRequest request) {
        request.setProgrammingLanguage(programmingLanguageCombo.getValue());
        request.setStarterCode(starterCodeArea.getText().trim());
        request.setTestCases(testCasesArea.getText().trim());
        
        if (!timeLimitField.getText().isEmpty()) {
            request.setTimeLimitSeconds(Integer.parseInt(timeLimitField.getText().trim()));
        }
        if (!memoryLimitField.getText().isEmpty()) {
            request.setMemoryLimitMb(Integer.parseInt(memoryLimitField.getText().trim()));
        }
    }
    
    private void buildFillInBlankRequest(CreateQuestionRequest request) {
        request.setBlankPositions(blankAnswersArea.getText().trim());
    }
    
    private void buildMatchingRequest(CreateQuestionRequest request) {
        request.setLeftItems(gson.toJson(leftItems));
        request.setRightItems(gson.toJson(rightItems));
        request.setCorrectMatches(correctMatchesArea.getText().trim());
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    // Utility methods
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
