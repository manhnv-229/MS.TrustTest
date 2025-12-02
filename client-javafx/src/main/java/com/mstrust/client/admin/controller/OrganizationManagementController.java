package com.mstrust.client.admin.controller;

import com.mstrust.client.admin.api.OrganizationApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/* ---------------------------------------------------
 * Controller cho Organization Management
 * Quản lý Departments, Classes, Subjects
 * @author: K24DTCN210-NVMANH (02/12/2025)
 * --------------------------------------------------- */
public class OrganizationManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationManagementController.class);
    
    // API Client
    private OrganizationApiClient apiClient;
    private Stage stage;
    
    // UI Components
    @FXML private TabPane mainTabPane;
    @FXML private Button refreshButton;
    
    // Departments
    @FXML private TextField departmentSearchField;
    @FXML private TableView<Map<String, Object>> departmentsTable;
    @FXML private TableColumn<Map<String, Object>, Long> deptIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> deptNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> deptCodeColumn;
    @FXML private TableColumn<Map<String, Object>, String> deptDescriptionColumn;
    @FXML private TableColumn<Map<String, Object>, String> deptActionsColumn;
    @FXML private Button deptFirstPageButton;
    @FXML private Button deptPrevPageButton;
    @FXML private Button deptNextPageButton;
    @FXML private Button deptLastPageButton;
    @FXML private Label deptCurrentPageLabel;
    @FXML private Label deptTotalPagesLabel;
    
    // Classes
    @FXML private TextField classSearchField;
    @FXML private TableView<Map<String, Object>> classesTable;
    @FXML private TableColumn<Map<String, Object>, Long> classIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> classNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> classCodeColumn;
    @FXML private TableColumn<Map<String, Object>, String> classDepartmentColumn;
    @FXML private TableColumn<Map<String, Object>, String> classYearColumn;
    @FXML private TableColumn<Map<String, Object>, String> classActionsColumn;
    @FXML private Button classFirstPageButton;
    @FXML private Button classPrevPageButton;
    @FXML private Button classNextPageButton;
    @FXML private Button classLastPageButton;
    @FXML private Label classCurrentPageLabel;
    @FXML private Label classTotalPagesLabel;
    
    // Data
    private ObservableList<Map<String, Object>> departments = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> classes = FXCollections.observableArrayList();
    
    // Pagination
    private int deptCurrentPage = 0;
    private int deptPageSize = 20;
    private int deptTotalPages = 1;
    private long deptTotalElements = 0;
    
    private int classCurrentPage = 0;
    private int classPageSize = 20;
    private int classTotalPages = 1;
    private long classTotalElements = 0;
    
    /* ---------------------------------------------------
     * Initialize controller
     * @param baseUrl Base URL của API
     * @param authToken JWT token
     * @param stage Stage reference
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public void initialize(String baseUrl, String authToken, Stage stage) {
        this.stage = stage;
        
        // Initialize API client
        this.apiClient = new OrganizationApiClient();
        this.apiClient.setToken(authToken);
        
        // Setup tables
        setupDepartmentsTable();
        setupClassesTable();
        
        // Load initial data
        loadDepartments();
        loadClasses();
    }
    
    /* ---------------------------------------------------
     * Setup departments table
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupDepartmentsTable() {
        departmentsTable.setItems(departments);
        
        // Setup cell value factories
        deptIdColumn.setCellValueFactory(data -> {
            Map<String, Object> dept = data.getValue();
            Long id = getLongValue(dept.get("id"));
            return new javafx.beans.property.SimpleObjectProperty<>(id);
        });
        
        deptNameColumn.setCellValueFactory(data -> {
            Map<String, Object> dept = data.getValue();
            String name = (String) dept.getOrDefault("departmentName", "");
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        
        deptCodeColumn.setCellValueFactory(data -> {
            Map<String, Object> dept = data.getValue();
            String code = (String) dept.getOrDefault("departmentCode", "");
            return new javafx.beans.property.SimpleStringProperty(code);
        });
        
        deptDescriptionColumn.setCellValueFactory(data -> {
            Map<String, Object> dept = data.getValue();
            String desc = (String) dept.getOrDefault("description", "");
            return new javafx.beans.property.SimpleStringProperty(desc);
        });
        
        // Actions column với Edit/Delete buttons
        deptActionsColumn.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox hbox = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 3 8;");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 3 8;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> dept = getTableView().getItems().get(getIndex());
                    handleEditDepartment(dept);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> dept = getTableView().getItems().get(getIndex());
                    handleDeleteDepartment(dept);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Setup classes table
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void setupClassesTable() {
        classesTable.setItems(classes);
        
        // Setup cell value factories
        classIdColumn.setCellValueFactory(data -> {
            Map<String, Object> clazz = data.getValue();
            Long id = getLongValue(clazz.get("id"));
            return new javafx.beans.property.SimpleObjectProperty<>(id);
        });
        
        classNameColumn.setCellValueFactory(data -> {
            Map<String, Object> clazz = data.getValue();
            String name = (String) clazz.getOrDefault("className", "");
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        
        classCodeColumn.setCellValueFactory(data -> {
            Map<String, Object> clazz = data.getValue();
            String code = (String) clazz.getOrDefault("classCode", "");
            return new javafx.beans.property.SimpleStringProperty(code);
        });
        
        classDepartmentColumn.setCellValueFactory(data -> {
            Map<String, Object> clazz = data.getValue();
            @SuppressWarnings("unchecked")
            Map<String, Object> dept = (Map<String, Object>) clazz.get("department");
            String deptName = dept != null ? (String) dept.getOrDefault("departmentName", "") : "";
            return new javafx.beans.property.SimpleStringProperty(deptName);
        });
        
        classYearColumn.setCellValueFactory(data -> {
            Map<String, Object> clazz = data.getValue();
            String year = (String) clazz.getOrDefault("academicYear", "");
            return new javafx.beans.property.SimpleStringProperty(year);
        });
        
        // Actions column
        classActionsColumn.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox hbox = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 3 8;");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 3 8;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> clazz = getTableView().getItems().get(getIndex());
                    handleEditClass(clazz);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> clazz = getTableView().getItems().get(getIndex());
                    handleDeleteClass(clazz);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Load departments từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadDepartments() {
        new Thread(() -> {
            try {
                Map<String, Object> response = apiClient.getDepartmentsPage(
                    deptCurrentPage, deptPageSize, "id", "asc");
                
                Platform.runLater(() -> updateDepartmentsTable(response));
                
            } catch (Exception e) {
                logger.error("Error loading departments", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải danh sách khoa: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update departments table
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @SuppressWarnings("unchecked")
    private void updateDepartmentsTable(Map<String, Object> response) {
        departments.clear();
        
        if (response.containsKey("content")) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            departments.addAll(content);
        }
        
        // Update pagination
        if (response.containsKey("totalElements")) {
            deptTotalElements = getLongValue(response.get("totalElements"));
        }
        if (response.containsKey("totalPages")) {
            deptTotalPages = getIntValue(response.get("totalPages"));
        }
        if (response.containsKey("number")) {
            deptCurrentPage = getIntValue(response.get("number"));
        }
        
        updateDeptPaginationControls();
    }
    
    /* ---------------------------------------------------
     * Load classes từ API
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void loadClasses() {
        new Thread(() -> {
            try {
                Map<String, Object> response = apiClient.getClassesPage(
                    classCurrentPage, classPageSize, "id", "asc");
                
                Platform.runLater(() -> updateClassesTable(response));
                
            } catch (Exception e) {
                logger.error("Error loading classes", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể tải danh sách lớp: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Update classes table
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @SuppressWarnings("unchecked")
    private void updateClassesTable(Map<String, Object> response) {
        classes.clear();
        
        if (response.containsKey("content")) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            classes.addAll(content);
        }
        
        // Update pagination
        if (response.containsKey("totalElements")) {
            classTotalElements = getLongValue(response.get("totalElements"));
        }
        if (response.containsKey("totalPages")) {
            classTotalPages = getIntValue(response.get("totalPages"));
        }
        if (response.containsKey("number")) {
            classCurrentPage = getIntValue(response.get("number"));
        }
        
        updateClassPaginationControls();
    }
    
    /* ---------------------------------------------------
     * Update pagination controls
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    private void updateDeptPaginationControls() {
        deptCurrentPageLabel.setText(String.valueOf(deptCurrentPage + 1));
        deptTotalPagesLabel.setText(String.valueOf(deptTotalPages));
        
        deptFirstPageButton.setDisable(deptCurrentPage == 0);
        deptPrevPageButton.setDisable(deptCurrentPage == 0);
        deptNextPageButton.setDisable(deptCurrentPage >= deptTotalPages - 1);
        deptLastPageButton.setDisable(deptCurrentPage >= deptTotalPages - 1);
    }
    
    private void updateClassPaginationControls() {
        classCurrentPageLabel.setText(String.valueOf(classCurrentPage + 1));
        classTotalPagesLabel.setText(String.valueOf(classTotalPages));
        
        classFirstPageButton.setDisable(classCurrentPage == 0);
        classPrevPageButton.setDisable(classCurrentPage == 0);
        classNextPageButton.setDisable(classCurrentPage >= classTotalPages - 1);
        classLastPageButton.setDisable(classCurrentPage >= classTotalPages - 1);
    }
    
    /* ---------------------------------------------------
     * Handle actions
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        loadDepartments();
        loadClasses();
    }
    
    @FXML
    private void handleCreateDepartment() {
        showInfo("Tạo Khoa", "Dialog tạo khoa sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    private void handleEditDepartment(Map<String, Object> dept) {
        showInfo("Sửa Khoa", "Dialog sửa khoa sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    private void handleDeleteDepartment(Map<String, Object> dept) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa khoa này?");
        confirm.setContentText("Khoa: " + dept.get("departmentName"));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteDepartment((Long) dept.get("id"));
            }
        });
    }
    
    private void deleteDepartment(Long deptId) {
        new Thread(() -> {
            try {
                apiClient.deleteDepartment(deptId);
                Platform.runLater(() -> {
                    showInfo("Thành công", "Đã xóa khoa thành công.");
                    loadDepartments();
                });
            } catch (Exception e) {
                logger.error("Error deleting department", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể xóa khoa: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleDepartmentSearch() {
        // TODO: Implement search
        loadDepartments();
    }
    
    @FXML
    private void handleCreateClass() {
        showInfo("Tạo Lớp", "Dialog tạo lớp sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    private void handleEditClass(Map<String, Object> clazz) {
        showInfo("Sửa Lớp", "Dialog sửa lớp sẽ được implement trong bản cập nhật tiếp theo.");
    }
    
    private void handleDeleteClass(Map<String, Object> clazz) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa lớp này?");
        confirm.setContentText("Lớp: " + clazz.get("className"));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteClass((Long) clazz.get("id"));
            }
        });
    }
    
    private void deleteClass(Long classId) {
        new Thread(() -> {
            try {
                apiClient.deleteClass(classId);
                Platform.runLater(() -> {
                    showInfo("Thành công", "Đã xóa lớp thành công.");
                    loadClasses();
                });
            } catch (Exception e) {
                logger.error("Error deleting class", e);
                Platform.runLater(() -> {
                    showError("Lỗi", "Không thể xóa lớp: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleClassSearch() {
        // TODO: Implement search
        loadClasses();
    }
    
    // Pagination handlers
    @FXML
    private void handleDeptFirstPage() { deptCurrentPage = 0; loadDepartments(); }
    @FXML
    private void handleDeptPrevPage() { if (deptCurrentPage > 0) { deptCurrentPage--; loadDepartments(); } }
    @FXML
    private void handleDeptNextPage() { if (deptCurrentPage < deptTotalPages - 1) { deptCurrentPage++; loadDepartments(); } }
    @FXML
    private void handleDeptLastPage() { deptCurrentPage = deptTotalPages - 1; loadDepartments(); }
    
    @FXML
    private void handleClassFirstPage() { classCurrentPage = 0; loadClasses(); }
    @FXML
    private void handleClassPrevPage() { if (classCurrentPage > 0) { classCurrentPage--; loadClasses(); } }
    @FXML
    private void handleClassNextPage() { if (classCurrentPage < classTotalPages - 1) { classCurrentPage++; loadClasses(); } }
    @FXML
    private void handleClassLastPage() { classCurrentPage = classTotalPages - 1; loadClasses(); }
    
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

