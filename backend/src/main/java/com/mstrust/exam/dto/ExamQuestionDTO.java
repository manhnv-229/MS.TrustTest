package com.mstrust.exam.dto;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** ------------------------------------------
 * Mục đích: DTO cho response ExamQuestion (join table)
 * 
 * Bao gồm:
 * - Thông tin từ exam_questions table (order, points)
 * - Thông tin cơ bản của question (không full để tránh quá nặng)
 * - Sử dụng khi list questions trong exam
 * 
 * Note: 
 * - Khác với QuestionDTO (full question info)
 * - Khác với ExamWithQuestionsDTO (exam + list questions)
 * - DTO này dùng cho từng item trong list
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    
    // From exam_questions table
    private Long examId;
    private Long questionId;
    private Integer questionOrder;
    private Double points;
    
    // From questions table (basic info only)
    private QuestionType questionType;
    private Difficulty difficulty;
    private String questionText;
    private String tags; // JSON string
    
    // For MULTIPLE_CHOICE / MULTIPLE_SELECT
    private String options; // JSON string
    private String correctAnswer; // Hidden from students during exam
    
    // For ESSAY / SHORT_ANSWER
    private Integer maxWords;
    private Integer minWords;
    
    // For CODING
    private String programmingLanguage;
    private String starterCode;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMb;
    
    // For FILL_IN_BLANK
    private String blankPositions; // JSON string
    
    // For MATCHING
    private String leftItems; // JSON string
    private String rightItems; // JSON string
    
    // Common
    private String attachments; // JSON string
    
    // Subject info (optional)
    private Long subjectId;
    private String subjectName;
    
    // Metadata
    private LocalDateTime createdAt;
    private String createdByName;
}
