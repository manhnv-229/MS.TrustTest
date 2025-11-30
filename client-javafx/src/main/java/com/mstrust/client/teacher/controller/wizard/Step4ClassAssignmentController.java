package com.mstrust.client. teacher.controller.wizard;

import com.mstrust.client. teacher.api.SubjectApiClient;
import com. mstrust.client.teacher. api.ExamManagementApiClient;
import com.mstrust.client.teacher.dto.ExamWizardData;
import com.mstrust. client.teacher.dto.ClassDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx. application.Platform;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/* ---------------------------------------------------
 * Controller cho Step 4 của Exam Creation Wizard
 * Xử lý việc assign classes cho đề thi
 * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
 * EditBy: K24DTCN210-NVMANH (30/11/2025 13:58) - Recreate corrupted file
 * --------------------------------------------------- */
public class Step4ClassAssignmentController {

    @FXML private ListView<ClassDTO> availableClassesList;
    @FXML private ListView<ClassDTO> assignedClassesList;
    @FXML private Label availableCountLabel;
    @FXML private Label assignedCountLabel;
    @FXML private Label errorLabel;

    private ExamWizardData wizardData;
    private ExamCreationWizardController parentController;
    private SubjectApiClient subjectApiClient;
    private ExamManagementApiClient examApiClient;
    
    // Observable Lists để quản lý dữ liệu ListView
    private ObservableList<ClassDTO> availableClasses = FXCollections.observableArrayList();
    private ObservableList<ClassDTO> assignedClasses = FXCollections.observableArrayList();

    /* ---------------------------------------------------
     * Khởi tạo controller
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    public void initialize() {
        hideError();
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
            availableClassesList. setCellFactory(listView -> new ListCell<ClassDTO>() {
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
     * --------------------------------------------------- */
    public void setWizardData(ExamWizardData wizardData) {
        this.wizardData = wizardData;
        
        // Debug print để kiểm tra data
        System.out.println("=== STEP 4 DEBUG: setWizardData() ===");
        if (wizardData != null) {
            System.out.println("Title: " + wizardData.getTitle());
            System.out. println("Start Time: " + wizardData.getStartTime());
            System.out.println("End Time: " + wizardData.getEndTime());
            System.out.println("Subject Class ID: " + wizardData.getSubjectClassId());
            System.out.println("Subject Class Name: " + wizardData.getSubjectClassName());
            System.out.println("Exam Purpose: " + wizardData. getExamPurpose());
            System.out.println("Exam Format: " + wizardData.getExamFormat());
            System.out.println("Assigned Classes Count: " + wizardData.getAssignedClassIds().size());
        } else {
            System.out. println("WizardData is null!");
        }
        System.out.println("=====================================");
        
        // Load available classes when wizard data is set
        loadAvailableClasses();
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
     * --------------------------------------------------- */
    public void setApiClient(ExamManagementApiClient examApiClient) {
        this.examApiClient = examApiClient;
    }

    /* ---------------------------------------------------
     * PUBLIC method để force save form data từ parent controller
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    public void saveFormToWizardData() {
        System.out.println("=== STEP4: saveFormToWizardData() CALLED ===");
        
        if (wizardData != null) {
            // Save assigned class IDs to wizard data
            wizardData. getAssignedClassIds().clear();
            List<Long> classIds = assignedClasses.stream()
                . map(ClassDTO::getId)
                .collect(Collectors.toList());
            wizardData. getAssignedClassIds().addAll(classIds);
            
            System.out.println("Saved " + classIds.size() + " assigned classes to wizard data");
        }
        
        System.out.println("=== STEP4: saveFormToWizardData() COMPLETED ===");
    }
    
    /* ---------------------------------------------------
     * Load available classes - Enhanced with real DB data lookup
     * TODO: Replace with proper API call when backend endpoints available
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private void loadAvailableClasses() {
        System.out. println("=== STEP4: loadAvailableClasses() CALLED ===");
        
        // FUTURE: Backend should provide:
        // GET /api/subject-classes - get all classes 
        // GET /api/subject-classes/by-subject/{subjectId} - filter by subject
        
        // For now: Use realistic mock data based on database structure
        List<ClassDTO> mockClasses = createRealisticMockClasses();
        
        Platform.runLater(() -> {
            availableClasses.clear();
            availableClasses. addAll(mockClasses);
            
            // Remove already assigned classes from available list
            if (wizardData != null) {
                List<Long> assignedIds = wizardData.getAssignedClassIds();
                availableClasses.removeIf(cls -> assignedIds. contains(cls.getId()));
            }
            
            updateCountLabels();
            hideError();
            System.out.println("=== STEP4: Loaded " + availableClasses. size() + " classes (mock data) ===");
        });
    }
    
    /* ---------------------------------------------------
     * Create realistic mock class data based on database schema
     * Uses subject_classes table structure: id, code, subject_id, semester, teacher_id, max_students
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    private List<ClassDTO> createRealisticMockClasses() {
        List<ClassDTO> mockClasses = new ArrayList<>();
        
        // Mock data matching database structure from MCP query results
        // Database has: CS101_DHTI15A1HN_1, semester: 2024-2025-1, max_students: 40
        mockClasses.add(new ClassDTO(1L, "CS101_DHTI15A1HN_1", "Lập Trình Java Updated", "DHTI15A1HN", "2024-2025-1", "GV Nguyễn Văn A", 40));
        mockClasses.add(new ClassDTO(2L, "CS102_DHTI15A2HN_1", "Lập Trình Python", "DHTI15A2HN", "2024-2025-1", "GV Trần Thị B", 35));
        mockClasses.add(new ClassDTO(3L, "CS103_DHTI15B1HN_1", "Cơ sở dữ liệu", "DHTI15B1HN", "2024-2025-1", "GV Lê Văn C", 42));
        mockClasses.add(new ClassDTO(4L, "CS104_DHTI15B2HN_1", "Mạng máy tính", "DHTI15B2HN", "2024-2025-1", "GV Phạm Thị D", 38));
        mockClasses.add(new ClassDTO(5L, "CS105_DHTI15C1HN_1", "An toàn thông tin", "DHTI15C1HN", "2024-2025-1", "GV Hoàng Văn E", 45));
        
        return mockClasses;
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
        System.out. println("=== STEP4: handleAssign() CALLED ===");
        hideError();
        
        ClassDTO selectedClass = availableClassesList.getSelectionModel(). getSelectedItem();
        if (selectedClass != null) {
            // Check if already assigned
            boolean alreadyAssigned = assignedClasses.stream()
                . anyMatch(cls -> cls.getId().equals(selectedClass.getId()));
                
            if (!alreadyAssigned) {
                availableClasses.remove(selectedClass);
                assignedClasses. add(selectedClass);
                updateCountLabels();
                System.out.println("Assigned class: " + selectedClass. getClassName());
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
        System. out.println("=== STEP4: handleAssignAll() CALLED ===");
        hideError();
        
        int assignedCount = 0;
        for (ClassDTO classDto : List.copyOf(availableClasses)) {
            boolean alreadyAssigned = assignedClasses.stream()
                . anyMatch(cls -> cls.getId().equals(classDto.getId()));
                
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
            availableClasses. add(selectedClass);
            updateCountLabels();
            System. out.println("Unassigned class: " + selectedClass.getClassName());
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
        
        for (ClassDTO classDto : List. copyOf(assignedClasses)) {
            assignedClasses. remove(classDto);
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
     * Xử lý nút Next
     * @author: K24DTCN210-NVMANH (30/11/2025 13:58)
     * --------------------------------------------------- */
    @FXML
    private void handleNext() {
        if (parentController != null) {
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
