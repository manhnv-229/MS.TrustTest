package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.OrganizationApiClient;
import com.mstrust.client.admin.api.UserManagementApiClient;
import com.mstrust.client.admin.controller.UserEditDialogController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/* ---------------------------------------------------
 * Controller cho User Management
 * Quản lý CRUD users, pagination, search/filter
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    // API Client
    private UserManagementApiClient apiClient;
    private OrganizationApiClient orgApiClient;
    private Stage stage;
    
    // UI Components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    
    @FXML private TableView<Map<String, Object>> usersTable;
    @FXML private TableColumn<Map<String, Object>, String> nameColumn;
    @FXML private TableColumn<Map<String, Object>, String> emailColumn;
    @FXML private TableColumn<Map<String, Object>, String> studentCodeColumn;
    @FXML private TableColumn<Map<String, Object>, String> roleColumn;
    @FXML private TableColumn<Map<String, Object>, String> statusColumn;
    @FXML private TableColumn<Map<String, Object>, String> departmentColumn;
    @FXML private TableColumn<Map<String, Object>, String> classColumn;
    @FXML private TableColumn<Map<String, Object>, String> lastLoginColumn;
    
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label inactiveUsersLabel;
    
    @FXML private Button createButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label currentPageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private ComboBox<String> pageSizeCombo;
    
    @FXML private StackPane loadingPane;
    @FXML private Label loadingMessage;
    
    // Data
    private ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    
    // Pagination
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 1;
    private long totalElements = 0;
    
    // Filters
    private String searchQuery = "";
    private String selectedRole = null;
    private String selectedStatus = null;
    
    /* ---------------------------------------------------
     * Initialize controller
     * @param baseUrl Base URL của API
     * @param authToken JWT token
     * @param stage Stage reference
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void initialize(String baseUrl, String authToken, Stage stage) {
        this.stage = stage;
        
        // Initialize API clients
        this.apiClient = new UserManagementApiClient();
        this.apiClient.setToken(authToken);
        
        this.orgApiClient = new OrganizationApiClient();
        this.orgApiClient.setToken(authToken);
        
        // Setup UI
        setupTable();
        setupFilters();
        setupPagination();
        setupSelectionListener();
        setupActionIcons();
        
        // Load initial data
        loadUsers();
    }
    
    /* ---------------------------------------------------
     * Setup Ikonli icons cho các action buttons
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupActionIcons() {
        editButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createEditIconForButton());
        deleteButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createDeleteIconForButton());
        activateButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createActivateIcon());
        deactivateButton.setGraphic(com.mstrust.client.exam.util.IconFactory.createDeactivateIcon());
    }
    
    /* ---------------------------------------------------
     * Setup table columns
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupTable() {
        usersTable.setItems(users);
        
        // Setup cell value factories
        nameColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            String name = (String) user.getOrDefault("fullName", "");
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        
        emailColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            String email = (String) user.getOrDefault("email", "");
            return new javafx.beans.property.SimpleStringProperty(email);
        });
        
        studentCodeColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            String code = (String) user.getOrDefault("studentCode", "");
            return new javafx.beans.property.SimpleStringProperty(code);
        });
        
        roleColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            Object rolesObj = user.get("roles");
            String roleStr = "";
            if (rolesObj != null) {
                if (rolesObj instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<String> roles = (Set<String>) rolesObj;
                    roleStr = roles.isEmpty() ? "" : String.join(", ", roles);
                } else if (rolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) rolesObj;
                    roleStr = roles.isEmpty() ? "" : String.join(", ", roles);
                } else {
                    roleStr = rolesObj.toString();
                }
            }
            return new javafx.beans.property.SimpleStringProperty(roleStr);
        });
        
        statusColumn.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Map<String, Object> user = getTableView().getItems().get(getIndex());
                    Boolean isActive = (Boolean) user.getOrDefault("isActive", false);
                    if (isActive) {
                        setText("Hoạt động");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("Vô hiệu hóa");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        statusColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            Boolean isActive = (Boolean) user.getOrDefault("isActive", false);
            return new javafx.beans.property.SimpleStringProperty(isActive ? "Hoạt động" : "Vô hiệu hóa");
        });
        
        departmentColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            String dept = (String) user.getOrDefault("departmentName", "");
            return new javafx.beans.property.SimpleStringProperty(dept);
        });
        
        classColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            String className = (String) user.getOrDefault("className", "");
            return new javafx.beans.property.SimpleStringProperty(className);
        });
        
        lastLoginColumn.setCellValueFactory(data -> {
            Map<String, Object> user = data.getValue();
            Object lastLogin = user.get("lastLoginAt");
            String dateStr = lastLogin != null ? lastLogin.toString() : "Chưa đăng nhập";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
    }
    
    /* ---------------------------------------------------
     * Setup filters
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupFilters() {
        // Role filter
        roleFilter.getItems().addAll("Tất cả", "STUDENT", "TEACHER", "CLASS_MANAGER", "DEPT_MANAGER", "ADMIN");
        roleFilter.setValue("Tất cả");
        
        // Status filter
        statusFilter.getItems().addAll("Tất cả", "Hoạt động", "Vô hiệu hóa");
        statusFilter.setValue("Tất cả");
        
        // Page size combo
        pageSizeCombo.getItems().addAll("10", "20", "50", "100");
        pageSizeCombo.setValue("20");
    }
    
    /* ---------------------------------------------------
     * Setup pagination controls
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupPagination() {
        updatePaginationControls();
    }
    
    /* ---------------------------------------------------
     * Setup selection listener để enable/disable buttons
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            activateButton.setDisable(!hasSelection);
            deactivateButton.setDisable(!hasSelection);
        });
    }
    
    /* ---------------------------------------------------
     * Load users từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadUsers() {
        showLoading(true, "Đang tải danh sách người dùng...");
        
        new Thread(() -> {
            try {
                Map<String, Object> response = apiClient.getUsersPage(
                    currentPage, pageSize, "id", "ASC");
                
                Platform.runLater(() -> {
                    updateUsersTable(response);
                    showLoading(false, "");
                });
                
            } catch (Exception e) {
                logger.error("Error loading users", e);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showError("Lỗi", "Không thể tải danh sách người dùng: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update users table với data từ API
     * @param response API response
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @SuppressWarnings("unchecked")
    private void updateUsersTable(Map<String, Object> response) {
        users.clear();
        
        if (response.containsKey("content")) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            users.addAll(content);
        }
        
        // Update pagination info
        if (response.containsKey("totalElements")) {
            totalElements = getLongValue(response.get("totalElements"));
        }
        if (response.containsKey("totalPages")) {
            totalPages = getIntValue(response.get("totalPages"));
        }
        if (response.containsKey("number")) {
            currentPage = getIntValue(response.get("number"));
        }
        
        // Update statistics
        updateStatistics();
        updatePaginationControls();
    }
    
    /* ---------------------------------------------------
     * Update statistics labels
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateStatistics() {
        totalUsersLabel.setText("Tổng số: " + totalElements);
        
        long activeCount = users.stream()
            .filter(u -> (Boolean) u.getOrDefault("isActive", false))
            .count();
        activeUsersLabel.setText("Đang hoạt động: " + activeCount);
        
        long inactiveCount = users.size() - activeCount;
        inactiveUsersLabel.setText("Vô hiệu hóa: " + inactiveCount);
    }
    
    /* ---------------------------------------------------
     * Update pagination controls
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updatePaginationControls() {
        currentPageLabel.setText(String.valueOf(currentPage + 1));
        totalPagesLabel.setText(String.valueOf(totalPages));
        
        firstPageButton.setDisable(currentPage == 0);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        lastPageButton.setDisable(currentPage >= totalPages - 1);
    }
    
    /* ---------------------------------------------------
     * Handle create button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleCreate() {
        showUserEditDialog(null);
    }
    
    /* ---------------------------------------------------
     * Handle edit button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleEdit() {
        Map<String, Object> selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserEditDialog(selectedUser);
        }
    }
    
    /* ---------------------------------------------------
     * Show user edit dialog
     * @param user User data (null for create)
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showUserEditDialog(Map<String, Object> user) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/user-edit-dialog.fxml"));
            Parent root = loader.load();
            
            // Get controller
            UserEditDialogController controller = loader.getController();
            
            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle(user == null ? "Tạo người dùng mới" : "Sửa người dùng");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            // Initialize controller
            controller.initialize(apiClient, orgApiClient, dialogStage);
            
            // Set user data nếu đang edit
            if (user != null) {
                controller.setUser(user);
            }
            
            // Show dialog and wait
            dialogStage.showAndWait();
            
            // Refresh table nếu user đã confirm
            if (controller.isConfirmed()) {
                loadUsers();
            }
            
        } catch (IOException e) {
            logger.error("Error loading user edit dialog", e);
            showError("Lỗi", "Không thể mở form tạo/sửa người dùng: " + e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Handle delete button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleDelete() {
        Map<String, Object> selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa người dùng này?");
        confirm.setContentText("Người dùng: " + selectedUser.get("fullName"));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser((Long) selectedUser.get("id"));
            }
        });
    }
    
    /* ---------------------------------------------------
     * Delete user
     * @param userId User ID
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void deleteUser(Long userId) {
        showLoading(true, "Đang xóa người dùng...");
        
        new Thread(() -> {
            try {
                apiClient.deleteUser(userId);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showInfo("Thành công", "Đã xóa người dùng thành công.");
                    loadUsers();
                });
            } catch (Exception e) {
                logger.error("Error deleting user", e);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showError("Lỗi", "Không thể xóa người dùng: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handle activate button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleActivate() {
        Map<String, Object> selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            setUserActive((Long) selectedUser.get("id"), true);
        }
    }
    
    /* ---------------------------------------------------
     * Handle deactivate button click
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleDeactivate() {
        Map<String, Object> selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            setUserActive((Long) selectedUser.get("id"), false);
        }
    }
    
    /* ---------------------------------------------------
     * Set user active/inactive
     * @param userId User ID
     * @param isActive Active status
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setUserActive(Long userId, boolean isActive) {
        showLoading(true, isActive ? "Đang kích hoạt..." : "Đang vô hiệu hóa...");
        
        new Thread(() -> {
            try {
                apiClient.setUserActive(userId, isActive);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showInfo("Thành công", 
                        isActive ? "Đã kích hoạt người dùng thành công." : "Đã vô hiệu hóa người dùng thành công.");
                    loadUsers();
                });
            } catch (Exception e) {
                logger.error("Error setting user active", e);
                Platform.runLater(() -> {
                    showLoading(false, "");
                    showError("Lỗi", "Không thể cập nhật trạng thái người dùng: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Handle search
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleSearch() {
        searchQuery = searchField.getText();
        currentPage = 0;
        loadUsers();
    }
    
    /* ---------------------------------------------------
     * Handle filter change
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleFilterChange() {
        String role = roleFilter.getValue();
        selectedRole = "Tất cả".equals(role) ? null : role;
        
        String status = statusFilter.getValue();
        selectedStatus = "Tất cả".equals(status) ? null : status;
        
        currentPage = 0;
        loadUsers();
    }
    
    /* ---------------------------------------------------
     * Handle pagination buttons
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleFirstPage() {
        currentPage = 0;
        loadUsers();
    }
    
    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadUsers();
        }
    }
    
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadUsers();
        }
    }
    
    @FXML
    private void handleLastPage() {
        currentPage = totalPages - 1;
        loadUsers();
    }
    
    @FXML
    private void handlePageSizeChange() {
        try {
            pageSize = Integer.parseInt(pageSizeCombo.getValue());
            currentPage = 0;
            loadUsers();
        } catch (NumberFormatException e) {
            logger.error("Invalid page size", e);
        }
    }
    
    /* ---------------------------------------------------
     * Handle refresh
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        loadUsers();
    }
    
    /* ---------------------------------------------------
     * Handle export
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleExport() {
        showInfo("Xuất Excel", "Chức năng xuất Excel sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    /* ---------------------------------------------------
     * Show/hide loading overlay
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void showLoading(boolean show, String message) {
        loadingPane.setVisible(show);
        loadingMessage.setText(message);
    }
    
    /* ---------------------------------------------------
     * Helper methods
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private Long getLongValue(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    private int getIntValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

