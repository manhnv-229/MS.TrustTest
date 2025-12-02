package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.OrganizationApiClient;
import com.mstrust.client.admin.api.UserManagementApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ---------------------------------------------------
 * Controller cho dialog tạo/sửa người dùng
 * Xử lý form validation và gọi API để tạo/cập nhật user
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class UserEditDialogController {
    
    @FXML private Label dialogTitle;
    
    // Basic Information Fields
    @FXML private TextField fullNameField;
    @FXML private Label fullNameErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordErrorLabel;
    @FXML private HBox passwordContainer;
    @FXML private TextField studentCodeField;
    @FXML private TextField phoneNumberField;
    
    // Personal Information Fields
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextArea addressArea;
    
    // Organization Fields
    @FXML private ComboBox<Map<String, Object>> departmentCombo;
    @FXML private Label departmentErrorLabel;
    @FXML private ComboBox<Map<String, Object>> classCombo;
    @FXML private Label classErrorLabel;
    
    // Role and Status
    @FXML private VBox rolesContainer;
    @FXML private CheckBox isActiveCheckBox;
    
    // Validation
    @FXML private VBox validationSummary;
    @FXML private Label validationMessage;
    @FXML private Button saveButton;
    
    private UserManagementApiClient userApiClient;
    private OrganizationApiClient orgApiClient;
    private Stage dialogStage;
    private Map<String, Object> user; // null = CREATE mode, not null = EDIT mode
    private Map<String, Object> currentUserInfo; // Thông tin user hiện tại đang đăng nhập
    private boolean confirmed = false;
    private List<Map<String, Object>> allDepartments;
    private List<Map<String, Object>> allClasses;
    private Map<CheckBox, String> roleCheckBoxes = new HashMap<>();
    
    // Available roles
    private static final List<String> AVAILABLE_ROLES = Arrays.asList(
        "STUDENT", "TEACHER", "CLASS_MANAGER", "DEPT_MANAGER", "ADMIN"
    );
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API clients và dialog stage
     * @param userApiClient Client để gọi User APIs
     * @param orgApiClient Client để gọi Organization APIs
     * @param dialogStage Stage của dialog
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Lấy current user info từ JWT
     * --------------------------------------------------- */
    public void initialize(UserManagementApiClient userApiClient, 
                          OrganizationApiClient orgApiClient, 
                          Stage dialogStage) {
        this.userApiClient = userApiClient;
        this.orgApiClient = orgApiClient;
        this.dialogStage = dialogStage;
        
        // Lấy thông tin user hiện tại từ JWT token
        this.currentUserInfo = userApiClient.getCurrentUserInfo();
        System.out.println("[DEBUG initialize] currentUserInfo from JWT: " + currentUserInfo);
        
        setupGenderCombo();
        setupRoles();
        loadDepartments();
        setupValidation();
    }
    
    /* ---------------------------------------------------
     * Thiết lập ComboBox giới tính
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupGenderCombo() {
        ObservableList<String> genders = FXCollections.observableArrayList(
            "MALE", "FEMALE", "OTHER"
        );
        genderCombo.setItems(genders);
    }
    
    /* ---------------------------------------------------
     * Thiết lập các CheckBox cho roles
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupRoles() {
        rolesContainer.getChildren().clear();
        roleCheckBoxes.clear();
        
        for (String role : AVAILABLE_ROLES) {
            CheckBox checkBox = new CheckBox(role);
            roleCheckBoxes.put(checkBox, role);
            rolesContainer.getChildren().add(checkBox);
        }
    }
    
    /* ---------------------------------------------------
     * Load danh sách departments từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadDepartments() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return orgApiClient.getAllDepartments();
            }
        };
        
        task.setOnSucceeded(event -> {
            allDepartments = task.getValue();
            ObservableList<Map<String, Object>> departments = FXCollections.observableArrayList(allDepartments);
            
            // Setup ComboBox với custom cell factory để hiển thị tên
            departmentCombo.setItems(departments);
            departmentCombo.setCellFactory(param -> new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = (String) item.getOrDefault("departmentName", "");
                        setText(name);
                    }
                }
            });
            departmentCombo.setButtonCell(new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = (String) item.getOrDefault("departmentName", "");
                        setText(name);
                    }
                }
            });
            
            // Load classes khi department thay đổi
            departmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadClassesForDepartment(getLongValue(newVal.get("id")));
                } else {
                    classCombo.getItems().clear();
                    classCombo.setDisable(true);
                }
            });
            
            // Nếu đang ở EDIT mode, set department value sau khi load xong
            if (user != null && user.get("departmentId") != null) {
                Long departmentId = getLongValue(user.get("departmentId"));
                if (departmentId != null) {
                    setDepartmentById(departmentId);
                }
            }
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            showError("Lỗi tải danh sách khoa", 
                ex != null ? ex.getMessage() : "Unknown error");
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Load danh sách classes theo department
     * @param departmentId ID của department
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadClassesForDepartment(Long departmentId) {
        if (departmentId == null) {
            classCombo.getItems().clear();
            classCombo.setDisable(true);
            return;
        }
        
        // Clear previous selection
        classCombo.getSelectionModel().clearSelection();
        classCombo.setDisable(true);
        
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                // Sử dụng endpoint chuyên dụng để lấy classes theo department
                return orgApiClient.getClassesByDepartment(departmentId);
            }
        };
        
        task.setOnSucceeded(event -> {
            List<Map<String, Object>> classes = task.getValue();
            ObservableList<Map<String, Object>> classList = FXCollections.observableArrayList(classes);
            
            classCombo.setItems(classList);
            classCombo.setDisable(false);
            
            // Setup cell factory để hiển thị tên lớp
            classCombo.setCellFactory(param -> new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = (String) item.getOrDefault("className", "");
                        setText(name);
                    }
                }
            });
            classCombo.setButtonCell(new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = (String) item.getOrDefault("className", "");
                        setText(name);
                    }
                }
            });
            
            // Nếu đang ở EDIT mode, set class value sau khi load xong
            if (user != null && user.get("classId") != null) {
                Long classId = getLongValue(user.get("classId"));
                if (classId != null) {
                    // Đợi một chút để đảm bảo items đã được set vào combo box
                    Platform.runLater(() -> {
                        setClassById(classId);
                    });
                }
            }
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            showError("Lỗi tải danh sách lớp", 
                ex != null ? ex.getMessage() : "Unknown error");
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Set department value vào ComboBox
     * @param departmentId ID của department
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setDepartmentById(Long departmentId) {
        if (departmentId == null || allDepartments == null || allDepartments.isEmpty()) {
            System.out.println("[DEBUG] Cannot set department: departmentId=" + departmentId + ", allDepartments=" + (allDepartments != null ? allDepartments.size() : "null"));
            return;
        }
        
        System.out.println("[DEBUG] Setting department with ID: " + departmentId);
        for (int i = 0; i < allDepartments.size(); i++) {
            Map<String, Object> dept = allDepartments.get(i);
            Long deptId = getLongValue(dept.get("id"));
            if (deptId != null && deptId.equals(departmentId)) {
                System.out.println("[DEBUG] Found department at index " + i + ": " + dept.get("departmentName"));
                departmentCombo.getSelectionModel().select(i);
                // Load classes cho department này, sau đó sẽ set class trong callback
                loadClassesForDepartment(departmentId);
                return;
            }
        }
        System.out.println("[DEBUG] Department with ID " + departmentId + " not found in list");
    }
    
    /* ---------------------------------------------------
     * Set class value vào ComboBox
     * @param classId ID của class
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setClassById(Long classId) {
        if (classId == null) {
            System.out.println("[DEBUG] Cannot set class: classId is null");
            return;
        }
        
        ObservableList<Map<String, Object>> items = classCombo.getItems();
        if (items == null || items.isEmpty()) {
            System.out.println("[DEBUG] Cannot set class: classCombo items is empty");
            return;
        }
        
        System.out.println("[DEBUG] Setting class with ID: " + classId + ", available classes: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> cls = items.get(i);
            Long clsId = getLongValue(cls.get("id"));
            if (clsId != null && clsId.equals(classId)) {
                System.out.println("[DEBUG] Found class at index " + i + ": " + cls.get("className"));
                classCombo.getSelectionModel().select(i);
                return;
            }
        }
        System.out.println("[DEBUG] Class with ID " + classId + " not found in list");
    }
    
    /* ---------------------------------------------------
     * Thiết lập dữ liệu user để edit (EDIT mode)
     * @param user User data cần edit
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void setUser(Map<String, Object> user) {
        this.user = user;
        dialogTitle.setText("Sửa người dùng: " + user.get("fullName"));
        
        // Ẩn password field khi edit
        passwordContainer.setVisible(false);
        passwordContainer.setManaged(false);
        
        // Pre-fill form fields
        fullNameField.setText((String) user.getOrDefault("fullName", ""));
        emailField.setText((String) user.getOrDefault("email", ""));
        emailField.setDisable(true); // Không cho sửa email
        studentCodeField.setText((String) user.getOrDefault("studentCode", ""));
        phoneNumberField.setText((String) user.getOrDefault("phoneNumber", ""));
        
        // Date of birth
        Object dob = user.get("dateOfBirth");
        if (dob != null) {
            if (dob instanceof String) {
                // Parse string date
                try {
                    LocalDate date = LocalDate.parse((String) dob);
                    dateOfBirthPicker.setValue(date);
                } catch (Exception e) {
                    // Ignore parse error
                }
            }
        }
        
        // Gender
        Object genderObj = user.get("gender");
        if (genderObj != null) {
            String gender = genderObj.toString();
            if (!gender.isEmpty()) {
                genderCombo.setValue(gender);
            }
        }
        
        // Address
        Object addressObj = user.get("address");
        if (addressObj != null) {
            addressArea.setText(addressObj.toString());
        } else {
            addressArea.setText("");
        }
        
        // Department và Class - sẽ được set sau khi departments load xong
        // Nếu departments đã load xong, set ngay. Nếu chưa, sẽ được set trong loadDepartments callback
        if (allDepartments != null && !allDepartments.isEmpty()) {
            Object deptIdObj = user.get("departmentId");
            if (deptIdObj != null) {
                Long departmentId = getLongValue(deptIdObj);
                if (departmentId != null) {
                    setDepartmentById(departmentId);
                }
            }
        }
        
        // Roles - normalize role names để so sánh đúng
        Object rolesObj = user.get("roles");
        if (rolesObj != null) {
            Set<String> roles = new HashSet<>();
            if (rolesObj instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> rolesSet = (Set<String>) rolesObj;
                for (String role : rolesSet) {
                    // Normalize: remove "ROLE_" prefix nếu có
                    String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                    roles.add(normalized.toUpperCase());
                }
            } else if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesObj;
                for (String role : rolesList) {
                    // Normalize: remove "ROLE_" prefix nếu có
                    String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                    roles.add(normalized.toUpperCase());
                }
            }
            
            // Set selected cho các checkboxes
            for (CheckBox checkBox : roleCheckBoxes.keySet()) {
                String roleKey = roleCheckBoxes.get(checkBox); // "STUDENT", "TEACHER", etc.
                checkBox.setSelected(roles.contains(roleKey.toUpperCase()));
            }
        } else {
            // Nếu không có roles, uncheck tất cả
            for (CheckBox checkBox : roleCheckBoxes.keySet()) {
                checkBox.setSelected(false);
            }
        }
        
        // Active status
        Boolean isActive = (Boolean) user.getOrDefault("isActive", true);
        isActiveCheckBox.setSelected(isActive != null ? isActive : true);
        
        // Kiểm tra và disable role checkboxes nếu cần
        checkAndDisableRoleEditing();
        
        // FALLBACK: Nếu validation trên không hoạt động, thử cách khác
        // Kiểm tra trực tiếp bằng email từ user data
        if (user != null) {
            String editingUserEmail = (String) user.get("email");
            System.out.println("[DEBUG setUser] Editing user email: " + editingUserEmail);
            
            // Hardcode check cho admin email (fallback)
            if ("admin@gmail.com".equalsIgnoreCase(editingUserEmail)) {
                System.out.println("[DEBUG setUser] FALLBACK: Detected admin email, disabling roles");
                disableAllRoleCheckboxes();
            }
            
            // Kiểm tra user có ADMIN role không
            Object userRolesObj = user.get("roles");
            if (userRolesObj != null) {
                boolean hasAdminRole = false;
                if (userRolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) userRolesObj;
                    for (String role : rolesList) {
                        if (role.contains("ADMIN")) {
                            hasAdminRole = true;
                            break;
                        }
                    }
                } else if (userRolesObj instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<String> rolesSet = (Set<String>) userRolesObj;
                    for (String role : rolesSet) {
                        if (role.contains("ADMIN")) {
                            hasAdminRole = true;
                            break;
                        }
                    }
                }
                
                if (hasAdminRole) {
                    System.out.println("[DEBUG setUser] FALLBACK: User has ADMIN role, disabling roles");
                    disableAllRoleCheckboxes();
                }
            }
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra và disable role checkboxes nếu:
     * 1. Current user có ADMIN role VÀ đang edit chính mình
     * 2. Current user có ADMIN role VÀ user đang edit có ADMIN role
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Cải thiện logic so sánh và thêm logging
     * --------------------------------------------------- */
    private void checkAndDisableRoleEditing() {
        if (currentUserInfo == null || user == null) {
            System.out.println("[DEBUG] checkAndDisableRoleEditing: currentUserInfo=" + currentUserInfo + ", user=" + user);
            return; // Không có thông tin current user hoặc không phải edit mode
        }
        
        // Kiểm tra current user có ADMIN role không
        @SuppressWarnings("unchecked")
        List<String> currentUserRoles = (List<String>) currentUserInfo.get("roles");
        boolean currentUserIsAdmin = false;
        if (currentUserRoles != null) {
            for (String role : currentUserRoles) {
                if ("ADMIN".equalsIgnoreCase(role)) {
                    currentUserIsAdmin = true;
                    break;
                }
            }
        }
        
        System.out.println("[DEBUG] Current user roles: " + currentUserRoles + ", isAdmin: " + currentUserIsAdmin);
        
        if (!currentUserIsAdmin) {
            System.out.println("[DEBUG] Current user is not ADMIN, no need to disable");
            return; // Current user không phải ADMIN, không cần disable
        }
        
        // Lấy current user email và ID
        String currentEmail = (String) currentUserInfo.get("email");
        Long currentUserId = null;
        Object userIdObj = currentUserInfo.get("userId");
        if (userIdObj != null) {
            if (userIdObj instanceof Number) {
                currentUserId = ((Number) userIdObj).longValue();
            } else {
                try {
                    currentUserId = Long.parseLong(userIdObj.toString());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Lấy user đang edit email và ID
        String editingUserEmail = (String) user.get("email");
        Long editingUserId = getLongValue(user.get("id"));
        
        System.out.println("[DEBUG] Current user - ID: " + currentUserId + ", Email: " + currentEmail);
        System.out.println("[DEBUG] Editing user - ID: " + editingUserId + ", Email: " + editingUserEmail);
        
        // Kiểm tra user đang edit có ADMIN role không
        Object rolesObj = user.get("roles");
        boolean editingUserIsAdmin = false;
        if (rolesObj != null) {
            Set<String> editingUserRoles = new HashSet<>();
            if (rolesObj instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> rolesSet = (Set<String>) rolesObj;
                for (String role : rolesSet) {
                    String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                    editingUserRoles.add(normalized.toUpperCase());
                }
            } else if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesObj;
                for (String role : rolesList) {
                    String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                    editingUserRoles.add(normalized.toUpperCase());
                }
            }
            editingUserIsAdmin = editingUserRoles.contains("ADMIN");
        }
        
        System.out.println("[DEBUG] Editing user isAdmin: " + editingUserIsAdmin);
        
        // Kiểm tra đang edit chính mình (so sánh ID hoặc email)
        boolean isEditingSelf = false;
        if (currentUserId != null && editingUserId != null) {
            isEditingSelf = currentUserId.equals(editingUserId);
        } else if (currentEmail != null && editingUserEmail != null) {
            isEditingSelf = currentEmail.equalsIgnoreCase(editingUserEmail);
        }
        
        System.out.println("[DEBUG] Is editing self: " + isEditingSelf);
        
        // Disable nếu:
        // 1. Đang edit chính mình
        // 2. User đang edit có ADMIN role
        if (isEditingSelf || editingUserIsAdmin) {
            System.out.println("[DEBUG] Disabling role checkboxes - isEditingSelf: " + isEditingSelf + ", editingUserIsAdmin: " + editingUserIsAdmin);
            disableAllRoleCheckboxes();
        } else {
            System.out.println("[DEBUG] No need to disable role checkboxes");
        }
    }
    
    /* ---------------------------------------------------
     * Disable tất cả role checkboxes
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Disable cả container và thêm visual feedback
     * --------------------------------------------------- */
    private void disableAllRoleCheckboxes() {
        System.out.println("[DEBUG disableAllRoleCheckboxes] Disabling all role checkboxes and container");
        
        // Disable từng checkbox
        for (CheckBox checkBox : roleCheckBoxes.keySet()) {
            checkBox.setDisable(true);
            // Thêm tooltip để giải thích
            checkBox.setTooltip(new Tooltip("Không thể thay đổi role của ADMIN hoặc chính mình"));
        }
        
        // Disable cả container để chắc chắn
        if (rolesContainer != null) {
            rolesContainer.setDisable(true);
            rolesContainer.setStyle("-fx-opacity: 0.6;"); // Visual feedback
        }
        
        // Thêm label cảnh báo
        Label warningLabel = new Label("⚠️ Không thể thay đổi role của ADMIN hoặc chính mình");
        warningLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 12px;");
        if (rolesContainer != null && !rolesContainer.getChildren().contains(warningLabel)) {
            rolesContainer.getChildren().add(0, warningLabel);
        }
    }
    
    /* ---------------------------------------------------
     * Thiết lập real-time validation cho các trường
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupValidation() {
        fullNameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateFullName();
            }
        });
        
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateEmail();
            }
        });
        
        passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validatePassword();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Validate họ tên
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Xử lý null an toàn
     * --------------------------------------------------- */
    private boolean validateFullName() {
        String name = getTextFieldTextSafely(fullNameField);
        if (name.isEmpty()) {
            fullNameErrorLabel.setVisible(true);
            fullNameErrorLabel.setText("Họ tên không được để trống");
            fullNameField.getStyleClass().add("error-field");
            return false;
        }
        
        fullNameErrorLabel.setVisible(false);
        fullNameField.getStyleClass().remove("error-field");
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate email
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Cải thiện validation và xử lý null
     * --------------------------------------------------- */
    private boolean validateEmail() {
        String email = getTextFieldTextSafely(emailField);
        if (email.isEmpty()) {
            emailErrorLabel.setVisible(true);
            emailErrorLabel.setText("Email không được để trống");
            emailField.getStyleClass().add("error-field");
            return false;
        }
        
        // Email validation pattern: có @ và có ít nhất một ký tự trước @, sau @ có domain
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailPattern)) {
            emailErrorLabel.setVisible(true);
            emailErrorLabel.setText("Email không đúng định dạng (ví dụ: user@example.com)");
            emailField.getStyleClass().add("error-field");
            return false;
        }
        
        emailErrorLabel.setVisible(false);
        emailField.getStyleClass().remove("error-field");
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate password
     * @return true nếu hợp lệ
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Xử lý null an toàn
     * --------------------------------------------------- */
    private boolean validatePassword() {
        // Chỉ validate khi tạo mới (password field visible)
        if (!passwordContainer.isVisible()) {
            return true;
        }
        
        String password = passwordField != null ? passwordField.getText() : null;
        if (password == null || password.trim().isEmpty()) {
            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setText("Mật khẩu không được để trống");
            passwordField.getStyleClass().add("error-field");
            return false;
        }
        
        if (password.length() < 6) {
            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setText("Mật khẩu phải có ít nhất 6 ký tự");
            passwordField.getStyleClass().add("error-field");
            return false;
        }
        
        passwordErrorLabel.setVisible(false);
        passwordField.getStyleClass().remove("error-field");
        return true;
    }
    
    /* ---------------------------------------------------
     * Validate toàn bộ form
     * @return true nếu form hợp lệ
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Hiển thị thông báo lỗi chi tiết
     * --------------------------------------------------- */
    private boolean validateForm() {
        boolean isFormValid = true;
        List<String> errors = new ArrayList<>();
        
        if (!validateFullName()) {
            isFormValid = false;
            errors.add("• Họ tên không được để trống");
        }
        
        if (!validateEmail()) {
            isFormValid = false;
            String emailError = emailErrorLabel.getText();
            errors.add("• " + (emailError != null ? emailError : "Email không hợp lệ"));
        }
        
        if (!validatePassword()) {
            isFormValid = false;
            String passwordError = passwordErrorLabel.getText();
            errors.add("• " + (passwordError != null ? passwordError : "Mật khẩu không hợp lệ"));
        }
        
        if (!isFormValid) {
            validationSummary.setVisible(true);
            validationSummary.setManaged(true);
            validationMessage.setText("Vui lòng sửa các lỗi sau:\n" + String.join("\n", errors));
            // Hiển thị dialog lỗi
            showError("Dữ liệu không hợp lệ", 
                "Vui lòng kiểm tra và sửa các lỗi sau:\n\n" + String.join("\n", errors));
        } else {
            validationSummary.setVisible(false);
            validationSummary.setManaged(false);
        }
        
        return isFormValid;
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện nhấn nút Save
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        saveButton.setDisable(true);
        
        if (user == null) {
            // CREATE mode
            createUser();
        } else {
            // EDIT mode
            updateUser();
        }
    }
    
    /* ---------------------------------------------------
     * Tạo user mới qua API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void createUser() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> userData = buildUserData();
                System.out.println("[DEBUG UserEditDialogController] Creating user with data: " + userData);
                return userApiClient.createUser(userData);
            }
        };
        
        task.setOnSucceeded(event -> {
            showInfo("Thành công", "Đã tạo người dùng mới thành công!");
            confirmed = true;
            dialogStage.close();
        });
        
        task.setOnFailed(event -> {
            saveButton.setDisable(false);
            Throwable ex = task.getException();
            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
            String userMessage = parseErrorMessage(errorMsg);
            showError("Không thể tạo người dùng", userMessage);
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Cập nhật user qua API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Thêm logging để debug
     * --------------------------------------------------- */
    private void updateUser() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Long userId = getLongValue(user.get("id"));
                Map<String, Object> userData = buildUserData();
                
                // Log dữ liệu gửi lên API
                System.out.println("==========================================");
                System.out.println("[DEBUG UserEditDialogController] PUT /api/users/" + userId);
                System.out.println("[DEBUG] Request Data:");
                for (Map.Entry<String, Object> entry : userData.entrySet()) {
                    // Không log password vì lý do bảo mật
                    if ("password".equals(entry.getKey())) {
                        System.out.println("  " + entry.getKey() + ": [REDACTED]");
                    } else {
                        System.out.println("  " + entry.getKey() + ": " + entry.getValue());
                    }
                }
                System.out.println("==========================================");
                
                return userApiClient.updateUser(userId, userData);
            }
        };
        
        task.setOnSucceeded(event -> {
            showInfo("Thành công", "Đã cập nhật người dùng thành công!");
            confirmed = true;
            dialogStage.close();
        });
        
        task.setOnFailed(event -> {
            saveButton.setDisable(false);
            Throwable ex = task.getException();
            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
            String userMessage = parseErrorMessage(errorMsg);
            showError("Không thể cập nhật người dùng", userMessage);
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy text từ TextField an toàn (xử lý null)
     * @param field TextField
     * @return String đã trim hoặc empty string nếu null
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private String getTextFieldTextSafely(TextField field) {
        if (field == null) return "";
        String text = field.getText();
        return text != null ? text.trim() : "";
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy text từ TextArea an toàn (xử lý null)
     * @param area TextArea
     * @return String đã trim hoặc null nếu null/empty
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private String getTextAreaTextSafely(TextArea area) {
        if (area == null) return null;
        String text = area.getText();
        if (text == null) return null;
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /* ---------------------------------------------------
     * Xây dựng user data từ form
     * @return Map chứa user data
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Không gửi dateOfBirth, gender, departmentId, classId khi edit
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Đảm bảo gửi đủ các trường có thể cập nhật
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Xử lý null an toàn cho tất cả text fields
     * --------------------------------------------------- */
    private Map<String, Object> buildUserData() {
        Map<String, Object> data = new HashMap<>();
        boolean isEditMode = (user != null);
        
        // Các trường luôn được gửi (có thể cập nhật) - xử lý null an toàn
        data.put("fullName", getTextFieldTextSafely(fullNameField));
        data.put("email", getTextFieldTextSafely(emailField));
        
        // Password chỉ khi tạo mới
        if (passwordContainer.isVisible()) {
            String password = passwordField != null ? passwordField.getText() : null;
            if (password != null && !password.isEmpty()) {
                data.put("password", password);
            }
        }
        
        // Student Code - có thể cập nhật
        String studentCode = getTextFieldTextSafely(studentCodeField);
        if (!studentCode.isEmpty()) {
            data.put("studentCode", studentCode);
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("studentCode", null);
        }
        
        // Phone Number - có thể cập nhật
        String phoneNumber = getTextFieldTextSafely(phoneNumberField);
        if (!phoneNumber.isEmpty()) {
            data.put("phoneNumber", phoneNumber);
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("phoneNumber", null);
        }
        
        // Date of birth - có thể cập nhật khi edit
        if (dateOfBirthPicker.getValue() != null) {
            data.put("dateOfBirth", dateOfBirthPicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("dateOfBirth", null);
        }
        
        // Gender - có thể cập nhật khi edit
        if (genderCombo.getValue() != null) {
            data.put("gender", genderCombo.getValue());
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("gender", null);
        }
        
        // Address - có thể cập nhật
        String address = getTextAreaTextSafely(addressArea);
        if (address != null) {
            data.put("address", address);
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("address", null);
        }
        
        // Department - có thể cập nhật khi edit
        Map<String, Object> selectedDept = departmentCombo.getValue();
        if (selectedDept != null) {
            data.put("departmentId", getLongValue(selectedDept.get("id")));
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("departmentId", null);
        }
        
        // Class - có thể cập nhật khi edit
        Map<String, Object> selectedClass = classCombo.getValue();
        if (selectedClass != null) {
            data.put("classId", getLongValue(selectedClass.get("id")));
        } else if (isEditMode) {
            // Khi edit, nếu để trống thì gửi null để xóa
            data.put("classId", null);
        }
        
        // Roles - có thể cập nhật, luôn gửi (kể cả khi rỗng)
        // NHƯNG: Nếu đang edit chính mình hoặc edit ADMIN khác, KHÔNG cho phép thay đổi roles
        List<String> selectedRoles = new ArrayList<>();
        boolean shouldBlockRoleChange = false;
        
        System.out.println("[DEBUG buildUserData] isEditMode: " + isEditMode + ", currentUserInfo: " + currentUserInfo + ", user: " + user);
        
        if (isEditMode && currentUserInfo != null && user != null) {
            System.out.println("[DEBUG buildUserData] Checking role change restrictions...");
            // Kiểm tra current user có ADMIN role không
            @SuppressWarnings("unchecked")
            List<String> currentUserRoles = (List<String>) currentUserInfo.get("roles");
            boolean currentUserIsAdmin = false;
            if (currentUserRoles != null) {
                for (String role : currentUserRoles) {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        currentUserIsAdmin = true;
                        break;
                    }
                }
            }
            
            if (currentUserIsAdmin) {
                // Lấy current user email và ID
                String currentEmail = (String) currentUserInfo.get("email");
                Long currentUserId = null;
                Object userIdObj = currentUserInfo.get("userId");
                if (userIdObj != null) {
                    if (userIdObj instanceof Number) {
                        currentUserId = ((Number) userIdObj).longValue();
                    } else {
                        try {
                            currentUserId = Long.parseLong(userIdObj.toString());
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
                
                // Lấy user đang edit email và ID
                String editingUserEmail = (String) user.get("email");
                Long editingUserId = getLongValue(user.get("id"));
                
                // Kiểm tra đang edit chính mình
                boolean isEditingSelf = false;
                if (currentUserId != null && editingUserId != null) {
                    isEditingSelf = currentUserId.equals(editingUserId);
                } else if (currentEmail != null && editingUserEmail != null) {
                    isEditingSelf = currentEmail.equalsIgnoreCase(editingUserEmail);
                }
                
                // Kiểm tra user đang edit có ADMIN role không
                Object rolesObj = user.get("roles");
                boolean editingUserIsAdmin = false;
                if (rolesObj != null) {
                    Set<String> editingUserRoles = new HashSet<>();
                    if (rolesObj instanceof Set) {
                        @SuppressWarnings("unchecked")
                        Set<String> rolesSet = (Set<String>) rolesObj;
                        for (String role : rolesSet) {
                            String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                            editingUserRoles.add(normalized.toUpperCase());
                        }
                    } else if (rolesObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> rolesList = (List<String>) rolesObj;
                        for (String role : rolesList) {
                            String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                            editingUserRoles.add(normalized.toUpperCase());
                        }
                    }
                    editingUserIsAdmin = editingUserRoles.contains("ADMIN");
                }
                
                // Block nếu đang edit chính mình hoặc edit ADMIN khác
                if (isEditingSelf || editingUserIsAdmin) {
                    shouldBlockRoleChange = true;
                    System.out.println("[DEBUG buildUserData] Blocking role change - isEditingSelf: " + isEditingSelf + ", editingUserIsAdmin: " + editingUserIsAdmin);
                } else {
                    System.out.println("[DEBUG buildUserData] No need to block - isEditingSelf: " + isEditingSelf + ", editingUserIsAdmin: " + editingUserIsAdmin);
                }
            } else {
                System.out.println("[DEBUG buildUserData] Current user is not ADMIN, no restriction");
            }
        } else {
            System.out.println("[DEBUG buildUserData] Skipping role restriction check - isEditMode: " + isEditMode + ", currentUserInfo: " + (currentUserInfo != null) + ", user: " + (user != null));
        }
        
        if (shouldBlockRoleChange) {
            System.out.println("[DEBUG buildUserData] BLOCKING role change - keeping original roles");
            // Giữ nguyên roles hiện tại của user, không cho thay đổi
            Object rolesObj = user.get("roles");
            if (rolesObj != null) {
                if (rolesObj instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<String> rolesSet = (Set<String>) rolesObj;
                    for (String role : rolesSet) {
                        String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                        selectedRoles.add(normalized);
                    }
                } else if (rolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) rolesObj;
                    for (String role : rolesList) {
                        String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
                        selectedRoles.add(normalized);
                    }
                }
            }
            System.out.println("[DEBUG] Keeping original roles: " + selectedRoles);
        } else {
            // Lấy roles từ checkboxes
            System.out.println("[DEBUG buildUserData] Getting roles from checkboxes");
            for (CheckBox checkBox : roleCheckBoxes.keySet()) {
                if (checkBox.isSelected()) {
                    selectedRoles.add(roleCheckBoxes.get(checkBox));
                }
            }
            System.out.println("[DEBUG buildUserData] Selected roles from checkboxes: " + selectedRoles);
        }
        
        // Luôn gửi roles, kể cả khi rỗng (để backend xử lý)
        data.put("roles", selectedRoles);
        
        // Active status - có thể cập nhật
        data.put("isActive", isActiveCheckBox.isSelected());
        
        return data;
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện nhấn nút Cancel
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        confirmed = false;
        dialogStage.close();
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem user đã confirm hay chưa
     * @return true nếu user đã click Save và thành công
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /* ---------------------------------------------------
     * Helper: Get long value từ Object
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private Long getLongValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /* ---------------------------------------------------
     * Hiển thị dialog thông báo lỗi
     * @param title Tiêu đề dialog
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (02/12/2025)
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
     * Hiển thị dialog thông báo thông tin
     * @param title Tiêu đề dialog
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (02/12/2025)
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
    
    /* ---------------------------------------------------
     * Parse error message từ backend thành user-friendly message
     * @param errorMsg Raw error message từ backend
     * @return User-friendly error message
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Cải thiện parse để hiển thị chi tiết hơn
     * --------------------------------------------------- */
    private String parseErrorMessage(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().isEmpty()) {
            return "Đã xảy ra lỗi không xác định. Vui lòng thử lại.";
        }
        
        // Handle HTTP status codes
        if (errorMsg.contains("403") || errorMsg.contains("Forbidden")) {
            return "Bạn không có quyền thực hiện thao tác này.\n" +
                   "Vui lòng liên hệ quản trị viên để được cấp quyền.";
        }
        
        if (errorMsg.contains("401") || errorMsg.contains("Unauthorized")) {
            return "Phiên đăng nhập đã hết hạn.\n" +
                   "Vui lòng đăng nhập lại.";
        }
        
        // Parse validation errors từ backend (400 Bad Request)
        if (errorMsg.contains("400") || errorMsg.contains("Bad Request")) {
            // Tìm các validation errors chi tiết
            if (errorMsg.contains("email") && (errorMsg.contains("invalid") || errorMsg.contains("không hợp lệ"))) {
                return "Email không đúng định dạng.\n" +
                       "Vui lòng nhập email hợp lệ (ví dụ: user@example.com).";
            }
            if (errorMsg.contains("email") && (errorMsg.contains("required") || errorMsg.contains("bắt buộc"))) {
                return "Email là trường bắt buộc.\n" +
                       "Vui lòng nhập email.";
            }
            if (errorMsg.contains("fullName") && (errorMsg.contains("required") || errorMsg.contains("bắt buộc"))) {
                return "Họ tên là trường bắt buộc.\n" +
                       "Vui lòng nhập họ và tên đầy đủ.";
            }
            if (errorMsg.contains("password") && (errorMsg.contains("required") || errorMsg.contains("bắt buộc"))) {
                return "Mật khẩu là trường bắt buộc.\n" +
                       "Vui lòng nhập mật khẩu (tối thiểu 6 ký tự).";
            }
            if (errorMsg.contains("password") && (errorMsg.contains("size") || errorMsg.contains("length"))) {
                return "Mật khẩu phải có độ dài từ 6 đến 50 ký tự.\n" +
                       "Vui lòng nhập lại mật khẩu.";
            }
            // Generic validation error
            return "Dữ liệu không hợp lệ.\n" +
                   "Vui lòng kiểm tra lại các trường bắt buộc và định dạng dữ liệu.";
        }
        
        // Duplicate resource errors (409 Conflict)
        if (errorMsg.contains("409") || errorMsg.contains("Conflict") ||
            errorMsg.contains("already exists") || errorMsg.contains("đã tồn tại") ||
            errorMsg.contains("duplicate") || errorMsg.contains("trùng lặp")) {
            if (errorMsg.contains("email")) {
                return "Email đã tồn tại trong hệ thống.\n" +
                       "Vui lòng sử dụng email khác.";
            }
            if (errorMsg.contains("studentCode") || errorMsg.contains("mã sinh viên")) {
                return "Mã sinh viên đã tồn tại trong hệ thống.\n" +
                       "Vui lòng sử dụng mã sinh viên khác.";
            }
            if (errorMsg.contains("phoneNumber") || errorMsg.contains("số điện thoại")) {
                return "Số điện thoại đã tồn tại trong hệ thống.\n" +
                       "Vui lòng sử dụng số điện thoại khác.";
            }
            return "Thông tin đã tồn tại trong hệ thống.\n" +
                   "Vui lòng kiểm tra lại email, mã sinh viên hoặc số điện thoại.";
        }
        
        if (errorMsg.contains("500") || errorMsg.contains("Internal Server Error")) {
            return "Lỗi hệ thống.\n" +
                   "Vui lòng thử lại sau hoặc liên hệ quản trị viên.";
        }
        
        // Check for network errors
        if (errorMsg.toLowerCase().contains("connection") || 
            errorMsg.toLowerCase().contains("timeout") ||
            errorMsg.toLowerCase().contains("connect")) {
            return "Không thể kết nối đến máy chủ.\n" +
                   "Vui lòng kiểm tra kết nối mạng và thử lại.";
        }
        
        // Default: return a cleaned up message
        if (errorMsg.length() > 200) {
            return "Đã xảy ra lỗi. Vui lòng thử lại sau.\n" +
                   "Nếu vấn đề vẫn tiếp diễn, hãy liên hệ quản trị viên.";
        }
        
        return errorMsg;
    }
}

