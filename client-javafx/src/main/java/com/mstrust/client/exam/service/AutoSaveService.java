package com.mstrust.client.exam.service;

import com.mstrust.client.exam.api.ExamApiClient;
import com.mstrust.client.exam.dto.SaveAnswerRequest;
import com.mstrust.client.exam.exception.ExamTimeExpiredException;
import com.mstrust.client.exam.model.ExamSession;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/* ---------------------------------------------------
 * Auto-Save Service - Quản lý auto-save câu trả lời
 * 
 * Features:
 * - Periodic save (mỗi 30 giây)
 * - Debounced save (3 giây sau khi answer thay đổi)
 * - Queue pending answers khi save fail
 * - Retry failed saves
 * - Notify UI về save status
 * 
 * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
 * --------------------------------------------------- */
public class AutoSaveService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoSaveService.class);
    
    // Configuration
    private static final int PERIODIC_SAVE_INTERVAL_SECONDS = 30;
    private static final int DEBOUNCE_DELAY_SECONDS = 3;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Dependencies
    private final ExamApiClient apiClient;
    private final ClientLogService logService;
    private final AnswerQueue answerQueue;
    private ExamSession session;
    
    // Schedulers
    private final ScheduledExecutorService periodicScheduler;
    private final ScheduledExecutorService debounceScheduler;
    
    // State
    private boolean isRunning = false;
    private ScheduledFuture<?> periodicSaveTask;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> debounceTasks;
    
    // Callbacks for UI updates
    private Consumer<SaveStatus> onSaveStatusChanged;
    private Consumer<String> onTimeExpired;
    
    /* ---------------------------------------------------
     * Constructor
     * @param apiClient API client để gọi save answer
     * @param logService Client log service
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * EditBy: K24DTCN210-NVMANH (04/12/2025) - Add logService dependency injection
     * --------------------------------------------------- */
    public AutoSaveService(ExamApiClient apiClient, ClientLogService logService) {
        this.apiClient = apiClient;
        this.logService = logService;
        this.answerQueue = new AnswerQueue();
        this.periodicScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoSave-Periodic");
            t.setDaemon(true);
            return t;
        });
        this.debounceScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "AutoSave-Debounce");
            t.setDaemon(true);
            return t;
        });
        this.debounceTasks = new ConcurrentHashMap<>();
    }

    /* ---------------------------------------------------
     * Start auto-save service
     * @param session ExamSession đang active
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public void start(ExamSession session) {
        if (isRunning) {
            logger.warn("AutoSaveService đã running");
            return;
        }
        
        this.session = session;
        this.isRunning = true;
        
        logger.info("Starting AutoSaveService - Periodic interval: {}s", PERIODIC_SAVE_INTERVAL_SECONDS);
        
        // Start periodic save task
        periodicSaveTask = periodicScheduler.scheduleAtFixedRate(
            this::saveAllPendingAnswers,
            PERIODIC_SAVE_INTERVAL_SECONDS,
            PERIODIC_SAVE_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
        
        notifyStatus(SaveStatus.READY);
    }

    /* ---------------------------------------------------
     * Stop auto-save service
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        logger.info("Stopping AutoSaveService...");
        isRunning = false;
        
        // Cancel periodic task
        if (periodicSaveTask != null) {
            periodicSaveTask.cancel(false);
        }
        
        // Cancel all debounce tasks
        debounceTasks.values().forEach(task -> task.cancel(false));
        debounceTasks.clear();
        
        // Final save attempt
        saveAllPendingAnswers();
        
        // Shutdown schedulers (wait for completion)
        shutdownScheduler(periodicScheduler, "Periodic");
        shutdownScheduler(debounceScheduler, "Debounce");
        
        logService.shutdown();
        logger.info("AutoSaveService stopped");
    }

    /* ---------------------------------------------------
     * Handle khi answer thay đổi (debounced save)
     * @param questionId ID của câu hỏi
     * @param answer Câu trả lời mới
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public void onAnswerChanged(Long questionId, String answer) {
        if (!isRunning) {
            logger.warn("AutoSaveService chưa start, bỏ qua answer change");
            return;
        }
        
        // Cancel existing debounce task cho question này
        ScheduledFuture<?> existingTask = debounceTasks.get(questionId);
        if (existingTask != null) {
            existingTask.cancel(false);
        }
        
        // Enqueue answer
        answerQueue.enqueue(questionId, answer);
        
        // Schedule new debounce task
        ScheduledFuture<?> newTask = debounceScheduler.schedule(
            () -> saveAnswerImmediate(questionId),
            DEBOUNCE_DELAY_SECONDS,
            TimeUnit.SECONDS
        );
        
        debounceTasks.put(questionId, newTask);
        
        logger.debug("Answer changed for question {}, scheduled debounce save", questionId);
    }

    /* ---------------------------------------------------
     * Save all pending answers (periodic + retry failed)
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public void saveAllPendingAnswers() {
        if (session == null) {
            logger.warn("Session null, không thể save");
            return;
        }
        
        int pendingCount = answerQueue.getPendingCount();
        if (pendingCount == 0) {
            logger.debug("Không có pending answers");
            return;
        }
        
        logger.info("Saving {} pending answers...", pendingCount);
        notifyStatus(SaveStatus.SAVING);
        
        // Dequeue tối đa 10 answers mỗi lần
        List<AnswerQueue.QueuedAnswer> queuedAnswers = answerQueue.dequeue(10);
        
        int successCount = 0;
        int failCount = 0;
        
        for (AnswerQueue.QueuedAnswer qa : queuedAnswers) {
            boolean success = saveToBackend(qa.getQuestionId(), qa.getAnswer(), true);
            
            if (success) {
                successCount++;
                logger.debug("Saved answer for question {} (retry {})", 
                    qa.getQuestionId(), qa.getRetryCount());
            } else {
                failCount++;
                
                // Requeue if under retry limit
                if (qa.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                    answerQueue.requeue(qa);
                    logger.warn("Save failed for question {}, requeued (retry {})", 
                        qa.getQuestionId(), qa.getRetryCount());
                } else {
                    logger.error("Save failed for question {} after {} attempts, DROPPED", 
                        qa.getQuestionId(), MAX_RETRY_ATTEMPTS);
                    
                    // Log to backend
                    logService.logError(
                        "AutoSaveService",
                        "Save failed after " + MAX_RETRY_ATTEMPTS + " attempts for question " + qa.getQuestionId(),
                        null,
                        session.getSubmissionId(),
                        qa.getAnswer()
                    );
                }
            }
        }
        
        logger.info("Save batch complete - Success: {}, Failed: {}, Remaining in queue: {}", 
            successCount, failCount, answerQueue.getPendingCount());
        
        // Notify status
        if (failCount > 0) {
            notifyStatus(SaveStatus.PARTIAL_FAILURE);
        } else {
            notifyStatus(SaveStatus.SUCCESS);
        }
    }

    /* ---------------------------------------------------
     * Save single answer immediately (debounced)
     * @param questionId ID của câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    private void saveAnswerImmediate(Long questionId) {
        AnswerQueue.QueuedAnswer qa = answerQueue.getAnswer(questionId);
        if (qa == null) {
            logger.warn("Question {} not in queue", questionId);
            return;
        }
        
        logger.info("Debounced save for question {}", questionId);
        notifyStatus(SaveStatus.SAVING);
        
        boolean success = saveToBackend(questionId, qa.getAnswer(), false);
        
        if (success) {
            answerQueue.remove(questionId);
            notifyStatus(SaveStatus.SUCCESS);
            logger.debug("Debounced save success for question {}", questionId);
        } else {
            // Keep in queue for periodic retry
            logger.warn("Debounced save failed for question {}, will retry periodically", questionId);
            notifyStatus(SaveStatus.FAILURE);
        }
        
        // Remove from debounce tasks
        debounceTasks.remove(questionId);
    }

    /* ---------------------------------------------------
     * Save answer to backend
     * @param questionId ID của câu hỏi
     * @param answer Câu trả lời
     * @param isAutoSave true nếu là auto-save
     * @returns true nếu save thành công
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    private boolean saveToBackend(Long questionId, String answer, boolean isAutoSave) {
        try {
            SaveAnswerRequest request = SaveAnswerRequest.builder()
                .questionId(questionId)
                .answerText(answer)
                .isAutoSave(isAutoSave)
                .build();
            
            apiClient.saveAnswer(session.getSubmissionId(), request);
            return true;
            
        } catch (ExamTimeExpiredException e) {
            logger.error("Save failed - Exam time expired: {}", e.getMessage());
            if (onTimeExpired != null) {
                Platform.runLater(() -> onTimeExpired.accept(e.getMessage()));
            }
            // Don't retry if time expired
            return false;
        } catch (IOException e) {
            logger.error("Save failed for question {}: {}", questionId, e.getMessage());
            
            // Nếu không phải lỗi mạng thông thường, có thể log warning
            // Nhưng để tránh spam log, ta chỉ log error khi drop answer (ở trên)
            
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error saving question {}: {}", questionId, e.getMessage());
            
            // Log unexpected errors immediately
            if (session != null) {
                 logService.logError(
                    "AutoSaveService",
                    "Unexpected error saving question " + questionId,
                    e,
                    session.getSubmissionId(),
                    answer
                );
            }
            
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /* ---------------------------------------------------
     * Shutdown scheduler gracefully
     * @param scheduler Scheduler cần shutdown
     * @param name Tên scheduler (for logging)
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    private void shutdownScheduler(ExecutorService scheduler, String name) {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("{} scheduler không shutdown trong 5s, forcing...", name);
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("{} scheduler shutdown interrupted", name);
            scheduler.shutdownNow();
        }
    }

    /* ---------------------------------------------------
     * Notify UI về save status thay đổi
     * @param status SaveStatus mới
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    private void notifyStatus(SaveStatus status) {
        if (onSaveStatusChanged != null) {
            Platform.runLater(() -> onSaveStatusChanged.accept(status));
        }
    }

    /* ---------------------------------------------------
     * Set callback khi save status thay đổi
     * @param callback Consumer nhận SaveStatus
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public void setOnSaveStatusChanged(Consumer<SaveStatus> callback) {
        this.onSaveStatusChanged = callback;
    }

    /* ---------------------------------------------------
     * Set callback khi hết giờ làm bài (từ server)
     * @param callback Consumer nhận message lỗi
     * @author: K24DTCN210-NVMANH (04/12/2025)
     * --------------------------------------------------- */
    public void setOnTimeExpired(Consumer<String> callback) {
        this.onTimeExpired = callback;
    }

    /* ---------------------------------------------------
     * Get pending count từ queue
     * @returns Số answers đang pending
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public int getPendingCount() {
        return answerQueue.getPendingCount();
    }

    /* ---------------------------------------------------
     * Check xem service đang running không
     * @returns true nếu đang running
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return isRunning;
    }

    /* ---------------------------------------------------
     * SaveStatus enum - Trạng thái save
     * @author: K24DTCN210-NVMANH (23/11/2025 17:35)
     * --------------------------------------------------- */
    public enum SaveStatus {
        READY("Sẵn sàng"),
        SAVING("Đang lưu..."),
        SUCCESS("Đã lưu"),
        FAILURE("Lưu thất bại"),
        PARTIAL_FAILURE("Lưu một phần");
        
        private final String displayText;
        
        SaveStatus(String displayText) {
            this.displayText = displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }
    }
}
