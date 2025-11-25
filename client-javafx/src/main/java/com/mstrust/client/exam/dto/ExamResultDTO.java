package com.mstrust.client.exam.dto;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO cho Exam Result từ backend
 * Mapping với ExamApiClient.ExamResultResponse
 * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
 * --------------------------------------------------- */
public class ExamResultDTO {
    private Long submissionId;
    private String examTitle;
    private Double totalScore;
    private Double maxScore;
    private String status; // "GRADING_IN_PROGRESS", "GRADED", "SUBMITTED"
    private LocalDateTime submittedAt;
    private List<AnswerResultDTO> answers;

    /* ---------------------------------------------------
     * Constructor
     * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
     * --------------------------------------------------- */
    public ExamResultDTO() {
    }

    /* ---------------------------------------------------
     * Check nếu bài thi đã được chấm điểm
     * @returns true nếu đã chấm, false nếu đang chờ chấm
     * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
     * --------------------------------------------------- */
    public boolean isGraded() {
        return "GRADED".equals(status);
    }

    /* ---------------------------------------------------
     * Get phần trăm điểm
     * @returns Phần trăm (0-100)
     * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
     * --------------------------------------------------- */
    public double getPercentage() {
        if (maxScore == null || maxScore == 0) {
            return 0.0;
        }
        return (totalScore / maxScore) * 100.0;
    }

    /* ---------------------------------------------------
     * Get grade letter (A/B/C/D/F)
     * @returns Grade letter
     * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
     * --------------------------------------------------- */
    public String getGrade() {
        double percentage = getPercentage();
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }

    // Getters and Setters
    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<AnswerResultDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerResultDTO> answers) {
        this.answers = answers;
    }

    /* ---------------------------------------------------
     * Inner class cho từng câu trả lời
     * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
     * --------------------------------------------------- */
    public static class AnswerResultDTO {
        private Long questionId;
        private String questionContent;
        private String studentAnswer;
        private String correctAnswer;
        private Double score;
        private Double maxScore;
        private String feedback;

        public AnswerResultDTO() {
        }

        /* ---------------------------------------------------
         * Check nếu câu trả lời đúng
         * @returns true nếu đúng hoàn toàn
         * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
         * --------------------------------------------------- */
        public boolean isCorrect() {
            return score != null && maxScore != null && score.equals(maxScore);
        }

        /* ---------------------------------------------------
         * Check nếu có điểm một phần
         * @returns true nếu có điểm nhưng không full
         * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
         * --------------------------------------------------- */
        public boolean isPartialCredit() {
            return score != null && maxScore != null && score > 0 && score < maxScore;
        }

        /* ---------------------------------------------------
         * Get trạng thái của câu trả lời
         * @returns "correct", "partial", "incorrect", "unanswered"
         * @author: K24DTCN210-NVMANH (23/11/2025 18:51)
         * --------------------------------------------------- */
        public String getAnswerStatus() {
            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                return "unanswered";
            }
            if (isCorrect()) {
                return "correct";
            }
            if (isPartialCredit()) {
                return "partial";
            }
            return "incorrect";
        }

        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getQuestionContent() {
            return questionContent;
        }

        public void setQuestionContent(String questionContent) {
            this.questionContent = questionContent;
        }

        public String getStudentAnswer() {
            return studentAnswer;
        }

        public void setStudentAnswer(String studentAnswer) {
            this.studentAnswer = studentAnswer;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Double getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(Double maxScore) {
            this.maxScore = maxScore;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }
}
