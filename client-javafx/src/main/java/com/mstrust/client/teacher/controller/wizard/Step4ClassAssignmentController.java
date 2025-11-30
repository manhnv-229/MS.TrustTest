package com.mstrust.client.teacher.controller.wizard;

import com.mstrust.client.teacher.api.SubjectApiClient;
import com.mstrust.client.teacher.api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust.client.teacher.dto.ClassDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.stage.Window;

import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Controller cho Step 4 của Exam Creation Wizard
 * Xử lý việc assign classes cho đề thi
 * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
 * EditBy: K24DTCN210-NVMANH (30/11/2025) - Load classes từ API thực tế, thêm loading spinner
 * --------------------------------------------------- */
public class Step4ClassAssignmentController {

    @FXML private ListView<ClassDTO> availableClassesList;
    @FXML private ListView<ClassDTO> assignedClassesList;
    @FXML private Label availableCountLabel;
    @FXML private Label assignedCountLabel;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;
    private SubjectApiClient subjectApiClient;
    private ExamManagementApiClient examApiClient;
    
    // Observable Lists để quản lý dữ liệu ListView
    private ObservableList<ClassDTO> availableClasses = FXCollections.observableArrayList();
    private ObservableList<ClassDTO> assignedClasses = FXCollections.observableArrayList();
    
    // Cache để tránh load lại từ API mỗi lần
    private boolean classesLoaded = false;

    /* ---------------------------------------------------
     * Khởi tạo controller
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        hideError();
        hideLoading();
        setupListViews();
        updateCountLabels();
    }
    
    /* ---------------------------------------------------
     * Setup ListView controls
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private void setupListViews() {
        // Bind data to ListViews
        if (availableClassesList != null) {
            availableClassesList.setItems(availableClasses);
            // Custom cell factory to display class name
            availableClassesList.setCellFactory(listView -> new ListCell<ClassDTO>() {
                @Override
                protected void updateItem(ClassDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getClassName() + " (" + item.getClassCode() + ")");
                    }
                }
            });
        }
        
        if (assignedClassesList != null) {
            assignedClassesList.setItems(assignedClasses);
            // Custom cell factory to display class name
            assignedClassesList.setCellFactory(listView -> new ListCell<ClassDTO>() {
                @Override
                protected void updateItem(ClassDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getClassName() + " (" + item.getClassCode() + ")");
                    }
                }
            });
        }
    }

    /* ---------------------------------------------------
     * Set wizard data từ parent controller
     * @param wizardData Đối tượng chứa dữ liệu wizard
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Restore assignedClasses và chỉ load API nếu chưa load
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        
        // Debug print để kiểm tra data
        System.out.println("=== STEP 4 DEBUG: setWizardData() ===");
        if (wizardData != null) {
            System.out.println("Title: " + wizardData.getTitle());
            System.out.println("Start Time: " + wizardData.getStartTime());
            System.out.println("End Time: " + wizardData.getEndTime());
            System.out.println("Subject Class ID: " + wizardData.getSubjectClassId());
            System.out.println("Subject Class Name: " + wizardData.getSubjectClassName());
            System.out.println("Exam Purpose: " + wizardData.getExamPurpose());
            System.out.println("Exam Format: " + wizardData.getExamFormat());
            System.out.println("Assigned Classes Count: " + wizardData.getAssignedClassIds().size());
        } else {
            System.out.println("WizardData is null!");
        }
        System.out.println("=====================================");
        
        // Restore assignedClasses từ wizardData trước
        restoreAssignedClasses();
        
        // Chỉ load classes từ API nếu chưa load hoặc cần refresh
        if (!classesLoaded && wizardData != null && examApiClient != null) {
            tryLoadAvailableClasses();
        } else if (classesLoaded) {
            // Đã load rồi, chỉ cần filter lại available classes
            filterAvailableClasses();
        }
    }

    /* ---------------------------------------------------
     * Set parent controller
     * @param parentController Controller cha
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    public void setParentController(ExamCreationWizardController parentController) {
        this.parentController = parentController;
    }

    /* ---------------------------------------------------
     * Set Subject API client
     * @param subjectApiClient Subject API client
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    public void setSubjectApiClient(SubjectApiClient subjectApiClient) {
        this.subjectApiClient = subjectApiClient;
    }

    /* ---------------------------------------------------
     * Set API client (required by ExamCreationWizardController)
     * @param examApiClient Exam management API client
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Tự động load classes sau khi set client
     * --------------------------------------------------- */
    public void setApiClient(ExamManagementApiClient examApiClient) {
        this.examApiClient = examApiClient;
        
        // Tự động load classes khi client đã được set và wizardData đã sẵn sàng
        tryLoadAvailableClasses();
    }

    /* ---------------------------------------------------
     * PUBLIC method để force save form data từ parent controller
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP4: saveFormToWizardData() CALLED ===");
        
        if (wizardData != null) {
            // Save assigned class IDs to wizard data
            wizardData.getAssignedClassIds().clear();
            List<Long> classIds = assignedClasses.stream()
                .map(ClassDTO::getId)
                .collect(Collectors.toList());
            wizardData.getAssignedClassIds().addAll(classIds);
            
            System.out.println("Saved " + classIds.size() + " assigned classes to wizard data");
        }
        
        System.out.println("=== STEP4: saveFormToWizardData() COMPLETED ===");
    }
    
    /* ---------------------------------------------------
     * Kiểm tra và load classes nếu đủ điều kiện
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void tryLoadAvailableClasses() {
        // Chỉ load khi cả wizardData và examApiClient đều đã sẵn sàng
        if (wizardData != null && examApiClient != null) {
            loadAvailableClasses();
        } else {
            System.out.println("=== STEP4: Chưa thể load classes - wizardData: " + 
                (wizardData != null ? "OK" : "NULL") + 
                ", examApiClient: " + 
                (examApiClient != null ? "OK" : "NULL") + " ===");
        }
    }
    
    /* ---------------------------------------------------
     * Load available classes from API
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Load từ API thực tế thay vì mock data
     * --------------------------------------------------- */
    private void loadAvailableClasses() {
        if (examApiClient == null) {
            showError("Exam API Client chưa được khởi tạo");
            return;
        }
        
        System.out.println("=== STEP4: loadAvailableClasses() CALLED ===");
        
        // Hiển thị loading spinner
        showLoading();
        
        // Run API call in background thread
        Task<List<ClassDTO>> loadTask = new Task<List<ClassDTO>>() {
            @Override
            protected List<ClassDTO> call() throws Exception {
                // Call API để lấy danh sách lớp học
                return examApiClient.getAllClasses();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    List<ClassDTO> allClasses = loadTask.getValue();
                    
                    availableClasses.clear();
                    availableClasses.addAll(allClasses);
                    classesLoaded = true;
                    
                    // Restore assignedClasses sau khi load availableClasses
                    restoreAssignedClasses();
                    
                    // Remove already assigned classes from available list
                    filterAvailableClasses();
                    
                    updateCountLabels();
                    hideError();
                    hideLoading();
                    System.out.println("=== STEP4: Loaded " + availableClasses.size() + " available classes from API ===");
                } catch (Exception ex) {
                    hideLoading();
                    showError("Lỗi khi xử lý dữ liệu: " + ex.getMessage());
                    System.err.println("Error processing classes: " + ex.getMessage());
                }
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                hideLoading();
                Throwable exception = loadTask.getException();
                System.err.println("Failed to load classes: " + exception.getMessage());
                showError("Không thể tải danh sách lớp học: " + exception.getMessage());
            });
        });
        
        // Run task in background
        new Thread(loadTask).start();
    }
    
    /* ---------------------------------------------------
     * Update count labels
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private void updateCountLabels() {
        if (availableCountLabel != null) {
            availableCountLabel.setText("Có sẵn: " + availableClasses.size());
        }
        if (assignedCountLabel != null) {
            assignedCountLabel.setText("Đã gán: " + assignedClasses.size() + " lớp");
        }
    }

    /* ---------------------------------------------------
     * FXML Event Handler: Gán lớp được chọn
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleAssign() {
        System.out.println("=== STEP4: handleAssign() CALLED ===");
        hideError();
        
        ClassDTO selectedClass = availableClassesList.getSelectionModel().getSelectedItem();
        if (selectedClass != null) {
            // Check if already assigned
            boolean alreadyAssigned = assignedClasses.stream()
                .anyMatch(cls -> cls.getId().equals(selectedClass.getId()));
                
            if (!alreadyAssigned) {
                availableClasses.remove(selectedClass);
                assignedClasses.add(selectedClass);
                updateCountLabels();
                System.out.println("Assigned class: " + selectedClass.getClassName());
            } else {
                showError("Lớp học này đã được gán rồi!");
            }
        } else {
            showError("Vui lòng chọn một lớp học để gán!");
        }
    }

    /* ---------------------------------------------------
     * FXML Event Handler: Gán tất cả lớp học
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleAssignAll() {
        System.out.println("=== STEP4: handleAssignAll() CALLED ===");
        hideError();
        
        int assignedCount = 0;
        for (ClassDTO classDto : List.copyOf(availableClasses)) {
            boolean alreadyAssigned = assignedClasses.stream()
                .anyMatch(cls -> cls.getId().equals(classDto.getId()));
                
            if (!alreadyAssigned) {
                availableClasses.remove(classDto);
                assignedClasses.add(classDto);
                assignedCount++;
            }
        }
        
        updateCountLabels();
        System.out.println("Assigned " + assignedCount + " classes");
    }

    /* ---------------------------------------------------
     * FXML Event Handler: Bỏ gán lớp được chọn
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleUnassign() {
        System.out.println("=== STEP4: handleUnassign() CALLED ===");
        hideError();
        
        ClassDTO selectedClass = assignedClassesList.getSelectionModel().getSelectedItem();
        if (selectedClass != null) {
            assignedClasses.remove(selectedClass);
            availableClasses.add(selectedClass);
            updateCountLabels();
            System.out.println("Unassigned class: " + selectedClass.getClassName());
        } else {
            showError("Vui lòng chọn một lớp học để bỏ gán!");
        }
    }

    /* ---------------------------------------------------
     * FXML Event Handler: Bỏ gán tất cả lớp học
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleUnassignAll() {
        System.out.println("=== STEP4: handleUnassignAll() CALLED ===");
        hideError();
        
        for (ClassDTO classDto : List.copyOf(assignedClasses)) {
            assignedClasses.remove(classDto);
            availableClasses.add(classDto);
        }
        
        updateCountLabels();
        System.out.println("Unassigned all classes");
    }

    /* ---------------------------------------------------
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /* ---------------------------------------------------
     * Ẩn thông báo lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
    
    /* ---------------------------------------------------
     * Hiển thị loading spinner
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showLoading() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
        }
    }
    
    /* ---------------------------------------------------
     * Ẩn loading spinner
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void hideLoading() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);
        }
    }

    /* ---------------------------------------------------
     * Restore assignedClasses từ wizardData
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void restoreAssignedClasses() {
        if (wizardData == null || wizardData.getAssignedClassIds().isEmpty()) {
            assignedClasses.clear();
            return;
        }
        
        // Nếu availableClasses chưa load, sẽ restore sau khi load xong
        if (availableClasses.isEmpty()) {
            System.out.println("=== STEP4: availableClasses chưa load, sẽ restore sau ===");
            return;
        }
        
        // Restore assigned classes từ wizardData
        assignedClasses.clear();
        for (Long classId : wizardData.getAssignedClassIds()) {
            ClassDTO foundClass = availableClasses.stream()
                .filter(c -> c.getId().equals(classId))
                .findFirst()
                .orElse(null);
            
            if (foundClass != null) {
                assignedClasses.add(foundClass);
            } else {
                // Nếu không tìm thấy trong availableClasses, có thể đã bị xóa hoặc không có quyền
                System.out.println("WARNING: Class ID " + classId + " not found in available classes");
            }
        }
        
        updateCountLabels();
        System.out.println("=== STEP4: Restored " + assignedClasses.size() + " assigned classes ===");
    }
    
    /* ---------------------------------------------------
     * Filter available classes: Remove already assigned
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void filterAvailableClasses() {
        if (wizardData != null && !wizardData.getAssignedClassIds().isEmpty()) {
            List<Long> assignedIds = wizardData.getAssignedClassIds();
            availableClasses.removeIf(cls -> assignedIds.contains(cls.getId()));
        }
        
        // Also remove from assignedClasses observable list
        List<Long> assignedIds = assignedClasses.stream()
            .map(ClassDTO::getId)
            .collect(Collectors.toList());
        
        availableClasses.removeIf(cls -> assignedIds.contains(cls.getId()));
    }
    
    /* ---------------------------------------------------
     * Validate form data
     * @return true nếu hợp lệ, false nếu có lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public boolean validateForm() {
        hideError();
        
        // Lưu dữ liệu trước khi validate
        saveFormToWizardData();
        
        // Validate sử dụng wizardData.validateStep4()
        List<String> errors = wizardData.validateStep4();
        
        if (!errors.isEmpty()) {
            showValidationError(String.join("\n", errors));
            return false;
        }
        
        return true;
    }
    
    /* ---------------------------------------------------
     * Hiển thị validation error bằng Alert dialog
     * @param message Nội dung lỗi
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText("Vui lòng kiểm tra lại thông tin");
        alert.setContentText(message);
        
        // Set owner window từ parent controller
        if (parentController != null && parentController.getWizardPane() != null) {
            Window owner = parentController.getWizardPane().getScene().getWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
        }
        
        alert.showAndWait();
    }

    /* ---------------------------------------------------
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * EditBy: K24DTCN210-NVMANH (30/11/2025) - Thêm validation trước khi next
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        boolean isValid = validateForm();
        
        if (isValid && parentController != null) {
            parentController.nextStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Previous
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handlePrevious() {
        if (parentController != null) {
            parentController.previousStep();
        }
    }

    /* ---------------------------------------------------
     * Xử lý nút Cancel
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleCancel() {
        if (parentController != null) {
            parentController.cancelWizard();
        }
    }
}
