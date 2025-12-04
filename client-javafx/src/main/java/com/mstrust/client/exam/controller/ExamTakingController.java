package com.mstrust.client.exam.controller;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.component.QuestionDisplayComponent;
import com.mstrust.client.exam.component.QuestionPaletteComponent;
import com.mstrust.client.exam.component.TimerComponent;
import com.mstrust.client.exam.dto.QuestionDTO;
import com.mstrust.client.exam.dto.SaveAnswerRequest;
import com.mstrust.client.exam.dto.StartExamResponse;
import com.mstrust.client.exam.model.ExamSession;
import com.mstrust.client.exam.service.AutoSaveService;
import com.mstrust.client.exam.service.ClientLogService;
import com.mstrust.client.exam.service.NetworkMonitor;
import com.mstrust.client.exam.service.ConnectionRecoveryService;
import com.mstrust.client.exam.service.FullScreenLockService;
import com.mstrust.client.exam.util.TimeFormatter;
import com.mstrust.client.exam.util.WindowCenterHelper;
import com.mstrust.client.util.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/* ---------------------------------------------------
 * Exam Taking Controller - Main controller cho exam taking interface
 * - Initialize exam session (call startExam API)
 * - Load questions từ backend
 * - Manage ExamSession model
 * - Coordinate 3 components: Timer + Palette + QuestionDisplay
 * - Handle navigation (Previous/Next/Jump)
 * - Handle save answer (manual + auto)
 * - Handle submit exam
 * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
 * EditBy: K24DTCN210-NVMANH (23/11/2025 18:00) - Phase 8.4: Integrated AutoSaveService + NetworkMonitor
 * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Phase 8.6 Step 3: Exit Protection & Polish
 * --------------------------------------------------- */
public class ExamTakingController {

    // FXML injected nodes (Phase 8.6: Added loading overlay + progress + statistics)
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingMessage;
    @FXML private VBox timerContainer;
    @FXML private Label examTitleLabel;
    @FXML private Label examSubtitleLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label studentCodeLabel;
    
    // Progress bar (Phase 8.6: Bug 8 fix)
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // Statistics (Phase 8.6: Bug 8 fix)
    @FXML private Label answeredCountLabel;
    @FXML private Label markedCountLabel;
    @FXML private Label unansweredCountLabel;
    
    // Navigation & question display
    @FXML private VBox paletteContainer;
    @FXML private VBox questionDisplayContainer;
    @FXML private TextField jumpToQuestionField;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button saveButton;
    @FXML private Button submitButton;
    
    // Status bar (Phase 8.6: Bug 8 fix)
    @FXML private Label lastSaveLabel;
    
    // Components
    private TimerComponent timerComponent;
    private QuestionPaletteComponent paletteComponent;
    private QuestionDisplayComponent questionDisplayComponent;
    
    // Model & API
    private ExamSession examSession;
    private ExamApiClient apiClient;
    
    // Phase 8.4: Auto-Save Services
    private AutoSaveService autoSaveService;
    private ClientLogService logService;
    private NetworkMonitor networkMonitor;
    private ConnectionRecoveryService recoveryService;
    
    // Phase 8.6: Full-Screen Security
    private Stage stage;
    private FullScreenLockService fullScreenLockService;
    
    // Phase 11: Monitoring System
    private com.mstrust.client.monitoring.MonitoringCoordinator monitoringCoordinator;
    
    // State tracking
    private Map<Long, String> answersCache; // questionId -> answer
    private Map<Long, Boolean> markedForReview; // questionId -> marked
    private boolean isExamActive = false; // Track if exam is in progress (Phase 8.6)
    
    // User info
    private String studentName = "Sinh viên";
    private String studentCode = "MSV";

    /* ---------------------------------------------------
     * Constructor
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    public ExamTakingController() {
        this.answersCache = new HashMap<>();
        this.markedForReview = new HashMap<>();
    }
    
    /* ---------------------------------------------------
     * Set Stage để sử dụng cho full-screen (Phase 8.6)
     * @param stage Primary stage của application
     * @author: K24DTCN210-NVMANH (24/11/2025 09:12)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Phase 8.6: Added exit confirmation & keyboard shortcuts
     * --------------------------------------------------- */
    public void setStage(Stage stage) {
        this.stage = stage;
        setupExitConfirmation();
        setupKeyboardShortcuts();
    }
    
    /* ---------------------------------------------------
     * Set user info
     * @param name Student name
     * @param code Student code/email
     * @author: K24DTCN210-NVMANH (04/12/2025)
     * --------------------------------------------------- */
    public void setUserInfo(String name, String code) {
        this.studentName = name;
        this.studentCode = code;
        
        // Update labels if initialized
        Platform.runLater(() -> {
            if (studentNameLabel != null) {
                studentNameLabel.setText(name);
            }
            if (studentCodeLabel != null) {
                studentCodeLabel.setText(code);
            }
        });
    }
    
    /* ---------------------------------------------------
     * Setup exit confirmation dialog (Phase 8.6 Step 3)
     * Xử lý khi user cố thoát bằng close window hoặc ESC
     * @author: K24DTCN210-NVMANH (25/11/2025 09:40)
     * --------------------------------------------------- */
    private void setupExitConfirmation() {
        if (stage == null) return;
        
        // Handle window close request
        stage.setOnCloseRequest(event -> {
            if (isExamActive) {
                event.consume(); // Prevent immediate close
                handleExitAttempt();
            }
        });
    }
    
    /* ---------------------------------------------------
     * Setup keyboard shortcuts (Phase 8.6 Step 3)
     * - Ctrl+S: Manual save
     * - Ctrl+N: Next question
     * - Ctrl+P: Previous question
     * - Ctrl+M: Mark for review
     * - 1-9: Jump to question
     * - ESC: Exit confirmation
     * @author: K24DTCN210-NVMANH (25/11/2025 09:40)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:00) - Bug 3: Fixed number keys in CodeArea
     * --------------------------------------------------- */
    private void setupKeyboardShortcuts() {
        if (stage == null) return;
        
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // ✅ CRITICAL: Check if focused node is ANY text input (TextField, TextArea, CodeArea)
            javafx.scene.Node focused = stage.getScene().getFocusOwner();
            
            // Check for standard JavaFX text inputs
            boolean isTextInput = focused instanceof javafx.scene.control.TextInputControl;
            
            // ✅ CRITICAL: Also check for RichTextFX CodeArea (used in programming questions)
            if (!isTextInput && focused != null) {
                String className = focused.getClass().getName();
                isTextInput = className.contains("CodeArea") || 
                             className.contains("StyledTextArea") ||
                             className.contains("InlineCssTextArea");
            }
            
            // ESC key - exit confirmation
            if (event.getCode() == KeyCode.ESCAPE && isExamActive) {
                event.consume();
                handleExitAttempt();
                return;
            }
            
            // Ctrl shortcuts - SKIP when typing in text input
            if (event.isControlDown()) {
                // Allow Ctrl+C, Ctrl+V, Ctrl+X in text inputs
                if (isTextInput && (event.getCode() == KeyCode.C || 
                                    event.getCode() == KeyCode.V || 
                                    event.getCode() == KeyCode.X)) {
                    return; // Let default behavior handle copy/paste/cut
                }
                
                switch (event.getCode()) {
                    case S: // Ctrl+S - Manual save
                        event.consume();
                        if (!saveButton.isDisabled()) {
                            onSave();
                        }
                        break;
                        
                    case N: // Ctrl+N - Next question (skip in text input)
                        if (!isTextInput) {
                            event.consume();
                            if (!nextButton.isDisabled()) {
                                onNext();
                            }
                        }
                        break;
                        
                    case P: // Ctrl+P - Previous question (skip in text input)
                        if (!isTextInput) {
                            event.consume();
                            if (!previousButton.isDisabled()) {
                                onPrevious();
                            }
                        }
                        break;
                        
                    case M: // Ctrl+M - Mark for review
                        event.consume();
                        toggleMarkForReview();
                        break;
                        
                    default:
                        break;
                }
            }
            
            // ✅ FIXED Bug 3: Number keys 1-9 - ONLY jump when NOT in text input
            if (event.getCode().isDigitKey() && !event.isControlDown()) {
                // SKIP if user is typing in TextField/TextArea/CodeArea
                if (isTextInput) {
                    return; // Let user type numbers normally
                }
                
                // Jump to question 1-9
                if (examSession != null) {
                    int digit = event.getCode().ordinal() - KeyCode.DIGIT1.ordinal() + 1;
                    if (digit >= 1 && digit <= 9) {
                        int questionIndex = digit - 1;
                        if (questionIndex < examSession.getQuestions().size()) {
                            event.consume();
                            jumpToQuestion(questionIndex);
                        }
                    }
                }
            }
        });
    }
    
    /* ---------------------------------------------------
     * Handle exit attempt - show confirmation dialog
     * @author: K24DTCN210-NVMANH (25/11/2025 09:40)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 15:03) - Fixed dialog owner & centering
     * --------------------------------------------------- */
    private void handleExitAttempt() {
        StringBuilder message = new StringBuilder();
        message.append("Nếu thoát bây giờ:\n\n");
        message.append("▪ Các câu trả lời chưa lưu sẽ BỊ MẤT\n");
        message.append("▪ Bài thi có thể KHÔNG ĐƯỢC NỘP\n");
        message.append("▪ Bạn có thể bị coi là VI PHẠM quy định\n\n");
        message.append("Bạn có CHẮC CHẮN muốn thoát không?");

        ButtonType continueExam = new ButtonType("Tiếp Tục Thi", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType exitAnyway = new ButtonType("Thoát Ngay", ButtonBar.ButtonData.OK_DONE);

        Optional<ButtonType> result = DialogUtils.showAlert(
            Alert.AlertType.CONFIRMATION,
            "Xác Nhận Thoát",
            "⚠️ Bạn đang trong quá trình làm bài thi!",
            message.toString(),
            stage,
            continueExam, exitAnyway
        );
        
        if (result.isPresent() && result.get() == exitAnyway) {
            performExitCleanup();
            Platform.exit();
        }
    }
    
    /* ---------------------------------------------------
     * Toggle mark for review on current question
     * @author: K24DTCN210-NVMANH (25/11/2025 09:40)
     * --------------------------------------------------- */
    private void toggleMarkForReview() {
        if (questionDisplayComponent != null) {
            boolean currentMark = questionDisplayComponent.isMarkedForReview();
            questionDisplayComponent.setMarkedForReview(!currentMark);
            
            // Update cache
            QuestionDTO currentQuestion = questionDisplayComponent.getCurrentQuestion();
            if (currentQuestion != null) {
                markedForReview.put(currentQuestion.getId(), !currentMark);
                
                // Update palette
                int index = examSession.getCurrentQuestionIndex();
                String answer = answersCache.get(currentQuestion.getId());
                if (!currentMark) {
                    paletteComponent.updateQuestionStatus(index, "marked");
                } else if (answer != null && !answer.isEmpty()) {
                    paletteComponent.updateQuestionStatus(index, "answered");
                } else {
                    paletteComponent.updateQuestionStatus(index, "unanswered");
                }
            }
        }
    }
    
    /* ---------------------------------------------------
     * Perform cleanup khi exit confirmed
     * @author: K24DTCN210-NVMANH (25/11/2025 09:40)
     * --------------------------------------------------- */
    private void performExitCleanup() {
        isExamActive = false;
        
        // Stop all services
        if (fullScreenLockService != null) {
            fullScreenLockService.cleanup();
        }
        if (autoSaveService != null) {
            autoSaveService.stop();
        }
        if (networkMonitor != null) {
            networkMonitor.stop();
        }
        if (timerComponent != null) {
            timerComponent.stop();
        }
        
        // Phase 11: Stop monitoring
        if (monitoringCoordinator != null) {
            monitoringCoordinator.stopMonitoring();
            System.out.println("[Phase 11] Monitoring stopped on exit");
        }
        
        System.out.println("[Phase 8.6] Exit cleanup completed");
    }

    /* ---------------------------------------------------
     * Initialize exam với StartExamResponse ĐÃ CÓ từ ExamListController
     * NEW method để tránh double API call (Phase 8.6 bugfix)
     * @param response StartExamResponse from ExamListController's API call
     * @param authToken Bearer token
     * @author: K24DTCN210-NVMANH (24/11/2025 13:42)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Phase 8.6: Added loading overlay & isExamActive flag
     * --------------------------------------------------- */
    public void initializeExamWithResponse(StartExamResponse response, String authToken) {
        this.apiClient = new ExamApiClient(authToken);
        this.logService = new ClientLogService(this.apiClient);
        
        // Show loading
        showLoading("Đang tải câu hỏi...");
        
        new Thread(() -> {
            try {
                // 1. Load questions (GET /api/exam-taking/questions/{submissionId})  
                List<QuestionDTO> questions = apiClient.getQuestionsForSubmission(response.getSubmissionId());
                
                // 2. Create ExamSession model
                examSession = new ExamSession();
                examSession.setSubmissionId(response.getSubmissionId());
                examSession.setExamTitle(response.getExamTitle());
                examSession.setQuestions(questions);
                examSession.setRemainingSeconds(response.getRemainingSeconds().longValue());
                examSession.setCurrentQuestionIndex(0);
                
                // 3. Initialize UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        initializeComponents(response);
                        initializeAutoSaveServices(); // Phase 8.4
                        initializeFullScreenSecurity(); // Phase 8.6
                        startMonitoringSystem(response.getSubmissionId(), authToken); // Phase 11
                        displayCurrentQuestion();
                        isExamActive = true; // Mark exam as active
                        hideLoading();
                    } catch (Exception e) {
                        showError("Lỗi khởi tạo UI", e.getMessage());
                        hideLoading();
                    }
                });
                
            } catch (IOException e) {
                logService.logError("ExamTakingController", "Load questions failed", e, response.getSubmissionId(), null);
                Platform.runLater(() -> {
                    showError("Lỗi tải câu hỏi", e.getMessage());
                    hideLoading();
                });
            } catch (Exception e) {
                logService.logError("ExamTakingController", "Unexpected error during init", e, response.getSubmissionId(), null);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    showError("Lỗi tải câu hỏi", "Lỗi: " + e.getMessage());
                    hideLoading();
                });
            }
        }).start();
    }
    
    /* ---------------------------------------------------
     * Initialize exam với examId và authToken
     * Called từ ExamListController
     * @deprecated Use initializeExamWithResponse() để tránh double API call
     * @param examId ID của đề thi
     * @param authToken Bearer token
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:00) - Phase 8.4: Added initializeAutoSaveServices()
     * EditBy: K24DTCN210-NVMANH (24/11/2025 13:42) - Deprecated: Use initializeExamWithResponse()
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Phase 8.6: Added loading overlay & isExamActive flag
     * --------------------------------------------------- */
    @Deprecated
    public void initializeExam(Long examId, String authToken) {
        this.apiClient = new ExamApiClient(authToken);
        
        // Show loading
        showLoading("Đang khởi tạo bài thi...");
        
        new Thread(() -> {
            try {
                // 1. Start exam (POST /api/exam-taking/start/{examId})
                StartExamResponse response = apiClient.startExam(examId);
                
                // 2. Load questions (GET /api/exam-taking/questions/{submissionId})  
                List<QuestionDTO> questions = apiClient.getQuestionsForSubmission(response.getSubmissionId());
                
                // 3. Create ExamSession model
                examSession = new ExamSession();
                examSession.setSubmissionId(response.getSubmissionId());
                examSession.setExamTitle(response.getExamTitle());
                examSession.setQuestions(questions);
                examSession.setRemainingSeconds(response.getRemainingSeconds().longValue());
                examSession.setCurrentQuestionIndex(0);
                
                // 4. Initialize UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        initializeComponents(response);
                        initializeAutoSaveServices(); // Phase 8.4
                        initializeFullScreenSecurity(); // Phase 8.6: NEW
                        displayCurrentQuestion();
                        isExamActive = true; // Mark exam as active
                        hideLoading();
                    } catch (Exception e) {
                        showError("Lỗi khởi tạo UI", e.getMessage());
                        hideLoading();
                    }
                });
                
            } catch (ExamApiClient.ExamStartException e) {
                Platform.runLater(() -> {
                    showError("Lỗi khởi tạo bài thi", e.getMessage());
                    hideLoading();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Lỗi khởi tạo bài thi", e.getMessage());
                    hideLoading();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    showError("Lỗi khởi tạo bài thi", "Bị gián đoạn: " + e.getMessage());
                    hideLoading();
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Initialize các components (Timer, Palette, QuestionDisplay)
     * @param response StartExamResponse từ API
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 11:36) - Bug 5: Added timer.start() call
     * --------------------------------------------------- */
    private void initializeComponents(StartExamResponse response) {
        // 1. Initialize Timer Component
        long totalSeconds = response.getDurationMinutes() * 60L;
        timerComponent = new TimerComponent(totalSeconds);
        timerComponent.setOnTimeExpired(this::handleTimeExpired);
        timerContainer.getChildren().clear();
        timerContainer.getChildren().add(timerComponent);
        
        // ✅ Bug 5 FIX: START the timer countdown!
        timerComponent.start();
        System.out.println("[Phase 8.6] Timer started: " + totalSeconds + " seconds");
        
        // 2. Initialize Question Palette Component
        int totalQuestions = examSession.getQuestions().size();
        paletteComponent = new QuestionPaletteComponent(totalQuestions);
        paletteComponent.setOnQuestionClick(this::jumpToQuestion);
        paletteContainer.getChildren().clear();
        paletteContainer.getChildren().add(paletteComponent);
        
        // 3. Initialize Question Display Component
        questionDisplayComponent = new QuestionDisplayComponent();
        questionDisplayContainer.getChildren().clear();
        questionDisplayContainer.getChildren().add(questionDisplayComponent);
        
        // 4. Update header info
        examTitleLabel.setText(response.getExamTitle());
        examSubtitleLabel.setText(String.format("Thời gian: %d phút", response.getDurationMinutes()));
        studentNameLabel.setText(getCurrentStudentName());
        studentCodeLabel.setText(getCurrentStudentCode());
        
        // 5. Setup button states
        updateNavigationButtons();
    }

    /* ---------------------------------------------------
     * Initialize Auto-Save Services (Phase 8.4)
     * - AutoSaveService: Periodic saves every 30s + debounced 3s
     * - NetworkMonitor: Health check every 10s
     * - ConnectionRecoveryService: Auto reconnect on disconnect
     * @author: K24DTCN210-NVMANH (23/11/2025 18:00)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:15) - Fixed constructor calls
     * EditBy: K24DTCN210-NVMANH (25/11/2025 12:31) - Connected save status to UI
     * EditBy: K24DTCN210-NVMANH (25/11/2025 14:12) - Added lastSaveLabel update
     * --------------------------------------------------- */
    private void initializeAutoSaveServices() {
        // 1. Initialize AutoSaveService
        autoSaveService = new AutoSaveService(apiClient, logService);
        
        // 2. Setup save status callback to update UI (Question component + Status bar)
        autoSaveService.setOnSaveStatusChanged(status -> {
            Platform.runLater(() -> {
                // Update QuestionDisplayComponent status indicator
                if (questionDisplayComponent != null) {
                    switch (status) {
                        case READY:
                            questionDisplayComponent.updateSaveStatus("unsaved");
                            break;
                        case SAVING:
                            questionDisplayComponent.updateSaveStatus("saving");
                            break;
                        case SUCCESS:
                            questionDisplayComponent.updateSaveStatus("saved");
                            break;
                        case FAILURE:
                        case PARTIAL_FAILURE:
                            questionDisplayComponent.updateSaveStatus("error");
                            break;
                    }
                }
                
                // ✅ NEW: Update lastSaveLabel in status bar
                if (lastSaveLabel != null) {
                    switch (status) {
                        case READY:
                            lastSaveLabel.setText("Trạng thái: Chưa lưu");
                            lastSaveLabel.setStyle("-fx-text-fill: #FF9800;"); // Orange
                            break;
                        case SAVING:
                            lastSaveLabel.setText("Trạng thái: Đang lưu...");
                            lastSaveLabel.setStyle("-fx-text-fill: #2196F3;"); // Blue
                            break;
                        case SUCCESS:
                            String timestamp = java.time.LocalTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                            lastSaveLabel.setText("Đã lưu lúc: " + timestamp);
                            lastSaveLabel.setStyle("-fx-text-fill: #4CAF50;"); // Green
                            break;
                        case FAILURE:
                        case PARTIAL_FAILURE:
                            lastSaveLabel.setText("Trạng thái: Lỗi lưu bài!");
                            lastSaveLabel.setStyle("-fx-text-fill: #F44336;"); // Red
                            break;
                    }
                }
            });
        });

        // Setup time expired callback
        autoSaveService.setOnTimeExpired(message -> {
             Platform.runLater(() -> {
                 // Avoid multiple dialogs
                 if (!isExamActive) return;
                 
                 handleServerTimeExpired(message);
             });
        });
        
        autoSaveService.start(examSession);
        
        // 3. Initialize NetworkMonitor
        networkMonitor = new NetworkMonitor();
        networkMonitor.start();
        
        // 4. Initialize ConnectionRecoveryService
        recoveryService = new ConnectionRecoveryService(autoSaveService);
        networkMonitor.addListener(recoveryService);
        
        System.out.println("[Phase 8.4] Auto-save services initialized successfully with UI status updates");
    }
    
    /* ---------------------------------------------------
     * Initialize Full-Screen Security (Phase 8.6)
     * - Enable full-screen mode
     * - Block keyboard shortcuts (Alt+Tab, Windows key, etc.)
     * @author: K24DTCN210-NVMANH (24/11/2025 09:12)
     * --------------------------------------------------- */
    private void initializeFullScreenSecurity() {
        if (stage == null) {
            System.out.println("[Phase 8.6] WARNING: Stage not set, skipping full-screen security");
            return;
        }
        
        try {
            // Initialize FullScreenLockService
            fullScreenLockService = new FullScreenLockService(stage);
            
            // Enable full-screen mode
            fullScreenLockService.enableFullScreen();
            
            System.out.println("[Phase 8.6] Full-screen security initialized successfully");
            
        } catch (Exception e) {
            System.err.println("[Phase 8.6] Failed to initialize full-screen security: " + e.getMessage());
            // Don't throw - allow exam to continue without full-screen if it fails
            DialogUtils.showWarning("Cảnh báo", "Không thể bật chế độ full-screen. " +
                     "Bạn vẫn có thể làm bài nhưng nên tránh chuyển cửa sổ.");
        }
    }
    
    /* ---------------------------------------------------
     * Start monitoring system (Phase 11)
     * @param submissionId ID của submission
     * @param authToken JWT token
     * @author: K24DTCN210-NVMANH (01/12/2025 22:55)
     * EditBy: K24DTCN210-NVMANH (04/12/2025 21:30) - Fix UI freeze by moving to background thread
     * --------------------------------------------------- */
    private void startMonitoringSystem(Long submissionId, String authToken) {
        // Run in background thread to prevent UI freeze during JNativeHook initialization
        Thread monitorThread = new Thread(() -> {
            try {
                System.out.println("[Phase 11] Initializing monitoring system in background thread...");
                
                // Initialize MonitoringApiClient
                com.mstrust.client.api.MonitoringApiClient apiClient = 
                    new com.mstrust.client.api.MonitoringApiClient();
                apiClient.setAuthToken(authToken);
                
                // Create MonitoringCoordinator
                // This might take time due to native library loading (JNA, JNativeHook)
                monitoringCoordinator = new com.mstrust.client.monitoring.MonitoringCoordinator(apiClient);
                
                // Start monitoring với 5 monitors
                monitoringCoordinator.startMonitoring(submissionId, authToken);
                
                System.out.println("[Phase 11] Monitoring system started successfully for submission: " + submissionId);
            } catch (Throwable e) {
                System.err.println("[Phase 11] Failed to start monitoring: " + e.getMessage());
                e.printStackTrace();
                // Don't throw - allow exam to continue even if monitoring fails
            }
        }, "Monitoring-Init-Thread");
        
        monitorThread.setDaemon(true); // Allow app to exit even if this thread is running
        monitorThread.start();
    }
    

    /* ---------------------------------------------------
     * Hiển thị câu hỏi hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 13:50) - Bug 8: Update progress & statistics
     * --------------------------------------------------- */
    private void displayCurrentQuestion() {
        if (examSession == null || examSession.getQuestions().isEmpty()) {
            return;
        }
        
        int index = examSession.getCurrentQuestionIndex();
        QuestionDTO question = examSession.getQuestions().get(index);
        
        // Update palette
        paletteComponent.setCurrentQuestion(index);
        
        // Display question
        questionDisplayComponent.displayQuestion(question);
        
        // Phase 8.6: Setup answer change listener
        questionDisplayComponent.setOnAnswerChanged((answer) -> {
            if (autoSaveService != null && autoSaveService.isRunning()) {
                autoSaveService.onAnswerChanged(question.getId(), answer);
            }
            // Update statistics when answer changes
            updateStatistics();
        });
        
        // Restore answer từ cache (nếu có)
        String cachedAnswer = answersCache.get(question.getId());
        if (cachedAnswer != null) {
            questionDisplayComponent.setCurrentAnswer(cachedAnswer);
        }
        
        // Restore mark status
        Boolean marked = markedForReview.get(question.getId());
        if (marked != null && marked) {
            questionDisplayComponent.setMarkedForReview(true);
        }
        
        // Bug 8 FIX: Update progress bar & statistics
        updateProgressBar();
        updateStatistics();
        
        // Update navigation buttons
        updateNavigationButtons();
        
        // Focus vào answer input
        questionDisplayComponent.focusAnswerInput();
    }

    /* ---------------------------------------------------
     * Handle Previous button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onPrevious() {
        saveCurrentAnswer();
        examSession.previousQuestion();
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Handle Next button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onNext() {
        saveCurrentAnswer();
        examSession.nextQuestion();
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Handle Jump to Question button
     * @author: K24DTCN210-NVMANH (24/11/2025 10:11)
     * --------------------------------------------------- */
    @FXML
    private void onJumpToQuestion() {
        String input = jumpToQuestionField.getText();
        if (input == null || input.trim().isEmpty()) {
            DialogUtils.showError("Lỗi", "Vui lòng nhập số câu hỏi!");
            return;
        }
        
        try {
            int questionNumber = Integer.parseInt(input.trim());
            int questionIndex = questionNumber - 1; // Convert to 0-based index
            
            // Validate range
            if (questionIndex < 0 || questionIndex >= examSession.getQuestions().size()) {
                DialogUtils.showError("Lỗi", 
                    String.format("Số câu hỏi phải từ 1 đến %d!", examSession.getQuestions().size()));
                return;
            }
            
            // Jump to question
            saveCurrentAnswer();
            examSession.jumpToQuestion(questionIndex);
            displayCurrentQuestion();
            
            // Clear field
            jumpToQuestionField.clear();
            
        } catch (NumberFormatException e) {
            DialogUtils.showError("Lỗi", "Vui lòng nhập số hợp lệ!");
        }
    }

    /* ---------------------------------------------------
     * Handle manual Save button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    @FXML
    private void onSave() {
        saveButton.setDisable(true);
        saveCurrentAnswer();
        
        // Re-enable sau 1s
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> saveButton.setDisable(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> saveButton.setDisable(false));
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Save câu trả lời hiện tại
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (24/11/2025 14:52) - Phase 8.6: Use AutoSaveService instead of direct API
     * EditBy: K24DTCN210-NVMANH (25/11/2025 13:50) - Bug 8: Update statistics after save
     * --------------------------------------------------- */
    private void saveCurrentAnswer() {
        QuestionDTO currentQuestion = questionDisplayComponent.getCurrentQuestion();
        if (currentQuestion == null) return;
        
        String answer = questionDisplayComponent.getCurrentAnswer();
        boolean marked = questionDisplayComponent.isMarkedForReview();
        
        // Cache answer locally
        if (answer != null && !answer.isEmpty()) {
            answersCache.put(currentQuestion.getId(), answer);
        }
        markedForReview.put(currentQuestion.getId(), marked);
        
        // Update palette status
        int index = examSession.getCurrentQuestionIndex();
        if (answer != null && !answer.isEmpty()) {
            paletteComponent.updateQuestionStatus(index, marked ? "marked" : "answered");
        } else {
            paletteComponent.updateQuestionStatus(index, marked ? "marked" : "unanswered");
        }
        
        // Bug 8 FIX: Update statistics after save
        updateProgressBar();
        updateStatistics();
        
        // Phase 8.6: Notify AutoSaveService (will handle queueing & API call)
        if (autoSaveService != null && autoSaveService.isRunning()) {
            autoSaveService.onAnswerChanged(currentQuestion.getId(), answer);
            System.out.println("[Phase 8.6] Notified AutoSaveService of answer change for question " + currentQuestion.getId());
        }
    }

    /* ---------------------------------------------------
     * Jump to specific question (từ palette click)
     * @param questionIndex Index của câu hỏi (0-based)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void jumpToQuestion(int questionIndex) {
        saveCurrentAnswer();
        examSession.jumpToQuestion(questionIndex);
        displayCurrentQuestion();
    }

    /* ---------------------------------------------------
     * Update navigation buttons state
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * --------------------------------------------------- */
    private void updateNavigationButtons() {
        if (examSession == null) return;
        
        int current = examSession.getCurrentQuestionIndex();
        int total = examSession.getQuestions().size();
        
        previousButton.setDisable(current == 0);
        nextButton.setDisable(current == total - 1);
    }

    /* ---------------------------------------------------
     * Handle Submit exam button
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:57) - Phase 8.5: Improved confirmation dialog
     * --------------------------------------------------- */
    @FXML
    private void onSubmit() {
        // Save current answer first
        saveCurrentAnswer();
        
        // Show improved confirmation dialog
        boolean confirmed = showSubmitConfirmationDialog();
        
        if (confirmed) {
            submitExam();
        }
    }

    /* ---------------------------------------------------
     * Show submit confirmation dialog với custom UI đẹp và statistics chi tiết
     * @returns true nếu user xác nhận submit, false nếu cancel
     * @author: K24DTCN210-NVMANH (23/11/2025 18:57)
     * EditBy: K24DTCN210-NVMANH (03/12/2025 14:15) - Replaced Alert with custom dialog
     * --------------------------------------------------- */
    private boolean showSubmitConfirmationDialog() {
        // Calculate statistics
        int total = examSession.getQuestions().size();
        int answered = 0;
        
        // Count answered questions from cache
        for (Long questionId : answersCache.keySet()) {
            String answer = answersCache.get(questionId);
            if (answer != null && !answer.trim().isEmpty()) {
                answered++;
            }
        }
        
        // Get REAL-TIME remaining time from timer component
        long remainingSeconds = timerComponent != null 
            ? timerComponent.getRemainingSeconds() 
            : examSession.getRemainingSeconds();
        
        // Show custom confirmation dialog
        return SubmitConfirmationDialogController.showConfirmationDialog(
            stage, total, answered, remainingSeconds);
    }

    /* ---------------------------------------------------
     * Submit exam to backend
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:00) - Phase 8.4: Stop services on submit
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:57) - Phase 8.5: Added navigation to results
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Phase 8.6: Mark exam as inactive
     * --------------------------------------------------- */
    private void submitExam() {
        submitButton.setDisable(true);
        showLoading("Đang nộp bài...");
        
        new Thread(() -> {
            try {
                // Phase 8.5: Flush pending answers from queue before submit
                if (autoSaveService != null) {
                    System.out.println("[Phase 8.5] Flushing pending answers before submit...");
                    // AutoSaveService will auto-flush pending items when stopped
                    // Also try to save pending answers explicitly
                    autoSaveService.saveAllPendingAnswers();
                }
                
                // Call submit API
                apiClient.submitExam(examSession.getSubmissionId());
                
                Platform.runLater(() -> {
                    // Phase 8.6: Mark exam as inactive
                    isExamActive = false;
                    
                    // Phase 8.4: Stop all services
                    if (autoSaveService != null) {
                        autoSaveService.stop();
                        System.out.println("[Phase 8.5] AutoSaveService stopped");
                    }
                    if (networkMonitor != null) {
                        networkMonitor.stop();
                        System.out.println("[Phase 8.5] NetworkMonitor stopped");
                    }
                    if (timerComponent != null) {
                        timerComponent.stop();
                        System.out.println("[Phase 8.5] Timer stopped");
                    }
                    
                    // Phase 11: Stop monitoring
                    if (monitoringCoordinator != null) {
                        monitoringCoordinator.stopMonitoring();
                        System.out.println("[Phase 11] Monitoring stopped on submit");
                    }
                    
                    hideLoading();
                    
                    // Phase 8.5: Navigate to results screen
                    navigateToResults(examSession.getSubmissionId());
                });
                
            } catch (IOException e) {
                logService.logError("ExamTakingController", "Submit exam failed", e, examSession.getSubmissionId(), null);
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    showError("Lỗi nộp bài", e.getMessage());
                    hideLoading();
                });
            } catch (Exception e) {
                logService.logError("ExamTakingController", "Unexpected error during submit", e, examSession.getSubmissionId(), null);
                if (e instanceof InterruptedException) {
                     Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    showError("Lỗi nộp bài", "Lỗi: " + e.getMessage());
                    hideLoading();
                });
            }
        }).start();
    }

    /* ---------------------------------------------------
     * Navigate to results screen (Phase 8.5)
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (23/11/2025 18:57)
     * --------------------------------------------------- */
    private void navigateToResults(Long submissionId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/exam-result.fxml"));
            Parent root = loader.load();
            
            ExamResultController controller = loader.getController();
            controller.initialize(submissionId, apiClient.getAuthToken());
            
            Scene scene = new Scene(root, 1200, 800);
            
            // Use class level stage if available, otherwise try to get from component
            Stage targetStage = this.stage;
            if (targetStage == null && submitButton != null && submitButton.getScene() != null) {
                targetStage = (Stage) submitButton.getScene().getWindow();
            }
            
            if (targetStage != null) {
                targetStage.setScene(scene);
                targetStage.setTitle("Kết Quả Bài Thi - MS.TrustTest");
                // Ensure stage is shown (in case it was hidden)
                targetStage.show();
                // Center window on screen
                targetStage.centerOnScreen();
                
                System.out.println("[Phase 8.5] Navigated to results screen for submission: " + submissionId);
            } else {
                System.err.println("Cannot navigate to results: Stage is null");
                if (logService != null) {
                    logService.logError("ExamTakingController", "Navigate failed: Stage is null", null, submissionId, null);
                }
            }
            
        } catch (IOException e) {
            showError("Lỗi điều hướng", 
                    "Không thể chuyển đến màn hình kết quả: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Handle khi hết giờ (auto-submit) - Client timer
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:57) - Phase 8.5: Improved time expired handling
     * --------------------------------------------------- */
    private void handleTimeExpired() {
        Platform.runLater(() -> {
            // Save current answer one last time
            saveCurrentAnswer();
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Hết Giờ Làm Bài");
            alert.setHeaderText("⏰ Thời gian làm bài đã hết!");
            alert.setContentText("Bài thi sẽ tự động được nộp.\n\n" +
                    "Tất cả câu trả lời đã được lưu sẽ được nộp lên hệ thống.");
            
            // Show alert but don't wait (non-blocking)
            alert.show();
            
            // Auto submit after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::submitExam);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    /* ---------------------------------------------------
     * Handle khi server báo hết giờ (qua AutoSave)
     * @param message Thông báo từ server
     * @author: K24DTCN210-NVMANH (04/12/2025)
     * --------------------------------------------------- */
    private void handleServerTimeExpired(String message) {
        // Mark exam inactive immediately
        isExamActive = false;
        
        // Stop services
        shutdown();
        
        DialogUtils.showAlert(
            Alert.AlertType.INFORMATION,
            "Thông Báo",
            "Hết thời gian làm bài",
            "Bài thi đã được tự động nộp.\n" + message,
            stage
        );
        
        // Navigate to results
        navigateToResults(examSession.getSubmissionId());
    }

    /* ---------------------------------------------------
     * Get current student name
     * @returns Student name
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (04/12/2025) - Use stored user info
     * --------------------------------------------------- */
    private String getCurrentStudentName() {
        return studentName;
    }
    
    /* ---------------------------------------------------
     * Get current student code
     * @returns Student code
     * @author: K24DTCN210-NVMANH (24/11/2025 11:40)
     * EditBy: K24DTCN210-NVMANH (04/12/2025) - Use stored user info
     * --------------------------------------------------- */
    private String getCurrentStudentCode() {
        return studentCode;
    }

    /* ---------------------------------------------------
     * Show loading overlay với message (Phase 8.6 Step 3)
     * @param message Loading message
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Implemented loading overlay
     * --------------------------------------------------- */
    private void showLoading(String message) {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                if (loadingMessage != null) {
                    loadingMessage.setText(message);
                }
                loadingOverlay.setVisible(true);
                loadingOverlay.toFront();
            });
        }
    }

    /* ---------------------------------------------------
     * Hide loading overlay (Phase 8.6 Step 3)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 09:40) - Implemented loading overlay
     * --------------------------------------------------- */
    private void hideLoading() {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
            });
        }
    }

    /* ---------------------------------------------------
     * Show error dialog
     * @param title Error title
     * @param content Error content
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 15:03) - Fixed dialog owner & centering
     * --------------------------------------------------- */
    private void showError(String title, String content) {
        DialogUtils.showError(title, content);
    }

    /* ---------------------------------------------------
     * Update progress bar (Bug 8 fix)
     * @author: K24DTCN210-NVMANH (25/11/2025 13:50)
     * --------------------------------------------------- */
    private void updateProgressBar() {
        if (examSession == null || progressBar == null || progressLabel == null) {
            return;
        }
        
        int total = examSession.getQuestions().size();
        int answered = 0;
        
        // Count answered questions from cache
        for (Long questionId : answersCache.keySet()) {
            String answer = answersCache.get(questionId);
            if (answer != null && !answer.trim().isEmpty()) {
                answered++;
            }
        }
        
        // Update progress bar
        double progress = total > 0 ? (double) answered / total : 0.0;
        progressBar.setProgress(progress);
        
        // Update label
        progressLabel.setText(String.format("%d/%d câu", answered, total));
    }
    
    /* ---------------------------------------------------
     * Update statistics box (Bug 8 fix)
     * @author: K24DTCN210-NVMANH (25/11/2025 13:50)
     * --------------------------------------------------- */
    private void updateStatistics() {
        if (examSession == null || 
            answeredCountLabel == null || 
            markedCountLabel == null || 
            unansweredCountLabel == null) {
            return;
        }
        
        int total = examSession.getQuestions().size();
        int answered = 0;
        int marked = 0;
        
        // Count answered questions
        for (Long questionId : answersCache.keySet()) {
            String answer = answersCache.get(questionId);
            if (answer != null && !answer.trim().isEmpty()) {
                answered++;
            }
        }
        
        // Count marked questions
        for (Boolean isMarked : markedForReview.values()) {
            if (isMarked != null && isMarked) {
                marked++;
            }
        }
        
        int unanswered = total - answered;
        
        // Update labels
        answeredCountLabel.setText(String.valueOf(answered));
        markedCountLabel.setText(String.valueOf(marked));
        unansweredCountLabel.setText(String.valueOf(unanswered));
    }

    /* ---------------------------------------------------
     * Cleanup khi controller destroyed
     * @author: K24DTCN210-NVMANH (23/11/2025 13:49)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 18:00) - Phase 8.4: Added service cleanup
     * EditBy: K24DTCN210-NVMANH (24/11/2025 09:12) - Phase 8.6: Added full-screen cleanup
     * --------------------------------------------------- */
    public void shutdown() {
        // Phase 8.6: Disable full-screen security
        if (fullScreenLockService != null) {
            fullScreenLockService.cleanup();
            System.out.println("[Phase 8.6] Full-screen security cleaned up");
        }
        
        // Phase 8.4: Stop all services
        if (autoSaveService != null) {
            autoSaveService.stop();
        }
        if (networkMonitor != null) {
            networkMonitor.stop();
        }
        if (timerComponent != null) {
            timerComponent.stop();
        }
        
        if (logService != null) {
            logService.shutdown();
        }
    }
}
