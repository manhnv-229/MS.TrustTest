package com.mstrust.exam.dto;

import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/* ---------------------------------------------------
 * DTO để review answer sau khi submit
 * Hiển thị theo exam settings
 * @author: K24DTCN210-NVMANH (19/11/2025 15:19)
 * EditBy: K24DTCN210-NVMANH (24/11/2025 10:31) - Changed options from Map to List<String>
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerReviewDTO {
    
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    
    // Question options (if applicable) - Format: ["A. Option text", "B. Option text", ...]
    private List<String> options;
    
    // Student's answer
    private Object studentAnswer;  // JSON format based on question type
    private String studentAnswerText;  // Plain text for ESSAY/SHORT_ANSWER
    
    // Correct answer (chỉ hiển thị nếu showCorrectAnswers = true)
    private Object correctAnswer;  // null nếu không cho phép xem
    private String correctAnswerText;
    
    // Grading result
    private Boolean isCorrect;  // null nếu chưa chấm
    private BigDecimal pointsEarned;
    private BigDecimal maxPoints;
    
    // Teacher feedback (for manually graded questions)
    private String teacherFeedback;
    private String gradedByName;  // Teacher name
    
    // Display info
    private Boolean isGraded;
    private Boolean requiresManualGrading;  // ESSAY, CODING
    
    // File upload (if any)
    private String uploadedFileUrl;
    private String uploadedFileName;
}
