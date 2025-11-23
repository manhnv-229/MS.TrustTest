package com.mstrust.client.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để save câu trả lời cho một câu hỏi
 * Map từ backend SubmitAnswerRequest
 * Hỗ trợ nhiều loại answer format
 * @author: K24DTCN210-NVMANH (23/11/2025 13:38)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveAnswerRequest {
    
    private Long questionId;
    
    // Answer data (flexible format)
    // For MULTIPLE_CHOICE: {"selectedOption": "A"}
    // For MULTIPLE_SELECT: {"selectedOptions": ["A", "C", "D"]}
    // For TRUE_FALSE: {"answer": true}
    // For SHORT_ANSWER/ESSAY: {"text": "answer text"}
    // For MATCHING: {"matches": {"item1": "match1", "item2": "match2"}}
    // For CODING: {"code": "...", "language": "java"}
    // For FILL_IN_BLANK: {"blanks": ["answer1", "answer2"]}
    private Object answer;
    
    // Optional: Plain text version (for SHORT_ANSWER, ESSAY)
    private String answerText;
    
    // Optional: File upload info (for ESSAY with file attachment)
    private String uploadedFileUrl;
    private String uploadedFileName;
    
    // Auto-save flag
    private Boolean isAutoSave;  // true = auto-save, false = manual save
}
