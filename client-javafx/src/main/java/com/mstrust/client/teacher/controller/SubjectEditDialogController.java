package com.mstrust.client. teacher.controller;

import com. mstrust.client.teacher. api.SubjectApiClient;
import com.mstrust.client. teacher.dto.SubjectDTO;
import com.mstrust.client.teacher.dto.DepartmentDTO;
import com. mstrust.client.teacher. dto.CreateSubjectRequest;
import com. mstrust.client.teacher. dto.UpdateSubjectRequest;
import javafx.application.Platform;
import javafx. collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx. fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/* ---------------------------------------------------
 * Controller cho dialog tạo/sửa môn học
 * Xử lý form validation và gọi API để tạo/cập nhật môn học
 * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
 * EditBy: K24DTCN210-NVMANH (27/11/2025 08:10) - Fix duplicate methods & syntax errors
 * --------------------------------------------------- */
public class SubjectEditDialogController {
    
    @FXML private Label dialogTitle;
    
    // Basic Information Fields
    @FXML private TextField codeField;
    @FXML private Label codeErrorLabel;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private Spinner<Integer> creditsSpinner;
    @FXML private ComboBox<DepartmentDTO> departmentCombo;
    @FXML private Label departmentErrorLabel;
    
    // Description
    @FXML private TextArea descriptionArea;
    
    // Additional Info
    @FXML private ComboBox<String> semesterCombo;
    @FXML private TextField academicYearField;
    @FXML private CheckBox isActiveCheckBox;
    
    // Validation
    @FXML private VBox validationSummary;
    @FXML private Label validationMessage;
    
    @FXML private Button saveButton;
    
    private SubjectApiClient apiClient;
    private Stage dialogStage;
    private SubjectDTO subject; // null = CREATE mode, not null = EDIT mode
    private boolean confirmed = false;
    private List<DepartmentDTO> allDepartments;
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API client và dialog stage
     * @param apiClient Client để gọi API backend
     * @param dialogStage Stage của dialog
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    public void initialize(SubjectApiClient apiClient, Stage dialogStage) {
        this.apiClient = apiClient;
        this.dialogStage = dialogStage;
        
        setupSemesterCombo();
        loadDepartments();
        setupValidation();
    }
    
    /* ---------------------------------------------------
     * Thiết lập ComboBox học kỳ
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private void setupSemesterCombo() {
        ObservableList<String> semesters = FXCollections. observableArrayList(
            "Học kỳ 1",
            "Học kỳ 2", 
            "Học kỳ 3 (Hè)"
        );
        semesterCombo.setItems(semesters);
    }
    
    /* ---------------------------------------------------
     * Load danh sách departments từ API
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 08:10) - Gọi getAllDepartments() & set department in EDIT mode
     * --------------------------------------------------- */
    private void loadDepartments() {
        Task<List<DepartmentDTO>> task = new Task<>() {
            @Override
            protected List<DepartmentDTO> call() throws Exception {
                return apiClient.getAllDepartments();
            }
        };
        
        task.setOnSucceeded(event -> {
            allDepartments = task.getValue();
            ObservableList<DepartmentDTO> departments = FXCollections.observableArrayList(allDepartments);
            departmentCombo.setItems(departments);
            
            System.out.println("[DEBUG SubjectEditDialogController] Departments loaded: " + departments.size());
            
            // IMPORTANT: Nếu đang ở EDIT mode, set department value sau khi load xong
            if (subject != null && subject.getDepartmentId() != null) {
                setDepartmentById(subject.getDepartmentId());
            }
            
            // Chỉ show warning nếu thực sự không có departments
            if (departments.isEmpty()) {
                showError("Cảnh báo", "Chưa có departments.  Vui lòng tạo departments trước.");
            }
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            System.err.println("[ERROR SubjectEditDialogController] Load departments failed:");
            if (ex != null) {
                System.err.println("[ERROR] Exception: " + ex.getClass().getName());
                System.err.println("[ERROR] Message: " + ex. getMessage());
                ex.printStackTrace();
            }
            showError("Lỗi tải danh sách departments", 
                ex != null ? ex.getMessage() : "Unknown error");
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Thiết lập real-time validation cho các trường
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private void setupValidation() {
        // Validate on focus lost
        codeField.focusedProperty(). addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateCode();
            }
        });
        
        nameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateName();
            }
        });
        
        departmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDepartment();
        });
    }
    
    /* ---------------------------------------------------
     * Thiết lập dữ liệu môn học để edit (EDIT mode)
     * @param subject Môn học cần edit
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 08:07) - Fix: Set department after departments loaded
     * --------------------------------------------------- */
    public void setSubject(SubjectDTO subject) {
        this.subject = subject;
        dialogTitle.setText("Sửa môn học: " + subject.getSubjectName());
        
        // Pre-fill form fields
        codeField.setText(subject.getSubjectCode());
        codeField.setDisable(true); // Không cho sửa mã môn học
        nameField.setText(subject.getSubjectName());
        creditsSpinner.getValueFactory().setValue(subject.getCredits());
        descriptionArea.setText(subject.getDescription());
        
        // Set department - sẽ được gọi sau khi departments đã load xong
        setDepartmentById(subject.getDepartmentId());
        
        // Additional info (if exists in DTO)
        isActiveCheckBox.setSelected(true); // Default active
    }
    
    /* ---------------------------------------------------
     * Set department value vào ComboBox
     * Nếu allDepartments chưa load xong, sẽ được gọi lại từ loadDepartments()
     * @param department Department cần set
     * @author: K24DTCN210-NVMANH (27/11/2025 08:07)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 08:15) - Use SelectionModel. select(index) instead of setValue()
     * --------------------------------------------------- */
    private void setDepartmentById(Long departmentId) {
        if (departmentId == null) {
            System. out.println("[DEBUG SubjectEditDialogController] DepartmentId is null, skipping");
            return;
        }
        
        // Nếu departments chưa load, chờ nó load xong
        if (allDepartments == null || allDepartments.isEmpty()) {
            System.out.println("[DEBUG SubjectEditDialogController] Departments not loaded yet, will retry after load");
            return;
        }
        
        System.out.println("[DEBUG SubjectEditDialogController] Attempting to set department by ID: " + departmentId);
        System.out.println("[DEBUG SubjectEditDialogController] Available departments: " + allDepartments.size());
        
        // Tìm index của department trong list với null safety
        for (int i = 0; i < allDepartments.size(); i++) {
            try {
                DepartmentDTO dept = allDepartments.get(i);
                
                // Null safety checks
                if (dept == null) {
                    System.out. println("[DEBUG SubjectEditDialogController]   [" + i + "] NULL DEPARTMENT OBJECT!");
                    continue;
                }
                
                Long deptId = dept.getId();
                String deptName = dept.getDepartmentName();
                
                System.out.println("[DEBUG SubjectEditDialogController]   [" + i + "] ID=" + 
                                  (deptId != null ? deptId : "NULL") + 
                                  ", Name=" + (deptName != null ? deptName : "NULL"));
                
                // Check nếu ID match
                if (deptId != null && deptId.equals(departmentId)) {
                    // Dùng SelectionModel. select(index) thay vì setValue()
                    departmentCombo.getSelectionModel().select(i);
                    System. out.println("[DEBUG SubjectEditDialogController] ✓ Department selected at index " + i + 
                                      ": " + deptName);
                    return;
                }
            } catch (Exception e) {
                System.err.println("[ERROR SubjectEditDialogController] Error processing department at index " + i);
                System.err.println("[ERROR] Exception: " + e.getClass().getName());
                System.err.println("[ERROR] Message: " + e. getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("[DEBUG SubjectEditDialogController] ✗ No matching department found for ID: " + departmentId);
    }
    
    /* ---------------------------------------------------
     * Validate mã môn học
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private boolean validateCode() {
        String code = codeField.getText();
        if (code == null || code.trim().isEmpty()) {
            codeErrorLabel.setVisible(true);
            codeField.getStyleClass().add("error-field");
            return false;
        }
        
        codeErrorLabel.setVisible(false);
        codeField.getStyleClass().remove("error-field");
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate tên môn học
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private boolean validateName() {
        String name = nameField.getText();
        if (name == null || name.trim(). isEmpty()) {
            nameErrorLabel.setVisible(true);
            nameField.getStyleClass().add("error-field");
            return false;
        }
        
        nameErrorLabel.setVisible(false);
        nameField.getStyleClass().remove("error-field");
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate department
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (27/11/2025 08:10)
     * --------------------------------------------------- */
    private boolean validateDepartment() {
        if (departmentCombo.getValue() == null) {
            departmentErrorLabel.setVisible(true);
            return false;
        }
        
        departmentErrorLabel.setVisible(false);
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate toàn bộ form
     * @return true nếu form hợp lệ
     * @author: K24DTCN210-NVMANH (27/11/2025 08:10)
     * --------------------------------------------------- */
    private boolean validateForm() {
        boolean isFormValid = true;
        
        isFormValid = validateCode() && isFormValid;
        isFormValid = validateName() && isFormValid;
        isFormValid = validateDepartment() && isFormValid;
        
        if (!isFormValid) {
            validationSummary.setVisible(true);
            validationMessage.setText("Vui lòng kiểm tra lại các trường có lỗi");
        } else {
            validationSummary.setVisible(false);
        }
        
        return isFormValid;
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện nhấn nút Save
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    @FXML
    private void handleSave() {
        if (! validateForm()) {
            return;
        }
        
        saveButton.setDisable(true);
        
        if (subject == null) {
            // CREATE mode
            createSubject();
        } else {
            // EDIT mode
            updateSubject();
        }
    }
    
    /* ---------------------------------------------------
     * Tạo môn học mới qua API
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 07:58) - Better error messages for users
     * --------------------------------------------------- */
    private void createSubject() {
        Task<SubjectDTO> task = new Task<>() {
            @Override
            protected SubjectDTO call() throws Exception {
                CreateSubjectRequest request = buildCreateRequest();
                return apiClient.createSubject(request);
            }
        };
        
        task.setOnSucceeded(event -> {
            showInfo("Thành công", "Đã tạo môn học mới thành công!");
            confirmed = true;
            dialogStage.close();
        });
        
        task.setOnFailed(event -> {
            saveButton.setDisable(false);
            Throwable ex = task.getException();
            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
            
            // Parse user-friendly error messages
            String userMessage = parseErrorMessage(errorMsg);
            showError("Không thể tạo môn học", userMessage);
        });
        
        new Thread(task). start();
    }
    
    /* ---------------------------------------------------
     * Cập nhật môn học qua API
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 07:58) - Better error messages for users
     * --------------------------------------------------- */
    private void updateSubject() {
        Task<SubjectDTO> task = new Task<>() {
            @Override
            protected SubjectDTO call() throws Exception {
                UpdateSubjectRequest request = buildUpdateRequest();
                return apiClient.updateSubject(subject.getId(), request);
            }
        };
        
        task.setOnSucceeded(event -> {
            showInfo("Thành công", "Đã cập nhật môn học thành công!");
            confirmed = true;
            dialogStage.close();
        });
        
        task.setOnFailed(event -> {
            saveButton.setDisable(false);
            Throwable ex = task. getException();
            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
            
            // Parse user-friendly error messages
            String userMessage = parseErrorMessage(errorMsg);
            showError("Không thể cập nhật môn học", userMessage);
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Xây dựng CreateSubjectRequest từ form data
     * @return CreateSubjectRequest object
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private CreateSubjectRequest buildCreateRequest() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setSubjectCode(codeField.getText().trim());
        request.setSubjectName(nameField.getText().trim());
        request.setCredits(creditsSpinner.getValue());
        request.setDepartmentId(departmentCombo.getValue().getId());
        
        String description = descriptionArea.getText();
        if (description != null && !description.trim().isEmpty()) {
            request.setDescription(description. trim());
        }
        
        return request;
    }
    
    /* ---------------------------------------------------
     * Xây dựng UpdateSubjectRequest từ form data
     * @return UpdateSubjectRequest object
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private UpdateSubjectRequest buildUpdateRequest() {
        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setSubjectName(nameField.getText().trim());
        request.setCredits(creditsSpinner.getValue());
        request.setDepartmentId(departmentCombo.getValue(). getId());
        
        String description = descriptionArea.getText();
        if (description != null && ! description.trim().isEmpty()) {
            request.setDescription(description. trim());
        }
        
        return request;
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện nhấn nút Cancel
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        confirmed = false;
        dialogStage.close();
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem user đã confirm hay chưa
     * @return true nếu user đã click Save và thành công
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /* ---------------------------------------------------
     * Hiển thị dialog thông báo lỗi
     * @param title Tiêu đề dialog
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert. setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Hiển thị dialog thông báo thông tin
     * @param title Tiêu đề dialog
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (26/11/2025 02:00)
     * --------------------------------------------------- */
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType. INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /* ---------------------------------------------------
     * Parse error message từ backend thành user-friendly message
     * @param errorMsg Raw error message từ backend
     * @return User-friendly error message
     * @author: K24DTCN210-NVMANH (27/11/2025 07:58)
     * --------------------------------------------------- */
    private String parseErrorMessage(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().isEmpty()) {
            return "Đã xảy ra lỗi không xác định.  Vui lòng thử lại.";
        }
        
        // Handle HTTP status codes
        if (errorMsg.contains("403")) {
            return "Bạn không có quyền thực hiện thao tác này.\n" +
                   "Vui lòng liên hệ quản trị viên để được cấp quyền.";
        }
        
        if (errorMsg.contains("401")) {
            return "Phiên đăng nhập đã hết hạn.\n" +
                   "Vui lòng đăng nhập lại. ";
        }
        
        if (errorMsg.contains("400")) {
            return "Dữ liệu không hợp lệ.\n" +
                   "Vui lòng kiểm tra lại thông tin đã nhập.";
        }
        
        if (errorMsg. contains("409") || 
            errorMsg.contains("already exists") || 
            errorMsg.contains("đã tồn tại")) {
            return "Mã môn học đã tồn tại trong hệ thống.\n" +
                   "Vui lòng sử dụng mã môn học khác.";
        }
        
        if (errorMsg.contains("500")) {
            return "Lỗi hệ thống.\n" +
                   "Vui lòng thử lại sau hoặc liên hệ quản trị viên.";
        }
        
        // Check for network errors
        if (errorMsg.toLowerCase().contains("connection") || 
            errorMsg.toLowerCase(). contains("timeout")) {
            return "Không thể kết nối đến máy chủ.\n" +
                   "Vui lòng kiểm tra kết nối mạng.";
        }
        
        // Default: return a cleaned up message
        // Extract JSON message if present
        if (errorMsg. contains("\"message\":")) {
            try {
                int start = errorMsg.indexOf("\"message\":\"") + 11;
                int end = errorMsg.indexOf("\"", start);
                if (end > start) {
                    String jsonMessage = errorMsg.substring(start, end);
                    // Translate common technical terms
                    jsonMessage = jsonMessage
                        .replace("Access denied", "Truy cập bị từ chối")
                        .replace("You don't have permission", "Bạn không có quyền")
                        .replace("resource", "chức năng này");
                    return jsonMessage;
                }
            } catch (Exception e) {
                // Fallback to original message
            }
        }
        
        // Return truncated error for very long messages
        if (errorMsg.length() > 200) {
            return "Đã xảy ra lỗi.  Vui lòng thử lại sau.\n" +
                   "Nếu vấn đề vẫn tiếp diễn, hãy liên hệ quản trị viên.";
        }
        
        return errorMsg;
    }
}
