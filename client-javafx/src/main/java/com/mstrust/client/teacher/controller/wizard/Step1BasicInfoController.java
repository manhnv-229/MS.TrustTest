package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.dto.ExamFormat;
import com.mstrust.client.teacher.dto.ExamPurpose;
import com.mstrust.client.teacher.dto.ExamWizardData;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/* ---------------------------------------------------
 * Controller cho Step 1 của Exam Creation Wizard
 * Xử lý nhập liệu thông tin cơ bản của đề thi:
 * - Tiêu đề, mô tả
 * - Subject class
 * - Mục đích thi (purpose)
 * - Định dạng đề thi (format)
 * - Thời gian bắt đầu và kết thúc (DatePicker + Time Spinners)
 * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
 * EditBy: K24DTCN210-NVMANH (29/11/2025 16:18) - Fixed duplicate methods & syntax errors
 * --------------------------------------------------- */
public class Step1BasicInfoController {

    // Basic form fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> subjectClassCombo;
    @FXML private ComboBox<ExamPurpose> examPurposeCombo;
    @FXML private ComboBox<ExamFormat> examFormatCombo;
    
    // Start DateTime fields
    @FXML private DatePicker startDatePicker;
    @FXML private Spinner<Integer> startHourSpinner;
    @FXML private Spinner<Integer> startMinuteSpinner;
    
    // End DateTime fields  
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> endHourSpinner;
    @FXML private Spinner<Integer> endMinuteSpinner;
    
    @FXML private Label errorLabel;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;

    /* ---------------------------------------------------
     * Initialize method được gọi sau khi FXML load
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * EditBy: K24DTCN210-NVMANH (29/11/2025 15:35) - Added initialization
     * --------------------------------------------------- */
    @FXML
    private void initialize() {
        hideError();
        setupTimeSpinners();
        setupDatePickers();
        initializeComboBoxes();
    }

    /* ---------------------------------------------------
     * Initialize combo boxes with proper display formatters
     * @author: K24DTCN210-NVMANH (29/11/2025 15:35)
     * --------------------------------------------------- */
    private void initializeComboBoxes() {
        // Setup ExamPurpose ComboBox
        examPurposeCombo.setItems(FXCollections.observableArrayList(ExamPurpose.values()));
        examPurposeCombo.setCellFactory(lv -> new ListCell<ExamPurpose>() {
            @Override
            protected void updateItem(ExamPurpose item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        examPurposeCombo.setButtonCell(new ListCell<ExamPurpose>() {
            @Override
            protected void updateItem(ExamPurpose item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        // Setup ExamFormat ComboBox
        examFormatCombo.setItems(FXCollections.observableArrayList(ExamFormat.values()));
        examFormatCombo.setCellFactory(lv -> new ListCell<ExamFormat>() {
            @Override
            protected void updateItem(ExamFormat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        examFormatCombo.setButtonCell(new ListCell<ExamFormat>() {
            @Override
            protected void updateItem(ExamFormat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
    }

    /* ---------------------------------------------------
     * Setup time spinners với giá trị mặc định
     * @author: K24DTCN210-NVMANH (29/11/2025 13:05)
     * --------------------------------------------------- */
    private void setupTimeSpinners() {
        // Setup hour spinners (0-23)
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        
        // Setup minute spinners (0-59, step 5)
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        
        // Make spinners editable
        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);
    }

    /* ---------------------------------------------------
     * Setup DatePickers với định dạng dd/MM/yyyy
     * @author: K24DTCN210-NVMANH (29/11/2025 13:05)
     * --------------------------------------------------- */
    private void setupDatePickers() {
        // Set converter cho định dạng dd/MM/yyyy
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        startDatePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(dateFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return string != null && !string.trim().isEmpty() ? 
                        LocalDate.parse(string, dateFormatter) : null;
                } catch (Exception e) {
                    return null;
                }
            }
        });
        
        endDatePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ?  date.format(dateFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return string != null && !string.trim().isEmpty() ?  
                        LocalDate.parse(string, dateFormatter) : null;
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Set default dates (tomorrow and day after)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        startDatePicker.setValue(tomorrow);
        endDatePicker.setValue(tomorrow.plusDays(1));
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        loadDataToForm();
    }

    /* ---------------------------------------------------
     * Set parent controller để có thể gọi navigation
     * @param parentController Controller cha
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    public void setParentController(ExamCreationWizardController parentController) {
        this.parentController = parentController;
    }

    /* ---------------------------------------------------
     * Load danh sách subject classes vào ComboBox
     * @param subjectClasses Danh sách tên các subject class
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    public void loadSubjectClasses(List<String> subjectClasses) {
        subjectClassCombo.setItems(FXCollections.observableArrayList(subjectClasses));
    }

    /* ---------------------------------------------------
     * Load dữ liệu từ wizardData vào form
     * (Dùng khi quay lại step này từ step sau)
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * EditBy: K24DTCN210-NVMANH (29/11/2025 13:05) - Load DatePicker + Spinners
     * --------------------------------------------------- */
    private void loadDataToForm() {
        if (wizardData == null) return;

        if (wizardData.getTitle() != null) {
            titleField.setText(wizardData.getTitle());
        }
        if (wizardData.getDescription() != null) {
            descriptionArea.setText(wizardData.getDescription());
        }
        if (wizardData.getSubjectClassName() != null) {
            subjectClassCombo.setValue(wizardData.getSubjectClassName());
        }
        if (wizardData.getExamPurpose() != null) {
            examPurposeCombo.setValue(wizardData.getExamPurpose());
        }
        if (wizardData.getExamFormat() != null) {
            examFormatCombo.setValue(wizardData.getExamFormat());
        }
        
        // Load datetime to DatePicker + Spinners
        if (wizardData.getStartTime() != null) {
            LocalDateTime startTime = wizardData.getStartTime();
            startDatePicker.setValue(startTime.toLocalDate());
            startHourSpinner.getValueFactory().setValue(startTime.getHour());
            startMinuteSpinner.getValueFactory().setValue(startTime.getMinute());
        }
        
        if (wizardData.getEndTime() != null) {
            LocalDateTime endTime = wizardData.getEndTime();
            endDatePicker.setValue(endTime.toLocalDate());
            endHourSpinner.getValueFactory().setValue(endTime.getHour());
            endMinuteSpinner.getValueFactory().setValue(endTime.getMinute());
        }
    }

    /* ---------------------------------------------------
     * Combine DatePicker + Spinners thành LocalDateTime
     * @param datePicker DatePicker component
     * @param hourSpinner Hour spinner
     * @param minuteSpinner Minute spinner
     * @return LocalDateTime hoặc null nếu invalid
     * @author: K24DTCN210-NVMANH (29/11/2025 13:05)
     * --------------------------------------------------- */
    private LocalDateTime combineDateAndTime(DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner) {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            return null;
        }
        
        Integer hour = hourSpinner.getValue();
        Integer minute = minuteSpinner.getValue();
        
        if (hour == null) hour = 0;
        if (minute == null) minute = 0;
        
        // Validate ranges
        if (hour < 0 || hour > 23) hour = 0;
        if (minute < 0 || minute > 59) minute = 0;
        
        return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
    }

    /* ---------------------------------------------------
     * Lưu dữ liệu từ form vào wizardData (PRIVATE)
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * EditBy: K24DTCN210-NVMANH (29/11/2025 13:05) - Save DatePicker + Spinners
     * --------------------------------------------------- */
    private void saveFormToData() {
        wizardData.setTitle(titleField.getText().trim());
        wizardData.setDescription(descriptionArea.getText().trim());
        wizardData.setSubjectClassName(subjectClassCombo.getValue());
        wizardData.setExamPurpose(examPurposeCombo.getValue());
        wizardData.setExamFormat(examFormatCombo.getValue());
        
        // Combine DatePicker + Spinners
        wizardData.setStartTime(combineDateAndTime(startDatePicker, startHourSpinner, startMinuteSpinner));
        wizardData.setEndTime(combineDateAndTime(endDatePicker, endHourSpinner, endMinuteSpinner));
        
        // TODO: Cần resolve subjectClassId từ subjectClassName
        // Tạm thời set null, sẽ implement sau khi có API
        wizardData.setSubjectClassId(null);
    }

    /* ---------------------------------------------------
     * PUBLIC method để force save form data từ parent controller
     * @author: K24DTCN210-NVMANH (29/11/2025 16:18)
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP1: saveFormToWizardData() CALLED ===");
        System.out.println("WizardData Object Hash: " + System.identityHashCode(wizardData));
        
        // Log form values BEFORE saving
        System.out.println("BEFORE SAVE - Form Values:");
        System.out.println("  titleField.getText(): '" + titleField.getText() + "'");
        System.out.println("  subjectClassCombo.getValue(): " + subjectClassCombo.getValue());
        System.out.println("  examPurposeCombo.getValue(): " + examPurposeCombo.getValue());
        System.out.println("  examFormatCombo.getValue(): " + examFormatCombo.getValue());
        System.out.println("  startDatePicker.getValue(): " + startDatePicker.getValue());
        System.out.println("  startHourSpinner.getValue(): " + startHourSpinner.getValue());
        System.out.println("  startMinuteSpinner.getValue(): " + startMinuteSpinner.getValue());
        
        // Call the existing private method
        saveFormToData();
        
        // Log wizardData values AFTER saving  
        System.out.println("AFTER SAVE - WizardData Values:");
        System.out.println("  wizardData.getTitle(): '" + wizardData.getTitle() + "'");
        System.out.println("  wizardData.getSubjectClassName(): " + wizardData.getSubjectClassName());
        System.out.println("  wizardData.getExamPurpose(): " + wizardData.getExamPurpose());
        System.out.println("  wizardData.getExamFormat(): " + wizardData.getExamFormat());
        System.out.println("  wizardData.getStartTime(): " + wizardData.getStartTime());
        System.out.println("  wizardData.getEndTime(): " + wizardData.getEndTime());
        System.out.println("=== STEP1: saveFormToWizardData() COMPLETED ===");
    }

    /* ---------------------------------------------------
     * Validate form data
     * @return true nếu hợp lệ, false nếu có lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    private boolean validateForm() {
        hideError();
        
        saveFormToData();
        List<String> errors = wizardData.validateStep1();
        
        if (! errors.isEmpty()) {
            showError(String.join("\n", errors));
            return false;
        }
        
        return true;
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /* ---------------------------------------------------
     * Xử lý nút Next - chuyển sang Step 2
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * EditBy: K24DTCN210-NVMANH (29/11/2025 15:35) - Added debug logging
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        System.out.println("=== STEP 1 DEBUG: handleNext() CALLED ===");
        
        boolean isValid = validateForm();
        System.out.println("validateForm() returned: " + isValid);
        
        if (isValid) {
            // DEBUG: Log data after saving to wizardData
            System.out.println("=== STEP 1 DEBUG: VALIDATION PASSED ===");
            if (wizardData != null) {
                System.out.println("Title: " + wizardData.getTitle());
                System.out.println("Start Time: " + wizardData.getStartTime());
                System.out.println("End Time: " + wizardData.getEndTime());
                System.out.println("Subject Class ID: " + wizardData.getSubjectClassId());
                System.out.println("Subject Class Name: " + wizardData.getSubjectClassName());
                System.out.println("Exam Purpose: " + wizardData.getExamPurpose());
                System.out.println("Exam Format: " + wizardData.getExamFormat());
            } else {
                System.out.println("ERROR: wizardData is NULL!");
            }
            System.out.println("=== Calling parentController.nextStep() ===");
            parentController.nextStep();
        } else {
            System.out.println("=== STEP 1 DEBUG: VALIDATION FAILED ===");
            System.out.println("Error label visible: " + errorLabel.isVisible());
            System.out.println("Error label text: '" + errorLabel.getText() + "'");
        }
        System.out.println("===================================");
    }

    /* ---------------------------------------------------
     * Xử lý nút Cancel - hủy wizard
     * @author: K24DTCN210-NVMANH (28/11/2025 08:24)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        parentController.cancelWizard();
    }
}
