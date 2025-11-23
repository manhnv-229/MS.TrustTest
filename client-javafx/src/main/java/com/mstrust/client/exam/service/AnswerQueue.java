package com.mstrust.client.exam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Answer Queue - Thread-safe queue cho pending answers
 * 
 * Features:
 * - In-memory queue sử dụng ConcurrentHashMap
 * - Thread-safe operations
 * - Auto-persist to JSON file
 * - Restore from JSON on startup
 * - Retry tracking với retry count
 * 
 * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
 * --------------------------------------------------- */
public class AnswerQueue {
    
    private static final Logger logger = LoggerFactory.getLogger(AnswerQueue.class);
    private static final String QUEUE_FILE = "exam_answer_queue.json";
    
    // Thread-safe queue storage
    private final ConcurrentHashMap<Long, QueuedAnswer> queue;
    private final Gson gson;
    
    /* ---------------------------------------------------
     * Constructor - Khởi tạo queue và restore từ file
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public AnswerQueue() {
        this.queue = new ConcurrentHashMap<>();
        this.gson = new Gson();
        restoreFromFile();
    }

    /* ---------------------------------------------------
     * Enqueue answer (thêm vào queue)
     * @param questionId ID của câu hỏi
     * @param answer Câu trả lời
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public void enqueue(Long questionId, String answer) {
        QueuedAnswer qa = new QueuedAnswer(questionId, answer);
        queue.put(questionId, qa);
        persistToFile();
        logger.debug("Enqueued answer for question {}", questionId);
    }

    /* ---------------------------------------------------
     * Dequeue answers (lấy N answers cũ nhất từ queue)
     * @param maxItems Số lượng tối đa cần lấy
     * @returns List các QueuedAnswer
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public List<QueuedAnswer> dequeue(int maxItems) {
        // Sort by queuedAt (oldest first)
        List<QueuedAnswer> sorted = queue.values().stream()
            .sorted(Comparator.comparing(QueuedAnswer::getQueuedAt))
            .limit(maxItems)
            .collect(Collectors.toList());
        
        // Remove from queue
        for (QueuedAnswer qa : sorted) {
            queue.remove(qa.getQuestionId());
        }
        
        persistToFile();
        
        logger.debug("Dequeued {} answers", sorted.size());
        return sorted;
    }

    /* ---------------------------------------------------
     * Requeue answer (put back if save failed)
     * @param answer QueuedAnswer cần requeue
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public void requeue(QueuedAnswer answer) {
        answer.incrementRetryCount();
        answer.setLastRetryAt(LocalDateTime.now());
        queue.put(answer.getQuestionId(), answer);
        persistToFile();
        logger.debug("Requeued answer for question {} (retry {})", 
            answer.getQuestionId(), answer.getRetryCount());
    }

    /* ---------------------------------------------------
     * Get answer từ queue (không remove)
     * @param questionId ID của câu hỏi
     * @returns QueuedAnswer hoặc null
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public QueuedAnswer getAnswer(Long questionId) {
        return queue.get(questionId);
    }

    /* ---------------------------------------------------
     * Remove answer khỏi queue
     * @param questionId ID của câu hỏi
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public void remove(Long questionId) {
        queue.remove(questionId);
        persistToFile();
        logger.debug("Removed answer for question {}", questionId);
    }

    /* ---------------------------------------------------
     * Get pending count
     * @returns Số answers đang pending
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public int getPendingCount() {
        return queue.size();
    }

    /* ---------------------------------------------------
     * Clear toàn bộ queue
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public void clear() {
        queue.clear();
        deleteFile();
        logger.info("Queue cleared");
    }

    /* ---------------------------------------------------
     * Persist queue to JSON file
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    private void persistToFile() {
        try {
            File file = new File(QUEUE_FILE);
            String json = gson.toJson(queue);
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
            logger.trace("Queue persisted to file: {} items", queue.size());
        } catch (IOException e) {
            logger.error("Failed to persist queue to file: {}", e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Restore queue from JSON file
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    private void restoreFromFile() {
        try {
            File file = new File(QUEUE_FILE);
            if (!file.exists()) {
                logger.info("No queue file found, starting fresh");
                return;
            }
            
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Type type = new TypeToken<ConcurrentHashMap<Long, QueuedAnswer>>(){}.getType();
            ConcurrentHashMap<Long, QueuedAnswer> restored = gson.fromJson(json, type);
            
            if (restored != null) {
                queue.putAll(restored);
                logger.info("Restored {} answers from queue file", queue.size());
            }
        } catch (IOException e) {
            logger.error("Failed to restore queue from file: {}", e.getMessage());
        }
    }

    /* ---------------------------------------------------
     * Delete queue file
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    private void deleteFile() {
        File file = new File(QUEUE_FILE);
        if (file.exists() && file.delete()) {
            logger.info("Queue file deleted");
        }
    }

    /* ---------------------------------------------------
     * QueuedAnswer class - Đại diện cho 1 answer trong queue
     * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
     * --------------------------------------------------- */
    public static class QueuedAnswer {
        private Long questionId;
        private String answer;
        private LocalDateTime queuedAt;
        private int retryCount;
        private LocalDateTime lastRetryAt;

        /* ---------------------------------------------------
         * Constructor
         * @param questionId ID của câu hỏi
         * @param answer Câu trả lời
         * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
         * --------------------------------------------------- */
        public QueuedAnswer(Long questionId, String answer) {
            this.questionId = questionId;
            this.answer = answer;
            this.queuedAt = LocalDateTime.now();
            this.retryCount = 0;
            this.lastRetryAt = null;
        }

        /* ---------------------------------------------------
         * Increment retry count
         * @author: K24DTCN210-NVMANH (23/11/2025 17:38)
         * --------------------------------------------------- */
        public void incrementRetryCount() {
            this.retryCount++;
        }

        // Getters and setters
        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public LocalDateTime getQueuedAt() {
            return queuedAt;
        }

        public void setQueuedAt(LocalDateTime queuedAt) {
            this.queuedAt = queuedAt;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public LocalDateTime getLastRetryAt() {
            return lastRetryAt;
        }

        public void setLastRetryAt(LocalDateTime lastRetryAt) {
            this.lastRetryAt = lastRetryAt;
        }

        @Override
        public String toString() {
            return "QueuedAnswer{" +
                    "questionId=" + questionId +
                    ", queuedAt=" + queuedAt +
                    ", retryCount=" + retryCount +
                    '}';
        }
    }
}
