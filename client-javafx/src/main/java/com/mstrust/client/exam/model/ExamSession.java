package com.mstrust.client.exam.model;

import com.mstrust.client.exam.dto.QuestionDTO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* ---------------------------------------------------
 * Model quản lý trạng thái của một exam session
 * - Track current question, answers, timer, etc.
 * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
 * --------------------------------------------------- */
@Data
public class ExamSession {
    // Exam info
    private Long examId;
    private Long submissionId;
    private String examTitle;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Questions
    private List<QuestionDTO> questions;
    private int currentQuestionIndex;
    private ConcurrentHashMap<Long, String> answersCache; // questionId -> answer
    
    // Timer
    private long remainingSeconds;
    private boolean timerRunning;
    
    // Status
    private boolean submitted;
    private boolean autoSubmitted;
    
    // Statistics
    private int answeredCount;
    private int markedForReviewCount;
    
    public ExamSession() {
        this.answersCache = new ConcurrentHashMap<>();
        this.currentQuestionIndex = 0;
        this.timerRunning = false;
        this.submitted = false;
        this.autoSubmitted = false;
    }
    
    /* ---------------------------------------------------
     * Lấy câu hỏi hiện tại
     * @returns QuestionDTO hiện tại hoặc null
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public QuestionDTO getCurrentQuestion() {
        if (questions == null || questions.isEmpty() 
            || currentQuestionIndex < 0 
            || currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }
    
    /* ---------------------------------------------------
     * Chuyển sang câu hỏi tiếp theo
     * @returns true nếu có câu tiếp theo, false nếu đã hết
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public boolean nextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            return true;
        }
        return false;
    }
    
    /* ---------------------------------------------------
     * Quay lại câu hỏi trước
     * @returns true nếu có câu trước, false nếu đang ở câu đầu
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public boolean previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            return true;
        }
        return false;
    }
    
    /* ---------------------------------------------------
     * Jump tới câu hỏi theo index
     * @param index Index của câu hỏi (0-based)
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public void jumpToQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            this.currentQuestionIndex = index;
        }
    }
    
    /* ---------------------------------------------------
     * Cache answer tạm thời (trước khi save)
     * @param questionId ID của câu hỏi
     * @param answer Câu trả lời
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public void cacheAnswer(Long questionId, String answer) {
        answersCache.put(questionId, answer);
    }
    
    /* ---------------------------------------------------
     * Đếm số câu đã trả lời
     * @returns Số câu đã có answer
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public int countAnsweredQuestions() {
        if (questions == null) return 0;
        return (int) questions.stream()
            .filter(QuestionDTO::isAnswered)
            .count();
    }
    
    /* ---------------------------------------------------
     * Đếm số câu đánh dấu xem lại
     * @returns Số câu marked for review
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public int countMarkedQuestions() {
        if (questions == null) return 0;
        return (int) questions.stream()
            .filter(QuestionDTO::isMarkedForReview)
            .count();
    }
    
    /* ---------------------------------------------------
     * Tính phần trăm hoàn thành
     * @returns Phần trăm (0-100)
     * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
     * --------------------------------------------------- */
    public double getCompletionPercentage() {
        if (questions == null || questions.isEmpty()) return 0.0;
        return (countAnsweredQuestions() * 100.0) / questions.size();
    }
}
