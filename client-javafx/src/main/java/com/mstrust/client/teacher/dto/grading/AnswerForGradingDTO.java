package com.mstrust.client.teacher.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO hiển thị từng câu trả lời cần chấm điểm
 * @author: K24DTCN210-NVMANH (01/12/2025)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerForGradingDTO {
    private Long answerId;
    private Long questionId;
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE, ESSAY, etc.
    private String studentAnswer;
    private String correctAnswer;
    private Double currentScore;
    private Double maxScore;
    private Boolean isAutoGraded;
    private String feedback;
    private Boolean isCorrect;
}

