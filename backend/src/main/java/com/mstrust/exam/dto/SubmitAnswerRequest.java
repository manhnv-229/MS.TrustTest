package com.mstrust.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để submit/save answer cho một câu hỏi
 * Hỗ trợ nhiều loại answer format
 * @author: K24DTCN210-NVMANH (19/11/2025 15:18)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    
    @NotNull(message = "Question ID không được để trống")
    private Long questionId;
    
    // Answer data (flexible format)
    // For SINGLE_CHOICE: {"selectedOption": "A"}
    // For MULTIPLE_CHOICE: {"selectedOptions": ["A", "C", "D"]}
    // For TRUE_FALSE: {"answer": true}
    // For SHORT_ANSWER/ESSAY: {"text": "answer text"}
    // For MATCHING: {"matches": {"item1": "match1", "item2": "match2"}}
    // For CODING: {"code": "...", "language": "java"}
    private Object answer;
    
    // Optional: Plain text version (for SHORT_ANSWER, ESSAY)
    private String answerText;
    
    // Optional: File upload info (for ESSAY with file attachment)
    private String uploadedFileUrl;
    private String uploadedFileName;
    
    // Auto-save flag
    private Boolean isAutoSave;  // true = auto-save, false = manual save
}
