package com.mstrust.client.teacher.controller;

import com.mstrust.client.teacher.api.SubjectApiClient;
import com.mstrust.client.teacher.api.SubjectApiClient.PageResponse;
import com.mstrust.client.teacher.dto.SubjectDTO;
import com.mstrust.client.teacher.dto.DepartmentDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/* ---------------------------------------------------
 * Controller quản lý giao diện Quản lý Môn học
 * Xử lý CRUD operations cho môn học với pagination
 * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
 * --------------------------------------------------- */
public class SubjectManagementController {
    
    @FXML private Button createButton;
    @FXML private Button refreshButton;
    
    // Filters
    @FXML private ComboBox<DepartmentDTO> departmentFilter;
    @FXML private TextField searchField;
    
    // Statistics
    @FXML private Label totalSubjectsLabel;
    @FXML private Label departmentCountLabel;
    
    // Table
    @FXML private TableView<SubjectDTO> subjectsTable;
    @FXML private TableColumn<SubjectDTO, String> idColumn;
    @FXML private TableColumn<SubjectDTO, String> codeColumn;
    @FXML private TableColumn<SubjectDTO, String> nameColumn;
    @FXML private TableColumn<SubjectDTO, String> creditsColumn;
    @FXML private TableColumn<SubjectDTO, String> departmentColumn;
    @FXML private TableColumn<SubjectDTO, String> descriptionColumn;
    @FXML private TableColumn<SubjectDTO, Void> actionsColumn;
    
    // Pagination
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> pageSizeComboBox;
    
    @FXML private StackPane loadingPane;
    
    private SubjectApiClient apiClient;
    private Stage primaryStage;
    private int currentPage = 0;
    private int totalPages = 1;
    private int pageSize = 20;
    private List<DepartmentDTO> allDepartments;
    
    /* ---------------------------------------------------
     * Khởi tạo controller với API client và stage
     * @param apiClient Client để gọi API backend
     * @param primaryStage Stage chính của ứng dụng
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    public void initialize(SubjectApiClient apiClient, Stage primaryStage) {
        this.apiClient = apiClient;
        this.primaryStage = primaryStage;
        
        setupTableColumns();
        setupPagination();
        loadDepartments();
        loadSubjects(0);
    }
    
    /* ---------------------------------------------------
     * Thiết lập các cột trong TableView
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        
        codeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubjectCode()));
        
        nameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubjectName()));
        
        creditsColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));
        
        departmentColumn.setCellValueFactory(data -> {
            SubjectDTO subject = data. getValue();
            String deptName = subject.getDepartmentName();
            return new SimpleStringProperty(deptName != null ? deptName : "N/A");
        });
        
        descriptionColumn.setCellValueFactory(data -> {
            String desc = data.getValue().getDescription();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(desc != null ? desc : "");
        });
        
        // Actions column với Edit và Delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏️ Sửa");
            private final Button deleteButton = new Button("🗑️ Xóa");
            private final HBox buttons = new HBox(5, editButton, deleteButton);
            
            {
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");
                
                editButton.setOnAction(event -> {
                    SubjectDTO subject = getTableView().getItems().get(getIndex());
                    handleEditSubject(subject);
                });
                
                deleteButton.setOnAction(event -> {
                    SubjectDTO subject = getTableView().getItems().get(getIndex());
                    handleDeleteSubject(subject);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /* ---------------------------------------------------
     * Thiết lập pagination controls
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void setupPagination() {
        ObservableList<Integer> pageSizes = FXCollections.observableArrayList(10, 20, 50, 100);
        pageSizeComboBox.setItems(pageSizes);
        pageSizeComboBox.setValue(20);
        
        pageSizeComboBox.setOnAction(event -> {
            pageSize = pageSizeComboBox.getValue();
            currentPage = 0;
            loadSubjects(currentPage);
        });
    }
    
    /* ---------------------------------------------------
     * Load danh sách departments vào filter ComboBox
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * EditBy: K24DTCN210-NVMANH (26/11/2025 12:40) - Fix: Gọi getAllDepartments() thay vì extract từ subjects
     * EditBy: K24DTCN210-NVMANH (27/11/2025 11:33) - Thêm option "Tất cả" vào đầu danh sách
     * --------------------------------------------------- */
    private void loadDepartments() {
        Task<List<DepartmentDTO>> task = new Task<>() {
            @Override
            protected List<DepartmentDTO> call() throws Exception {
                return apiClient. getAllDepartments();
            }
        };
        
        task.setOnSucceeded(event -> {
            allDepartments = task.getValue();
            
            // Tạo danh sách departments với option "Tất cả" ở đầu
            ObservableList<DepartmentDTO> departmentsWithAll = FXCollections.observableArrayList();
            
            // Thêm option "Tất cả" (dummy object với ID=null)
            DepartmentDTO allOption = new DepartmentDTO();
            allOption.setId(null);
            allOption.setDepartmentName("-- Tất cả --");
            allOption.setDepartmentCode("ALL");
            departmentsWithAll.add(allOption);
            
            // Thêm các departments thực tế
            departmentsWithAll.addAll(allDepartments);
            
            departmentFilter.setItems(departmentsWithAll);
            
            // Set "Tất cả" làm giá trị mặc định
            departmentFilter.setValue(allOption);
            
            // Update statistics
            departmentCountLabel.setText("Số khoa: " + allDepartments.size());
            System.out.println("[DEBUG] Departments loaded successfully into UI with 'All' option");
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            System.err.println("[ERROR] Task failed in loadDepartments:");
            if (ex != null) {
                System.err.println("[ERROR] Exception: " + ex.getClass().getName());
                System.err.println("[ERROR] Message: " + ex. getMessage());
                ex.printStackTrace();
            }
            showError("Lỗi tải danh sách môn học", 
                ex != null ? ex. getMessage() : "Unknown error");
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Load danh sách subjects với pagination
     * @param page Số trang cần load (bắt đầu từ 0)
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 11:33) - Fix: Check null cho department ID
     * --------------------------------------------------- */
    private void loadSubjects(int page) {
        showLoading(true);
        
        Task<PageResponse<SubjectDTO>> task = new Task<>() {
            @Override
            protected PageResponse<SubjectDTO> call() throws Exception {
                DepartmentDTO selectedDept = departmentFilter.getValue();
                String keyword = searchField.getText();
                
                // Check nếu có department được chọn VÀ không phải option "Tất cả" (ID != null)
                if (selectedDept != null && selectedDept. getId() != null) {
                    // Filter by department
                    List<SubjectDTO> subjects = apiClient.getSubjectsByDepartment(selectedDept.getId());
                    PageResponse<SubjectDTO> response = new PageResponse<>();
                    response.setContent(subjects);
                    response. setTotalElements(subjects.size());
                    response.setTotalPages(1);
                    response.setNumber(0);
                    response.setSize(subjects.size());
                    return response;
                } else if (keyword != null && !keyword.trim(). isEmpty()) {
                    // Search by keyword
                    return apiClient.searchSubjects(keyword.trim(), page, pageSize);
                } else {
                    // Get all with pagination
                    return apiClient.getSubjectsPage(page, pageSize, "id", "asc");
                }
            }
        };
        
        task.setOnSucceeded(event -> {
            PageResponse<SubjectDTO> response = task.getValue();
            updateTable(response.getContent());
            updatePagination(response);
            showLoading(false);
        });
        
        task.setOnFailed(event -> {
            showLoading(false);
            showError("Lỗi tải danh sách môn học", 
                task. getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Cập nhật dữ liệu trong TableView
     * @param subjects Danh sách subjects cần hiển thị
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void updateTable(List<SubjectDTO> subjects) {
        ObservableList<SubjectDTO> data = FXCollections.observableArrayList(subjects);
        subjectsTable.setItems(data);
        totalSubjectsLabel.setText("Tổng số: " + subjects.size() + " môn học");
    }
    
    /* ---------------------------------------------------
     * Cập nhật thông tin pagination
     * @param response Response từ API chứa thông tin phân trang
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void updatePagination(PageResponse<SubjectDTO> response) {
        currentPage = response.getNumber();
        totalPages = response.getTotalPages();
        
        pageInfoLabel.setText("Trang " + (currentPage + 1) + " / " + totalPages);
        
        firstPageButton.setDisable(currentPage == 0);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        lastPageButton.setDisable(currentPage >= totalPages - 1);
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện tạo môn học mới
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleCreateSubject() {
        openSubjectDialog(null);
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện sửa môn học
     * @param subject Môn học cần sửa
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void handleEditSubject(SubjectDTO subject) {
        openSubjectDialog(subject);
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện xóa môn học
     * @param subject Môn học cần xóa
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void handleDeleteSubject(SubjectDTO subject) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa môn học: " + subject.getSubjectName());
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa môn học này?\nHành động này không thể hoàn tác.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteSubject(subject.getId());
        }
    }
    
    /* ---------------------------------------------------
     * Thực hiện xóa môn học qua API
     * @param subjectId ID của môn học cần xóa
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void deleteSubject(Long subjectId) {
        showLoading(true);
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.deleteSubject(subjectId);
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            showLoading(false);
            showInfo("Thành công", "Đã xóa môn học thành công!");
            loadSubjects(currentPage);
        });
        
        task.setOnFailed(event -> {
            showLoading(false);
            showError("Lỗi xóa môn học", task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    /* ---------------------------------------------------
     * Mở dialog tạo/sửa môn học
     * @param subject Môn học cần sửa (null nếu tạo mới)
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void openSubjectDialog(SubjectDTO subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/subject-edit-dialog.fxml"));
            Parent root = loader.load();
            
            SubjectEditDialogController controller = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setTitle(subject == null ? "Tạo môn học mới" : "Sửa môn học");
            dialogStage.setScene(new Scene(root));
            
            controller.initialize(apiClient, dialogStage);
            
            if (subject != null) {
                controller.setSubject(subject);
            }
            
            dialogStage.showAndWait();
            
            if (controller.isConfirmed()) {
                loadSubjects(currentPage);
            }
            
        } catch (IOException e) {
            showError("Lỗi mở dialog", e.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện tìm kiếm
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleSearch() {
        currentPage = 0;
        loadSubjects(currentPage);
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện reset filters
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 11:33) - Reset về option "Tất cả"
     * --------------------------------------------------- */
    @FXML
    private void handleResetFilters() {
        // Reset về option "Tất cả" (item đầu tiên trong list)
        if (departmentFilter. getItems().size() > 0) {
            departmentFilter. setValue(departmentFilter.getItems(). get(0));
        }
        searchField.clear();
        currentPage = 0;
        loadSubjects(currentPage);
    }
    
    /* ---------------------------------------------------
     * Xử lý sự kiện refresh dữ liệu
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleRefresh() {
        loadDepartments();
        loadSubjects(currentPage);
    }
    
    /* ---------------------------------------------------
     * Chuyển đến trang đầu tiên
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleFirstPage() {
        loadSubjects(0);
    }
    
    /* ---------------------------------------------------
     * Chuyển đến trang trước
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            loadSubjects(currentPage - 1);
        }
    }
    
    /* ---------------------------------------------------
     * Chuyển đến trang tiếp theo
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            loadSubjects(currentPage + 1);
        }
    }
    
    /* ---------------------------------------------------
     * Chuyển đến trang cuối cùng
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    @FXML
    private void handleLastPage() {
        loadSubjects(totalPages - 1);
    }
    
    /* ---------------------------------------------------
     * Hiển thị/ẩn loading indicator
     * @param show true để hiển thị, false để ẩn
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
     * --------------------------------------------------- */
    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            loadingPane.setVisible(show);
            loadingPane.setManaged(show);
        });
    }
    
    /* ---------------------------------------------------
     * Hiển thị dialog thông báo lỗi
     * @param title Tiêu đề dialog
     * @param message Nội dung thông báo
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
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
     * @author: K24DTCN210-NVMANH (26/11/2025 01:58)
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
}
